package dev.aether.engine;

import org.junit.jupiter.api.Test;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import static org.junit.jupiter.api.Assertions.*;

class AetherPlayerVertexWriterTest {
    @Test void packsPositionUvNormalAndLightCoordinates(){
        float[] source={1.25f,-2.5f,3.75f,0.25f,0.75f,1.0f,-1.0f,0.5f,64.0f,240.0f};
        ByteBuffer target=ByteBuffer.allocate(AetherPlayerVertexWriter.GPU_BYTES_PER_VERTEX);
        assertTrue(AetherPlayerVertexWriter.write(source,source.length,target));
        assertEquals(28,target.position());target.flip();target.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(1.25f,target.getFloat());assertEquals(-2.5f,target.getFloat());assertEquals(3.75f,target.getFloat());
        assertEquals(0.25f,target.getFloat());assertEquals(0.75f,target.getFloat());
        assertEquals((byte)127,target.get());assertEquals((byte)-127,target.get());assertEquals((byte)64,target.get());assertEquals((byte)0,target.get());
        assertEquals((short)64,target.getShort());assertEquals((short)240,target.getShort());
    }
    @Test void rejectsNonFiniteVertexData(){
        float[] source={0,0,0,0,0,Float.NaN,0,1,0,0};
        assertFalse(AetherPlayerVertexWriter.write(source,source.length,ByteBuffer.allocate(28)));
    }
    @Test void rejectsInvalidLightCoordinates(){
        float[] source={0,0,0,0,0,0,0,1,15,256};
        assertFalse(AetherPlayerVertexWriter.write(source,source.length,ByteBuffer.allocate(28)));
    }
    @Test void rejectsIncompleteVertexAndInsufficientBuffer(){
        float[] source={0,0,0,0,0,0,0,1,0,0};
        assertFalse(AetherPlayerVertexWriter.write(source,9,ByteBuffer.allocate(28)));
        assertFalse(AetherPlayerVertexWriter.write(source,source.length,ByteBuffer.allocate(27)));
    }
}
