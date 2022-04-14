package oliv.lib.vulkan.v8.s5.commandeBuffer;

import java.util.List;

import org.lwjgl.vulkan.VkCommandBuffer;

import oliv.lib.vulkan.objet.Parametre;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;
import oliv.lib.vulkan.v6.commandPool.GestionCommandPool;
import oliv.lib.vulkan.v7.modele.GestionModele;
import oliv.lib.vulkan.v8.s1.swapChain.GestionSwapChain;
import oliv.lib.vulkan.v8.s2.renderPass.GestionRenderPass;
import oliv.lib.vulkan.v8.s3.graphiquePipeline.GestionGraphicsPipeline;
import oliv.lib.vulkan.v8.s4.frameBuffer.GestionFrameBuffer;

public interface GestionCommandBuffer {
	void cree(GestionFrameBuffer swapChainFramebuffers, GestionCommandPool commandPool, GestionDevice device,
			GestionRenderPass renderPass, GestionSwapChain swapChain, GestionModele modele, Parametre param,
			GestionGraphicsPipeline... graphicsPipeline);

	List<VkCommandBuffer> commandBuffers();

	void detruit();

}
