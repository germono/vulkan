package oliv.lib.vulkan.v9.synchro;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_FENCE_CREATE_SIGNALED_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateFence;
import static org.lwjgl.vulkan.VK10.vkCreateSemaphore;
import static org.lwjgl.vulkan.VK10.vkDestroyFence;
import static org.lwjgl.vulkan.VK10.vkDestroySemaphore;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

import oliv.lib.vulkan.objet.Frame;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;
import oliv.lib.vulkan.v8.s1.swapChain.GestionSwapChain;

public class Synchro implements GestionSyncObjects {
    List<Frame> inFlightFrames;
    Map<Integer, Frame> imagesInFlight;
    GestionDevice device;
    
	@Override
	public Map<Integer, Frame> imagesInFlight() {
		return imagesInFlight;
	}
	@Override
	public List<Frame> inFlightFrames() {
		return inFlightFrames;
	}

	@Override
	public void cree(GestionSwapChain swapChain,GestionDevice device,int MAX_FRAMES_IN_FLIGHT) {
		this.device=device;
        inFlightFrames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
        imagesInFlight = new HashMap<>(swapChain.swapChainImages().size());

        try(MemoryStack stack = stackPush()) {

            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack);
            semaphoreInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack);
            fenceInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

            LongBuffer pImageAvailableSemaphore = stack.mallocLong(1);
            LongBuffer pRenderFinishedSemaphore = stack.mallocLong(1);
            LongBuffer pFence = stack.mallocLong(1);

            for(int i = 0;i < MAX_FRAMES_IN_FLIGHT;i++) {

                if(vkCreateSemaphore(device.device(), semaphoreInfo, null, pImageAvailableSemaphore) != VK_SUCCESS
                        || vkCreateSemaphore(device.device(), semaphoreInfo, null, pRenderFinishedSemaphore) != VK_SUCCESS
                        || vkCreateFence(device.device(), fenceInfo, null, pFence) != VK_SUCCESS) {

                    throw new RuntimeException("Failed to create synchronization objects for the frame " + i);
                }

                inFlightFrames.add(new Frame(pImageAvailableSemaphore.get(0), pRenderFinishedSemaphore.get(0), pFence.get(0)));
            }

        }
	}


	@Override
	public void detruit() {
		 inFlightFrames.forEach(frame -> {

                vkDestroySemaphore(device.device(), frame.renderFinishedSemaphore(), null);
                vkDestroySemaphore(device.device(), frame.imageAvailableSemaphore(), null);
                vkDestroyFence(device.device(), frame.fence(), null);
            });
            imagesInFlight.clear();
	}
}