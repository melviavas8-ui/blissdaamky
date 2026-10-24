#version 330

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

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

float sdRoundRect(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    float r = cornerRadius();
    vec2 b = halfSize;

    float d = sdRoundRect(localPos, b, r);
    float coverage = clamp(0.5 - d / max(fwidth(d), 1e-4), 0.0, 1.0);
    float a = coverage * opacity();
    if (a <= 0.0) {
        discard;
    }

    vec2 pos = localPos;
    float maxDist = max(min(b.x, b.y), 1.0);
    float edgeGradient = 1.0 - clamp(abs(d) / maxDist, 0.0, 1.0);
    float fresnel = clamp(exp(50.0 * log(clamp(edgeGradient, 0.001, 1.0))), 0.0, 1.0);

    vec2 texSize = vec2(textureSize(Sampler0, 0)) * float(1 << blurLevel());
    vec2 uv = gl_FragCoord.xy / texSize;

    vec2 dir = normalize(pos + vec2(0.001));
    dir.y = -dir.y;
    float distortStrength = 0.08;
    vec2 baseDistort = dir * fresnel * distortStrength;

    float aberration = distortStrength * 2.5;
    vec3 refracted = vec3(0.0);
    float totalWeight = 0.0;
    for (int i = 0; i < 5; i++) {
        float s = float(i) * 0.25;
        float g = s * 2.0 - 1.0;
        float weight = exp(-g * g * 2.0);
        vec2 offset = baseDistort * (0.3 + s * 1.4);
        refracted.r += texture(Sampler0, uv + offset * (1.0 + aberration)).r * weight;
        refracted.g += texture(Sampler0, uv + offset).g * weight;
        refracted.b += texture(Sampler0, uv + offset * (1.0 - aberration)).b * weight;
        totalWeight += weight;
    }
    refracted /= totalWeight;

    vec3 sharp = texture(Sampler1, uv + baseDistort).rgb;
    refracted = mix(refracted, sharp, fresnel * 0.15);

    float body = smoothstep(0.6, 1.4, -d);
    vec3 color = mix(refracted, vertexColor.rgb, vertexColor.a * body);
    vec3 fresnelColor = vertexColor.rgb;

    float caustic = pow(edgeGradient, 4.0) * 0.1;

    color += caustic * fresnelColor;

    float dither = fract(sin(dot(gl_FragCoord.xy, vec2(12.9898, 78.233))) * 43758.5453) - 0.5;
    fragColor = vec4(clamp(color, 0.0, 1.0) + dither / 255.0, a);
}
