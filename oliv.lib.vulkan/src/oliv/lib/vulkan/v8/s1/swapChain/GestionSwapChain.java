package oliv.lib.vulkan.v8.s1.swapChain;

import java.util.List;

import org.lwjgl.vulkan.VkExtent2D;

import oliv.lib.vulkan.v3.surface.GestionSurface;
import oliv.lib.vulkan.v4.devicePhysique.GestionPhysicalDevice;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;

public interface GestionSwapChain {
	void cree(GestionSurface surface, GestionPhysicalDevice physicalDevice, GestionDevice device,int WIDTH,int HEIGHT);

	long swapChain();

	List<Long> swapChainImages();

	int swapChainImageFormat();

	VkExtent2D swapChainExtent();
    List<Long> swapChainImageViews();
	void detruit();


}
