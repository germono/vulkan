package oliv.lib.vulkan.v8.s4.frameBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateFramebuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyFramebuffer;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;
import oliv.lib.vulkan.v7.modele.GestionModele;
import oliv.lib.vulkan.v8.s1.swapChain.GestionSwapChain;
import oliv.lib.vulkan.v8.s2.renderPass.GestionRenderPass;

public class FrameBuffer  implements GestionFrameBuffer {
	List<Long> swapChainFramebuffers; 
	GestionDevice device;
	@Override
	public List<Long> swapChainFramebuffers() {
		return swapChainFramebuffers;
	}

	@Override
	public void cree(GestionSwapChain swapChain, GestionRenderPass renderPass,GestionDevice device,GestionModele modele) {
		this.device=device;
        swapChainFramebuffers = new ArrayList<>(swapChain.swapChainImageViews().size());

        try(MemoryStack stack = stackPush()) {

        	LongBuffer attachments = stack.longs(modele.colorImageView(), modele.depthImageView(), VK_NULL_HANDLE);
            LongBuffer pFramebuffer = stack.mallocLong(1);

            // Lets allocate the create info struct once and just update the pAttachments field each iteration
            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack);
            framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferInfo.renderPass(renderPass.renderPass());
            framebufferInfo.width(swapChain.swapChainExtent().width());
            framebufferInfo.height(swapChain.swapChainExtent().height());
            framebufferInfo.layers(1);

            for(long imageView : swapChain.swapChainImageViews()) {

            	attachments.put(2, imageView);

                framebufferInfo.pAttachments(attachments);

                if(vkCreateFramebuffer(device.device(), framebufferInfo, null, pFramebuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create framebuffer");
                }

                swapChainFramebuffers.add(pFramebuffer.get(0));
            }
        }
	}

	@Override
	public void detruit() {
		swapChainFramebuffers.forEach(framebuffer -> vkDestroyFramebuffer(device.device(), framebuffer, null));
	}
}
