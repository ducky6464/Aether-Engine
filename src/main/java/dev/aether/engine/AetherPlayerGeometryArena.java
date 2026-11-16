package dev.aether.engine;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Arrays;
import java.util.Objects;

/**
 * Fixed-capacity per-frame arena for captured player base-model geometry.
 * The extractor supplies eight floats per vertex; this arena appends the
 * player's packed block/sky light coordinates for the GPU vertex format.
 */
public final class AetherPlayerGeometryArena {
    private static final int SOURCE_FLOATS_PER_VERTEX = AetherModelGeometryBuffer.FLOATS_PER_VERTEX;
    private static final int DRAW_FLOATS_PER_VERTEX = AetherPlayerVertexWriter.SOURCE_FLOATS_PER_VERTEX;

    private final int maxPlayers;
    private final float[] vertexData;
    private final int[] offsets, floatCounts, vertexCounts;
    private final AetherModelGeometryBuffer scratch;
    private int capturedPlayers;
    private int usedFloats;
    private long overflowCount;
    private long rejectedCount;

    public AetherPlayerGeometryArena(int maxPlayers, int maxQuadsPerPlayer, int totalFloatCapacity) {
        if (maxPlayers < 1 || maxQuadsPerPlayer < 1 || totalFloatCapacity < 1) {
            throw new IllegalArgumentException("all arena capacities must be positive");
        }
        this.maxPlayers = maxPlayers;
        this.vertexData = new float[totalFloatCapacity];
        this.offsets = new int[maxPlayers];
        this.floatCounts = new int[maxPlayers];
        this.vertexCounts = new int[maxPlayers];
        this.scratch = new AetherModelGeometryBuffer(maxQuadsPerPlayer);
        beginFrame();
    }

    public void beginFrame() {
        Arrays.fill(offsets, -1);
        Arrays.fill(floatCounts, 0);
        Arrays.fill(vertexCounts, 0);
        capturedPlayers = 0;
        usedFloats = 0;
    }

    /** Caller must invoke on Minecraft's render thread. */
    public boolean capture(int playerIndex, ModelPart baseModelRoot, MatrixStack matrices, int packedLight) {
        Objects.requireNonNull(baseModelRoot, "baseModelRoot");
        Objects.requireNonNull(matrices, "matrices");
        if (playerIndex < 0 || playerIndex >= maxPlayers) {
            rejectedCount++;
            return false;
        }
        if (offsets[playerIndex] >= 0) {
            rejectedCount++;
            return false;
        }
        if (!scratch.capture(baseModelRoot, matrices)) {
            overflowCount++;
            return false;
        }

        int vertices = scratch.vertexCount();
        int sourceFloats = scratch.usedFloats();
        int outputFloats = Math.multiplyExact(vertices, DRAW_FLOATS_PER_VERTEX);
        if (vertices == 0 || sourceFloats != vertices * SOURCE_FLOATS_PER_VERTEX
                || outputFloats > vertexData.length - usedFloats) {
            overflowCount++;
            return false;
        }

        int blockLight = (packedLight >>> 4) & 15;
        int skyLight = (packedLight >>> 20) & 15;
        float blockCoord = blockLight << 4;
        float skyCoord = skyLight << 4;
        float[] source = scratch.data();
        int output = usedFloats;
        for (int vertex = 0; vertex < vertices; vertex++) {
            int sourceOffset = vertex * SOURCE_FLOATS_PER_VERTEX;
            System.arraycopy(source, sourceOffset, vertexData, output, SOURCE_FLOATS_PER_VERTEX);
            vertexData[output + 8] = blockCoord;
            vertexData[output + 9] = skyCoord;
            output += DRAW_FLOATS_PER_VERTEX;
        }

        offsets[playerIndex] = usedFloats;
        floatCounts[playerIndex] = outputFloats;
        vertexCounts[playerIndex] = vertices;
        usedFloats += outputFloats;
        capturedPlayers++;
        return true;
    }

    public boolean captureOnRenderThread(int playerIndex, ModelPart baseModelRoot,
                                          MatrixStack matrices, int packedLight) {
        AetherRenderThread.assertCurrent();
        return capture(playerIndex, baseModelRoot, matrices, packedLight);
    }

    public int maxPlayers() { return maxPlayers; }
    public int capturedPlayers() { return capturedPlayers; }
    public int usedFloats() { return usedFloats; }
    public int capacityFloats() { return vertexData.length; }
    public long overflowCount() { return overflowCount; }
    public long rejectedCount() { return rejectedCount; }
    public float[] data() { return vertexData; }
    /** Backing metadata for render-thread draw-plan construction; do not mutate. */
    public int[] offsets() { return offsets; }
    public int[] floatCounts() { return floatCounts; }
    public int[] vertexCounts() { return vertexCounts; }

    public int offset(int playerIndex) { checkPlayer(playerIndex); return offsets[playerIndex]; }
    public int floatCount(int playerIndex) { checkPlayer(playerIndex); return floatCounts[playerIndex]; }
    public int vertexCount(int playerIndex) { checkPlayer(playerIndex); return vertexCounts[playerIndex]; }
    public boolean hasGeometry(int playerIndex) { checkPlayer(playerIndex); return offsets[playerIndex] >= 0; }

    private void checkPlayer(int playerIndex) {
        if (playerIndex < 0 || playerIndex >= maxPlayers) {
            throw new IndexOutOfBoundsException("player " + playerIndex + " of " + maxPlayers);
        }
    }
}
