package oliv.lib.vulkan.objet;

import org.lwjgl.vulkan.VkInstance;


/**
 * Contains information to create a {@link VKCanvas}.
 * 
 * @author Kai Burjack
 */
public class VKData {

    /**
     * The {@link VkInstance} on behalf of which to create a window surface.
     */
    public VkInstance instance;

}
