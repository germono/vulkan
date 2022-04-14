package oliv.lib.vulkan.v5.deviceLogique;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateDevice;
import static org.lwjgl.vulkan.VK10.vkDestroyDevice;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;

import java.util.Set;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkQueue;

import oliv.lib.vulkan.v1.debug.GestionDebugMessenger;
import oliv.lib.vulkan.v4.devicePhysique.GestionPhysicalDevice;
import oliv.lib.vulkan.v4.devicePhysique.QueueFamilyIndices;

public class DeviceLogique  implements GestionDevice {
	VkDevice device;
	VkQueue graphicsQueue;
	VkQueue presentQueue;

	@Override
	public VkDevice device() {
		return device;
	}

	@Override
	public VkQueue graphicsQueue() {
		return graphicsQueue;
	}

	@Override
	public VkQueue presentQueue() {
		return presentQueue;
	}

	@Override
	public void cree(GestionPhysicalDevice physicalDevice, Set<String> DEVICE_EXTENSIONS, GestionDebugMessenger debug) {

		try (MemoryStack stack = stackPush()) {

			QueueFamilyIndices indices = physicalDevice.findQueueFamilies();

			int[] uniqueQueueFamilies = indices.unique();

			VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo
					.calloc(uniqueQueueFamilies.length, stack);

			for (int i = 0; i < uniqueQueueFamilies.length; i++) {
				VkDeviceQueueCreateInfo queueCreateInfo = queueCreateInfos.get(i);
				queueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
				queueCreateInfo.queueFamilyIndex(uniqueQueueFamilies[i]);
				queueCreateInfo.pQueuePriorities(stack.floats(1.0f));
			}

			VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);
			deviceFeatures.samplerAnisotropy(true);
            deviceFeatures.sampleRateShading(true); // Enable sample shading feature for the device

			VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc(stack);

			createInfo.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
			createInfo.pQueueCreateInfos(queueCreateInfos);
			// queueCreateInfoCount is automatically set

			createInfo.pEnabledFeatures(deviceFeatures);

			createInfo.ppEnabledExtensionNames(asPointerBuffer(DEVICE_EXTENSIONS));
			debug.ajouteLayer(createInfo);
			

			PointerBuffer pDevice = stack.pointers(VK_NULL_HANDLE);

			if (vkCreateDevice(physicalDevice.physicalDevice(), createInfo, null, pDevice) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create logical device");
			}

			device = new VkDevice(pDevice.get(0), physicalDevice.physicalDevice(), createInfo);

			PointerBuffer pQueue = stack.pointers(VK_NULL_HANDLE);

			vkGetDeviceQueue(device, indices.graphicsFamily, 0, pQueue);
			graphicsQueue = new VkQueue(pQueue.get(0), device);

			vkGetDeviceQueue(device, indices.presentFamily, 0, pQueue);
			presentQueue = new VkQueue(pQueue.get(0), device);
		}
	}

	@Override
	public void detruit() {
		vkDestroyDevice(device, null);
	}

}