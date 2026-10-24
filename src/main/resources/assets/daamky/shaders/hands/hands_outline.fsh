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


uniform sampler2D HandMaskSampler;
uniform sampler2D BloomSampler;
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

float coverageAt(vec2 uv) {
    return texture(HandMaskSampler, uv).r;
}

void main() {
    vec2 texel = 1.0 / max(Effects.xy, vec2(1.0));
    vec2 safeUv = clamp(texCoord, texel * 0.5, vec2(1.0) - texel * 0.5);
    vec4 bloom = texture(BloomSampler, safeUv);
    if (handsBloomMask(bloom, Params.y) < 0.0005) discard;
    float handAlpha = coverageAt(safeUv);
    if (handAlpha >= 0.22) discard;

    float left = coverageAt(safeUv - vec2(texel.x, 0.0));
    float right = coverageAt(safeUv + vec2(texel.x, 0.0));
    float down = coverageAt(safeUv - vec2(0.0, texel.y));
    float up = coverageAt(safeUv + vec2(0.0, texel.y));
    float downLeft = coverageAt(safeUv - texel);
    float upRight = coverageAt(safeUv + texel);
    float upLeft = coverageAt(safeUv + vec2(-texel.x, texel.y));
    float downRight = coverageAt(safeUv + vec2(texel.x, -texel.y));
    float bridge = max(max(min(left, right), min(down, up)),
            max(min(downLeft, upRight), min(upLeft, downRight)));
    float neighborCount = left + right + down + up + downLeft + upRight + upLeft + downRight;
    float closedAlpha = max(handAlpha, max(bridge, step(5.5, neighborCount)));
    float handMask = smoothstep(0.01, 0.22, closedAlpha);
    if (handMask >= 0.999) discard;

    float neighborMask;
    if (Effects.z == 1.0) {
        neighborMask = max(max(max(left, right), max(down, up)),
                max(max(downLeft, upRight), max(upLeft, downRight)));
    } else {
        vec2 outlineStep = texel * max(Effects.z, 0.0);
        neighborMask = 0.0;
        neighborMask = max(neighborMask, coverageAt(safeUv + vec2(outlineStep.x, 0.0)));
        neighborMask = max(neighborMask, coverageAt(safeUv - vec2(outlineStep.x, 0.0)));
        neighborMask = max(neighborMask, coverageAt(safeUv + vec2(0.0, outlineStep.y)));
        neighborMask = max(neighborMask, coverageAt(safeUv - vec2(0.0, outlineStep.y)));
        neighborMask = max(neighborMask, coverageAt(safeUv + outlineStep * 0.7071));
        neighborMask = max(neighborMask, coverageAt(safeUv - outlineStep * 0.7071));
        neighborMask = max(neighborMask, coverageAt(safeUv + vec2(outlineStep.x, -outlineStep.y) * 0.7071));
        neighborMask = max(neighborMask, coverageAt(safeUv + vec2(-outlineStep.x, outlineStep.y) * 0.7071));
    }

    float outline = smoothstep(0.02, 0.28, neighborMask) * (1.0 - handMask);
    float alpha = outline * OutlineColor.a;
    if (alpha < 0.002) discard;
    vec3 color = Params.y > 0.5
            ? handsOutlineColor(HandSampler, safeUv, texel, Effects.z, handsBloomColor(bloom))
            : OutlineColor.rgb;
    fragColor = vec4(color, alpha);
}
