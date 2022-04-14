package oliv.lib.vulkan.v2.instance;

import static oliv.lib.vulkan.fonction.VKUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSurface.VK_KHR_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRWin32Surface.VK_KHR_WIN32_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRXlibSurface.VK_KHR_XLIB_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.VK_MAKE_VERSION;
import static org.lwjgl.vulkan.VK10.VK_VERSION_MAJOR;
import static org.lwjgl.vulkan.VK10.VK_VERSION_MINOR;
import static org.lwjgl.vulkan.VK10.VK_VERSION_PATCH;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateInstance;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;

import java.nio.ByteBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.Platform;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

import oliv.lib.vulkan.objet.VKData;
import oliv.lib.vulkan.v1.debug.GestionDebugMessenger;

public class InstanceSWT  implements GestionInstance{
	VkInstance instance;
	VKData data;
	@Override
	public VkInstance instance() {
		return instance;
	}
	@Override
	public VKData data() {
		return data;
	}
	@Override
	public void cree(GestionDebugMessenger debug) {
        VkApplicationInfo appInfo = VkApplicationInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pApplicationName(memUTF8("SWT Vulkan Application"))
                .pEngineName(memUTF8(""))
                .apiVersion(VK_MAKE_VERSION(1, 0, 2));
        int ver=appInfo.apiVersion();
        System.out.println("Vulkan version : "+VK_VERSION_MAJOR(ver)+"."+VK_VERSION_MINOR(ver)+"."+VK_VERSION_PATCH(ver));
        ByteBuffer VK_KHR_SURFACE_EXTENSION = memUTF8(VK_KHR_SURFACE_EXTENSION_NAME);
        ByteBuffer VK_EXT_DEBUG_REPORT_EXTENSION = memUTF8(VK_EXT_DEBUG_REPORT_EXTENSION_NAME);
        ByteBuffer VK_EXT_DEBUG_UTILS_EXTENSION = memUTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
        ByteBuffer VK_KHR_OS_SURFACE_EXTENSION;
        if (Platform.get() == Platform.WINDOWS)
            VK_KHR_OS_SURFACE_EXTENSION = memUTF8(VK_KHR_WIN32_SURFACE_EXTENSION_NAME);
        else
            VK_KHR_OS_SURFACE_EXTENSION = memUTF8(VK_KHR_XLIB_SURFACE_EXTENSION_NAME);
        PointerBuffer ppEnabledExtensionNames = memAllocPointer(4)
		        .put(VK_KHR_SURFACE_EXTENSION)
		        .put(VK_KHR_OS_SURFACE_EXTENSION)
		        .put(VK_EXT_DEBUG_REPORT_EXTENSION)
		        .put(VK_EXT_DEBUG_UTILS_EXTENSION)
		        .flip();
        VkInstanceCreateInfo pCreateInfo = VkInstanceCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                .pNext(NULL)
                .pApplicationInfo(appInfo)
                .ppEnabledExtensionNames(ppEnabledExtensionNames);
        debug.ajouteLayer(pCreateInfo);
//                .ppEnabledLayerNames(ppEnabledLayerNames);
        PointerBuffer pInstance = memAllocPointer(1);
        int err = vkCreateInstance(pCreateInfo, null, pInstance);
        long instanceid = pInstance.get(0);
        memFree(pInstance);
        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to create VkInstance: " + translateVulkanResult(err));
        }
        VkInstance ret = new VkInstance(instanceid, pCreateInfo);
        pCreateInfo.free();
//        memFree(ppEnabledLayerNames);
        memFree(ppEnabledExtensionNames);
        memFree(VK_KHR_OS_SURFACE_EXTENSION);
        memFree(VK_EXT_DEBUG_REPORT_EXTENSION);
        memFree(VK_KHR_SURFACE_EXTENSION);
        appInfo.free();
        
        instance=ret;
        data = new VKData();
		data.instance = instance; 
	}

	@Override
	public void detruit() {
		vkDestroyInstance(instance, null);
	}
	
}