#version 330

uniform sampler2D Sampler0;

in vec2 localPos;
in vec4 vertexColor;
flat in vec2 halfSize;
flat in ivec2 radii;

out vec4 fragColor;

float cornerRadius() {
    int bits = localPos.y < 0.0 ? radii.x : radii.y;
    int shift = localPos.x < 0.0 ? 0 : 7;
    return float((bits >> shift) & 127) * 0.5;
}

void main() {
    float r = cornerRadius();
    vec2 q = abs(localPos) - halfSize + r;
    float d = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
    float coverage = clamp(0.5 - d / max(fwidth(d), 1e-4), 0.0, 1.0);
    if (coverage <= 0.0) {
        discard;
    }
    vec2 uv = clamp(localPos / (halfSize * 2.0) + 0.5, 0.0, 1.0);
    if (((radii.x >> 14) & 1) != 0) {
        uv.x = 1.0 - uv.x;
    }
    if (((radii.x >> 15) & 1) != 0) {
        uv.y = 1.0 - uv.y;
    }
    vec4 color = texture(Sampler0, uv) * vertexColor;
    color.a *= coverage;
    if (color.a <= 0.0) {
        discard;
    }
    fragColor = color;
}
