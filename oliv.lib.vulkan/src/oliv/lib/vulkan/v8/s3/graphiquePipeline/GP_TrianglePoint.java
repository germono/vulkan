package oliv.lib.vulkan.v8.s3.graphiquePipeline;

import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_NONE;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_POINT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;

public class GP_TrianglePoint extends GP_Triangle {

	VkPipelineRasterizationStateCreateInfo rasterizer(MemoryStack stack) {
		// ===> RASTERIZATION STAGE <===

		VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack);
		rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
		rasterizer.depthClampEnable(false);
		rasterizer.rasterizerDiscardEnable(false);
		rasterizer.polygonMode(VK_POLYGON_MODE_POINT);
		rasterizer.lineWidth(1.0f);
		rasterizer.cullMode(VK_CULL_MODE_NONE);
		rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
		rasterizer.depthBiasEnable(false);
		return rasterizer;

	}
}