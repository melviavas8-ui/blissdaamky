#version 330

in vec2 localPos;
in vec4 vertexColor;
flat in vec2 halfSize;
flat in ivec2 radii;

out vec4 fragColor;

float cornerRadius() {
    int bits = localPos.y < 0.0 ? radii.x : radii.y;
    int shift = localPos.x < 0.0 ? 0 : 8;
    return float((bits >> shift) & 255) * 0.5;
}

void main() {
    float r = cornerRadius();
    vec2 q = abs(localPos) - halfSize + r;
    float d = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
    float coverage = clamp(0.5 - d / max(fwidth(d), 1e-4), 0.0, 1.0);
    float a = vertexColor.a * coverage;
    if (a <= 0.0) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb, a);
}