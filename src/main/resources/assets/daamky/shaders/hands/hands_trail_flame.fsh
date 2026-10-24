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

layout(std140) uniform TrailFlame {
    vec4 Params;
    vec4 GlowColor1;
    vec4 GlowColor2;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 bloom = texture(BloomSampler, texCoord);
    float mask = handsBloomMask(bloom, Params.y);
    if (mask < 0.002) discard;
    float intensity = mask * Params.x;
    vec3 outerColor = GlowColor2.rgb;
    vec3 middleColor = GlowColor1.rgb;
    vec3 hotColor = mix(GlowColor1.rgb, GlowColor2.rgb, 0.22);
    float body = smoothstep(0.05, 0.45, mask);
    float core = smoothstep(0.60, 1.10, intensity);
    vec3 color = mix(outerColor, middleColor, body);
    color = mix(color, hotColor, core);
    if (Params.y > 0.5) color = handsBloomColor(bloom);
    float sourceAlpha = clamp(intensity, 0.0, 1.0);
    float frameScale = clamp(Params.w * 60.0, 0.0, 6.0);
    float alpha = 1.0 - pow(1.0 - sourceAlpha, frameScale);
    float boost = 1.0 + core * 0.55;
    fragColor = vec4(color * alpha * boost, alpha);
}
