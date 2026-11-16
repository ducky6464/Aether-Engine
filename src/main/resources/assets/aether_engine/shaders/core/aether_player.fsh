#version 150

uniform sampler2D Sampler0;

in vec2 texCoord;
in vec3 viewNormal;
out vec4 fragColor;

void main() {
    vec4 skinColor = texture(Sampler0, texCoord);
    // Player skins use transparent texels for the outer-layer cutouts.
    // Discard them so they do not write depth as opaque black pixels.
    if (skinColor.a < 0.1) {
        discard;
    }

    // A conservative directional shading term gives captured cuboid faces
    // readable shape. This is not Minecraft's block/sky lightmap; that remains
    // a separate integration task before replacement can be considered complete.
    vec3 normal = normalize(viewNormal);
    vec3 lightDirection = normalize(vec3(-0.35, 0.75, 0.55));
    float diffuse = max(dot(normal, lightDirection), 0.0);
    float lighting = 0.78 + 0.22 * diffuse;
    fragColor = vec4(skinColor.rgb * lighting, skinColor.a);
}
