package oliv.lib.vulkan.v4.devicePhysique;

import java.nio.IntBuffer;

import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

public class SwapChainSupportDetails {
	public VkSurfaceCapabilitiesKHR capabilities;
	public VkSurfaceFormatKHR.Buffer formats;
	public IntBuffer presentModes;
}
