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

layout(std140) uniform TrailEnergy {
    vec4 Params;
    vec4 EnergyParams;
    vec4 GlowColor1;
    vec4 GlowColor2;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 bloom = texture(BloomSampler, texCoord);
    float mask = handsBloomMask(bloom, Params.y);
    if (mask < 0.001) discard;

    float time = EnergyParams.x;
    float strength = EnergyParams.y;
    float pulseStrength = EnergyParams.z;
    float exposure = Params.x * strength;
    float halo = smoothstep(0.002, 0.24, mask);
    float body = smoothstep(0.10, 0.58, mask);
    float core = smoothstep(0.38, 0.92, mask * exposure);

    float gradient = smoothstep(0.18, 0.92, texCoord.y);
    vec3 color1 = Params.y > 0.5 ? handsBloomColor(bloom) : GlowColor1.rgb;
    vec3 color2 = Params.y > 0.5 ? color1 : GlowColor2.rgb;
    vec3 baseColor = mix(color1, color2, gradient);
    vec3 outerColor = mix(color2, baseColor, 0.35);

    float filamentA = 0.5 + 0.5 * sin(
            texCoord.y * 92.0 + sin(texCoord.x * 31.0 - time * 1.7) * 2.2 - time * 8.0
    );
    float filamentB = 0.5 + 0.5 * sin(
            texCoord.x * 67.0 - texCoord.y * 29.0 + time * 5.3
    );
    float filament = pow(max(filamentA, filamentB * 0.82), 7.0) * halo * pulseStrength;
    float pulse = 1.0 + 0.12 * pulseStrength
            * sin(time * (3.5 + pulseStrength * 2.0) + texCoord.y * 18.0);

    vec3 color = mix(outerColor * 0.68, baseColor * 1.18, body);
    color = mix(color, min(baseColor * 1.22, vec3(1.0)), core * 0.72);
    color += mix(baseColor, color1, 0.55) * filament * 0.42;

    float sourceAlpha = clamp(
            (halo * 0.22 + body * 0.38 + core * 0.34 + filament * 0.12)
                    * exposure * pulse,
            0.0, 0.94
    );
    float frameScale = clamp(Params.w * 60.0, 0.0, 6.0);
    float alpha = 1.0 - pow(1.0 - sourceAlpha, frameScale);
    float lightBoost = 1.20 + core * 0.48 + filament * 0.20;

    fragColor = vec4(color * alpha * lightBoost, alpha);
}
