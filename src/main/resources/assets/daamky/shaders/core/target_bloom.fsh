#version 330

uniform sampler2D Sampler0;

in vec2 vUV;
in vec4 vColor;

out vec4 fragColor;

void main() {
    vec2 d = (vUV - vec2(0.5)) * 2.0;
    float distSq = dot(d, d);
    if (distSq >= 1.0) discard;
    float dist = sqrt(distSq);
    float falloff = (1.0 - dist) * (1.0 - dist * 0.5);
    vec4 col = texture(Sampler0, vUV) * vColor;
    col.a *= falloff;
    if (col.a <= 0.001) discard;
    fragColor = col;
}
