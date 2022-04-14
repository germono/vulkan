package oliv.lib.vulkan.v8.s3.graphiquePipeline;

import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_LINE_LIST;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;

public class GP_Ligne extends GP_Triangle{
	@Override
	VkPipelineInputAssemblyStateCreateInfo inputAssembly(MemoryStack stack) {
		 // ===> ASSEMBLY STAGE <===

        VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack);
        inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
        inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_LINE_LIST);
        inputAssembly.primitiveRestartEnable(false);
        return inputAssembly;
	}
}
