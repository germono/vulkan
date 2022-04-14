package oliv.lib.vulkan.v5.deviceLogique;

import java.util.Set;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

import oliv.lib.vulkan.fonction.FonctionUtil;
import oliv.lib.vulkan.v1.debug.GestionDebugMessenger;
import oliv.lib.vulkan.v4.devicePhysique.GestionPhysicalDevice;

public interface GestionDevice extends FonctionUtil {
	void cree(GestionPhysicalDevice physicalDevice, Set<String> DEVICE_EXTENSIONS, GestionDebugMessenger debug);

	VkDevice device();

	VkQueue graphicsQueue();

	VkQueue presentQueue();

	void detruit();

}
