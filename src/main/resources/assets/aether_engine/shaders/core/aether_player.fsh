#version 330

uniform sampler2D Sampler0;
in vec2 texCoord;
in vec3 viewNormal;
in vec4 lightMapColor;
out vec4 fragColor;

void main() {
    vec4 skinColor = texture(Sampler0, texCoord);
    if (skinColor.a < 0.1) discard;
    vec3 normal = normalize(viewNormal);
    vec3 lightDirection = normalize(vec3(-0.35, 0.75, 0.55));
    float diffuse = max(dot(normal, lightDirection), 0.0);
    float directionalLighting = 0.78 + 0.22 * diffuse;
    fragColor = vec4(skinColor.rgb * lightMapColor.rgb * directionalLighting, skinColor.a);
}
