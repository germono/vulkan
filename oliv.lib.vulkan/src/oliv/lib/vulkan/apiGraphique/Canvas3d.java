package oliv.lib.vulkan.apiGraphique;



import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;


import oliv.lib.vulkan.apiDonnee.LecteurMaillageNonStructure;

public class Canvas3d extends Composite{

	private CanvasVulkanSWT canvas;

	public Canvas3d(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new FillLayout());
		canvas=new CanvasVulkanSWT(this);
		canvas.lance3D(null);		
	}

	public void chargeModele(LecteurMaillageNonStructure modele) {
		//canvas.setDetruit(true);
		Point p=canvas.getSize();
		canvas.dispose();
		canvas=new CanvasVulkanSWT(this);
		canvas.setSize(p);
		this.redraw();
		canvas.lance3D(modele);
	}



}
