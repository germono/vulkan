package oliv.lib.vulkan.v1.debug;

import static java.util.stream.Collectors.toSet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.vkCreateDebugUtilsMessengerEXT;
import static org.lwjgl.vulkan.EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT;
import static org.lwjgl.vulkan.VK10.VK_ERROR_EXTENSION_NOT_PRESENT;
import static org.lwjgl.vulkan.VK10.VK_FALSE;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkEnumerateInstanceLayerProperties;
import static org.lwjgl.vulkan.VK10.vkGetInstanceProcAddr;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.HashSet;
import java.util.Set;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;

import oliv.lib.vulkan.v2.instance.GestionInstance;

public class DebugConsole implements GestionDebugMessenger {
	long debugMessenger;
	boolean ENABLE_VALIDATION_LAYERS;
	private Set<String> VALIDATION_LAYERS;
	private GestionInstance instance;
	

	@Override
	public void cree(boolean ENABLE_VALIDATION_LAYERS) {
		this.ENABLE_VALIDATION_LAYERS=ENABLE_VALIDATION_LAYERS;
		if (ENABLE_VALIDATION_LAYERS) {
			VALIDATION_LAYERS = new HashSet<>();
			VALIDATION_LAYERS.add("VK_LAYER_KHRONOS_validation");
		}
		if(ENABLE_VALIDATION_LAYERS && !checkValidationLayerSupport(VALIDATION_LAYERS)) {
            throw new RuntimeException("Validation requested but not supported");
        }
	}
	
	@Override
	public void init(GestionInstance instance) {
		this.instance=instance;
        if(!ENABLE_VALIDATION_LAYERS) {
            return;
        }

        try(MemoryStack stack = stackPush()) {

            VkDebugUtilsMessengerCreateInfoEXT createInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);

            populateDebugMessengerCreateInfo(createInfo);

            LongBuffer pDebugMessenger = stack.longs(VK_NULL_HANDLE);

            if(createDebugUtilsMessengerEXT(instance.instance(), createInfo, null, pDebugMessenger) != VK_SUCCESS) {
                throw new RuntimeException("Failed to set up debug messenger");
            }

            debugMessenger = pDebugMessenger.get(0);
        }
	}
	  void populateDebugMessengerCreateInfo(VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo) {
	        debugCreateInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
	        debugCreateInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
	        debugCreateInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
	        debugCreateInfo.pfnUserCallback(DebugConsole::debugCallback);
	    }
	    private static int debugCallback(int messageSeverity, int messageType, long pCallbackData, long pUserData) {

	        VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
	        switch (messageSeverity) {
			case VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT->System.out.println("Vl v : " + callbackData.pMessageString());
			case VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT->System.out.println("Vl i : " + callbackData.pMessageString());
			case VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT->System.err.println("Vl w : " + callbackData.pMessageString());
			case VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT->System.err.println("Vl e : " + callbackData.pMessageString());
			}
	        return VK_FALSE;
	    }

	@Override
	public void detruit() {
		if(ENABLE_VALIDATION_LAYERS) {
            destroyDebugUtilsMessengerEXT(instance.instance(), debugMessenger, null);
        }
	}
	 private static void destroyDebugUtilsMessengerEXT(VkInstance instance, long debugMessenger, VkAllocationCallbacks allocationCallbacks) {

         if(vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT") != NULL) {
             vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, allocationCallbacks);
         }

     }
	private static int createDebugUtilsMessengerEXT(VkInstance instance,
			VkDebugUtilsMessengerCreateInfoEXT createInfo, VkAllocationCallbacks allocationCallbacks,
			LongBuffer pDebugMessenger) {

		if (vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT") != NULL) {
			return vkCreateDebugUtilsMessengerEXT(instance, createInfo, allocationCallbacks, pDebugMessenger);
		}

		return VK_ERROR_EXTENSION_NOT_PRESENT;
	}

	@Override
	public boolean actif() {
		return ENABLE_VALIDATION_LAYERS;
	}

	@Override
	public void ajouteLayer(VkInstanceCreateInfo createInfo) {
		if(!ENABLE_VALIDATION_LAYERS)
			return;
		createInfo.ppEnabledLayerNames(asPointerBuffer(VALIDATION_LAYERS));

        VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc();
        populateDebugMessengerCreateInfo(debugCreateInfo);
        createInfo.pNext(debugCreateInfo.address());
	}
	@Override
	public void ajouteLayer(VkDeviceCreateInfo createInfo) {
		if (ENABLE_VALIDATION_LAYERS) {
			createInfo.ppEnabledLayerNames(asPointerBuffer(VALIDATION_LAYERS));
		}
	}
	private boolean checkValidationLayerSupport(Set<String> VALIDATION_LAYERS) {

        try(MemoryStack stack = stackPush()) {

            IntBuffer layerCount = stack.ints(0);

            vkEnumerateInstanceLayerProperties(layerCount, null);

            VkLayerProperties.Buffer availableLayers = VkLayerProperties.malloc(layerCount.get(0), stack);

            vkEnumerateInstanceLayerProperties(layerCount, availableLayers);

            Set<String> availableLayerNames = availableLayers.stream()
                    .map(VkLayerProperties::layerNameString)
                    .collect(toSet());

            return availableLayerNames.containsAll(VALIDATION_LAYERS);
        }
    }
}


