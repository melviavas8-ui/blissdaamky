#version 330

uniform sampler2D Sampler0;

layout(std140) uniform BloomData {
    vec4 Params;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 c = texture(Sampler0, texCoord).rgb * Params.x;
    c = vec3(1.0) - exp(-c * Params.y);
    fragColor = vec4(c, 1.0);
}
