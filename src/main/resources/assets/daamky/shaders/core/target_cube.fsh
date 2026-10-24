#version 330

uniform sampler2D Sampler0;

in vec2 vUV;
in vec4 vColor;

out vec4 fragColor;

float mask(vec2 uv) {
    if (any(lessThan(uv, vec2(0.0))) || any(greaterThan(uv, vec2(1.0)))) return 0.0;
    return texture(Sampler0, uv).a;
}

void main() {
    vec2 uv = (vUV - vec2(0.5)) * 1.16 + vec2(0.5);
    float core = mask(uv);
    float glow = 0.0;
    for (int x = -2; x <= 2; x++) {
        for (int y = -2; y <= 2; y++) {
            glow += mask(uv + vec2(float(x), float(y)) * 0.014);
        }
    }
    glow *= 0.018;
    float alpha = max(core * 0.9, glow) * vColor.a;
    if (alpha <= 0.003) discard;
    vec3 color = mix(vColor.rgb, vec3(1.0), core * 0.25);
    fragColor = vec4(color, alpha);
}
