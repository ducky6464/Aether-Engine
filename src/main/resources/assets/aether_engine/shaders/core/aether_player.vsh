#version 330

in vec3 Position;
in vec2 UV0;
in vec3 Normal;
in ivec2 UV2;

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};
layout(std140) uniform Projection { mat4 ProjMat; };

uniform sampler2D Sampler2;
out vec2 texCoord;
out vec3 viewNormal;
out vec4 lightMapColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position + ModelOffset, 1.0);
    texCoord = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
    viewNormal = mat3(ModelViewMat) * Normal;
    lightMapColor = texelFetch(Sampler2, UV2 / 16, 0);
}
