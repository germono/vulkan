package oliv.lib.vulkan.v8.s1.swapChain;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkCreateSwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_IDENTITY;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_CONCURRENT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateImageView;
import static org.lwjgl.vulkan.VK10.vkDestroyImageView;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import oliv.lib.vulkan.v2.instance.GestionInstance;
import oliv.lib.vulkan.v3.surface.GestionSurface;
import oliv.lib.vulkan.v4.devicePhysique.GestionPhysicalDevice;
import oliv.lib.vulkan.v4.devicePhysique.QueueFamilyIndices;
import oliv.lib.vulkan.v4.devicePhysique.SwapChainSupportDetails;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;

public class SwapChain implements GestionSwapChain {
	GestionInstance instance;
	GestionDevice device;
	GestionSurface surface;
	long swapChain;
	List<Long> swapChainImageViews;
	List<Long> swapChainImages;
	int swapChainImageFormat;
	VkExtent2D swapChainExtent;
	int WIDTH;
	int HEIGHT;

	@Override
	public long swapChain() {
		return swapChain;
	}

	@Override
	public List<Long> swapChainImages() {
		return swapChainImages;
	}

	@Override
	public int swapChainImageFormat() {
		return swapChainImageFormat;
	}

	@Override
	public VkExtent2D swapChainExtent() {
		return swapChainExtent;
	}

	@Override
	public List<Long> swapChainImageViews() {
		return swapChainImageViews;
	}

	@Override
	public void cree(GestionSurface surface, GestionPhysicalDevice physicalDevice, GestionDevice device, int WIDTH,
			int HEIGHT) {
		this.device = device;
		this.surface = surface;
		this.WIDTH = WIDTH;
		this.HEIGHT = HEIGHT;
		try (MemoryStack stack = stackPush()) {

			SwapChainSupportDetails swapChainSupport = physicalDevice.querySwapChainSupport(stack);

			VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(swapChainSupport.formats);
			int presentMode = chooseSwapPresentMode(swapChainSupport.presentModes);
			VkExtent2D extent = chooseSwapExtent(swapChainSupport.capabilities);

			IntBuffer imageCount = stack.ints(swapChainSupport.capabilities.minImageCount() + 1);

			if (swapChainSupport.capabilities.maxImageCount() > 0
					&& imageCount.get(0) > swapChainSupport.capabilities.maxImageCount()) {
				imageCount.put(0, swapChainSupport.capabilities.maxImageCount());
			}

			VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack);

			createInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
			createInfo.surface(surface.surface());

			// Image settings
			createInfo.minImageCount(imageCount.get(0));
			createInfo.imageFormat(surfaceFormat.format());
			createInfo.imageColorSpace(surfaceFormat.colorSpace());
			createInfo.imageExtent(extent);
			createInfo.imageArrayLayers(1);
			createInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

			QueueFamilyIndices indices = physicalDevice.findQueueFamilies();

			if (!indices.graphicsFamily.equals(indices.presentFamily)) {
				createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
				createInfo.pQueueFamilyIndices(stack.ints(indices.graphicsFamily, indices.presentFamily));
			} else {
				createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
			}

			createInfo.preTransform(swapChainSupport.capabilities.currentTransform());
			createInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
			createInfo.presentMode(presentMode);
			createInfo.clipped(true);

			createInfo.oldSwapchain(VK_NULL_HANDLE);

			LongBuffer pSwapChain = stack.longs(VK_NULL_HANDLE);

			if (vkCreateSwapchainKHR(device.device(), createInfo, null, pSwapChain) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create swap chain");
			}

			swapChain = pSwapChain.get(0);

			vkGetSwapchainImagesKHR(device.device(), swapChain, imageCount, null);

			LongBuffer pSwapchainImages = stack.mallocLong(imageCount.get(0));

			vkGetSwapchainImagesKHR(device.device(), swapChain, imageCount, pSwapchainImages);

			swapChainImages = new ArrayList<>(imageCount.get(0));

			for (int i = 0; i < pSwapchainImages.capacity(); i++) {
				swapChainImages.add(pSwapchainImages.get(i));
			}

			swapChainImageFormat = surfaceFormat.format();
			swapChainExtent = VkExtent2D.create().set(extent);
		}
		createImageViews();
	}

	void createImageViews() {

		swapChainImageViews = new ArrayList<>(swapChainImages.size());

		try (MemoryStack stack = stackPush()) {

			LongBuffer pImageView = stack.mallocLong(1);

			for (long swapChainImage : swapChainImages) {

				VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.calloc(stack);

				createInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
				createInfo.image(swapChainImage);
				createInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
				createInfo.format(swapChainImageFormat);

				createInfo.components().r(VK_COMPONENT_SWIZZLE_IDENTITY);
				createInfo.components().g(VK_COMPONENT_SWIZZLE_IDENTITY);
				createInfo.components().b(VK_COMPONENT_SWIZZLE_IDENTITY);
				createInfo.components().a(VK_COMPONENT_SWIZZLE_IDENTITY);

				createInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
				createInfo.subresourceRange().baseMipLevel(0);
				createInfo.subresourceRange().levelCount(1);
				createInfo.subresourceRange().baseArrayLayer(0);
				createInfo.subresourceRange().layerCount(1);

				if (vkCreateImageView(device.device(), createInfo, null, pImageView) != VK_SUCCESS) {
					throw new RuntimeException("Failed to create image views");
				}

				swapChainImageViews.add(pImageView.get(0));
			}

		}
	}

	@Override
	public void detruit() {
		swapChainImageViews.forEach(imageView -> vkDestroyImageView(device.device(), imageView, null));

		vkDestroySwapchainKHR(device.device(), swapChain, null);
	}

	VkSurfaceFormatKHR chooseSwapSurfaceFormat(VkSurfaceFormatKHR.Buffer availableFormats) {
		return availableFormats.stream().filter(availableFormat -> availableFormat.format() == VK_FORMAT_B8G8R8_UNORM)
				.filter(availableFormat -> availableFormat.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR).findAny()
				.orElse(availableFormats.get(0));
	}

	private int chooseSwapPresentMode(IntBuffer availablePresentModes) {

		for (int i = 0; i < availablePresentModes.capacity(); i++) {
			if (availablePresentModes.get(i) == VK_PRESENT_MODE_MAILBOX_KHR) {
				return availablePresentModes.get(i);
			}
		}

		return VK_PRESENT_MODE_FIFO_KHR;
	}

	protected static final int UINT32_MAX = 0xFFFFFFFF;

	protected VkExtent2D chooseSwapExtent(VkSurfaceCapabilitiesKHR capabilities) {

		if (capabilities.currentExtent().width() != UINT32_MAX) {
			return capabilities.currentExtent();
		}

		VkExtent2D actualExtent = VkExtent2D.malloc().set(WIDTH, HEIGHT);

		VkExtent2D minExtent = capabilities.minImageExtent();
		VkExtent2D maxExtent = capabilities.maxImageExtent();

		actualExtent.width(clamp(minExtent.width(), maxExtent.width(), actualExtent.width()));
		actualExtent.height(clamp(minExtent.height(), maxExtent.height(), actualExtent.height()));

		return actualExtent;
	}

	protected int clamp(int min, int max, int value) {
		return Math.max(min, Math.min(max, value));
	}
}
