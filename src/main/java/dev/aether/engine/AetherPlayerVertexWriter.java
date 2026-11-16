package dev.aether.engine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Packs xyz, uv, normalized normal, and two packed lightmap coordinates into 28 GPU bytes. */
public final class AetherPlayerVertexWriter {
    public static final int SOURCE_FLOATS_PER_VERTEX=10;
    public static final int GPU_BYTES_PER_VERTEX=28;
    private AetherPlayerVertexWriter(){}
    public static boolean write(float[] source,int floatCount,ByteBuffer target){
        if(source==null||target==null)throw new NullPointerException("vertex data");
        if(floatCount<0||floatCount>source.length||floatCount%SOURCE_FLOATS_PER_VERTEX!=0)return false;
        int vertices=floatCount/SOURCE_FLOATS_PER_VERTEX,bytes=Math.multiplyExact(vertices,GPU_BYTES_PER_VERTEX);
        if(target.remaining()<bytes)return false;
        for(int i=0;i<floatCount;i++)if(!Float.isFinite(source[i]))return false;
        for(int i=8;i<floatCount;i+=SOURCE_FLOATS_PER_VERTEX)
            if(!validLight(source[i])||!validLight(source[i+1]))return false;
        target.order(ByteOrder.LITTLE_ENDIAN);
        for(int i=0;i<floatCount;i+=SOURCE_FLOATS_PER_VERTEX){
            target.putFloat(source[i]).putFloat(source[i+1]).putFloat(source[i+2]);
            target.putFloat(source[i+3]).putFloat(source[i+4]);
            target.put(toNormalizedByte(source[i+5])).put(toNormalizedByte(source[i+6])).put(toNormalizedByte(source[i+7])).put((byte)0);
            target.putShort((short)source[i+8]).putShort((short)source[i+9]);
        }
        return true;
    }
    private static boolean validLight(float v){return v>=0&&v<=240&&v%16==0;}
    private static byte toNormalizedByte(float v){return (byte)Math.round(Math.max(-1,Math.min(1,v))*127);}
}
