package oliv.lib.vulkan.platform;

import org.eclipse.swt.widgets.Composite;
import org.lwjgl.system.Platform;
import org.lwjgl.vulkan.VkPhysicalDevice;

import oliv.lib.vulkan.objet.VKData;

public interface PlatformVKCanvas {

	int checkStyle(Composite parent, int style);

	void resetStyle(Composite parent);

	long create(Composite composite, VKData data);

	boolean getPhysicalDevicePresentationSupport(VkPhysicalDevice physicalDevice, int queueFamily);

	static PlatformVKCanvas get() {
		return switch (Platform.get()) {
		case WINDOWS -> new PlatformWin32VKCanvas();
		case LINUX -> throw new UnsupportedOperationException("Unimplemented case: " + Platform.get());
		case MACOSX -> throw new UnsupportedOperationException("Unimplemented case: " + Platform.get());
		default -> throw new IllegalArgumentException("Unexpected value: " + Platform.get());

		};
	}
}
