package oliv.lib.vulkan.v1.debug;

import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

import oliv.lib.vulkan.fonction.FonctionUtil;
import oliv.lib.vulkan.v2.instance.GestionInstance;


public interface GestionDebugMessenger extends FonctionUtil {
	void cree(boolean ENABLE_VALIDATION_LAYERS);
	void init(GestionInstance instance);
	void detruit();
	boolean actif();
	void ajouteLayer(VkInstanceCreateInfo createInfo);
	void ajouteLayer(VkDeviceCreateInfo createInfo);


}
