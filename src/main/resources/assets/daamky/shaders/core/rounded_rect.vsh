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
out vec4 vertexColor;
flat out vec2 halfSize;
flat out ivec2 radii;

const float PADDING = 1.0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    localPos = UV0;
    halfSize = abs(UV0) - PADDING;
    radii = UV2 & 0xFFFF;
    vertexColor = Color * ColorModulator;
}