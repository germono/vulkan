package oliv.lib.vulkan.v3.surface;



import oliv.lib.vulkan.apiGraphique.CanvasVulkanSWT;
import oliv.lib.vulkan.apiGraphique.InteractionHumaine;
import oliv.lib.vulkan.v2.instance.GestionInstance;


public interface GestionSurface {
	void initWindow(int WIDTH, int HEIGHT);

	void cree(GestionInstance instance);
	void cree(GestionInstance instance,CanvasVulkanSWT can);

	long surface();

	long window();

	void detruit();

	void detruitWindow();

	void recreate();

	boolean fenetreVeuxFermee();

	boolean framebufferResize();

	void setFramebufferResize(boolean b);

	void poll();
	public void ajouteInteractionHumain(InteractionHumaine s);
}
