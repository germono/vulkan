package oliv.lib.vulkan.v6.commandPool;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateCommandPool;
import static org.lwjgl.vulkan.VK10.vkDestroyCommandPool;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;

import oliv.lib.vulkan.v4.devicePhysique.GestionPhysicalDevice;
import oliv.lib.vulkan.v4.devicePhysique.QueueFamilyIndices;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;

public class CommandePool implements GestionCommandPool {
	long commandPool;
	GestionDevice device;
	@Override
	public long commandPool() {
		return commandPool;
	}

	@Override
	public void cree(GestionPhysicalDevice physicalDevice,GestionDevice device) {
		this.device=device;
        try(MemoryStack stack = stackPush()) {

            QueueFamilyIndices queueFamilyIndices = physicalDevice.findQueueFamilies();

            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc(stack);
            poolInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
            poolInfo.queueFamilyIndex(queueFamilyIndices.graphicsFamily);

            LongBuffer pCommandPool = stack.mallocLong(1);

            if (vkCreateCommandPool(device.device(), poolInfo, null, pCommandPool) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create command pool");
            }

            commandPool = pCommandPool.get(0);
        }
	}

	@Override
	public void detruit() {
		vkDestroyCommandPool(device.device(), commandPool, null); 
	}

}

