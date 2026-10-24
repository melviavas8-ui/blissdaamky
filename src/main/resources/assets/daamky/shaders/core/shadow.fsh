#version 330

in vec2 localPos;
in vec4 vertexColor;
flat in vec2 halfSize;
flat in ivec2 params;

out vec4 fragColor;

float cornerRadius() {
    int bits = localPos.y < 0.0
        ? (localPos.x < 0.0 ? params.x : params.x >> 5)
        : (localPos.x < 0.0 ? params.y : params.x >> 10);
    return float(bits & 31);
}

void main() {
    float blur = float((params.y >> 5) & 127) * 0.5;
    float strength = 1.0 + float((params.y >> 12) & 15) * 0.2;
    float r = cornerRadius();
    vec2 q = abs(localPos) - halfSize + r;
    float d = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
    float w = max(blur, 0.5);
    float t = clamp((d + w * 0.15) / (w * 1.15), 0.0, 1.0);
    float falloff = pow(1.0 - t, 2.4);
    float a = vertexColor.a * clamp(falloff * strength, 0.0, 1.0);
    if (a <= 0.0) {
        discard;
    }
    float dither = fract(sin(dot(gl_FragCoord.xy, vec2(12.9898, 78.233))) * 43758.5453) - 0.5;
    fragColor = vec4(vertexColor.rgb, clamp(a + dither / 255.0, 0.0, 1.0));
}
