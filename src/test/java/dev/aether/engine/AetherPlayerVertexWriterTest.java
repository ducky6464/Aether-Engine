package dev.aether.engine;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class AetherPlayerVertexWriterTest {
    @Test
    void packsPositionUvAndNormalizedNormalIntoGpuLayout() {
        float[] source = {
                1.25f, -2.5f, 3.75f,
                0.25f, 0.75f,
                1.0f, -1.0f, 0.5f
        };
        ByteBuffer target = ByteBuffer.allocate(AetherPlayerVertexWriter.GPU_BYTES_PER_VERTEX);

        assertTrue(AetherPlayerVertexWriter.write(source, source.length, target));
        assertEquals(AetherPlayerVertexWriter.GPU_BYTES_PER_VERTEX, target.position());

        target.flip();
        target.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(1.25f, target.getFloat());
        assertEquals(-2.5f, target.getFloat());
        assertEquals(3.75f, target.getFloat());
        assertEquals(0.25f, target.getFloat());
        assertEquals(0.75f, target.getFloat());
        assertEquals((byte) 127, target.get());
        assertEquals((byte) -127, target.get());
        assertEquals((byte) 64, target.get());
        assertEquals((byte) 0, target.get());
    }

    @Test
    void rejectsNonFiniteVertexData() {
        float[] source = {0, 0, 0, 0, 0, Float.NaN, 0, 1};
        assertFalse(AetherPlayerVertexWriter.write(source, source.length,
                ByteBuffer.allocate(AetherPlayerVertexWriter.GPU_BYTES_PER_VERTEX)));
    }

    @Test
    void rejectsIncompleteVertexAndInsufficientBuffer() {
        float[] source = {0, 0, 0, 0, 0, 0, 0, 1};
        assertFalse(AetherPlayerVertexWriter.write(source, 7,
                ByteBuffer.allocate(AetherPlayerVertexWriter.GPU_BYTES_PER_VERTEX)));
        assertFalse(AetherPlayerVertexWriter.write(source, source.length,
                ByteBuffer.allocate(AetherPlayerVertexWriter.GPU_BYTES_PER_VERTEX - 1)));
    }
}
