#version 450
/**/

#define DSET_SCENE        0
#define DSET_OBJECT       1
#define DSET_GEOMETRY     2

#define SCENE_UBO_VIEW    0
#define SCENE_SSBO_STATS  1
// changing order requires glsl changes in drawmesh_native.mesh.glsl
// geometryBuffer ubo
#define GEOMETRY_SSBO_MESHLETDESC   0
#define GEOMETRY_SSBO_PRIM          1
#define GEOMETRY_TEX_IBO            2
#define GEOMETRY_TEX_VBO            3
#define GEOMETRY_TEX_ABO            4
#define GEOMETRY_BINDINGS           5

////////////////////////////////////////////////////

#ifndef NVMESHLET_VERTEX_COUNT
// primitive count should be 40, 84 or 126
// vertex count should be 32 or 64
// 64 & 126 is the preferred size
#define NVMESHLET_VERTEX_COUNT      64
#define NVMESHLET_PRIMITIVE_COUNT   126
#endif

#ifndef EXTRA_ATTRIBUTES
// add how many extra fake attributes (vec4) you want to use
#define EXTRA_ATTRIBUTES    4
#endif

#ifndef USE_CLIPPING
#define USE_CLIPPING        0
#endif

#define NUM_CLIPPING_PLANES 3

#define NORMAL_STRIDE (1 + EXTRA_ATTRIBUTES)

struct SceneData {
  mat4  viewProjMatrix;
  mat4  viewMatrix;
  mat4  viewMatrixIT;

  vec4  viewPos;
  vec4  viewDir;
  
  vec4  wLightPos;
  
  ivec2 viewport;
  vec2  viewportf;

  vec2  viewportTaskCull;
  int   colorize;
  int   _pad0;
  
  vec4  wClipPlanes[NUM_CLIPPING_PLANES];
};
   struct ObjectData {
  mat4 worldMatrix;
  mat4 worldMatrixIT;
  mat4 objectMatrix;
  vec4 bboxMin;
  vec4 bboxMax;
  vec3 _pad0;
  float winding;
  vec4 color;
};
struct CullStats {
  uint  tasksInput;
  uint  tasksOutput;
  uint  meshletsInput;
  uint  meshletsOutput;
  uint  trisInput;
  uint  trisOutput;
  uint  attrInput;
  uint  attrOutput;
};
uint murmurHash(uint idx)
{
  uint m = 0x5bd1e995;
  uint r = 24;
  
  uint h = 64684;
  uint k = idx;
  
  k *= m;
  k ^= (k >> r);
  k *= m;
  h *= m;
  h ^= k;
  
  return h;
}
//////////////////////////////////////////////////
// UNIFORMS

  layout(std140,binding= SCENE_UBO_VIEW,set=DSET_SCENE) uniform sceneBuffer {
    SceneData scene;
  };

  layout(std140,binding=0,set=DSET_OBJECT) uniform objectBuffer {
    ObjectData object;
  };
  
//////////////////////////////////////////////////
// INPUT

layout(location=0) in Interpolants {
  vec3  wPos;
  float dummy;
  vec3  wNormal;
  flat uint meshletID;
} IN;

//////////////////////////////////////////////////
// OUTPUT

layout(location=0,index=0) out vec4 out_Color;


//////////////////////////////////////////////////
// EXECUTION

vec4 shading()
{  
  vec4 color = object.color * 0.8 + 0.2 + IN.dummy;
  if (scene.colorize != 0) {
    uint colorPacked = murmurHash(IN.meshletID);
    color = color * 0.5 + unpackUnorm4x8(colorPacked) * 0.5;
  }
  
  vec3 eyePos = vec3(scene.viewMatrixIT[0].w,scene.viewMatrixIT[1].w,scene.viewMatrixIT[2].w);
  
  vec3 wNormal =  IN.wNormal;
  vec3 lightDir = normalize(scene.wLightPos.xyz - IN.wPos.xyz);
  vec3 normal   = normalize(wNormal) * (gl_FrontFacing ? 1 : 1);

#if 1
  vec4 diffuse  = vec4(abs(dot(normal,lightDir)));
  vec4 outColor = diffuse * color;
#else
  float lt = abs(dot(normal,lightDir));
  float wt = dot(normal,lightDir) * 0.5 + 0.5;
  vec4 diffuse  = mix( pow((vec4(1)-color) * (1 - lt), vec4(0.7)), pow(color * lt * vec4(1,1,0.9,1), vec4(0.9)), pow(wt,0.5));
  vec4 outColor = diffuse;
#endif

  
  return outColor;
}

void main()
{
  out_Color = shading();
}