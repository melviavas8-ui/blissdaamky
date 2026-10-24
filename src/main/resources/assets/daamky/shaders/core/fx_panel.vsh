#version 330

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

layout(std140) uniform Projection {
    mat4 ProjMat;
};

in vec3 Position;
in vec2 UV0;
in ivec2 UV2;
in vec4 Color;

out vec2 localPos;
out vec2 screenPos;
out vec4 vertexColor;
flat out vec2 halfSize;
flat out float radius;
flat out int mode;
flat out float time;

const float PADDING = 1.0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    localPos = UV0;
    screenPos = Position.xy;
    halfSize = abs(UV0) - PADDING;
    int packedBits = UV2.x & 0xFFFF;
    radius = float(packedBits & 255) * 0.5;
    mode = (packedBits >> 8) & 255;
    time = float(UV2.y & 0xFFFF) / 62.5;
    vertexColor = Color * ColorModulator;
}
