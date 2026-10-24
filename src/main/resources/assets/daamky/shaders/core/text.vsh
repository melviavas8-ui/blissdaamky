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

out vec2 texCoord;
out vec4 vertexColor;
flat out ivec2 params;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord = UV0;
    params = UV2 & 0xFFFF;
    vertexColor = Color * ColorModulator;
}
