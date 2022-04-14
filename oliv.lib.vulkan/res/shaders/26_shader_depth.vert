#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 0) uniform UniformBufferObject {
    mat4 model;
    mat4 view;
    mat4 proj;
} ubo;

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inNormal;
layout(location = 2) in vec2 inTexCoord;

layout(location = 0) out float fragLightIntensity;
layout(location = 1) out float fragLightIntensityCamera;
layout(location = 2) out vec2 fragTexCoord;
const vec3 DIRECTION_TO_LIGHT=normalize(vec3(2.0,3.0,1.0));
const vec3 DIRECTION_TO_LIGHT_CAMERA=normalize(vec3(.0,0.0,1.0));


void main() {
    gl_Position = ubo.proj * ubo.view * ubo.model * vec4(inPosition, 1.0);
    vec3 normalWorldSpace=normalize(mat3(ubo.model)*inNormal);
    vec3 normalCamera=normalize(mat3( ubo.view) * normalWorldSpace);
    
    
    
    
    float lightIntensity=(0.8+0.2*dot(normalWorldSpace,DIRECTION_TO_LIGHT));
    float lightIntensityCamera=0.3+0.7*dot(normalCamera,DIRECTION_TO_LIGHT_CAMERA);
    fragLightIntensity = lightIntensity;
    fragLightIntensityCamera=lightIntensityCamera;
    fragTexCoord = inTexCoord;
}