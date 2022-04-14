package oliv.lib.vulkan.v4.devicePhysique;

import java.util.Set;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;

import oliv.lib.vulkan.v2.instance.GestionInstance;
import oliv.lib.vulkan.v3.surface.GestionSurface;

public interface GestionPhysicalDevice {
	void cree(GestionSurface surface, GestionInstance instance, Set<String> DEVICE_EXTENSIONS);

	VkPhysicalDevice physicalDevice();

	GestionSurface surface();

	Set<String> DEVICE_EXTENSIONS();

	int msaaSamples();

	void detruit();

	default QueueFamilyIndices findQueueFamilies() {
		return  findQueueFamilies(physicalDevice());
		 
	}

	QueueFamilyIndices  findQueueFamilies(VkPhysicalDevice physicalDevice);

	default  SwapChainSupportDetails querySwapChainSupport(MemoryStack stack){	
		return  querySwapChainSupport(stack,physicalDevice());		 
	}

	SwapChainSupportDetails querySwapChainSupport(MemoryStack stack, VkPhysicalDevice physicalDevice);

	

}
