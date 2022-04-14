package oliv.lib.vulkan.v8.s1.swapChain;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateImageView;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import oliv.lib.vulkan.v4.devicePhysique.GestionPhysicalDevice;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;

public class SwapChainRedimentionnable extends SwapChain{

	GestionPhysicalDevice physicaldevice;
	@Override
	protected VkExtent2D chooseSwapExtent(VkSurfaceCapabilitiesKHR capabilities) {

		if (capabilities.currentExtent().width() != UINT32_MAX) {
			return capabilities.currentExtent();
		}
		IntBuffer width = stackGet().ints(0);
        IntBuffer height = stackGet().ints(0);

        glfwGetFramebufferSize(surface.window(), width, height);

        VkExtent2D actualExtent = VkExtent2D.malloc().set(width.get(0), height.get(0));


		VkExtent2D minExtent = capabilities.minImageExtent();
		VkExtent2D maxExtent = capabilities.maxImageExtent();

		actualExtent.width(clamp(minExtent.width(), maxExtent.width(), actualExtent.width()));
		actualExtent.height(clamp(minExtent.height(), maxExtent.height(), actualExtent.height()));

		return actualExtent;
	}
	@Override
	 void createImageViews() {

        swapChainImageViews = new ArrayList<>(swapChainImages.size());

        for(long swapChainImage : swapChainImages) {
            swapChainImageViews.add(createImageView(device,swapChainImage, swapChainImageFormat, VK_IMAGE_ASPECT_COLOR_BIT, 1));
        }
    }
	@Override
	 VkSurfaceFormatKHR chooseSwapSurfaceFormat(VkSurfaceFormatKHR.Buffer availableFormats) {
		return availableFormats.stream()
				.filter(availableFormat -> availableFormat.format() == VK_FORMAT_B8G8R8_SRGB)
                .filter(availableFormat -> availableFormat.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
				.findAny().orElse(availableFormats.get(0));
	}
	 long createImageView(GestionDevice device, long image, int format, int aspectFlags, int mipLevels) {

        try(MemoryStack stack = stackPush()) {

            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc(stack);
            viewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            viewInfo.image(image);
            viewInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
            viewInfo.format(format);
            viewInfo.subresourceRange().aspectMask(aspectFlags);
            viewInfo.subresourceRange().baseMipLevel(0);
            viewInfo.subresourceRange().levelCount(mipLevels);
            viewInfo.subresourceRange().baseArrayLayer(0);
            viewInfo.subresourceRange().layerCount(1);

            LongBuffer pImageView = stack.mallocLong(1);

            if(vkCreateImageView(device.device(), viewInfo, null, pImageView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create texture image view");
            }

            return pImageView.get(0);
        }
    }
}
