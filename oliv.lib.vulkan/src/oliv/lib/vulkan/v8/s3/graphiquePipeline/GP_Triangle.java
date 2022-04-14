package oliv.lib.vulkan.v8.s3.graphiquePipeline;

import static oliv.lib.vulkan.fonction.ShaderSPIRVUtils.compileShaderFile;
import static oliv.lib.vulkan.fonction.ShaderSPIRVUtils.ShaderKind.FRAGMENT_SHADER;
import static oliv.lib.vulkan.fonction.ShaderSPIRVUtils.ShaderKind.VERTEX_SHADER;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_DST_ALPHA;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ONE;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_SRC_ALPHA;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ZERO;
import static org.lwjgl.vulkan.VK10.VK_BLEND_OP_ADD;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_LESS;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_NONE;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_LOGIC_OP_COPY;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_LINE_LIST;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkCreateShaderModule;
import static org.lwjgl.vulkan.VK10.vkDestroyPipeline;
import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyShaderModule;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkViewport;

import oliv.lib.vulkan.fonction.ShaderSPIRVUtils.SPIRV;
import oliv.lib.vulkan.objet.noeud.Vertex;
import oliv.lib.vulkan.objet.noeud.VertexNormalTexture;
import oliv.lib.vulkan.v2.instance.GestionInstance;
import oliv.lib.vulkan.v4.devicePhysique.GestionPhysicalDevice;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;
import oliv.lib.vulkan.v7.modele.GestionModele;
import oliv.lib.vulkan.v8.s1.swapChain.GestionSwapChain;
import oliv.lib.vulkan.v8.s2.renderPass.GestionRenderPass;

public class GP_Triangle implements GestionGraphicsPipeline {
	long surface;
	GestionInstance instance;
	long graphicsPipeline;
	long pipelineLayout;
	GestionDevice device;

	@Override
	public long graphicsPipeline() {
		return graphicsPipeline;
	}

	@Override
	public long pipelineLayout() {
		return pipelineLayout;
	}

	@Override
	public void cree(GestionPhysicalDevice devicePhysique, GestionDevice device, GestionSwapChain swapChain,
			GestionRenderPass renderPass, GestionModele modele) {
		this.device = device;
		try (MemoryStack stack = stackPush()) {

			// Let's compile the GLSL shaders into SPIR-V at runtime using the shaderc
			// library
			// Check ShaderSPIRVUtils class to see how it can be done
			SPIRV vertShaderSPIRV = vertShaderSPIRV();
			SPIRV fragShaderSPIRV = fragShaderSPIRV();

			long vertShaderModule = createShaderModule(vertShaderSPIRV.bytecode());
			long fragShaderModule = createShaderModule(fragShaderSPIRV.bytecode());

			ByteBuffer entryPoint = stack.UTF8("main");

			VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack);

			VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);

			vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
			vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
			vertShaderStageInfo.module(vertShaderModule);
			vertShaderStageInfo.pName(entryPoint);

			VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(1);

			fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
			fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
			fragShaderStageInfo.module(fragShaderModule);
			fragShaderStageInfo.pName(entryPoint);

			// ===> PIPELINE LAYOUT CREATION <===

			VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
			pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
			pipelineLayoutInfo.pSetLayouts(stack.longs(modele.descriptorSetLayout()));

			LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);

			if (vkCreatePipelineLayout(device.device(), pipelineLayoutInfo, null, pPipelineLayout) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create pipeline layout");
			}

			pipelineLayout = pPipelineLayout.get(0);

			VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
			pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
			pipelineInfo.pStages(shaderStages);
			pipelineInfo.pVertexInputState(vertexInputInfo(stack));
			pipelineInfo.pInputAssemblyState(inputAssembly(stack));
			pipelineInfo.pViewportState(viewportState(stack,swapChain));
			pipelineInfo.pRasterizationState(rasterizer(stack));
			pipelineInfo.pMultisampleState(multisampling(stack,devicePhysique));
			pipelineInfo.pDepthStencilState(depthStencil(stack));
			pipelineInfo.pColorBlendState(colorBlending(stack));
			pipelineInfo.layout(pipelineLayout);
			pipelineInfo.renderPass(renderPass.renderPass());
			pipelineInfo.subpass(0);
			pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
			pipelineInfo.basePipelineIndex(-1);

			LongBuffer pGraphicsPipeline = stack.mallocLong(1);

			if (vkCreateGraphicsPipelines(device.device(), VK_NULL_HANDLE, pipelineInfo, null,
					pGraphicsPipeline) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create graphics pipeline");
			}

			graphicsPipeline = pGraphicsPipeline.get(0);

			// ===> RELEASE RESOURCES <===

			vkDestroyShaderModule(device.device(), vertShaderModule, null);
			vkDestroyShaderModule(device.device(), fragShaderModule, null);

			vertShaderSPIRV.free();
			fragShaderSPIRV.free();
		}
	}
	
	SPIRV vertShaderSPIRV(){
		return compileShaderFile("shaders/26_shader_depth.vert", VERTEX_SHADER);
	}
	SPIRV fragShaderSPIRV(){
		return compileShaderFile("shaders/26_shader_depth.frag", FRAGMENT_SHADER);
	}

	VkPipelineVertexInputStateCreateInfo vertexInputInfo(MemoryStack stack) {
		// ===> VERTEX STAGE <===
		VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
		vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
		vertexInputInfo.pVertexBindingDescriptions(VertexNormalTexture.getBindingDescription());
		vertexInputInfo.pVertexAttributeDescriptions(VertexNormalTexture.getAttributeDescriptions());
		return vertexInputInfo;
	}

	VkPipelineInputAssemblyStateCreateInfo inputAssembly(MemoryStack stack) {
		// ===> ASSEMBLY STAGE <===

		VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo
				.calloc(stack);
		inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
		inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP);
		inputAssembly.primitiveRestartEnable(true);
		return inputAssembly;
	}

	VkPipelineViewportStateCreateInfo viewportState(MemoryStack stack,GestionSwapChain swapChain) {
		// ===> VIEWPORT & SCISSOR

		VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
		viewport.x(0.0f);
		viewport.y(0.0f);
		viewport.width(swapChain.swapChainExtent().width());
		viewport.height(swapChain.swapChainExtent().height());
		viewport.minDepth(0.0f);
		viewport.maxDepth(1.0f);

		VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
		scissor.offset(VkOffset2D.calloc(stack).set(0, 0));
		scissor.extent(swapChain.swapChainExtent());

		VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc(stack);
		viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
		viewportState.pViewports(viewport);
		viewportState.pScissors(scissor);
		return viewportState;
	}

	VkPipelineRasterizationStateCreateInfo rasterizer(MemoryStack stack) {
		// ===> RASTERIZATION STAGE <===

		VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack);
		rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
		rasterizer.depthClampEnable(false);
		rasterizer.rasterizerDiscardEnable(false);
		rasterizer.polygonMode(VK_POLYGON_MODE_FILL);
		rasterizer.lineWidth(1.0f);
		rasterizer.cullMode(VK_CULL_MODE_NONE);
		rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
		rasterizer.depthBiasEnable(false);
		return rasterizer;

	}

	VkPipelineMultisampleStateCreateInfo multisampling(MemoryStack stack, GestionPhysicalDevice devicePhysique) {
		// ===> MULTISAMPLING <===

		VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.calloc(stack);
		multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
		multisampling.sampleShadingEnable(true);
		multisampling.minSampleShading(0.2f); // Enable sample shading in the pipeline
		multisampling.rasterizationSamples(devicePhysique.msaaSamples()); // Min fraction for sample shading; closer
																			// to one is smoother
		return multisampling;

	}

	VkPipelineDepthStencilStateCreateInfo depthStencil(MemoryStack stack) {
		VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.calloc(stack);
		depthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
		depthStencil.depthTestEnable(true);
		depthStencil.depthWriteEnable(true);
		depthStencil.depthCompareOp(VK_COMPARE_OP_LESS);
		depthStencil.depthBoundsTestEnable(false);
		depthStencil.minDepthBounds(0.0f); // Optional
		depthStencil.maxDepthBounds(1.0f); // Optional
		depthStencil.stencilTestEnable(false);
		return depthStencil;
	}

	VkPipelineColorBlendStateCreateInfo colorBlending(MemoryStack stack) {
		// ===> COLOR BLENDING <===

		VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState
				.calloc(1, stack);
		// colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT );//|
		// VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT |
		// VK_COLOR_COMPONENT_A_BIT);
		colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT
				| VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
		colorBlendAttachment.blendEnable(false);
		colorBlendAttachment.srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA);
		colorBlendAttachment.dstColorBlendFactor(VK_BLEND_FACTOR_DST_ALPHA);
//		            colorBlendAttachment.srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA);
//		            colorBlendAttachment.dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA);
		colorBlendAttachment.colorBlendOp(VK_BLEND_OP_ADD);
		colorBlendAttachment.srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE);
		colorBlendAttachment.dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO);
		colorBlendAttachment.alphaBlendOp(VK_BLEND_OP_ADD);

		VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack);
		colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
		colorBlending.logicOpEnable(false);
		colorBlending.logicOp(VK_LOGIC_OP_COPY);
		colorBlending.pAttachments(colorBlendAttachment);
		colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
		return colorBlending;
	}

	@Override
	public void detruit() {
		vkDestroyPipeline(device.device(), graphicsPipeline, null);

		vkDestroyPipelineLayout(device.device(), pipelineLayout, null);
	}

	public long createShaderModule(ByteBuffer spirvCode) {

		try (MemoryStack stack = stackPush()) {

			VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.calloc(stack);

			createInfo.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
			createInfo.pCode(spirvCode);

			LongBuffer pShaderModule = stack.mallocLong(1);

			if (vkCreateShaderModule(device.device(), createInfo, null, pShaderModule) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create shader module");
			}

			return pShaderModule.get(0);
		}
	}
}