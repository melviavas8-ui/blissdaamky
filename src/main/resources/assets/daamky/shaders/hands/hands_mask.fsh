#version 330

uniform sampler2D HandSampler;
uniform sampler2D HandDepthSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    float alpha = texture(HandSampler, texCoord).a;
    float depth = 1.0 - texture(HandDepthSampler, texCoord).r;
    float coverage = max(smoothstep(0.001, 0.08, alpha), step(0.000001, depth));
    fragColor = vec4(coverage);
}
