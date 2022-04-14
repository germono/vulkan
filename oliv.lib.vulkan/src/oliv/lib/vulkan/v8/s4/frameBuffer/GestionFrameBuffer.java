package oliv.lib.vulkan.v8.s4.frameBuffer;

import java.util.List;

import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;
import oliv.lib.vulkan.v7.modele.GestionModele;
import oliv.lib.vulkan.v8.s1.swapChain.GestionSwapChain;
import oliv.lib.vulkan.v8.s2.renderPass.GestionRenderPass;

public interface GestionFrameBuffer {
	void cree(GestionSwapChain swapChain, GestionRenderPass renderPass,GestionDevice device,GestionModele modele);
	public List<Long> swapChainFramebuffers();

	void detruit();

}
