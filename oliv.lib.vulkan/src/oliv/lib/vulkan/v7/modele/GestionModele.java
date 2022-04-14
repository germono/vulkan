package oliv.lib.vulkan.v7.modele;

import java.util.List;

import oliv.lib.vulkan.apiDonnee.LecteurMaillageNonStructure;
import oliv.lib.vulkan.apiDonnee.Texture;
import oliv.lib.vulkan.apiGraphique.InteractionHumaine;
import oliv.lib.vulkan.v4.devicePhysique.GestionPhysicalDevice;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;
import oliv.lib.vulkan.v6.commandPool.GestionCommandPool;
import oliv.lib.vulkan.v8.bufferUniforme.GestionBufferUniforme;
import oliv.lib.vulkan.v8.s1.swapChain.GestionSwapChain;

public interface GestionModele {

	long descriptorSetLayout();

	long depthImageView();

	long colorImageView();

	long vertexBuffer();

	int indicesTriangleLength();

	long indexBufferTriangle();

	List<Long> descriptorSets();

	int indicesLineLength();

	long indexBufferLine();

	default void cree(GestionDevice device, GestionPhysicalDevice physicalDevice, GestionCommandPool commandPool,
			Texture lectureTexture, LecteurMaillageNonStructure modele) {

		creeTexture(device,physicalDevice,commandPool,modele.lectureTexture());
		model(modele);
		buffer();
		description();
	}

	void description();

	void buffer();

	void model(LecteurMaillageNonStructure modele);

	void creeTexture(GestionDevice device, GestionPhysicalDevice physicalDevice, GestionCommandPool commandPool,
			Texture lectureTexture);

	void createColorDepthResources(GestionSwapChain swapChain);

	void createUniformBuffersDescriptorPoolSets(GestionBufferUniforme uniformBuffers);


	void detruit();

	void cleanupSwapChain();


}
