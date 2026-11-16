package dev.aether.engine;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

/** Vertex declaration matching the compact player GPU byte layout. */
public final class AetherPlayerVertexLayout {
    public static final int STRIDE_BYTES=AetherPlayerVertexWriter.GPU_BYTES_PER_VERTEX;
    public static final VertexFormat FORMAT=VertexFormat.builder()
            .add("Position",VertexFormatElement.POSITION)
            .add("UV0",VertexFormatElement.UV0)
            .add("Normal",VertexFormatElement.NORMAL)
            .add("UV2",VertexFormatElement.UV2).build();
    static {
        int stride=FORMAT.getVertexSize(),position=FORMAT.getOffset(VertexFormatElement.POSITION);
        int uv=FORMAT.getOffset(VertexFormatElement.UV0),normal=FORMAT.getOffset(VertexFormatElement.NORMAL);
        int light=FORMAT.getOffset(VertexFormatElement.UV2);
        if(stride!=STRIDE_BYTES||position!=0||uv!=12||normal!=20||light!=24)
            throw new IllegalStateException("Aether player vertex format mismatch: stride="+stride+", position="+position+", uv="+uv+", normal="+normal+", light="+light);
    }
    private AetherPlayerVertexLayout(){}
}
