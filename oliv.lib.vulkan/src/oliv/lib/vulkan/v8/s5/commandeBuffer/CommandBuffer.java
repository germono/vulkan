package oliv.lib.vulkan.v8.s5.commandeBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT32;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_CONTENTS_INLINE;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBeginRenderPass;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindIndexBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexed;
import static org.lwjgl.vulkan.VK10.vkCmdEndRenderPass;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import oliv.lib.vulkan.objet.Parametre;
import oliv.lib.vulkan.v2.instance.GestionInstance;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;
import oliv.lib.vulkan.v6.commandPool.GestionCommandPool;
import oliv.lib.vulkan.v7.modele.GestionModele;
import oliv.lib.vulkan.v8.s1.swapChain.GestionSwapChain;
import oliv.lib.vulkan.v8.s2.renderPass.GestionRenderPass;
import oliv.lib.vulkan.v8.s3.graphiquePipeline.GestionGraphicsPipeline;
import oliv.lib.vulkan.v8.s4.frameBuffer.GestionFrameBuffer;

public class CommandBuffer implements GestionCommandBuffer {
	List<VkCommandBuffer> commandBuffers;
	GestionInstance instance;

	@Override
	public List<VkCommandBuffer> commandBuffers() {
		return commandBuffers;
	}

	@Override
	public void cree(GestionFrameBuffer swapChainFramebuffers, GestionCommandPool commandPool, GestionDevice device,
			GestionRenderPass renderPass, GestionSwapChain swapChain, GestionModele modele, Parametre param,
			GestionGraphicsPipeline... graphicsPipeline) {

		final int commandBuffersCount = swapChainFramebuffers.swapChainFramebuffers().size();

		commandBuffers = new ArrayList<>(commandBuffersCount);

		try (MemoryStack stack = stackPush()) {

			VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
			allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
			allocInfo.commandPool(commandPool.commandPool());
			allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
			allocInfo.commandBufferCount(commandBuffersCount);

			PointerBuffer pCommandBuffers = stack.mallocPointer(commandBuffersCount);

			if (vkAllocateCommandBuffers(device.device(), allocInfo, pCommandBuffers) != VK_SUCCESS) {
				throw new RuntimeException("Failed to allocate command buffers");
			}

			for (int i = 0; i < commandBuffersCount; i++) {
				commandBuffers.add(new VkCommandBuffer(pCommandBuffers.get(i), device.device()));
			}

			VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
			beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

			VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
			renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);

			renderPassInfo.renderPass(renderPass.renderPass());

			VkRect2D renderArea = VkRect2D.calloc(stack);
			renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
			renderArea.extent(swapChain.swapChainExtent());
			renderPassInfo.renderArea(renderArea);

			VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
			clearValues.color().float32(
					stack.floats(param.Font_G() / 255.0f, param.Font_G() / 255.0f, param.Font_B() / 255.0f, 1.0f));
			clearValues.get(1).depthStencil().set(1.0f, 0);

			renderPassInfo.pClearValues(clearValues);

			for (int i = 0; i < commandBuffersCount; i++) {

				VkCommandBuffer commandBuffer = commandBuffers.get(i);

				if (vkBeginCommandBuffer(commandBuffer, beginInfo) != VK_SUCCESS) {
					throw new RuntimeException("Failed to begin recording command buffer");
				}

				renderPassInfo.framebuffer(swapChainFramebuffers.swapChainFramebuffers().get(i));

				vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
				{
					LongBuffer vertexBuffers = stack.longs(modele.vertexBuffer());
					LongBuffer offsets = stack.longs(0);
					vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);
					if (modele.indicesTriangleLength() != 0) {
						vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
								graphicsPipeline[0].graphicsPipeline());

						vkCmdBindIndexBuffer(commandBuffer, modele.indexBufferTriangle(), 0, VK_INDEX_TYPE_UINT32);

						vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
								graphicsPipeline[0].pipelineLayout(), 0,
								stack.longs(modele.descriptorSets().get(i)), null);

						vkCmdDrawIndexed(commandBuffer, modele.indicesTriangleLength(), 1, 0, 0, 0);
					}
					if (modele.indicesLineLength() != 0) {
						vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
								graphicsPipeline[1].graphicsPipeline());

						vkCmdBindIndexBuffer(commandBuffer, modele.indexBufferLine(), 0, VK_INDEX_TYPE_UINT32);

						vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
								graphicsPipeline[1].pipelineLayout(), 0,
								stack.longs(modele.descriptorSets().get(i)), null);
						vkCmdDrawIndexed(commandBuffer, modele.indicesLineLength(), 1, 0, 0, 0);
					}

				}
				vkCmdEndRenderPass(commandBuffer);

				if (vkEndCommandBuffer(commandBuffer) != VK_SUCCESS) {
					throw new RuntimeException("Failed to record command buffer");
				}

			}

		}
	}

	@Override
	public void detruit() {

	}

}