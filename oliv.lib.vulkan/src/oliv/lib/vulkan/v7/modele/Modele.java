package oliv.lib.vulkan.v7.modele;

import static java.lang.ClassLoader.getSystemClassLoader;
import static org.lwjgl.stb.STBImage.STBI_rgb_alpha;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import oliv.lib.vulkan.apiDonnee.LecteurMaillageNonStructure;
import oliv.lib.vulkan.apiDonnee.Texture;
import oliv.lib.vulkan.objet.ModelLoader;
import oliv.lib.vulkan.objet.ModelLoader.Model;
import oliv.lib.vulkan.objet.UniformModelVueProj;
import oliv.lib.vulkan.objet.noeud.Vertex;
import oliv.lib.vulkan.objet.noeud.VertexNormalTexture;
import oliv.lib.vulkan.v4.devicePhysique.GestionPhysicalDevice;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;
import oliv.lib.vulkan.v6.commandPool.GestionCommandPool;
import oliv.lib.vulkan.v8.bufferUniforme.GestionBufferUniforme;
import oliv.lib.vulkan.v8.s1.swapChain.GestionSwapChain;

public class Modele implements GestionModele{
	private int mipLevels;
	GestionDevice device;
	GestionPhysicalDevice physicalDevice;
	GestionCommandPool commandPool;
	private VertexNormalTexture[] vertices;
	private int[] indicesTriangle;
	private int[] indicesLine;
	private long textureImage;
	private long textureImageMemory;
	private long textureSampler;
	private long textureImageView;
	private long vertexBuffer;
	private long vertexBufferMemory;
	private long indexBufferLine;
	private long indexBufferMemoryLine;
	private long indexBufferTriangle;
	private long indexBufferMemoryTrianle;
	private long descriptorSetLayout;
	private long colorImage;
	private long colorImageMemory;
	private long colorImageView;
	private long depthImage;
	private long depthImageMemory;
	private long depthImageView;
	private GestionSwapChain swapChain;
	private long descriptorPool;
	private List<Long> descriptorSets;

	boolean texturer = true;

	@Override
	public void creeTexture(GestionDevice device, GestionPhysicalDevice physicalDevice, GestionCommandPool commandPool,
			Texture texture) {
		this.device = device;
		this.commandPool = commandPool;
		this.physicalDevice = physicalDevice;
		if (texturer) {
			createTextureImage(texture);
			createTextureImageView();
			createTextureSampler();
		}
	}
	@Override
	public void model(LecteurMaillageNonStructure modele) {
		loadModel(modele);
	}
	@Override
	public void buffer() {
		createVertexBuffer();
		createIndexBuffer();
	}
	@Override
	public void description() {
		createDescriptorSetLayout();
	}
	
	public static ByteBuffer genereImage()
	{     
		int width=10;
		int height=10;
		int BYTES_PER_PIXEL = 4;
	     
	    ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * BYTES_PER_PIXEL);
	     
	    for(int y = 0; y < height; y++)
	    {
	        for(int x = 0; x < width; x++)
	        {
	            buffer.put((byte) 254);     // Red component
	            buffer.put((byte) 254);      // Green component
	            buffer.put((byte) 254);               // Blue component
	            buffer.put((byte) 254);    // Alpha component. Only for RGBA
	        }
	    }
	 
	    buffer.flip();
	     
	    return buffer;
	}

	private void createTextureImage(Texture tex) {

		try (MemoryStack stack = stackPush()) {

			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);
			IntBuffer pChannels = stack.mallocInt(1);
			ByteBuffer pixels;
			if (tex != null) {
				pWidth.put(tex.largeur());
				pHeight.put(tex.hauteur());
				pixels = tex.image();
			} else {
				String filename = getSystemClassLoader().getResource("textures/texture.jpg").toExternalForm();
				pixels = stbi_load(filename, pWidth, pHeight, pChannels, STBI_rgb_alpha);
			}
			long imageSize = pWidth.get(0) * pHeight.get(0) * 4; // pChannels.get(0);

			mipLevels = (int) Math.floor(log2(Math.max(pWidth.get(0), pHeight.get(0)))) + 1;


			LongBuffer pStagingBuffer = stack.mallocLong(1);
			LongBuffer pStagingBufferMemory = stack.mallocLong(1);
			createBuffer(imageSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
					VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, pStagingBuffer,
					pStagingBufferMemory);

			PointerBuffer data = stack.mallocPointer(1);
			vkMapMemory(device.device(), pStagingBufferMemory.get(0), 0, imageSize, 0, data);
			{
				memcpy(data.getByteBuffer(0, (int) imageSize), pixels, imageSize);
			}
			vkUnmapMemory(device.device(), pStagingBufferMemory.get(0));

			stbi_image_free(pixels);

			LongBuffer pTextureImage = stack.mallocLong(1);
			LongBuffer pTextureImageMemory = stack.mallocLong(1);
			createImage(pWidth.get(0), pHeight.get(0), mipLevels, VK_SAMPLE_COUNT_1_BIT, VK_FORMAT_R8G8B8A8_SRGB,
					VK_IMAGE_TILING_OPTIMAL,
					VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
					VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, pTextureImage, pTextureImageMemory);

			textureImage = pTextureImage.get(0);
			textureImageMemory = pTextureImageMemory.get(0);

			transitionImageLayout(textureImage, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_UNDEFINED,
					VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, mipLevels);

			copyBufferToImage(pStagingBuffer.get(0), textureImage, pWidth.get(0), pHeight.get(0));

			// Transitioned to VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL while generating
			// mipmaps
			generateMipmaps(textureImage, VK_FORMAT_R8G8B8A8_SRGB, pWidth.get(0), pHeight.get(0), mipLevels);

			vkDestroyBuffer(device.device(), pStagingBuffer.get(0), null);
			vkFreeMemory(device.device(), pStagingBufferMemory.get(0), null);

		} 
	}

	private double log2(double n) {
		return Math.log(n) / Math.log(2);
	}

	private void createImage(int width, int height, int mipLevels, int numSamples, int format, int tiling, int usage,
			int memProperties, LongBuffer pTextureImage, LongBuffer pTextureImageMemory) {

		try (MemoryStack stack = stackPush()) {

			VkImageCreateInfo imageInfo = VkImageCreateInfo.calloc(stack);
			imageInfo.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO);
			imageInfo.imageType(VK_IMAGE_TYPE_2D);
			imageInfo.extent().width(width);
			imageInfo.extent().height(height);
			imageInfo.extent().depth(1);
			imageInfo.mipLevels(mipLevels);
			imageInfo.arrayLayers(1);
			imageInfo.format(format);
			imageInfo.tiling(tiling);
			imageInfo.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
			imageInfo.usage(usage);
			imageInfo.samples(numSamples);
			imageInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

			if (vkCreateImage(device.device(), imageInfo, null, pTextureImage) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create image");
			}

			VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
			vkGetImageMemoryRequirements(device.device(), pTextureImage.get(0), memRequirements);

			VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack);
			allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
			allocInfo.allocationSize(memRequirements.size());
			allocInfo.memoryTypeIndex(findMemoryType(memRequirements.memoryTypeBits(), memProperties));

			if (vkAllocateMemory(device.device(), allocInfo, null, pTextureImageMemory) != VK_SUCCESS) {
				throw new RuntimeException("Failed to allocate image memory");
			}

			vkBindImageMemory(device.device(), pTextureImage.get(0), pTextureImageMemory.get(0), 0);
		}
	}

	private void transitionImageLayout(long image, int format, int oldLayout, int newLayout, int mipLevels) {

		try (MemoryStack stack = stackPush()) {

			VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack);
			barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
			barrier.oldLayout(oldLayout);
			barrier.newLayout(newLayout);
			barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
			barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
			barrier.image(image);

			barrier.subresourceRange().baseMipLevel(0);
			barrier.subresourceRange().levelCount(mipLevels);
			barrier.subresourceRange().baseArrayLayer(0);
			barrier.subresourceRange().layerCount(1);

			if (newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {

				barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);

				if (hasStencilComponent(format)) {
					barrier.subresourceRange()
							.aspectMask(barrier.subresourceRange().aspectMask() | VK_IMAGE_ASPECT_STENCIL_BIT);
				}

			} else {
				barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
			}

			int sourceStage;
			int destinationStage;

			if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {

				barrier.srcAccessMask(0);
				barrier.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);

				sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
				destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;

			} else if (oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL
					&& newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {

				barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
				barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

				sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
				destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;

			} else if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED
					&& newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {

				barrier.srcAccessMask(0);
				barrier.dstAccessMask(
						VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);

				sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
				destinationStage = VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;

			} else if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED
					&& newLayout == VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL) {

				barrier.srcAccessMask(0);
				barrier.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

				sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
				destinationStage = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;

			} else {
				throw new IllegalArgumentException("Unsupported layout transition");
			}

			VkCommandBuffer commandBuffer = beginSingleTimeCommands();

			vkCmdPipelineBarrier(commandBuffer, sourceStage, destinationStage, 0, null, null, barrier);

			endSingleTimeCommands(commandBuffer);
		}
	}

	private boolean hasStencilComponent(int format) {
		return format == VK_FORMAT_D32_SFLOAT_S8_UINT || format == VK_FORMAT_D24_UNORM_S8_UINT;
	}

	private VkCommandBuffer beginSingleTimeCommands() {

		try (MemoryStack stack = stackPush()) {

			VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
			allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
			allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
			allocInfo.commandPool(commandPool.commandPool());
			allocInfo.commandBufferCount(1);

			PointerBuffer pCommandBuffer = stack.mallocPointer(1);
			vkAllocateCommandBuffers(device.device(), allocInfo, pCommandBuffer);
			VkCommandBuffer commandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), device.device());

			VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
			beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
			beginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

			vkBeginCommandBuffer(commandBuffer, beginInfo);

			return commandBuffer;
		}
	}

	private void endSingleTimeCommands(VkCommandBuffer commandBuffer) {

		try (MemoryStack stack = stackPush()) {

			vkEndCommandBuffer(commandBuffer);

			VkSubmitInfo.Buffer submitInfo = VkSubmitInfo.calloc(1, stack);
			submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
			submitInfo.pCommandBuffers(stack.pointers(commandBuffer));

			vkQueueSubmit(device.graphicsQueue(), submitInfo, VK_NULL_HANDLE);
			vkQueueWaitIdle(device.graphicsQueue());

			vkFreeCommandBuffers(device.device(), commandPool.commandPool(), commandBuffer);
		}
	}

	private void generateMipmaps(long image, int imageFormat, int width, int height, int mipLevels) {

		try (MemoryStack stack = stackPush()) {

			// Check if image format supports linear blitting
			VkFormatProperties formatProperties = VkFormatProperties.malloc(stack);
			vkGetPhysicalDeviceFormatProperties(physicalDevice.physicalDevice(), imageFormat, formatProperties);

			if ((formatProperties.optimalTilingFeatures() & VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_LINEAR_BIT) == 0) {
				throw new RuntimeException("Texture image format does not support linear blitting");
			}

			VkCommandBuffer commandBuffer = beginSingleTimeCommands();

			VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack);
			barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
			barrier.image(image);
			barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
			barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
			barrier.dstAccessMask(VK_QUEUE_FAMILY_IGNORED);
			barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
			barrier.subresourceRange().baseArrayLayer(0);
			barrier.subresourceRange().layerCount(1);
			barrier.subresourceRange().levelCount(1);

			int mipWidth = width;
			int mipHeight = height;

			for (int i = 1; i < mipLevels; i++) {

				barrier.subresourceRange().baseMipLevel(i - 1);
				barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
				barrier.newLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
				barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
				barrier.dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);

				vkCmdPipelineBarrier(commandBuffer, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT, 0,
						null, null, barrier);

				VkImageBlit.Buffer blit = VkImageBlit.calloc(1, stack);
				blit.srcOffsets(0).set(0, 0, 0);
				blit.srcOffsets(1).set(mipWidth, mipHeight, 1);
				blit.srcSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
				blit.srcSubresource().mipLevel(i - 1);
				blit.srcSubresource().baseArrayLayer(0);
				blit.srcSubresource().layerCount(1);
				blit.dstOffsets(0).set(0, 0, 0);
				blit.dstOffsets(1).set(mipWidth > 1 ? mipWidth / 2 : 1, mipHeight > 1 ? mipHeight / 2 : 1, 1);
				blit.dstSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
				blit.dstSubresource().mipLevel(i);
				blit.dstSubresource().baseArrayLayer(0);
				blit.dstSubresource().layerCount(1);

				vkCmdBlitImage(commandBuffer, image, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, image,
						VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, blit, VK_FILTER_LINEAR);

				barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
				barrier.newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
				barrier.srcAccessMask(VK_ACCESS_TRANSFER_READ_BIT);
				barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

				vkCmdPipelineBarrier(commandBuffer, VK_PIPELINE_STAGE_TRANSFER_BIT,
						VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, 0, null, null, barrier);

				if (mipWidth > 1) {
					mipWidth /= 2;
				}

				if (mipHeight > 1) {
					mipHeight /= 2;
				}
			}

			barrier.subresourceRange().baseMipLevel(mipLevels - 1);
			barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
			barrier.newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
			barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

			vkCmdPipelineBarrier(commandBuffer, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
					0, null, null, barrier);

			endSingleTimeCommands(commandBuffer);
		}
	}

	private int findMemoryType(int typeFilter, int properties) {

		VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.malloc();
		vkGetPhysicalDeviceMemoryProperties(physicalDevice.physicalDevice(), memProperties);

		for (int i = 0; i < memProperties.memoryTypeCount(); i++) {
			if ((typeFilter & (1 << i)) != 0
					&& (memProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
				return i;
			}
		}

		throw new RuntimeException("Failed to find suitable memory type");
	}

	private void copyBufferToImage(long buffer, long image, int width, int height) {

		try (MemoryStack stack = stackPush()) {

			VkCommandBuffer commandBuffer = beginSingleTimeCommands();

			VkBufferImageCopy.Buffer region = VkBufferImageCopy.calloc(1, stack);
			region.bufferOffset(0);
			region.bufferRowLength(0); // Tightly packed
			region.bufferImageHeight(0); // Tightly packed
			region.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
			region.imageSubresource().mipLevel(0);
			region.imageSubresource().baseArrayLayer(0);
			region.imageSubresource().layerCount(1);
			region.imageOffset().set(0, 0, 0);
			region.imageExtent(VkExtent3D.calloc(stack).set(width, height, 1));

			vkCmdCopyBufferToImage(commandBuffer, buffer, image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region);

			endSingleTimeCommands(commandBuffer);
		}
	}

	private void memcpy(ByteBuffer dst, ByteBuffer src, long size) {
		src.limit((int) size);
		dst.put(src);
		src.limit(src.capacity()).rewind();
	}

	private void loadModel(LecteurMaillageNonStructure modele) {

		// File modelFile = new
		// File(getSystemClassLoader().getResource("models/chalet.obj").getFile());

		Model model = ModelLoader.lecteur(modele);// ModelLoader.loadModel(modelFile, aiProcess_FlipUVs |
													// aiProcess_DropNormals);

		final int vertexCount = model.positions.size();

		vertices = new VertexNormalTexture[vertexCount];

		//final Vector3fc color = new Vector3f(1.0f, 1.0f, 1.0f);

		for (int i = 0; i < vertexCount; i++) {
			vertices[i] = new VertexNormalTexture(model.positions.get(i), model.normals.get(i), model.texCoords.get(i));
		}

		indicesTriangle = new int[model.indices.size()];

		for (int i = 0; i < indicesTriangle.length; i++) {
			indicesTriangle[i] = model.indices.get(i);
		}
		indicesLine = new int[model.indicesLines.size()];

		for (int i = 0; i < indicesLine.length; i++) {
			indicesLine[i] = model.indicesLines.get(i);
		}
		
	}

	private void memcpy(ByteBuffer buffer, VertexNormalTexture[] vertices) {
//		for (int i = 0; i < nbrepe; i++) {
//			for (int j = 0; j < nbrepeY; j++) {
		for (VertexNormalTexture vertex : vertices) {
			buffer.putFloat(vertex.pos.x());// + 2.0f * i);
			buffer.putFloat(vertex.pos.y());// + 2.0f * j);
			buffer.putFloat(vertex.pos.z());

			buffer.putFloat(vertex.normal.x());
			buffer.putFloat(vertex.normal.y());
			buffer.putFloat(vertex.normal.z());
			buffer.putFloat(vertex.texCoords.x());
			buffer.putFloat(vertex.texCoords.y());

		}
//			}
//		}

	}

	private void memcpy(ByteBuffer buffer, int[] indices) {

//		for (int i = 0; i < nbrepe*nbrepeY; i++) {
		for (int index : indices) {
			buffer.putInt(index);// + vertices.length * i);
//			}
		}

		buffer.rewind();
	}

	private void createTextureImageView() {
		textureImageView = createImageView(textureImage, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_ASPECT_COLOR_BIT, mipLevels);
	}

	private long createImageView(long image, int format, int aspectFlags, int mipLevels) {

		try (MemoryStack stack = stackPush()) {

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

			if (vkCreateImageView(device.device(), viewInfo, null, pImageView) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create texture image view");
			}

			return pImageView.get(0);
		}
	}

	private void createTextureSampler() {

		try (MemoryStack stack = stackPush()) {

			VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.calloc(stack);
			samplerInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
			samplerInfo.magFilter(VK_FILTER_LINEAR);
			samplerInfo.minFilter(VK_FILTER_LINEAR);
			samplerInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT);
			samplerInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT);
			samplerInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT);
			samplerInfo.anisotropyEnable(true);
			samplerInfo.maxAnisotropy(16.0f);
			samplerInfo.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK);
			samplerInfo.unnormalizedCoordinates(false);
			samplerInfo.compareEnable(false);
			samplerInfo.compareOp(VK_COMPARE_OP_ALWAYS);
			samplerInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
			samplerInfo.minLod(0); // Optional
			samplerInfo.maxLod((float) mipLevels);
			samplerInfo.mipLodBias(0); // Optional

			LongBuffer pTextureSampler = stack.mallocLong(1);

			if (vkCreateSampler(device.device(), samplerInfo, null, pTextureSampler) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create texture sampler");
			}

			textureSampler = pTextureSampler.get(0);
		}
	}

	private void createVertexBuffer() {
//		System.err.println("########### Vertex buffer");

		try (MemoryStack stack = stackPush()) {

			long bufferSize = VertexNormalTexture.SIZEOF * vertices.length;// * nbrepe*nbrepeY;

			LongBuffer pBuffer = stack.mallocLong(1);
			LongBuffer pBufferMemory = stack.mallocLong(1);
			createBuffer(bufferSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
					VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, pBuffer, pBufferMemory);

			long stagingBuffer = pBuffer.get(0);
			long stagingBufferMemory = pBufferMemory.get(0);

			PointerBuffer data = stack.mallocPointer(1);

			vkMapMemory(device.device(), stagingBufferMemory, 0, bufferSize, 0, data);
			{
				memcpy(data.getByteBuffer(0, (int) bufferSize), vertices);
			}
			vkUnmapMemory(device.device(), stagingBufferMemory);

			createBuffer(bufferSize, VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
					VK_MEMORY_HEAP_DEVICE_LOCAL_BIT, pBuffer, pBufferMemory);

			vertexBuffer = pBuffer.get(0);
			vertexBufferMemory = pBufferMemory.get(0);

			copyBuffer(stagingBuffer, vertexBuffer, bufferSize);

			vkDestroyBuffer(device.device(), stagingBuffer, null);
			vkFreeMemory(device.device(), stagingBufferMemory, null);
		}

	}

	private void copyBuffer(long srcBuffer, long dstBuffer, long size) {

		try (MemoryStack stack = stackPush()) {

			VkCommandBuffer commandBuffer = beginSingleTimeCommands();

			VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc(1, stack);
			copyRegion.size(size);

			vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, copyRegion);

			endSingleTimeCommands(commandBuffer);
		}
	}

	private void createIndexBuffer() {

		try (MemoryStack stack = stackPush()) {

			long bufferSize = Integer.BYTES * indicesTriangle.length;// * nbrepe*nbrepeY;
			if (bufferSize != 0) {
				LongBuffer pBuffer = stack.mallocLong(1);
				LongBuffer pBufferMemory = stack.mallocLong(1);
				createBuffer(bufferSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
						VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, pBuffer,
						pBufferMemory);

				long stagingBuffer = pBuffer.get(0);
				long stagingBufferMemory = pBufferMemory.get(0);

				PointerBuffer data = stack.mallocPointer(1);

				vkMapMemory(device.device(), stagingBufferMemory, 0, bufferSize, 0, data);
				{
					memcpy(data.getByteBuffer(0, (int) bufferSize), indicesTriangle);
				}
				vkUnmapMemory(device.device(), stagingBufferMemory);

				createBuffer(bufferSize, VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
						VK_MEMORY_HEAP_DEVICE_LOCAL_BIT, pBuffer, pBufferMemory);

				indexBufferTriangle = pBuffer.get(0);
				indexBufferMemoryTrianle = pBufferMemory.get(0);

				copyBuffer(stagingBuffer, indexBufferTriangle, bufferSize);

				vkDestroyBuffer(device.device(), stagingBuffer, null);
				vkFreeMemory(device.device(), stagingBufferMemory, null);
			}
		}
		try (MemoryStack stack = stackPush()) {

			long bufferSize = Integer.BYTES * indicesLine.length;// * nbrepe*nbrepeY;
			if (bufferSize != 0) {
				LongBuffer pBuffer = stack.mallocLong(1);
				LongBuffer pBufferMemory = stack.mallocLong(1);
				createBuffer(bufferSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
						VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, pBuffer,
						pBufferMemory);

				long stagingBuffer = pBuffer.get(0);
				long stagingBufferMemory = pBufferMemory.get(0);

				PointerBuffer data = stack.mallocPointer(1);

				vkMapMemory(device.device(), stagingBufferMemory, 0, bufferSize, 0, data);
				{
					memcpy(data.getByteBuffer(0, (int) bufferSize), indicesLine);
				}
				vkUnmapMemory(device.device(), stagingBufferMemory);

				createBuffer(bufferSize, VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
						VK_MEMORY_HEAP_DEVICE_LOCAL_BIT, pBuffer, pBufferMemory);

				indexBufferLine = pBuffer.get(0);
				indexBufferMemoryLine = pBufferMemory.get(0);

				copyBuffer(stagingBuffer, indexBufferLine, bufferSize);

				vkDestroyBuffer(device.device(), stagingBuffer, null);
				vkFreeMemory(device.device(), stagingBufferMemory, null);
			}
		}
	}

	private void createBuffer(long size, int usage, int properties, LongBuffer pBuffer, LongBuffer pBufferMemory) {

		try (MemoryStack stack = stackPush()) {

			VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.calloc(stack);
			bufferInfo.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
			bufferInfo.size(size);
			bufferInfo.usage(usage);
			bufferInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

			if (vkCreateBuffer(device.device(), bufferInfo, null, pBuffer) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create vertex buffer");
			}

			VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
			vkGetBufferMemoryRequirements(device.device(), pBuffer.get(0), memRequirements);

			VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack);
			allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
			allocInfo.allocationSize(memRequirements.size());
			allocInfo.memoryTypeIndex(findMemoryType(memRequirements.memoryTypeBits(), properties));

			if (vkAllocateMemory(device.device(), allocInfo, null, pBufferMemory) != VK_SUCCESS) {
				throw new RuntimeException("Failed to allocate vertex buffer memory");
			}

			vkBindBufferMemory(device.device(), pBuffer.get(0), pBufferMemory.get(0), 0);
		}
	}

	private void createDescriptorSetLayout() {

		try (MemoryStack stack = stackPush()) {

			VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc(2, stack);

			VkDescriptorSetLayoutBinding uboLayoutBinding = bindings.get(0);
			uboLayoutBinding.binding(0);
			uboLayoutBinding.descriptorCount(1);
			uboLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
			uboLayoutBinding.pImmutableSamplers(null);
			uboLayoutBinding.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);

			VkDescriptorSetLayoutBinding samplerLayoutBinding = bindings.get(1);
			samplerLayoutBinding.binding(1);
			samplerLayoutBinding.descriptorCount(1);
			samplerLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
			samplerLayoutBinding.pImmutableSamplers(null);
			samplerLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

			VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack);
			layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
			layoutInfo.pBindings(bindings);

			LongBuffer pDescriptorSetLayout = stack.mallocLong(1);

			if (vkCreateDescriptorSetLayout(device.device(), layoutInfo, null, pDescriptorSetLayout) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create descriptor set layout");
			}
			descriptorSetLayout = pDescriptorSetLayout.get(0);
		}
	}
	@Override
	public void createColorDepthResources(GestionSwapChain swapChain) {
		this.swapChain = swapChain;
		createColorResources();
		createDepthResources();

	}

	private void createColorResources() {

		try (MemoryStack stack = stackPush()) {

			LongBuffer pColorImage = stack.mallocLong(1);
			LongBuffer pColorImageMemory = stack.mallocLong(1);

			createImage(swapChain.swapChainExtent().width(), swapChain.swapChainExtent().height(), 1,
					physicalDevice.msaaSamples(), swapChain.swapChainImageFormat(), VK_IMAGE_TILING_OPTIMAL,
					VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT | VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
					VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, pColorImage, pColorImageMemory);

			colorImage = pColorImage.get(0);
			colorImageMemory = pColorImageMemory.get(0);

			colorImageView = createImageView(colorImage, swapChain.swapChainImageFormat(), VK_IMAGE_ASPECT_COLOR_BIT,
					1);

			transitionImageLayout(colorImage, swapChain.swapChainImageFormat(), VK_IMAGE_LAYOUT_UNDEFINED,
					VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, 1);
		}
	}

	private int findDepthFormat() {
		return findSupportedFormat(
				stackGet().ints(VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT),
				VK_IMAGE_TILING_OPTIMAL, VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT);
	}

	private int findSupportedFormat(IntBuffer formatCandidates, int tiling, int features) {

		try (MemoryStack stack = stackPush()) {

			VkFormatProperties props = VkFormatProperties.calloc(stack);

			for (int i = 0; i < formatCandidates.capacity(); ++i) {

				int format = formatCandidates.get(i);

				vkGetPhysicalDeviceFormatProperties(physicalDevice.physicalDevice(), format, props);

				if (tiling == VK_IMAGE_TILING_LINEAR && (props.linearTilingFeatures() & features) == features) {
					return format;
				} else if (tiling == VK_IMAGE_TILING_OPTIMAL
						&& (props.optimalTilingFeatures() & features) == features) {
					return format;
				}

			}
		}

		throw new RuntimeException("Failed to find supported format");
	}

	private void createDepthResources() {

		try (MemoryStack stack = stackPush()) {

			int depthFormat = findDepthFormat();

			LongBuffer pDepthImage = stack.mallocLong(1);
			LongBuffer pDepthImageMemory = stack.mallocLong(1);

			createImage(swapChain.swapChainExtent().width(), swapChain.swapChainExtent().height(), 1,
					physicalDevice.msaaSamples(), depthFormat, VK_IMAGE_TILING_OPTIMAL,
					VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, pDepthImage,
					pDepthImageMemory);

			depthImage = pDepthImage.get(0);
			depthImageMemory = pDepthImageMemory.get(0);

			depthImageView = createImageView(depthImage, depthFormat, VK_IMAGE_ASPECT_DEPTH_BIT, 1);

			// Explicitly transitioning the depth image
			transitionImageLayout(depthImage, depthFormat, VK_IMAGE_LAYOUT_UNDEFINED,
					VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, 1);

		}
	}
	@Override
	public long vertexBuffer() {
		return vertexBuffer;
	}
	@Override
	public long indexBufferLine() {
		return indexBufferLine;
	}
	@Override
	public long indexBufferTriangle() {
		return indexBufferTriangle;
	}
	@Override
	public int indicesTriangleLength() {
		return indicesTriangle.length;// * nbrepe * nbrepeY;
	}
	@Override
	public int indicesLineLength() {
		return indicesLine.length;// * nbrepe * nbrepeY;
	}

	

	private void createDescriptorPool() {

		try (MemoryStack stack = stackPush()) {

			VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.calloc(2, stack);

			VkDescriptorPoolSize uniformBufferPoolSize = poolSizes.get(0);
			uniformBufferPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
			uniformBufferPoolSize.descriptorCount(swapChain.swapChainImages().size());

			VkDescriptorPoolSize textureSamplerPoolSize = poolSizes.get(1);
			textureSamplerPoolSize.type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
			textureSamplerPoolSize.descriptorCount(swapChain.swapChainImages().size());

			VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.calloc(stack);
			poolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
			poolInfo.pPoolSizes(poolSizes);
			poolInfo.maxSets(swapChain.swapChainImages().size());

			LongBuffer pDescriptorPool = stack.mallocLong(1);

			if (vkCreateDescriptorPool(device.device(), poolInfo, null, pDescriptorPool) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create descriptor pool");
			}

			descriptorPool = pDescriptorPool.get(0);
		}
	}

	private void createDescriptorSets(GestionBufferUniforme uniformBuffers) {

		try (MemoryStack stack = stackPush()) {

			LongBuffer layouts = stack.mallocLong(swapChain.swapChainImages().size());
			for (int i = 0; i < layouts.capacity(); i++) {
				layouts.put(i, descriptorSetLayout);
			}

			VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.calloc(stack);
			allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
			allocInfo.descriptorPool(descriptorPool);
			allocInfo.pSetLayouts(layouts);

			LongBuffer pDescriptorSets = stack.mallocLong(swapChain.swapChainImages().size());

			if (vkAllocateDescriptorSets(device.device(), allocInfo, pDescriptorSets) != VK_SUCCESS) {
				throw new RuntimeException("Failed to allocate descriptor sets");
			}

			descriptorSets = new ArrayList<>(pDescriptorSets.capacity());

			VkDescriptorBufferInfo.Buffer bufferInfo = VkDescriptorBufferInfo.calloc(1, stack);
			bufferInfo.offset(0);
			bufferInfo.range(UniformModelVueProj.SIZEOF);

			VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc(1, stack);
			imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			imageInfo.imageView(textureImageView);
			imageInfo.sampler(textureSampler);

			VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.calloc(2, stack);

			VkWriteDescriptorSet uboDescriptorWrite = descriptorWrites.get(0);
			uboDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
			uboDescriptorWrite.dstBinding(0);
			uboDescriptorWrite.dstArrayElement(0);
			uboDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
			uboDescriptorWrite.descriptorCount(1);
			uboDescriptorWrite.pBufferInfo(bufferInfo);

			VkWriteDescriptorSet samplerDescriptorWrite = descriptorWrites.get(1);
			samplerDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
			samplerDescriptorWrite.dstBinding(1);
			samplerDescriptorWrite.dstArrayElement(0);
			samplerDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
			samplerDescriptorWrite.descriptorCount(1);
			samplerDescriptorWrite.pImageInfo(imageInfo);

			for (int i = 0; i < pDescriptorSets.capacity(); i++) {

				long descriptorSet = pDescriptorSets.get(i);

				bufferInfo.buffer(uniformBuffers.get(i));

				uboDescriptorWrite.dstSet(descriptorSet);
				samplerDescriptorWrite.dstSet(descriptorSet);

				vkUpdateDescriptorSets(device.device(), descriptorWrites, null);

				descriptorSets.add(descriptorSet);
			}
		}
	}
	@Override
	public void createUniformBuffersDescriptorPoolSets(GestionBufferUniforme uniformBuffers) {
		createDescriptorPool();
		createDescriptorSets(uniformBuffers);

	}
	@Override
	public void detruit() {
		vkDestroySampler(device.device(), textureSampler, null);
		vkDestroyImageView(device.device(), textureImageView, null);
		vkDestroyImage(device.device(), textureImage, null);
		vkFreeMemory(device.device(), textureImageMemory, null);

		vkDestroyDescriptorSetLayout(device.device(), descriptorSetLayout, null);

		vkDestroyBuffer(device.device(), indexBufferLine, null);
		vkFreeMemory(device.device(), indexBufferMemoryLine, null);
		vkDestroyBuffer(device.device(), indexBufferTriangle, null);
		vkFreeMemory(device.device(), indexBufferMemoryTrianle, null);

		vkDestroyBuffer(device.device(), vertexBuffer, null);
		vkFreeMemory(device.device(), vertexBufferMemory, null);
	}
	@Override
	public void cleanupSwapChain() {
		vkDestroyImageView(device.device(), colorImageView, null);
		vkDestroyImage(device.device(), colorImage, null);
		vkFreeMemory(device.device(), colorImageMemory, null);

		vkDestroyImageView(device.device(), depthImageView, null);
		vkDestroyImage(device.device(), depthImage, null);
		vkFreeMemory(device.device(), depthImageMemory, null);
		vkDestroyDescriptorPool(device.device(), descriptorPool, null);
	}
	@Override
	public List<Long> descriptorSets() {
		return descriptorSets;
	}
	@Override
	public long colorImageView() {
		return colorImageView;
	}
	@Override
	public long depthImageView() {
		return depthImageView;
	}
	@Override
	public long descriptorSetLayout() {
		return descriptorSetLayout;
	}
	

}
