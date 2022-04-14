package oliv.lib.vulkan.apiGraphique;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.lwjgl.system.Platform;
import org.lwjgl.vulkan.VkPhysicalDevice;

import oliv.lib.vulkan.ProcedureVulkan;
import oliv.lib.vulkan.apiDonnee.LecteurMaillageNonStructure;
import oliv.lib.vulkan.objet.VKData;
import oliv.lib.vulkan.platform.PlatformVKCanvas;

public class CanvasVulkanSWT extends Canvas {
	static PlatformVKCanvas platformCanvas =PlatformVKCanvas.get();
   
    /**
     * The Vulkan surface handle for this {@link VKCanvas}.
     */
    public long surface;
	public ProcedureVulkan pr;
	boolean detruit=false;
	public void setDetruit(boolean detruit) {
		this.detruit = detruit;
		pr.cleanup();
	}

    /**
     * Create a {@link VKCanvas} widget using the attributes described in the supplied {@link VKData} object.
     *
     * @param parent
     *            a parent composite widget
     * @param style
     *            the bitwise OR'ing of widget styles
     * @param data
     *            the necessary data to create a VKCanvas
     */
	public CanvasVulkanSWT(Composite parent) {
		super(parent, platformCanvas.checkStyle(parent,  SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE));
        if (Platform.get() == Platform.WINDOWS) {
            platformCanvas.resetStyle(parent);
        }
        pr = new ProcedureVulkan();
        if (pr.data == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        surface = platformCanvas.create(this, pr.data);
	}
	public CanvasVulkanSWT(Composite parent, int checkStyle) {
		super(parent,checkStyle);
 	}
	public CanvasVulkanSWT(Composite parent, int style, VKData data) {
		super(parent, platformCanvas.checkStyle(parent,platformCanvas.checkStyle(parent, style)));
        if (Platform.get() == Platform.WINDOWS) {
            platformCanvas.resetStyle(parent);
        }
        pr = new ProcedureVulkan();
        if (pr.data == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        surface = platformCanvas.create(this, pr.data);
	}
	
	/**
     * Determine whether there is presentation support for the given {@link VkPhysicalDevice} in a command queue of the specified
     * <code>queueFamiliy</code>.
     * 
     * @param physicalDevice
     *            the Vulkan {@link VkPhysicalDevice}
     * @param queueFamily
     *            the command queue family
     * @return <code>true</code> of <code>false</code>
     */
    boolean getPhysicalDevicePresentationSupport(VkPhysicalDevice physicalDevice, int queueFamily) {
        return platformCanvas.getPhysicalDevicePresentationSupport(physicalDevice, queueFamily);
    }
	public void lance3D(LecteurMaillageNonStructure modele) {

		Display.getCurrent().asyncExec(new Runnable() {
			
			@Override
			public void run() {

				if(CanvasVulkanSWT.this.isDisposed()) 
					return;
				pr.phase2(surface,CanvasVulkanSWT.this,modele);
				System.out.println("phase 2");
					pr.drawFrame();
				Display.getCurrent().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						if(!CanvasVulkanSWT.this.isDisposed()) {
							pr.drawFrame();
							Display.getCurrent().asyncExec(this);
						}else {
							//pr.cleanup();
						}
					} 
				});
			} 
		});
		
		
		
	}
	@Override
	public void dispose() {
		super.dispose();
		pr.cleanup();
	}

	

	
}
