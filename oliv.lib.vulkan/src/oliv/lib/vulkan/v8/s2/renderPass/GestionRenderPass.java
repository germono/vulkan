package oliv.lib.vulkan.v8.s2.renderPass;

import oliv.lib.vulkan.v4.devicePhysique.GestionPhysicalDevice;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;
import oliv.lib.vulkan.v8.s1.swapChain.GestionSwapChain;


public interface GestionRenderPass {
	void cree(GestionPhysicalDevice physicalDevice,GestionDevice device,GestionSwapChain swapChain);

	long renderPass();

	void detruit();

	
}
