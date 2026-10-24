#version 330

out vec4 fragColor;

uniform sampler2D SceneSampler;
uniform sampler2D DepthSampler;

layout(std140) uniform WorldFxUniforms {
    mat4 InvProjMat;
    mat4 InvViewMat;
    mat4 ProjMat;
    mat4 ViewMat;
    vec4 CameraPos;
    vec4 ScreenParams;
    vec4 Modes;
    vec4 FogColor;
    vec4 FogRange;
    vec4 SkyColor;
    vec4 CamForward;
    vec4 CamUp;
    vec4 CamRight;
    vec4 SunDir;
};

float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.zyx + 31.32);
    return fract((p.x + p.y) * p.z);
}

float flowerNoise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float n000 = hash13(i);
    float n100 = hash13(i + vec3(1, 0, 0));
    float n010 = hash13(i + vec3(0, 1, 0));
    float n110 = hash13(i + vec3(1, 1, 0));
    float n001 = hash13(i + vec3(0, 0, 1));
    float n101 = hash13(i + vec3(1, 0, 1));
    float n011 = hash13(i + vec3(0, 1, 1));
    float n111 = hash13(i + vec3(1, 1, 1));
    return mix(mix(mix(n000, n100, f.x), mix(n010, n110, f.x), f.y),
               mix(mix(n001, n101, f.x), mix(n011, n111, f.x), f.y), f.z);
}

float fbm(vec3 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += a * flowerNoise3(p);
        p = p * 2.03 + vec3(17.1, 3.7, 9.2);
        a *= 0.5;
    }
    return v;
}

const vec3 MIST = vec3(0.60, 0.63, 0.67);

bool isSky(float depth) {
    return depth <= 0.0 || depth >= 1.0;
}

vec3 worldPos(vec2 uv, float depth);

float fogFactor(vec2 uv) {
    float depth = texture(DepthSampler, uv).r;
    if (isSky(depth)) {
        return 1.0;
    }
    vec3 to = worldPos(uv, depth) - CameraPos.xyz;
    float t = clamp((length(to) - FogRange.x) / max(FogRange.y - FogRange.x, 0.001), 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}

vec3 fogBlur(vec2 uv, vec3 scene, float k) {
    vec3 sum = scene * (0.35 + k);
    float total = 0.35 + k;
    for (int i = 0; i < 28; i++) {
        float a = float(i) * 2.39996;
        float r = sqrt((float(i) + 0.5) / 28.0) * 16.0;
        vec2 suv = clamp(uv + vec2(cos(a), sin(a)) * r * ScreenParams.xy, vec2(0.001), vec2(0.999));
        float sk = fogFactor(suv);
        float w = sk * (1.0 - r / 20.0);
        sum += texture(SceneSampler, suv).rgb * w;
        total += w;
    }
    vec3 blurred = sum / max(total, 0.0001);
    vec3 result = mix(scene, blurred, smoothstep(0.0, 0.85, k));
    if (Modes.w > 1.5) {
        result = mix(result, FogColor.rgb, k * FogColor.w);
    }
    return result;
}

vec3 viewDir(vec2 uv) {
    vec2 ndc = uv * 2.0 - 1.0;
    return normalize(CamForward.xyz + CamRight.xyz * ndc.x * CamForward.w + CamUp.xyz * ndc.y * CamUp.w);
}

vec3 worldPos(vec2 uv, float depth) {
    float clipZ = (CameraPos.w > 0.5) ? depth : depth * 2.0 - 1.0;
    vec4 clip = vec4(uv * 2.0 - 1.0, clipZ, 1.0);
    vec4 view = InvProjMat * clip;
    view /= view.w;
    return (InvViewMat * view).xyz + CameraPos.xyz;
}

vec2 projectWorld(vec3 p, out float behind) {
    vec4 view = ViewMat * vec4(p - CameraPos.xyz, 1.0);
    vec4 clip = ProjMat * view;
    behind = clip.w <= 0.0 ? 1.0 : 0.0;
    return (clip.xy / clip.w) * 0.5 + 0.5;
}

float hashP(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float noiseP(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    vec4 a = mix(vec4(hashP(i), hashP(i + vec3(0, 1, 0)), hashP(i + vec3(0, 0, 1)), hashP(i + vec3(0, 1, 1))),
                 vec4(hashP(i + vec3(1, 0, 0)), hashP(i + vec3(1, 1, 0)), hashP(i + vec3(1, 0, 1)), hashP(i + vec3(1, 1, 1))), f.x);
    vec2 b = mix(a.xz, a.yw, f.y);
    return mix(b.x, b.y, f.z);
}

float fbmP(vec3 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 3; ++i) {
        v += a * noiseP(p);
        p = p * 2.0 + vec3(100.0);
        a *= 0.5;
    }
    return v;
}

float hashJ(vec2 p) {
    uvec2 cell = uvec2(ivec2(p));
    uint h = cell.x * 1597334677u ^ cell.y * 3812015801u;
    h = (h ^ (h >> 16u)) * 2246822519u;
    h = (h ^ (h >> 13u)) * 3266489917u;
    h ^= h >> 16u;
    return float(h >> 8u) * (1.0 / 16777216.0);
}

float noiseJ(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hashJ(i), hashJ(i + vec2(1.0, 0.0)), f.x), mix(hashJ(i + vec2(0.0, 1.0)), hashJ(i + vec2(1.0, 1.0)), f.x), f.y);
}

float fbmJ(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    float freq = 1.0;
    for (int k = 0; k < 5; k++) {
        v += a * noiseJ(p * freq);
        freq *= 2.1;
        a *= 0.48;
    }
    return v;
}

vec2 warpJ(vec2 p, float t) {
    vec2 q = vec2(fbmJ(p + t * 0.12), fbmJ(p + vec2(5.2, 1.3) + t * 0.09));
    vec2 r = vec2(fbmJ(p + 4.0 * q + vec2(1.7, 9.2) + t * 0.06), fbmJ(p + 4.0 * q + vec2(8.3, 2.8) + t * 0.07));
    return p + 1.8 * r;
}

vec4 juiceSky(vec3 dir, vec2 uv, float anim, vec3 neb1, vec3 neb2) {
    float up = clamp(dir.y, 0.0, 1.0);
    float horizonFade = smoothstep(-0.15, 0.05, dir.y);
    if (horizonFade < 0.005) return vec4(0.0);
    vec2 p = dir.xz + vec2(0.0, anim * 0.04) * 0.9;
    p.y += dir.y * 0.2;
    vec2 wp = warpJ(p, anim * 0.045);
    float n1 = fbmJ(wp * 0.9);
    float n2 = fbmJ(wp * 1.5 + vec2(2.7, 4.3));
    float n3 = fbmJ(wp * 0.5 + vec2(-1.8, 3.1));
    float n4 = fbmJ(wp * 2.2 + vec2(-3.0, 1.2));
    float juice = smoothstep(0.15, 0.85, n1 * 0.4 + n2 * 0.25 + n3 * 0.20 + n4 * 0.15);
    float heightMask = 0.7 + 0.3 * smoothstep(0.0, 0.6, up);
    float flicker = (0.8 + 0.2 * sin(anim * 0.5 + dir.x * 14.0 + dir.z * 10.0)) * (0.9 + 0.1 * sin(anim * 0.9 + dir.y * 18.0));
    juice *= heightMask * flicker;
    float glitter = 0.0;
    for (int i = 0; i < 3; i++) {
        float seed = float(i) * 2.13;
        vec2 pos = vec2(0.5 + 0.8 * sin(seed * 1.7 + anim * 0.2 + p.x * 0.5), 0.5 + 0.8 * cos(seed * 2.3 + anim * 0.15 + p.y * 0.5));
        float d = length(uv - pos);
        float sz = max(abs(0.01 + 0.02 * sin(seed * 3.1 + anim * 0.3)), 0.001);
        glitter += exp(-d * d / (2.0 * sz * sz)) * 0.6;
    }
    glitter *= 0.7 + 0.3 * sin(anim * 0.6 + p.x * 5.0);
    float mix1 = 0.5 + 0.5 * sin(anim * 0.06 + p.x * 1.2 + p.y * 0.8 + juice * 1.5);
    float mix2 = 0.5 + 0.5 * cos(anim * 0.08 + p.x * 0.9 - p.y * 1.1 + juice * 2.0);
    vec3 color = mix(mix(neb1, neb2, mix1), mix(neb2, vec3(1.0, 0.9, 0.8), 0.3), mix2);
    float hot = exp(-abs(juice - 0.4) * 6.0) * 0.6;
    color += vec3(1.0, 0.95, 0.7) * hot + vec3(1.0) * glitter * 0.8 + vec3(0.9, 0.8, 1.0) * exp(-juice * 3.0) * 0.2;
    float intensity = juice * 1.3 + hot * 0.3 + glitter * 0.4;
    float alpha = clamp(intensity * 1.5, 0.0, 1.0) * horizonFade * clamp(1.0 - length(dir.xz) * 0.15, 0.0, 1.0);
    return vec4(clamp(color * intensity, 0.0, 1.0), alpha);
}

vec3 starField(vec3 rd, float t, float density, float size) {
    vec3 cell = floor(rd * size);
    float h = hashP(cell);
    if (h <= 1.0 - density) return vec3(0.0);
    vec3 off = vec3(hashP(cell + 1.1), hashP(cell + 2.2), hashP(cell + 3.3)) - 0.5;
    vec3 pos = normalize((cell + 0.5 + off * 0.8) / size);
    float d = length(rd - pos);
    float twinkle = 0.6 + 0.4 * sin(t * 2.5 + h * 97.0);
    float temp = hashP(cell + 7.7);
    vec3 col = temp < 0.3 ? vec3(0.75, 0.85, 1.0) : (temp < 0.5 ? vec3(1.0, 0.85, 0.7) : vec3(1.0));
    return col * exp(-d * size * 4.5) * twinkle * (0.4 + 0.6 * hashP(cell + 5.5));
}

vec4 auroraSky(vec3 dir, float t, vec3 accent) {
    float y = max(dir.y, 0.0);
    vec3 col = mix(vec3(0.02, 0.04, 0.08), vec3(0.0, 0.01, 0.03), sqrt(y));
    col += starField(dir, t, 0.012, 140.0);
    if (dir.y > 0.0) {
        vec3 aurora = vec3(0.0);
        for (int i = 0; i < 24; i++) {
            float h = 0.8 + float(i) * 0.06;
            vec2 p = dir.xz / (dir.y + 0.05) * h;
            float n = fbmP(vec3(p * 0.6, t * 0.05 + float(i) * 0.02));
            float band = smoothstep(0.55, 0.75, n) * smoothstep(0.85, 0.6, n);
            float curtain = sin(p.x * 1.2 + n * 6.0 + t * 0.2) * 0.5 + 0.5;
            vec3 c = mix(vec3(0.1, 1.0, 0.55), mix(accent, vec3(0.6, 0.2, 1.0), 0.5), float(i) / 24.0);
            aurora += c * band * curtain * exp(-float(i) * 0.08) * 0.09;
        }
        col += aurora * smoothstep(0.0, 0.2, dir.y);
    }
    return vec4(col, 1.0);
}

vec4 starryNightSky(vec3 dir, float t, vec3 accent) {
    float y = clamp(dir.y, -0.2, 1.0);
    vec3 col = mix(vec3(0.05, 0.07, 0.16), vec3(0.005, 0.01, 0.03), smoothstep(-0.1, 0.8, y));
    vec3 axis = normalize(vec3(0.3, 0.8, 0.5));
    float band = exp(-abs(dot(dir, axis)) * 6.0);
    float dust = fbmP(dir * 6.0);
    col += mix(vec3(0.25, 0.22, 0.35), accent * 0.4, 0.3) * band * smoothstep(0.35, 0.8, dust) * 0.35;
    col += starField(dir, t, 0.02 + band * 0.04, 180.0);
    col += starField(dir, t * 0.7, 0.006, 70.0) * 1.4;
    float meteorT = fract(t * 0.07);
    vec3 mStart = normalize(vec3(sin(floor(t * 0.07) * 4.1), 0.7, cos(floor(t * 0.07) * 4.1)));
    vec3 mDir = normalize(cross(mStart, vec3(0.0, 1.0, 0.0)));
    vec3 mPos = normalize(mStart + mDir * meteorT * 0.6 - vec3(0.0, meteorT * 0.2, 0.0));
    vec3 toM = dir - mPos;
    float along = dot(toM, -mDir);
    float side = length(toM + mDir * along);
    float meteor = exp(-side * 900.0) * smoothstep(0.15, 0.0, along) * step(0.0, along) * smoothstep(0.0, 0.1, meteorT) * smoothstep(0.5, 0.3, meteorT);
    col += vec3(1.0) * meteor * 2.0;
    return vec4(col, 1.0);
}

vec4 sunsetSky(vec3 dir, float t, vec3 accent) {
    float y = dir.y;
    vec3 horizon = vec3(1.0, 0.45, 0.15);
    vec3 mid = vec3(0.85, 0.3, 0.45);
    vec3 top = mix(vec3(0.15, 0.1, 0.35), accent * 0.3, 0.3);
    vec3 col = y < 0.0 ? mix(horizon, vec3(0.25, 0.1, 0.12), smoothstep(0.0, -0.3, y))
                       : mix(mix(horizon, mid, smoothstep(0.0, 0.15, y)), top, smoothstep(0.12, 0.7, y));
    vec3 sun = normalize(vec3(0.0, 0.06, -1.0));
    float sd = dot(dir, sun);
    col += vec3(1.0, 0.75, 0.4) * pow(max(sd, 0.0), 40.0) * 0.8;
    col += vec3(1.0, 0.9, 0.7) * smoothstep(0.9985, 0.999, sd);
    if (y > 0.0) {
        vec2 p = dir.xz / (y + 0.08);
        float c = fbmP(vec3(p * 0.4 + vec2(t * 0.01, 0.0), 0.0));
        float cloud = smoothstep(0.5, 0.75, c) * smoothstep(0.6, 0.05, y);
        vec3 cloudCol = mix(vec3(0.35, 0.12, 0.2), vec3(1.0, 0.55, 0.3), pow(max(sd, 0.0), 3.0));
        col = mix(col, cloudCol, cloud * 0.85);
    }
    return vec4(col, 1.0);
}

vec4 cloudsSky(vec3 dir, float t, vec3 accent) {
    float y = dir.y;
    vec3 col = mix(vec3(0.7, 0.82, 0.95), vec3(0.22, 0.45, 0.85), smoothstep(-0.05, 0.6, y));
    vec3 sun = normalize(vec3(0.4, 0.6, 0.3));
    float sd = max(dot(dir, sun), 0.0);
    col += vec3(1.0, 0.95, 0.85) * (pow(sd, 400.0) * 3.0 + pow(sd, 8.0) * 0.2);
    if (y > 0.0) {
        vec2 p = dir.xz / (y + 0.1) * 0.6 + vec2(t * 0.02, t * 0.01);
        float c = fbm(vec3(p, t * 0.01)) + 0.5 * fbm(vec3(p * 3.0, t * 0.02)) - 0.25;
        float cover = smoothstep(0.35, 0.65, c);
        float shade = smoothstep(0.3, 0.9, c);
        vec3 cloudCol = mix(vec3(1.0), vec3(0.6, 0.65, 0.75), shade * 0.7) + vec3(1.0, 0.9, 0.8) * pow(sd, 4.0) * 0.3;
        col = mix(col, cloudCol, cover * smoothstep(0.0, 0.15, y));
    }
    return vec4(col, 1.0);
}

vec4 nebulaSky(vec3 dir, float t, vec3 accent) {
    vec3 p = dir * 2.5 + vec3(t * 0.004, 0.0, t * 0.003);
    float n1 = fbmP(p);
    float n2 = fbmP(p * 2.0 + n1 * 1.5);
    float n3 = fbmP(p * 4.0 - n2);
    vec3 c1 = accent;
    vec3 c2 = vec3(accent.b, accent.r, accent.g) * 0.8 + vec3(0.1, 0.0, 0.2);
    vec3 col = vec3(0.005, 0.005, 0.015);
    col += mix(c1, c2, n2) * pow(n1, 3.0) * 1.4;
    col += vec3(1.0, 0.9, 0.95) * pow(n3 * n2, 4.0) * 1.2;
    col *= 1.0 - smoothstep(0.5, 0.9, n3) * 0.6;
    col += starField(dir, t, 0.015, 160.0);
    col += starField(dir, t, 0.004, 60.0) * mix(vec3(1.0), accent, 0.3) * 1.5;
    return vec4(col, 1.0);
}

vec4 skyLayer(vec3 dir, vec2 uv, float time) {
    float mode = Modes.x;
    vec3 accent = SkyColor.rgb;
    if (mode < 1.5) return juiceSky(dir, uv, time, accent, mix(accent, vec3(1.0), 0.45));
    if (mode < 2.5) return auroraSky(dir, time, accent);
    if (mode < 3.5) return starryNightSky(dir, time, accent);
    if (mode < 4.5) return sunsetSky(dir, time, accent);
    if (mode < 5.5) return cloudsSky(dir, time, accent);
    return nebulaSky(dir, time, accent);
}

vec3 skyColor(vec3 dir, float time) {
    vec4 layer = skyLayer(dir, vec2(0.5), time);
    return layer.rgb * layer.a;
}

float raysMask(vec2 uv, vec2 sunUv, float jitter) {
    vec2 stepUv = (sunUv - uv) / 32.0;
    vec2 position = uv + stepUv * jitter;
    float sum = 0.0;
    float weight = 1.0;
    float total = 0.0;
    for (int i = 0; i < 32; i++) {
        vec2 sampleUv = clamp(position, vec2(0.001), vec2(0.999));
        sum += (isSky(texture(DepthSampler, sampleUv).r) ? 1.0 : 0.0) * weight;
        total += weight;
        weight *= 0.955;
        position += stepUv;
    }
    return sum / max(total, 0.0001);
}

vec3 sunRays(vec2 uv, float time) {
    float strength = SunDir.w;
    if (strength <= 0.0) return vec3(0.0);
    vec3 sun = normalize(SunDir.xyz);
    bool moon = sun.y < -0.05;
    vec3 rayColor = mix(vec3(1.0, 0.82, 0.55), SkyColor.rgb, 0.18);
    if (moon) {
        sun = -sun;
        strength *= 0.4;
        rayColor = mix(vec3(0.62, 0.72, 1.0), SkyColor.rgb, 0.2);
    }
    float behind;
    vec2 sunUv = projectWorld(CameraPos.xyz + sun * 1000.0, behind);
    vec3 dir = viewDir(uv);
    float sunAlignment = dot(dir, sun);
    float facing = dot(normalize(CamForward.xyz), sun);
    float visibility = behind > 0.5 ? 0.0 : smoothstep(0.0, 0.35, facing);
    if (visibility <= 0.001) return vec3(0.0);
    float lowSun = clamp(1.0 - abs(sun.y) * 2.2, 0.0, 1.0);
    float jitter = fract(sin(dot(gl_FragCoord.yx + vec2(17.0, 59.0), vec2(12.9898, 78.233))) * 43758.5453);
    float mask = raysMask(uv, sunUv, jitter);
    float angular = pow(clamp(sunAlignment * 0.5 + 0.5, 0.0, 1.0), 3.0);
    float rays = mask * angular * visibility * strength;
    float skyHere = isSky(texture(DepthSampler, uv).r) ? 1.0 : 0.0;
    rays *= 0.55 + 0.45 * skyHere;
    float deltaY = dir.y - sun.y;
    float angularSquared = max(2.0 * (1.0 - sunAlignment), 0.0);
    float disc = exp(-angularSquared * 2600.0);
    float deltaX2 = max(angularSquared - deltaY * deltaY, 0.0);
    float streak = exp(-(deltaY * deltaY * 850.0 + deltaX2 * 34.0));
    float glowVisibility = visibility * strength * (0.35 + 0.65 * lowSun);
    float halo = pow(clamp(sunAlignment, 0.0, 1.0), 24.0) * 0.35;
    vec3 sunGlow = rayColor * (disc * 1.05 * skyHere + streak * 0.38 * mix(0.25, 1.0, skyHere) + halo * mix(0.4, 1.0, skyHere)) * glowVisibility;
    return clamp(rayColor * rays * (1.1 + lowSun * 0.6) + sunGlow, vec3(0.0), vec3(1.0));
}

float snowHeight(vec3 wp) {
    float h = (fbm(vec3(wp.xz * 3.2, 0.0)) - 0.5) * 0.025 + (noiseJ(wp.xz * 14.0) - 0.5) * 0.006;
    vec2 wind = vec2(0.8, 0.6);
    float ripple = sin(dot(wp.xz, wind) * 9.0 + fbmJ(wp.xz * 1.5) * 6.0);
    h += ripple * 0.004 * smoothstep(0.35, 0.7, noiseJ(wp.xz * 0.8));
    h += (noiseJ(wp.xz * 41.0) - 0.5) * 0.0025;
    return h;
}

vec3 winterGround(vec3 scene, vec2 uv, vec3 wp, vec3 n, vec3 rayDir, float dist, float time, float amount) {
    float lum = dot(scene, vec3(0.299, 0.587, 0.114));
    float up = smoothstep(0.55, 0.92, n.y);
    float drift = fbm(vec3(wp.xz * 0.45, wp.y * 0.2));
    float side = 1.0 - smoothstep(0.15, 0.45, abs(n.y));
    float gridY = abs(fract(wp.y * 16.0 + 0.5) - 0.5) / 16.0;
    float aligned = 1.0 - smoothstep(0.004, 0.012 + dist * 0.0006, gridY);
    float cover = up * aligned * mix(0.9, 1.0, smoothstep(0.2, 0.5, drift));
    float sideFrost = side * smoothstep(0.55, 0.8, fbm(wp * 2.2)) * 0.3;
    float grain = fbm(wp * 5.0);
    float dayLight = mix(0.22, 1.0, smoothstep(-0.15, 0.25, SunDir.y));
    float area = 0.0;
    for (int i = 0; i < 12; i++) {
        float ang = float(i) * 2.39996;
        float rad = 6.0 + float(i) * 2.2;
        vec2 suv = clamp(uv + vec2(cos(ang), sin(ang)) * rad * ScreenParams.xy, vec2(0.001), vec2(0.999));
        vec3 sc = texture(SceneSampler, suv).rgb;
        area += max(max(sc.r, sc.g), sc.b);
    }
    area /= 12.0;
    float light = dayLight * clamp(area * 1.9 + 0.25, 0.3, 1.0);
    vec3 snowCol = vec3(0.0);
    if (cover > 0.001) {
        vec2 e = vec2(0.03, 0.0);
        float h0 = snowHeight(wp);
        float hx = snowHeight(wp + e.xyy);
        float hz = snowHeight(wp + e.yyx);
        vec3 sn = normalize(vec3(-(hx - h0) / e.x, 1.0, -(hz - h0) / e.x));
        sn = normalize(mix(sn, normalize(n + vec3(0.0, 0.6, 0.0)), side));
        vec3 sunD = normalize(SunDir.xyz);
        if (sunD.y < 0.0) sunD = -sunD;
        float ndl = clamp(dot(sn, sunD), 0.0, 1.0);
        float wrap = clamp((dot(sn, sunD) + 0.35) / 1.35, 0.0, 1.0);
        float sss = pow(clamp(1.0 - dot(sn, -rayDir), 0.0, 1.0), 3.0);
        vec3 skyAmb = vec3(0.58, 0.7, 0.92);
        vec3 sunCol = vec3(1.0, 0.97, 0.92);
        float detailFade = 1.0 - smoothstep(4.0, 24.0, dist);
        float micro = (noiseJ(wp.xz * 60.0) - 0.5) * 0.06 * detailFade;
        vec3 base = mix(vec3(0.8, 0.84, 0.9), vec3(0.9, 0.92, 0.95), smoothstep(0.3, 0.7, grain)) + micro;
        snowCol = base * (skyAmb * 0.55 + sunCol * (wrap * 0.35 + ndl * 0.35)) + vec3(0.45, 0.65, 1.0) * sss * 0.12;
        snowCol *= mix(vec3(0.62, 0.74, 0.95), vec3(1.0), smoothstep(-0.06, 0.0, h0));
        float spec = pow(max(dot(reflect(rayDir, sn), sunD), 0.0), 60.0);
        snowCol += sunCol * spec * 0.12;
        snowCol *= light;
        float cavity = (1.0 - smoothstep(-0.05, 0.0, h0)) * 0.18;
        snowCol *= 1.0 - cavity;
        snowCol = snowCol / (1.0 + snowCol * 0.45) * 1.25;
        snowCol = mix(snowCol, snowCol * vec3(0.84, 0.9, 1.06), smoothstep(0.55, 0.9, drift) * 0.4);
    }

    float icePatch = up * smoothstep(0.68, 0.74, fbm(vec3(wp.xz * 0.18, 3.0)));
    vec3 iceNormal = normalize(vec3((noiseJ(wp.xz * 1.3) - 0.5) * 0.04, 1.0, (noiseJ(wp.zx * 1.3 + 7.0) - 0.5) * 0.04));
    vec3 refl = reflect(rayDir, iceNormal);
    vec3 iceSky = Modes.x > 0.5 ? skyColor(refl, time) : mix(vec3(0.55, 0.7, 0.9), vec3(0.85, 0.93, 1.0), clamp(refl.y, 0.0, 1.0));
    float fresnel = 0.05 + 0.95 * pow(1.0 - max(dot(-rayDir, iceNormal), 0.0), 5.0);
    vec3 iceCol = mix(scene * vec3(0.55, 0.78, 1.0) + vec3(0.04, 0.1, 0.18), iceSky, clamp(fresnel * 1.4 + 0.25, 0.0, 0.9));
    float crack = 1.0 - smoothstep(0.0, 0.025, abs(noiseJ(wp.xz * 2.4) - 0.5)) * 1.0;
    crack = max(crack, 1.0 - smoothstep(0.0, 0.018, abs(noiseJ(wp.xz * 5.1 + 13.0) - 0.5)));
    iceCol += vec3(0.75, 0.9, 1.0) * crack * 0.35 * light;

    vec3 result = mix(scene, scene * vec3(0.86, 0.94, 1.1) + vec3(0.02, 0.03, 0.05), amount * 0.8);
    result = mix(result, mix(result, vec3(0.88, 0.94, 1.0) * light, 0.7), sideFrost * amount);
    float green = smoothstep(0.02, 0.09, scene.g - scene.b) * smoothstep(-0.05, 0.02, scene.g - scene.r);
    float leafShade = clamp(lum / max(area, 0.05), 0.55, 1.15);
    vec3 leafSnow = mix(vec3(0.66, 0.72, 0.82), vec3(0.86, 0.89, 0.93), smoothstep(0.2, 0.8, grain)) * light * leafShade;
    result = mix(result, leafSnow, smoothstep(0.0, 0.6, green) * smoothstep(0.0, 0.3, amount));
    float coverMask = smoothstep(1.0 - amount, 1.0 - amount + 0.08, cover * mix(0.6, 1.0, drift));
    result = mix(result, snowCol, max(coverMask, step(0.95, amount) * cover));
    result = mix(result, iceCol, icePatch * amount * 0.85);

    return result;
}

vec3 screenBlend(vec3 base, vec3 light) {
    return 1.0 - (1.0 - clamp(base, vec3(0.0), vec3(1.0))) * (1.0 - light);
}

void main() {
    vec2 uv = gl_FragCoord.xy * ScreenParams.xy;
    float time = ScreenParams.z;
    float depth = texture(DepthSampler, uv).r;
    vec3 scene = texture(SceneSampler, uv).rgb;
    bool sky = isSky(depth);
    vec3 wp = worldPos(uv, depth);
    vec3 faceN = cross(dFdx(wp), dFdy(wp));

    if (sky) {
        vec3 dir = viewDir(uv);
        if (Modes.x > 0.5) {
            vec4 layer = skyLayer(dir, uv, time);
            scene = mix(scene, layer.rgb, layer.a);
        }
        if (Modes.y > 0.5) {
            vec3 gray = vec3(dot(scene, vec3(0.299, 0.587, 0.114)));
            scene = mix(scene, mix(gray, MIST, 0.7), clamp(FogRange.z, 0.0, 1.0) * 0.9);
        }
        if (Modes.w > 0.5) {
            scene = fogBlur(uv, scene, 1.0);
        }
        if (SkyColor.w > 0.0) {
            scene = mix(scene, scene * vec3(0.85, 0.93, 1.08) + vec3(0.03, 0.04, 0.06), SkyColor.w * 0.7);
        }
        fragColor = vec4(screenBlend(scene, sunRays(uv, time)), 1.0);
        return;
    }

    vec3 toPos = wp - CameraPos.xyz;
    float dist = length(toPos);
    vec3 rayDir = toPos / max(dist, 0.0001);

    if (Modes.z > 0.5) {
        vec3 dx = dFdx(wp);
        vec3 dy = dFdy(wp);
        vec3 n = normalize(cross(dx, dy));
        if (dot(n, rayDir) > 0.0) n = -n;
        float up = smoothstep(0.75, 0.95, n.y);
        float puddle = smoothstep(0.42, 0.62, fbm(vec3(wp.xz * 0.35, 0.0)));
        float wet = FogRange.w;
        scene *= mix(1.0, 0.72, wet);
        if (up > 0.01) {
            vec3 normal = normalize(vec3(
                (fbm(vec3(wp.xz * 3.0, time * 0.6)) - 0.5) * 0.06 * (1.0 - puddle),
                1.0,
                (fbm(vec3(wp.zx * 3.0, time * 0.6 + 4.0)) - 0.5) * 0.06 * (1.0 - puddle)));
            vec3 refl = reflect(rayDir, normal);
            vec3 hitColor = vec3(0.0);
            float hit = 0.0;
            float stepLen = 0.35 + dist * 0.02;
            vec3 p = wp + normal * 0.02;
            for (int i = 0; i < 28; i++) {
                p += refl * stepLen;
                stepLen *= 1.12;
                float behind;
                vec2 suv = projectWorld(p, behind);
                if (behind > 0.5 || suv.x < 0.0 || suv.x > 1.0 || suv.y < 0.0 || suv.y > 1.0) break;
                float sd = texture(DepthSampler, suv).r;
                float rayDist = length(p - CameraPos.xyz);
                if (isSky(sd)) continue;
                float sceneDist = length(worldPos(suv, sd) - CameraPos.xyz);
                if (sceneDist < rayDist && rayDist - sceneDist < stepLen * 2.0 + 0.3) {
                    vec2 edge = smoothstep(vec2(0.0), vec2(0.08), suv) * smoothstep(vec2(0.0), vec2(0.08), 1.0 - suv);
                    hitColor = texture(SceneSampler, suv).rgb;
                    hit = edge.x * edge.y;
                    break;
                }
            }
            if (hit <= 0.0) {
                vec3 skyRefl = Modes.x > 0.5 ? skyColor(refl, time) : vec3(0.35, 0.42, 0.55);
                hitColor = skyRefl;
                hit = 0.6;
            }
            float fresnel = 0.04 + 0.96 * pow(1.0 - max(dot(-rayDir, normal), 0.0), 5.0);
            float strength = up * wet * mix(0.35, 1.0, puddle) * clamp(fresnel * 1.6 + 0.15, 0.0, 1.0);
            scene = mix(scene, hitColor, strength * hit);
        }
    }

    if (SkyColor.w > 0.0 && dist > 1.1) {
        vec3 wn = dot(faceN, faceN) > 1.0e-12 ? normalize(faceN) : vec3(0.0, 1.0, 0.0);
        if (dot(wn, rayDir) > 0.0) wn = -wn;
        scene = winterGround(scene, uv, wp, wn, rayDir, dist, time, SkyColor.w);
    }

    if (Modes.w > 0.5) {
        float t = clamp((dist - FogRange.x) / max(FogRange.y - FogRange.x, 0.001), 0.0, 1.0);
        float k = t * t * (3.0 - 2.0 * t);
        if (k > 0.001) {
            scene = fogBlur(uv, scene, k);
        }
    }

    if (Modes.y > 0.5) {
        float density = FogRange.z;
        vec3 gray = vec3(dot(scene, vec3(0.299, 0.587, 0.114)));
        scene = mix(scene, gray * vec3(0.95, 0.97, 1.0), density * 0.75);
        float fogAmount = 1.0 - exp(-dist * 0.045 * density);
        float marchLen = min(dist, 40.0);
        float acc = 0.0;
        for (int i = 0; i < 10; i++) {
            float t = (float(i) + 0.5) / 10.0 * marchLen;
            vec3 sp = CameraPos.xyz + rayDir * t;
            float heightFade = exp(-max(sp.y - CameraPos.y + 2.0, 0.0) * 0.12);
            float n = fbm(sp * 0.09 + vec3(time * 0.35, time * 0.05, time * 0.22));
            acc += smoothstep(0.35, 0.75, n) * heightFade;
        }
        acc = acc / 10.0 * min(marchLen / 20.0, 1.0);
        float total = clamp(fogAmount + acc * density * 0.9, 0.0, 0.97);
        scene = mix(scene, MIST, total);
    }

    if (SkyColor.w > 0.0) {
        scene = mix(scene, vec3(0.8, 0.86, 0.94), (1.0 - exp(-dist * 0.012)) * SkyColor.w * 0.5);
    }

    fragColor = vec4(screenBlend(scene, sunRays(uv, time)), 1.0);
}
