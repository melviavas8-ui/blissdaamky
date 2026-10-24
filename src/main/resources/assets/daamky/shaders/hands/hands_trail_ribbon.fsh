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

layout(std140) uniform TrailRibbon {
    vec4 Params;
    vec4 RibbonParams;
    vec4 GlowColor1;
    vec4 GlowColor2;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 bloom = texture(BloomSampler, texCoord);
    float mask = handsBloomMask(bloom, RibbonParams.z);
    if (mask < 0.003 || mask > 0.98) discard;
    float time = Params.y;
    float width = clamp(Params.w, 0.25, 1.5);
    vec2 position = texCoord * vec2(RibbonParams.y, 1.0);
    float bend = sin(position.y * 19.0 - time * 2.3
            + sin(position.x * 8.0 + time) * 1.2) * RibbonParams.x;
    float contour = mask * 21.0 + bend * 0.65;
    float ribbon = pow(0.5 + 0.5 * cos(contour), 5.0 / width);
    float halo = smoothstep(0.003, 0.045, mask) * (1.0 - smoothstep(0.6, 0.98, mask));
    float flow = 0.82 + 0.18 * sin(position.y * 22.0 - time * 4.0 + contour * 0.4);
    float gradient = 0.5 + 0.5 * sin(position.y * 5.0 - time * 0.8 + mask * 3.0);
    vec3 color = mix(GlowColor1.rgb, GlowColor2.rgb, gradient);
    float colorAlpha = mix(GlowColor1.a, GlowColor2.a, gradient);
    if (RibbonParams.z > 0.5) {
        color = handsBloomColor(bloom);
        colorAlpha = 1.0;
    }
    float sourceAlpha = clamp(halo * (0.045 + ribbon * 0.58) * flow * Params.x * colorAlpha, 0.0, 0.85);
    float alpha = 1.0 - pow(1.0 - sourceAlpha, clamp(Params.z * 60.0, 0.0, 6.0));
    fragColor = vec4(color * alpha, alpha);
}
