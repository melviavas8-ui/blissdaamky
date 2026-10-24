#version 330

uniform sampler2D Sampler0;

in vec2 localPos;
in vec4 vertexColor;
flat in vec2 halfSize;
flat in ivec2 radii;

out vec4 fragColor;

float cornerRadius() {
    int bits = localPos.y < 0.0 ? radii.x : radii.y;
    int shift = localPos.x < 0.0 ? 0 : 6;
    return float((bits >> shift) & 63) * 0.5;
}

int blurLevel() {
    return (radii.x >> 12) & 7;
}

float opacity() {
    int steps = ((radii.x >> 15) & 1) | (((radii.y >> 12) & 15) << 1);
    return float(steps) / 31.0;
}

void main() {
    float r = cornerRadius();
    vec2 q = abs(localPos) - halfSize + r;
    float d = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
    float coverage = clamp(0.5 - d / max(fwidth(d), 1e-4), 0.0, 1.0);
    float a = coverage * opacity();
    if (a <= 0.0) {
        discard;
    }
    vec2 uv = gl_FragCoord.xy / (vec2(textureSize(Sampler0, 0)) * float(1 << blurLevel()));
    vec3 color = mix(texture(Sampler0, uv).rgb, vertexColor.rgb, vertexColor.a);
    float dither = fract(sin(dot(gl_FragCoord.xy, vec2(12.9898, 78.233))) * 43758.5453) - 0.5;
    fragColor = vec4(color + dither / 255.0, a);
}
