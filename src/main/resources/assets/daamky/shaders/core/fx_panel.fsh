#version 330

in vec2 localPos;
in vec2 screenPos;
in vec4 vertexColor;
flat in vec2 halfSize;
flat in float radius;
flat in int mode;
flat in float time;

uniform sampler2D Sampler0;

out vec4 fragColor;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float sdBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

vec2 rot2(vec2 p, float a) {
    float s = sin(a);
    float c = cos(a);
    return vec2(c * p.x - s * p.y, s * p.x + c * p.y);
}

float fill(float d, float px) {
    return 1.0 - smoothstep(-px, px, d);
}

float totemShape(vec2 f, float px) {
    float body = sdBox(f - vec2(0.0, 0.08), vec2(0.1, 0.15), 0.035);
    float head = sdBox(f - vec2(0.0, -0.16), vec2(0.11, 0.09), 0.035);
    float wingL = sdBox(rot2(f - vec2(-0.19, 0.04), -0.25), vec2(0.055, 0.12), 0.035);
    float wingR = sdBox(rot2(f - vec2(0.19, 0.04), 0.25), vec2(0.055, 0.12), 0.035);
    float d = min(min(body, head), min(wingL, wingR));
    float eyes = min(sdBox(f - vec2(-0.045, -0.17), vec2(0.022, 0.02), 0.008),
                     sdBox(f - vec2(0.045, -0.17), vec2(0.022, 0.02), 0.008));
    float shape = fill(d, px) * (1.0 - fill(eyes, px) * 0.85);
    float belt = 1.0 - smoothstep(0.0, px * 1.5, abs(f.y - 0.02));
    return shape * (1.0 - belt * step(abs(f.x), 0.1) * 0.6);
}

float textureShape(vec2 f) {
    vec2 uv = f * 1.15 + 0.5;
    if (any(lessThan(uv, vec2(0.0))) || any(greaterThan(uv, vec2(1.0)))) return 0.0;
    return texture(Sampler0, uv).a;
}

float iconShape(int kind, vec2 f, float spin, float px) {
    if (kind == 0) return textureShape(rot2(f, spin) * 0.75);
    vec2 r = rot2(f, sin(spin) * 0.35);
    if (kind == 1) return totemShape(r, px);
    return textureShape(r);
}

vec4 icons(vec2 p, vec3 c, int set) {
    float a = 0.0;
    float glow = 0.0;
    for (int l = 0; l < 2; l++) {
        float fl = float(l);
        float s = 26.0 + fl * 16.0;
        float px = 1.0 / s;
        vec2 q = p / s + vec2(time * 0.03 * (1.0 + fl), -time * (0.05 + fl * 0.03)) + fl * 13.7;
        vec2 base = floor(q);
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                vec2 id = base + vec2(x, y);
                float h = hash(id + fl * 7.1);
                if (h > 0.5) continue;
                int kind = set;
                vec2 o = vec2(hash(id + 2.7), hash(id + 9.3)) - 0.5;
                vec2 f = q - id - 0.5 - o * 0.5;
                float size = (0.6 + hash(id + 5.1) * 0.35) * (kind == 0 ? 0.6 : 1.0);
                float dirSign = hash(id + 1.9) > 0.5 ? 1.0 : -1.0;
                float spin = time * (0.15 + hash(id + 3.3) * 0.25) * dirSign + h * 6.28;
                if (kind != 0) spin = time * (0.6 + hash(id + 3.3) * 0.5) + h * 6.28;
                float pulse = 0.7 + 0.3 * sin(time * 0.9 + h * 6.28);
                vec2 lf = f / size;
                a = max(a, iconShape(kind, lf, spin, px / size) * pulse * (0.85 - fl * 0.3));
                glow += exp(-dot(lf, lf) * 14.0) * 0.18 * pulse;
            }
        }
    }
    vec3 col = mix(c * 0.8, c * 1.15 + 0.1, clamp(a, 0.0, 1.0));
    return vec4(col, clamp(a + glow * (1.0 - a), 0.0, 1.0));
}

void main() {
    vec2 q = abs(localPos) - halfSize + radius;
    float d = min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius;
    float coverage = clamp(0.5 - d / max(fwidth(d), 1e-4), 0.0, 1.0);
    if (coverage <= 0.0) {
        discard;
    }
    vec2 p = screenPos;
    vec3 c = vertexColor.rgb;
    if (mode < 1 || mode > 3) discard;
    vec4 fx = icons(p, c, mode - 1);
    vec3 rgb = clamp(fx.rgb, 0.0, 1.0);
    float luma = dot(rgb, vec3(0.299, 0.587, 0.114));
    float cLuma = max(dot(c, vec3(0.299, 0.587, 0.114)), 0.05);
    rgb = mix(rgb, c * (luma / cLuma), 0.55);
    rgb = rgb / (1.0 + max(max(rgb.r, rgb.g), rgb.b) * 0.6) * 1.15;
    rgb *= 0.85;
    float soft = clamp(fx.a, 0.0, 1.0);
    soft = soft / (1.0 + soft * 0.9) * 1.6;
    float a = min(soft, 0.7) * vertexColor.a * coverage;
    if (a <= 0.002) {
        discard;
    }
    fragColor = vec4(clamp(rgb, 0.0, 1.0), a);
}
