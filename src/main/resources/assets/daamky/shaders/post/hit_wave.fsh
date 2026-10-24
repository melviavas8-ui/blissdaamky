#version 330

out vec4 fragColor;

uniform sampler2D SceneSampler;

layout(std140) uniform HitWaveUniforms {
    vec4 Screen;
    vec4 Waves[4];
};

void main() {
    vec2 uv = gl_FragCoord.xy * Screen.xy;
    float aspect = Screen.z;
    vec2 offset = vec2(0.0);
    float shade = 0.0;
    for (int i = 0; i < 4; i++) {
        vec4 w = Waves[i];
        if (w.w <= 0.0) continue;
        float t = w.z;
        vec2 delta = uv - w.xy;
        delta.x *= aspect;
        float d = length(delta);
        vec2 dir = delta / max(d, 0.0001);
        float radius = mix(0.02, 1.1, 1.0 - pow(1.0 - t, 2.2));
        float width = mix(0.035, 0.11, t);
        float x = (d - radius) / width;
        float band = exp(-x * x);
        float ripple = sin(x * 2.6) * band;
        float fade = pow(1.0 - t, 1.6) * smoothstep(0.0, 0.08, t);
        float amp = w.w * fade;
        vec2 o = dir * ripple * amp * 0.012;
        o.x /= aspect;
        offset += o;
        shade += band * amp;
    }
    vec3 base = texture(SceneSampler, uv + offset).rgb;
    vec2 ca = offset * 0.35;
    vec3 color = vec3(texture(SceneSampler, uv + offset + ca).r, base.g, texture(SceneSampler, uv + offset - ca).b);
    color += shade * 0.025;
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
