#version 330

out vec4 fragColor;

uniform sampler2D SceneSampler;
uniform sampler2D DepthSampler;

struct CircleData {
    vec4 originAndProgress;
    vec4 colorAndRadius;
    vec4 fxParams;
};

layout(std140) uniform WorldJumpCircleUniforms {
    mat4 InvProjMat;
    mat4 InvViewMat;
    vec4 CameraPos;
    vec4 ScreenParams;
    CircleData Circles[16];
};

vec3 reconstructWorldPos(vec2 texCoord, float depth) {
    float z = depth;
    float clipZ = (CameraPos.w > 0.5) ? z : z * 2.0 - 1.0;
    vec4 clipSpace = vec4(texCoord * 2.0 - 1.0, clipZ, 1.0);
    vec4 viewSpace = InvProjMat * clipSpace;
    viewSpace /= viewSpace.w;
    vec4 worldSpace = InvViewMat * viewSpace;
    return worldSpace.xyz + CameraPos.xyz;
}

void main() {
    vec2 uv = gl_FragCoord.xy / vec2(textureSize(SceneSampler, 0));
    float depth = texture(DepthSampler, uv).r;

    if (depth >= 1.0) {
        fragColor = texture(SceneSampler, uv);
        return;
    }

    vec3 worldPos = reconstructWorldPos(uv, depth);

    vec3 dWorldX = dFdx(worldPos);
    vec3 dWorldY = dFdy(worldPos);
    vec3 surfaceNormal = cross(dWorldX, dWorldY);
    float normalLen = length(surfaceNormal);

    float slopeMask = (normalLen > 1e-4) ? smoothstep(0.3, 0.7, abs(surfaceNormal.y / normalLen)) : 1.0;

    vec3 totalGlow = vec3(0.0);

    int count = int(ScreenParams.w);

    for (int i = 0; i < count; i++) {
        CircleData circle = Circles[i];

        vec3 origin = circle.originAndProgress.xyz;
        float progress = circle.originAndProgress.w;
        vec3 circleColor = circle.colorAndRadius.rgb;
        float maxRadius = circle.colorAndRadius.w;

        float glowPower = circle.fxParams.y;
        float ringWidth = max(circle.fxParams.z, 0.05);
        float heightFalloff = circle.fxParams.w;

        vec3 deltaWorld = worldPos - origin;
        float distXZ = length(deltaWorld.xz);
        float distY = abs(deltaWorld.y);

        float easedProgress = sin(progress * 1.5707963);
        float currentRadius = easedProgress * maxRadius;

        float d = distXZ - currentRadius;
        float absD = abs(d);

        float verticalMask = smoothstep(heightFalloff, 0.0, distY);
        float fadeIn = smoothstep(0.0, 0.06, progress);
        float fadeOut = pow(1.0 - progress, 1.2);

        float lifeMask = fadeIn * fadeOut * verticalMask * slopeMask;

        if (lifeMask > 0.0005) {
            float lineWidth = ringWidth * 0.12;
            float line = smoothstep(lineWidth, lineWidth * 0.35, absD);
            float halo = exp(-absD / (ringWidth * 0.4)) * 0.22;
            vec3 lineColor = mix(circleColor, vec3(1.0), 0.25);
            totalGlow += (lineColor * line * 0.9 + circleColor * halo) * glowPower * lifeMask;
        }
    }

    vec3 sceneColor = texture(SceneSampler, uv).rgb;
    fragColor = vec4(sceneColor + totalGlow, 1.0);
}
