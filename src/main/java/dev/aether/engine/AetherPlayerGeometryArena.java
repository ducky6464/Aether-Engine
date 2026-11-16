package dev.aether.engine;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;
import java.util.Arrays;
import java.util.Objects;

/** Per-frame player geometry arena with packed per-player block/sky light coordinates. */
public final class AetherPlayerGeometryArena {
    private static final int SOURCE_FLOATS_PER_VERTEX = AetherModelGeometryBuffer.FLOATS_PER_VERTEX;
    private static final int DRAW_FLOATS_PER_VERTEX = AetherPlayerVertexWriter.SOURCE_FLOATS_PER_VERTEX;
    private final int maxPlayers;
    private final float[] vertexData;
    private final int[] offsets, floatCounts, vertexCounts;
    private final AetherModelGeometryBuffer scratch;
    private int capturedPlayers, usedFloats;
    private long overflowCount, rejectedCount;

    public AetherPlayerGeometryArena(int maxPlayers, int maxQuadsPerPlayer, int totalFloatCapacity) {
        if (maxPlayers < 1 || maxQuadsPerPlayer < 1 || totalFloatCapacity < 1) throw new IllegalArgumentException("all arena capacities must be positive");
        this.maxPlayers=maxPlayers; vertexData=new float[totalFloatCapacity];
        offsets=new int[maxPlayers]; floatCounts=new int[maxPlayers]; vertexCounts=new int[maxPlayers];
        scratch=new AetherModelGeometryBuffer(maxQuadsPerPlayer); beginFrame();
    }
    public void beginFrame(){Arrays.fill(offsets,-1);Arrays.fill(floatCounts,0);Arrays.fill(vertexCounts,0);capturedPlayers=usedFloats=0;}

    /** Caller must invoke on Minecraft's render thread. */
    public boolean capture(int playerIndex, ModelPart root, MatrixStack matrices, int packedLight) {
        Objects.requireNonNull(root,"root"); Objects.requireNonNull(matrices,"matrices");
        if(playerIndex<0||playerIndex>=maxPlayers||offsets[playerIndex]>=0){rejectedCount++;return false;}
        if(!scratch.capture(root,matrices)){overflowCount++;return false;}
        int vertices=scratch.vertexCount(), sourceFloats=scratch.usedFloats();
        int outputFloats=Math.multiplyExact(vertices,DRAW_FLOATS_PER_VERTEX);
        if(vertices==0||sourceFloats!=vertices*SOURCE_FLOATS_PER_VERTEX||outputFloats>vertexData.length-usedFloats){overflowCount++;return false;}
        float block=(packedLight>>>4&15)<<4, sky=(packedLight>>>20&15)<<4;
        float[] source=scratch.data(); int output=usedFloats;
        for(int v=0;v<vertices;v++){
            System.arraycopy(source,v*SOURCE_FLOATS_PER_VERTEX,vertexData,output,SOURCE_FLOATS_PER_VERTEX);
            vertexData[output+8]=block; vertexData[output+9]=sky; output+=DRAW_FLOATS_PER_VERTEX;
        }
        offsets[playerIndex]=usedFloats;floatCounts[playerIndex]=outputFloats;vertexCounts[playerIndex]=vertices;
        usedFloats+=outputFloats;capturedPlayers++;return true;
    }
    public boolean captureOnRenderThread(int playerIndex,ModelPart root,MatrixStack matrices,int packedLight){AetherRenderThread.assertCurrent();return capture(playerIndex,root,matrices,packedLight);}
    public int maxPlayers(){return maxPlayers;} public int capturedPlayers(){return capturedPlayers;}
    public int usedFloats(){return usedFloats;} public int capacityFloats(){return vertexData.length;}
    public long overflowCount(){return overflowCount;} public long rejectedCount(){return rejectedCount;}
    public float[] data(){return vertexData;} public int[] offsets(){return offsets;}
    public int[] floatCounts(){return floatCounts;} public int[] vertexCounts(){return vertexCounts;}
    public int offset(int i){checkPlayer(i);return offsets[i];} public int floatCount(int i){checkPlayer(i);return floatCounts[i];}
    public int vertexCount(int i){checkPlayer(i);return vertexCounts[i];} public boolean hasGeometry(int i){checkPlayer(i);return offsets[i]>=0;}
    private void checkPlayer(int i){if(i<0||i>=maxPlayers)throw new IndexOutOfBoundsException("player "+i+" of "+maxPlayers);}
}
