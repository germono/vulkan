package oliv.lib.vulkan.v2.instance;

 

import org.lwjgl.vulkan.VkInstance;

import oliv.lib.vulkan.fonction.FonctionUtil;
import oliv.lib.vulkan.objet.VKData;
import oliv.lib.vulkan.v1.debug.GestionDebugMessenger;

public interface GestionInstance extends FonctionUtil {
	void cree(GestionDebugMessenger debug);
	VkInstance instance();
	VKData data();
	void detruit();
	
}
