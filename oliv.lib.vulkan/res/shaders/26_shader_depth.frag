#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 1) uniform sampler2D texSampler;

layout(location = 0) in float fragLightIntensity;
layout(location = 1) in float fragLightIntensityCamera;
layout(location = 2) in vec2 fragTexCoord;

layout(location = 0) out vec4 outColor;

const vec3 edgeColor = vec3(0.0, 0.0, 0.0);
//float edge() {
//  vec2 ox = vec2(0.0, 0.0);
//  ox.x = 1.0/800;
//  vec2 oy = vec2(0.0, 0.0);
//  oy.y = 1.0/800;
//
//  vec2 PP = fragTexCoord - oy;
//  vec4 g00 = texture(texSampler, PP-ox);
//  vec4 g01 = texture(texSampler, PP);
//  vec4 g02 = texture(texSampler, PP+ox);
//
//  PP = fragTexCoord;
//  vec4 g10 = texture(texSampler, PP-ox);
//  vec4 g12 = texture(texSampler, PP+ox);
//
//  PP = fragTexCoord + oy;
//  vec4 g20 = texture(texSampler, PP-ox);
//  vec4 g21 = texture(texSampler, PP);
//  vec4 g22 = texture(texSampler, PP+ox);
//
//  vec4 sx = vec4(0.0), sy = vec4(0.0);
//  sx = sx - g00 - g01 * 2.0 - g02 + g20 + g21 * 2.0 + g22;
//  sy = sy - g00 - g10 * 2.0 - g20 + g02 + g12 * 2.0 + g22;
//
//  float dist = (length(sx) + length(sy)) / 4.0;
//  return 1.0 - dist;
//}
void main() {
	
	vec4 diff =(fragLightIntensity*vec4(1.0,1.0,0.95,0.0)+fragLightIntensityCamera*vec4(1.0,1.0,1.0,0.0))/2;
    outColor = vec4(vec3(texture(texSampler, fragTexCoord)*diff),0.5f);
   // outColor = vec4(mix(vec3(texture(texSampler, fragTexCoord)*diff),edgeColor,edge()),0.5f);
}