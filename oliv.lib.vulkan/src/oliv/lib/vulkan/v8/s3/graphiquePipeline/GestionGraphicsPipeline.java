package oliv.lib.vulkan.v8.s3.graphiquePipeline;

import oliv.lib.vulkan.v4.devicePhysique.GestionPhysicalDevice;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;
import oliv.lib.vulkan.v7.modele.GestionModele;
import oliv.lib.vulkan.v8.s1.swapChain.GestionSwapChain;
import oliv.lib.vulkan.v8.s2.renderPass.GestionRenderPass;

public interface GestionGraphicsPipeline {
	void cree(GestionPhysicalDevice devicePhysique,GestionDevice device,GestionSwapChain swapChain,GestionRenderPass renderPass,GestionModele modele);

	long graphicsPipeline();
	long pipelineLayout();

	void detruit();

	
}
