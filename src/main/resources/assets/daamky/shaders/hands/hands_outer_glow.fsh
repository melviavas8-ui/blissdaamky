#version 330

float handsBloomMask(vec4 bloom, float itemColor) {
    return itemColor > 0.5 ? bloom.a : bloom.r;
}

vec3 handsBloomColor(vec4 bloom) {
    return clamp(bloom.rgb / max(bloom.a, 0.00001), 0.0, 1.0);
}

vec3 handsItemColor(vec4 sampleColor) {
    vec3 color = sampleColor.rgb;
    if (sampleColor.a > 0.001) color /= max(sampleColor.a, 0.03125);
    color = clamp(color, 0.0, 1.0);
    float peak = max(color.r, max(color.g, color.b));
    vec3 hue = color / max(peak, 0.001);
    return mix(vec3(0.45), hue * (0.78 + 0.22 * sqrt(peak)), smoothstep(0.005, 0.08, peak));
}

vec4 handsOutlineTap(sampler2D hand, vec2 uv, vec2 texel) {
    vec4 sampleColor = texture(hand, clamp(uv, texel * 0.5, vec2(1.0) - texel * 0.5));
    float weight = smoothstep(0.001, 0.08, sampleColor.a);
    return vec4(handsItemColor(sampleColor) * weight, weight);
}

vec3 handsOutlineColor(sampler2D hand, vec2 uv, vec2 texel, float thickness, vec3 fallbackColor) {
    vec2 offset = texel * max(thickness, 1.0);
    vec2 diagonal = offset * 0.7071;
    vec4 color = handsOutlineTap(hand, uv - vec2(offset.x, 0.0), texel)
            + handsOutlineTap(hand, uv + vec2(offset.x, 0.0), texel)
            + handsOutlineTap(hand, uv - vec2(0.0, offset.y), texel)
            + handsOutlineTap(hand, uv + vec2(0.0, offset.y), texel)
            + handsOutlineTap(hand, uv - diagonal, texel)
            + handsOutlineTap(hand, uv + diagonal, texel)
            + handsOutlineTap(hand, uv + vec2(-diagonal.x, diagonal.y), texel)
            + handsOutlineTap(hand, uv + vec2(diagonal.x, -diagonal.y), texel);
    return color.a > 0.001 ? color.rgb / color.a : fallbackColor;
}


uniform sampler2D BloomSampler;
uniform sampler2D HandMaskSampler;

layout(std140) uniform HandsGlow {
    vec4 Params;
    vec4 GlowColor1;
    vec4 GlowColor2;
    vec4 OutlineColor;
    vec4 Effects;
};

in vec2 texCoord;
out vec4 fragColor;

float coverageAt(vec2 uv) {
    return texture(HandMaskSampler, uv).r;
}

void main() {
    vec2 texel = 1.0 / max(Effects.xy, vec2(1.0));
    vec2 low = texel * 0.5;
    vec2 high = vec2(1.0) - low;
    vec2 safeUv = clamp(texCoord, low, high);
    vec4 bloom = texture(BloomSampler, safeUv);
    float bloomMask = handsBloomMask(bloom, Params.y);
    if (bloomMask < 0.002) discard;
    float coverage = coverageAt(safeUv);
    if (coverage >= 0.22) discard;

    if (coverage < 0.5 && bloomMask > 0.08) {
        float left = coverageAt(safeUv - vec2(texel.x, 0.0));
        float right = coverageAt(safeUv + vec2(texel.x, 0.0));
        float down = coverageAt(safeUv - vec2(0.0, texel.y));
        float up = coverageAt(safeUv + vec2(0.0, texel.y));
        coverage = max(coverage, max(min(left, right), min(down, up)));
        if (coverage < 0.5 && left + right + down + up > 1.5) coverage = 1.0;
    }
    float handMask = smoothstep(0.01, 0.22, coverage);
    float bloomAlpha = bloomMask * (1.0 - handMask);
    if (bloomAlpha < 0.002) discard;

    float intensity = bloomAlpha * Params.x;
    float core = pow(clamp(bloomAlpha, 0.0, 1.0), 0.55);
    vec3 baseColor = mix(GlowColor2.rgb, GlowColor1.rgb, core);
    if (Params.y > 0.5) baseColor = handsBloomColor(bloom);
    fragColor = vec4(max(baseColor, vec3(0.0)), clamp(intensity, 0.0, 1.0));
}
