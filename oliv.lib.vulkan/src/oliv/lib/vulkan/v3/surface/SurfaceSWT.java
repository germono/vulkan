package oliv.lib.vulkan.v3.surface;

import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwTerminate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import oliv.lib.vulkan.apiGraphique.CanvasVulkanSWT;
import oliv.lib.vulkan.apiGraphique.InteractionHumaine;
import oliv.lib.vulkan.v2.instance.GestionInstance;

public class SurfaceSWT  implements GestionSurface {
	CanvasVulkanSWT canvas;
	long surface;
	long window;
	GestionInstance instance;
	boolean framebufferResize;
	Shell shell;
	Display display;
	int WIDTH;
	int HEIGHT;

	@Override
	public long surface() {
		return surface;
	}

	@Override
	public long window() {
		return window;
	}

	@Override
	public void detruitWindow() {
		glfwDestroyWindow(window);
		glfwTerminate();
	}

	@Override
	public boolean framebufferResize() {
		return framebufferResize;
	}

	@Override
	public void setFramebufferResize(boolean b) {
		framebufferResize = b;
	}

	@Override
	public void initWindow(int WIDTH, int HEIGHT) {
		// Create SWT Display, Shell and VKCanvas
		this.WIDTH = WIDTH;
		this.HEIGHT = HEIGHT;
		display = new Display();
		shell = new Shell(display, SWT.SHELL_TRIM | SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE);
		shell.setLayout(new FillLayout());
		shell.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event event) {
				switch (event.detail) {
				case SWT.TRAVERSE_ESCAPE:
					shell.close();
					event.detail = SWT.TRAVERSE_NONE;
					event.doit = false;
					break;
				}
			}
		});

	}

	@Override
	public boolean fenetreVeuxFermee() {
		return shell.isDisposed();
	}

	@Override
	public void poll() {
		while (display.readAndDispatch())
			;
	}

	@Override
	public void recreate() {
	}

	@Override
	public void cree(GestionInstance instance) {
		this.instance = instance;
		// <- set Vulkan instance
		canvas = new CanvasVulkanSWT(shell, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE, instance.data());
		surface = canvas.surface;

		canvas.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				if (canvas.getSize().x <= 0 || canvas.getSize().y <= 0)
					return;
				framebufferResize = true;
			}
		});

		int dw = shell.getSize().x - shell.getClientArea().width;
		int dh = shell.getSize().y - shell.getClientArea().height;
		shell.setSize(WIDTH + dw, HEIGHT + dh);
		shell.open();
	}
	@Override
	public void cree(GestionInstance instance, CanvasVulkanSWT can) {
		this.instance = instance;
		// <- set Vulkan instance
		canvas = can;
		surface = canvas.surface;

		canvas.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				if (canvas.getSize().x <= 0 || canvas.getSize().y <= 0)
					return;
				framebufferResize = true;
			}
		});

	}

	public void ajouteInteractionHumain(InteractionHumaine s) {
		canvas.addMouseListener(s);
		canvas.addMouseMoveListener(s);
		canvas.addMouseWheelListener(s);
		canvas.addKeyListener(s);
	}

	

	record Position(int x, int y) {};
	record Zoom(int x, int y,int zoom) {};

	@Override
	public void detruit() {
		shell.dispose();
		display.dispose();
	}

}



