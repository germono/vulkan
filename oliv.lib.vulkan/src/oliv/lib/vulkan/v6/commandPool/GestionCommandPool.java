package oliv.lib.vulkan.v6.commandPool;

import oliv.lib.vulkan.v4.devicePhysique.GestionPhysicalDevice;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;

public interface GestionCommandPool {
	void cree(GestionPhysicalDevice physicalDevice,GestionDevice device);

	long commandPool();

	void detruit();

}
