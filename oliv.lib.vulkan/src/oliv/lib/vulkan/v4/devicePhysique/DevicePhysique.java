package oliv.lib.vulkan.v4.devicePhysique;

import static java.util.stream.Collectors.toSet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.VK10.VK_FALSE;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_16_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_2_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_32_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_4_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_64_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_8_BIT;
import static org.lwjgl.vulkan.VK10.VK_TRUE;
import static org.lwjgl.vulkan.VK10.vkEnumerateDeviceExtensionProperties;
import static org.lwjgl.vulkan.VK10.vkEnumeratePhysicalDevices;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceFeatures;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceProperties;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceQueueFamilyProperties;

import java.nio.IntBuffer;
import java.util.Set;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import oliv.lib.vulkan.v2.instance.GestionInstance;
import oliv.lib.vulkan.v3.surface.GestionSurface;

public class DevicePhysique implements GestionPhysicalDevice {
	VkPhysicalDevice physicalDevice;
	GestionSurface surface;
	Set<String> DEVICE_EXTENSIONS;
	  private int msaaSamples = VK_SAMPLE_COUNT_1_BIT;
	@Override
	public VkPhysicalDevice physicalDevice() {
		return physicalDevice;
	}
	@Override
	public GestionSurface surface() {
		return surface;
	}
	@Override
	public Set<String> DEVICE_EXTENSIONS() {
		return DEVICE_EXTENSIONS;
	}
      @Override
      public int msaaSamples() {
      	return msaaSamples;
      }
      @Override
		public void cree(GestionSurface surface,GestionInstance instance,Set<String> DEVICE_EXTENSIONS) {
			this.surface=surface;
			this.DEVICE_EXTENSIONS=DEVICE_EXTENSIONS;
			try (MemoryStack stack = stackPush()) {

				IntBuffer deviceCount = stack.ints(0);

				vkEnumeratePhysicalDevices(instance.instance(), deviceCount, null);

				if (deviceCount.get(0) == 0) {
					throw new RuntimeException("Failed to find GPUs with Vulkan support");
				}

				PointerBuffer ppPhysicalDevices = stack.mallocPointer(deviceCount.get(0));

				vkEnumeratePhysicalDevices(instance.instance(), deviceCount, ppPhysicalDevices);

				for (int i = 0; i < ppPhysicalDevices.capacity(); i++) {
					
					VkPhysicalDevice device = new VkPhysicalDevice(ppPhysicalDevices.get(i), instance.instance());
					VkPhysicalDevice vphysicalDevice = device;
					int vmsaaSamples = getMaxUsableSampleCount( vphysicalDevice );
					if (isDeviceSuitable(vphysicalDevice)) {
						physicalDevice=vphysicalDevice;
						msaaSamples=vmsaaSamples;
						System.out.println("Selection carte graphique : "+i);
					}
				}
				if(physicalDevice!=null)
					return;
				throw new RuntimeException("Failed to find a suitable GPU");
			}
		}

	@Override
	public void detruit() {

	}
	private int getMaxUsableSampleCount(VkPhysicalDevice vphysicalDevice) {

        try(MemoryStack stack = stackPush()) {

            VkPhysicalDeviceProperties physicalDeviceProperties = VkPhysicalDeviceProperties.malloc(stack);
            vkGetPhysicalDeviceProperties(vphysicalDevice, physicalDeviceProperties);
            System.out.println(physicalDeviceProperties.deviceNameString()+" : "+physicalDeviceProperties.driverVersion());
            int sampleCountFlags = physicalDeviceProperties.limits().framebufferColorSampleCounts()
                    & physicalDeviceProperties.limits().framebufferDepthSampleCounts();

            if((sampleCountFlags & VK_SAMPLE_COUNT_64_BIT) != 0) {
                return VK_SAMPLE_COUNT_64_BIT;
            }
            if((sampleCountFlags & VK_SAMPLE_COUNT_32_BIT) != 0) {
                return VK_SAMPLE_COUNT_32_BIT;
            }
            if((sampleCountFlags & VK_SAMPLE_COUNT_16_BIT) != 0) {
                return VK_SAMPLE_COUNT_16_BIT;
            }
            if((sampleCountFlags & VK_SAMPLE_COUNT_8_BIT) != 0) {
                return VK_SAMPLE_COUNT_8_BIT;
            }
            if((sampleCountFlags & VK_SAMPLE_COUNT_4_BIT) != 0) {
                return VK_SAMPLE_COUNT_4_BIT;
            }
            if((sampleCountFlags & VK_SAMPLE_COUNT_2_BIT) != 0) {
                return VK_SAMPLE_COUNT_2_BIT;
            }

            return VK_SAMPLE_COUNT_1_BIT;
        }
    }
	 boolean isDeviceSuitable(VkPhysicalDevice vphysicalDevice) {


        QueueFamilyIndices indices = findQueueFamilies(vphysicalDevice);

        boolean extensionsSupported = checkDeviceExtensionSupport(vphysicalDevice);
        boolean swapChainAdequate = false;
        boolean anisotropySupported = false;

        if(extensionsSupported) {
            try(MemoryStack stack = stackPush()) {
                SwapChainSupportDetails swapChainSupport = querySwapChainSupport( stack,vphysicalDevice);
                swapChainAdequate = swapChainSupport.formats.hasRemaining() && swapChainSupport.presentModes.hasRemaining();
                VkPhysicalDeviceFeatures supportedFeatures = VkPhysicalDeviceFeatures.malloc(stack);
                vkGetPhysicalDeviceFeatures(vphysicalDevice, supportedFeatures);
                anisotropySupported = supportedFeatures.samplerAnisotropy();
            }
        }

        return indices.isComplete() && extensionsSupported && swapChainAdequate && anisotropySupported;

	}
	 public QueueFamilyIndices findQueueFamilies(VkPhysicalDevice vphysicalDevice) {

			QueueFamilyIndices indices = new QueueFamilyIndices();

			try (MemoryStack stack = stackPush()) {

				IntBuffer queueFamilyCount = stack.ints(0);

				vkGetPhysicalDeviceQueueFamilyProperties(vphysicalDevice, queueFamilyCount, null);

				VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.malloc(queueFamilyCount.get(0),
						stack);

				vkGetPhysicalDeviceQueueFamilyProperties(vphysicalDevice, queueFamilyCount, queueFamilies);

				IntBuffer presentSupport = stack.ints(VK_FALSE);

				for (int i = 0; i < queueFamilies.capacity() || !indices.isComplete(); i++) {

					if ((queueFamilies.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
						indices.graphicsFamily = i;
					}

					vkGetPhysicalDeviceSurfaceSupportKHR(vphysicalDevice, i, surface().surface(), presentSupport);

					if (presentSupport.get(0) == VK_TRUE) {
						indices.presentFamily = i;
					}
				}

				return indices;
			}
		}

		boolean checkDeviceExtensionSupport(VkPhysicalDevice vphysicalDevice) {

			try (MemoryStack stack = stackPush()) {

				IntBuffer extensionCount = stack.ints(0);

				vkEnumerateDeviceExtensionProperties(vphysicalDevice, (String) null, extensionCount, null);

				VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.malloc(extensionCount.get(0),
						stack);

				vkEnumerateDeviceExtensionProperties(vphysicalDevice, (String) null, extensionCount, availableExtensions);

				return availableExtensions.stream().map(VkExtensionProperties::extensionNameString).collect(toSet())
						.containsAll(DEVICE_EXTENSIONS());
			}
		}

		public SwapChainSupportDetails querySwapChainSupport(MemoryStack stack,VkPhysicalDevice vphysicalDevice) {

			SwapChainSupportDetails details = new SwapChainSupportDetails();

			details.capabilities = VkSurfaceCapabilitiesKHR.malloc(stack);
			vkGetPhysicalDeviceSurfaceCapabilitiesKHR(vphysicalDevice, surface().surface(), details.capabilities);

			IntBuffer count = stack.ints(0);

			vkGetPhysicalDeviceSurfaceFormatsKHR(vphysicalDevice, surface().surface(), count, null);

			if (count.get(0) != 0) {
				details.formats = VkSurfaceFormatKHR.malloc(count.get(0), stack);
				vkGetPhysicalDeviceSurfaceFormatsKHR(vphysicalDevice, surface().surface(), count, details.formats);
			}

			vkGetPhysicalDeviceSurfacePresentModesKHR(vphysicalDevice, surface().surface(), count, null);

			if (count.get(0) != 0) {
				details.presentModes = stack.mallocInt(count.get(0));
				vkGetPhysicalDeviceSurfacePresentModesKHR(vphysicalDevice, surface().surface(), count,
						details.presentModes);
			}

			return details;
		}
}
