#version 330

uniform sampler2D BloomSampler;
uniform sampler2D HandMaskSampler;
uniform sampler2D HandSampler;

layout(std140) uniform HandsGlow {
    vec4 Params;
    vec4 GlowColor1;
    vec4 GlowColor2;
    vec4 OutlineColor;
    vec4 Effects;
};

in vec2 texCoord;
out vec4 fragColor;

vec3 entityColor(vec4 sampleColor) {
    vec3 color = sampleColor.rgb;
    if (sampleColor.a > 0.001) color /= max(sampleColor.a, 0.03125);
    return clamp(color, 0.0, 1.0);
}

vec4 ring(vec2 uv, vec2 offset) {
    vec2 diagonal = offset * 0.7071;
    vec4 sides = vec4(
            texture(HandMaskSampler, uv - vec2(offset.x, 0.0)).r,
            texture(HandMaskSampler, uv + vec2(offset.x, 0.0)).r,
            texture(HandMaskSampler, uv - vec2(0.0, offset.y)).r,
            texture(HandMaskSampler, uv + vec2(0.0, offset.y)).r);
    vec4 corners = vec4(
            texture(HandMaskSampler, uv - diagonal).r,
            texture(HandMaskSampler, uv + diagonal).r,
            texture(HandMaskSampler, uv + vec2(-diagonal.x, diagonal.y)).r,
            texture(HandMaskSampler, uv + vec2(diagonal.x, -diagonal.y)).r);
    vec4 hi = max(sides, corners);
    vec4 lo = min(sides, corners);
    return vec4(max(max(hi.x, hi.y), max(hi.z, hi.w)), min(min(lo.x, lo.y), min(lo.z, lo.w)), 0.0, 0.0);
}

vec3 colorAround(vec2 uv, vec2 offset, vec3 fallback) {
    vec2 diagonal = offset * 0.7071;
    vec4 sum = vec4(0.0);
    vec2 taps[8] = vec2[8](vec2(-offset.x, 0.0), vec2(offset.x, 0.0), vec2(0.0, -offset.y), vec2(0.0, offset.y),
            -diagonal, diagonal, vec2(-diagonal.x, diagonal.y), vec2(diagonal.x, -diagonal.y));
    for (int i = 0; i < 8; i++) {
        vec4 s = texture(HandSampler, uv + taps[i]);
        float w = smoothstep(0.001, 0.08, s.a);
        sum += vec4(entityColor(s) * w, w);
    }
    return sum.a > 0.001 ? sum.rgb / sum.a : fallback;
}

void main() {
    vec2 texel = 1.0 / max(Effects.xy, vec2(1.0));
    vec2 uv = clamp(texCoord, texel * 0.5, vec2(1.0) - texel * 0.5);
    vec4 bloomSample = texture(BloomSampler, uv);
    vec3 bloomColor = clamp(bloomSample.rgb / max(bloomSample.a, 0.00001), 0.0, 1.0);
    float bloom = bloomSample.a;
    float center = texture(HandMaskSampler, uv).r;
    float thickness = max(Effects.z, 1.0);

    if (center >= 0.5) {
        vec3 base = entityColor(texture(HandSampler, uv));
        float inner = 0.0;
        for (float i = 1.0; i <= 3.0; i += 1.0) {
            inner = max(inner, (1.0 - ring(uv, texel * thickness * i).y) * (1.0 - (i - 1.0) / 3.0));
        }
        float alpha = clamp(Params.z + inner * 0.45 * Params.z * 2.0, 0.0, 1.0);
        if (alpha < 0.002) discard;
        vec3 color = mix(base, vec3(1.0), inner * 0.35);
        fragColor = vec4(color * alpha, alpha);
        return;
    }

    vec4 near = ring(uv, texel * thickness);
    vec4 wide = ring(uv, texel * (thickness + 1.5));
    float edge = smoothstep(0.05, 0.4, near.x);
    float edgeSoft = smoothstep(0.05, 0.4, wide.x) * 0.55;
    float outlineAlpha = max(edge, edgeSoft) * (1.0 - smoothstep(0.2, 0.5, center));

    vec3 base = colorAround(uv, texel * (thickness + 1.0), bloomColor);
    vec3 core = mix(base, vec3(1.0), 0.6 * edge);
    vec3 halo = mix(base, vec3(1.0), 0.15);

    float glowAlpha = clamp(pow(clamp(bloom, 0.0, 1.0), 0.6) * Params.x, 0.0, 1.0);
    if (outlineAlpha < 0.002 && glowAlpha < 0.002) discard;

    vec3 glow = halo * glowAlpha;
    fragColor = vec4(mix(glow, core, outlineAlpha), outlineAlpha);
}
