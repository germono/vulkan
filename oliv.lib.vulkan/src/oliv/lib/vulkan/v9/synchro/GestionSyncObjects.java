package oliv.lib.vulkan.v9.synchro;

import java.util.List;
import java.util.Map;

import oliv.lib.vulkan.objet.Frame;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;
import oliv.lib.vulkan.v8.s1.swapChain.GestionSwapChain;

public interface GestionSyncObjects {
	void cree(GestionSwapChain swapChain,GestionDevice device,int MAX_FRAMES_IN_FLIGHT);

	List<Frame> inFlightFrames();
	Map<Integer, Frame> imagesInFlight();
	void detruit();
}
