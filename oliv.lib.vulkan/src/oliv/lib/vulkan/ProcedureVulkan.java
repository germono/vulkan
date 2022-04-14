package oliv.lib.vulkan;

import static java.util.stream.Collectors.toSet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_SUBOPTIMAL_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkAcquireNextImageKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkQueuePresentKHR;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;
import static org.lwjgl.vulkan.VK10.vkFreeCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;
import static org.lwjgl.vulkan.VK10.vkResetFences;
import static org.lwjgl.vulkan.VK10.vkWaitForFences;

import java.nio.IntBuffer;
import java.util.Set;
import java.util.stream.Stream;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSubmitInfo;

import oliv.lib.vulkan.apiDonnee.LecteurMaillageNonStructure;
import oliv.lib.vulkan.apiGraphique.CanvasVulkanSWT;
import oliv.lib.vulkan.apiGraphique.InteractionHumaine;
import oliv.lib.vulkan.fonction.FonctionUtil;
import oliv.lib.vulkan.objet.Frame;
import oliv.lib.vulkan.objet.Parametre;
import oliv.lib.vulkan.objet.VKData;
import oliv.lib.vulkan.v1.debug.DebugConsole;
import oliv.lib.vulkan.v1.debug.GestionDebugMessenger;
import oliv.lib.vulkan.v2.instance.GestionInstance;
import oliv.lib.vulkan.v2.instance.InstanceSWT;
import oliv.lib.vulkan.v3.surface.GestionSurface;
import oliv.lib.vulkan.v3.surface.SurfaceSWT;
import oliv.lib.vulkan.v4.devicePhysique.DevicePhysique;
import oliv.lib.vulkan.v4.devicePhysique.GestionPhysicalDevice;
import oliv.lib.vulkan.v5.deviceLogique.DeviceLogique;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;
import oliv.lib.vulkan.v6.commandPool.CommandePool;
import oliv.lib.vulkan.v6.commandPool.GestionCommandPool;
import oliv.lib.vulkan.v7.modele.GestionModele;
import oliv.lib.vulkan.v7.modele.Modele;
import oliv.lib.vulkan.v8.bufferUniforme.BufferUniforme;
import oliv.lib.vulkan.v8.bufferUniforme.GestionBufferUniforme;
import oliv.lib.vulkan.v8.s1.swapChain.GestionSwapChain;
import oliv.lib.vulkan.v8.s1.swapChain.SwapChainRedimentionnable;
import oliv.lib.vulkan.v8.s2.renderPass.GestionRenderPass;
import oliv.lib.vulkan.v8.s2.renderPass.RenderPass;
import oliv.lib.vulkan.v8.s3.graphiquePipeline.GP_Ligne;
import oliv.lib.vulkan.v8.s3.graphiquePipeline.GP_LignePoint;
import oliv.lib.vulkan.v8.s3.graphiquePipeline.GP_Triangle;
import oliv.lib.vulkan.v8.s3.graphiquePipeline.GP_TrianglePoint;
import oliv.lib.vulkan.v8.s3.graphiquePipeline.GP_TriangleWire;
import oliv.lib.vulkan.v8.s3.graphiquePipeline.GestionGraphicsPipeline;
import oliv.lib.vulkan.v8.s4.frameBuffer.FrameBuffer;
import oliv.lib.vulkan.v8.s4.frameBuffer.GestionFrameBuffer;
import oliv.lib.vulkan.v8.s5.commandeBuffer.CommandBuffer;
import oliv.lib.vulkan.v8.s5.commandeBuffer.GestionCommandBuffer;
import oliv.lib.vulkan.v9.synchro.GestionSyncObjects;
import oliv.lib.vulkan.v9.synchro.Synchro;

public class ProcedureVulkan implements FonctionUtil {
	public VKData data;
	GestionInstance instance;
	GestionDebugMessenger debugMessenger;
	private GestionSurface surface;
	private GestionPhysicalDevice physicalDevice;
	GestionDevice device;
	private GestionCommandPool commandPool;
	private GestionModele chargementModele;
	private GestionBufferUniforme uniformeMVP;
	private GestionSyncObjects syncObj;
	private GestionSwapChain swapChain;
	private GestionRenderPass renderPass;
	private GestionGraphicsPipeline graphicsPipelineTriangle;
	private GestionGraphicsPipeline graphicsPipelineLine;
	private GestionFrameBuffer swapChainFramebuffers;
	private GestionCommandBuffer commandBuffers;
	private int currentFrame;
	private int WIDTH;
	private int HEIGHT;
	private static final boolean ENABLE_VALIDATION_LAYERS = true;// DEBUG.get(true);
	private static final int MAX_FRAMES_IN_FLIGHT = 2;
	private static final Set<String> DEVICE_EXTENSIONS = Stream.of(VK_KHR_SWAPCHAIN_EXTENSION_NAME).collect(toSet());

	private static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;

	private static Parametre param = new Parametre(204, 204, 201);

	public ProcedureVulkan() {
		debugMessenger = new DebugConsole();
		debugMessenger.cree(ENABLE_VALIDATION_LAYERS);
		System.out.println("### debug creer");
		instance = new InstanceSWT();
		instance.cree(debugMessenger);
		System.out.println("### instance creer");
		debugMessenger.init(instance);
		System.out.println("### debug init");
		data = instance.data();
	}

	public void phase2(long surfaceid, CanvasVulkanSWT canvas, LecteurMaillageNonStructure modele) {
		if (canvas.isDisposed())
			return;
		WIDTH = Math.max(10, canvas.getSize().x);
		HEIGHT = Math.max(10, canvas.getSize().y);
		System.out.println(WIDTH + " " + HEIGHT);
		surface = new SurfaceSWT();
		surface.cree(instance, canvas);
		System.out.println("### surface creer");
		physicalDevice = new DevicePhysique();
		physicalDevice.cree(surface, instance, DEVICE_EXTENSIONS);
		System.out.println("### physicalDevice creer");
		device = new DeviceLogique();
		device.cree(physicalDevice, DEVICE_EXTENSIONS, debugMessenger);
		System.out.println("### device creer");
		commandPool = new CommandePool();
		commandPool.cree(physicalDevice, device);
		System.out.println("### commandPool creer");
		chargementModele = new Modele();
		chargementModele.cree(device, physicalDevice, commandPool, modele.lectureTexture(), modele);
		uniformeMVP = new BufferUniforme();
		System.out.println("### chargementModele creer");
		surface.ajouteInteractionHumain(new InteractionHumaine(uniformeMVP, this));
		System.out.println("### Interaction creer");
		createSwapChainObjects();
		System.out.println("### SwapChain complete");
		syncObj = new Synchro();
		syncObj.cree(swapChain, device, MAX_FRAMES_IN_FLIGHT);
		System.out.println("### SyncObject creer");
	}

	private void createSwapChainObjects() {
		swapChain = new SwapChainRedimentionnable();
		swapChain.cree(surface, physicalDevice, device, WIDTH, HEIGHT);
		System.out.println("### swapChain creer");
		renderPass = new RenderPass();
		renderPass.cree(physicalDevice, device, swapChain);
		System.out.println("### renderPass creer");
		graphicsPipelineTriangle = pipelineSolide();
		graphicsPipelineTriangle.cree(physicalDevice, device, swapChain, renderPass, chargementModele);
		System.out.println("### graphicsPipelineTriangle creer");
		graphicsPipelineLine = pipelinePipe();
		graphicsPipelineLine.cree(physicalDevice, device, swapChain, renderPass, chargementModele);
		System.out.println("### graphicsPipelineLine creer");
		uniformeMVP.cree(device, swapChain, physicalDevice);
		chargementModele.createColorDepthResources(swapChain);
		System.out.println("### chargementModele creer");
		swapChainFramebuffers = new FrameBuffer();
		swapChainFramebuffers.cree(swapChain, renderPass, device, chargementModele);
		System.out.println("### swapChainFramebuffers creer");
		uniformeMVP.createUniformBuffers();
		chargementModele.createUniformBuffersDescriptorPoolSets(uniformeMVP);
		System.out.println("### chargementModele creer");
		commandBuffers = new CommandBuffer();
		commandBuffers.cree(swapChainFramebuffers, commandPool, device, renderPass, swapChain, chargementModele, param,
				graphicsPipelineTriangle, graphicsPipelineLine);
	}

	public void drawFrame() {

//		System.out.println("drawFrame");
		try (MemoryStack stack = stackPush()) {

			Frame thisFrame = syncObj.inFlightFrames().get(currentFrame);

			vkWaitForFences(device.device(), thisFrame.pFence(), true, UINT64_MAX);

			IntBuffer pImageIndex = stack.mallocInt(1);

			int vkResult = vkAcquireNextImageKHR(device.device(), swapChain.swapChain(), UINT64_MAX,
					thisFrame.imageAvailableSemaphore(), VK_NULL_HANDLE, pImageIndex);

			if (vkResult == VK_ERROR_OUT_OF_DATE_KHR) {
				recreateSwapChain();
				return;
			}

			final int imageIndex = pImageIndex.get(0);

			uniformeMVP.updateUniformBuffer(imageIndex);

			if (syncObj.imagesInFlight().containsKey(imageIndex)) {
				vkWaitForFences(device.device(), syncObj.imagesInFlight().get(imageIndex).fence(), true, UINT64_MAX);
			}

			syncObj.imagesInFlight().put(imageIndex, thisFrame);

			VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
			submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);

			submitInfo.waitSemaphoreCount(1);
			submitInfo.pWaitSemaphores(thisFrame.pImageAvailableSemaphore());
			submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));

			submitInfo.pSignalSemaphores(thisFrame.pRenderFinishedSemaphore());

			submitInfo.pCommandBuffers(stack.pointers(commandBuffers.commandBuffers().get(imageIndex)));

			vkResetFences(device.device(), thisFrame.pFence());

			if ((vkResult = vkQueueSubmit(device.graphicsQueue(), submitInfo, thisFrame.fence())) != VK_SUCCESS) {
				vkResetFences(device.device(), thisFrame.pFence());
				throw new RuntimeException("Failed to submit draw command buffer: " + vkResult);
			}

			VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
			presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);

			presentInfo.pWaitSemaphores(thisFrame.pRenderFinishedSemaphore());

			presentInfo.swapchainCount(1);
			presentInfo.pSwapchains(stack.longs(swapChain.swapChain()));

			presentInfo.pImageIndices(pImageIndex);

//			vkQueuePresentKHR(device.presentQueue(), presentInfo);
			vkResult = vkQueuePresentKHR(device.presentQueue(), presentInfo);

			if (vkResult == VK_ERROR_OUT_OF_DATE_KHR || vkResult == VK_SUBOPTIMAL_KHR || surface.framebufferResize()) {
				surface.setFramebufferResize(false);
				recreateSwapChain();
			} else if (vkResult != VK_SUCCESS) {
				throw new RuntimeException("Failed to present swap chain image");
			}
			currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
		}
	}

	private void recreateSwapChain() {
		surface.recreate();

		vkDeviceWaitIdle(device.device());

		cleanupSwapChain();

		createSwapChainObjects();
	}

	public void cleanup() {
		System.out.println("netoyage demandé");
		if (device == null)
			return;
		System.out.println("netoyage réalisé");
		vkDeviceWaitIdle(device.device());
		cleanupSwapChain();
		uniformeMVP.detruit();
		chargementModele.detruit();
		syncObj.detruit();

		commandPool.detruit();

		device.detruit();

		debugMessenger.detruit();
		instance.detruit();

	}

	private void cleanupSwapChain() {

		chargementModele.cleanupSwapChain();
		uniformeMVP.cleanupSwapChain();
		swapChainFramebuffers.detruit();

		vkFreeCommandBuffers(device.device(), commandPool.commandPool(),
				asPointerBuffer(commandBuffers.commandBuffers()));

		graphicsPipelineTriangle.detruit();
		graphicsPipelineLine.detruit();
		renderPass.detruit();
		swapChain.detruit();

	}

	enum Mode {
		Shader, Wire, Point;
	}

	private Mode mode = Mode.Shader;

	public void modeShader() {
		if (mode == Mode.Shader)
			return;
		mode = Mode.Shader;
		recreateSwapChain();
	}

	public void modeWire() {
		if (mode == Mode.Wire)
			return;
		mode = Mode.Wire;
		recreateSwapChain();
	}

	public void modePoint() {
		if (mode == Mode.Point)
			return;
		mode = Mode.Point;
		recreateSwapChain();
	}

	GestionGraphicsPipeline pipelineSolide() {
		return switch (mode) {
		case Shader -> new GP_Triangle();
		case Wire -> new GP_TriangleWire();
		case Point -> new GP_TrianglePoint();
		};
	}

	GestionGraphicsPipeline pipelinePipe() {
		return switch (mode) {
		case Shader -> new GP_Ligne();
		case Wire -> new GP_Ligne();
		case Point -> new GP_LignePoint();
		};
	}
}
