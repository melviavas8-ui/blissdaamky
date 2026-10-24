#version 330

uniform sampler2D TrailSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    fragColor = texture(TrailSampler, texCoord);
}
