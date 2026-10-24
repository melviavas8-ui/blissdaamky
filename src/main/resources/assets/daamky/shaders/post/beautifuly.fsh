#version 330

out vec4 fragColor;

uniform sampler2D SceneSampler;

layout(std140) uniform BeautifulyUniforms {
    vec4 Params;
    vec4 Tint;
};

void main() {
    vec3 color = texelFetch(SceneSampler, ivec2(gl_FragCoord.xy), 0).rgb;
    float luma = dot(color, vec3(0.2126, 0.7152, 0.0722));
    color = mix(vec3(luma), color, Params.x);
    float w = Params.y;
    color *= vec3(1.0 + 0.12 * w, 1.0 + 0.03 * w, 1.0 - 0.15 * w);
    vec3 lumaWeights = vec3(0.2126, 0.7152, 0.0722);
    float tintLuma = max(dot(Tint.rgb, lumaWeights), 0.05);
    vec3 tinted = Tint.rgb * (dot(color, lumaWeights) / tintLuma);
    color = mix(color, tinted, Tint.a);
    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
