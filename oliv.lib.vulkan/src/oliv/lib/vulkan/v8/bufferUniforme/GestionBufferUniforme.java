package oliv.lib.vulkan.v8.bufferUniforme;

import org.eclipse.swt.events.MouseEvent;

import oliv.lib.vulkan.apiGraphique.InteractionHumaine;
import oliv.lib.vulkan.v4.devicePhysique.GestionPhysicalDevice;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;
import oliv.lib.vulkan.v8.s1.swapChain.GestionSwapChain;

public interface GestionBufferUniforme {
	
	public void cree(GestionDevice device,
			GestionSwapChain swapChain,GestionPhysicalDevice physicalDevice);
	public void detruit();
	public void updateUniformBuffer(int currentImage);
	public void creeSwanp();
	public void netoyeSwanp() ;
	public void add(long l);
	public void onScroll(MouseEvent e);
	public void zpress(boolean b);
	public void ypress(boolean b);
	public void xpress(boolean b);
	public void basculeProjetction();
	public void viewEnd();
	public void dragEnd();
	public void viewBegin();
	public void dragBegin();
	public void rotReset();
	public void transReset();
	public void onMouseMove(MouseEvent e);
	public void autoCentre();
	public void c_setRotation(int i, int j, int k);
	public void createUniformBuffers();
	public void cleanupSwapChain();
	public long get(int i);

	
}
