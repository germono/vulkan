package oliv.lib.vulkan.apiDonnee;



import static java.lang.ClassLoader.getSystemClassLoader;
import static org.lwjgl.stb.STBImage.STBI_rgb_alpha;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;

import org.lwjgl.system.MemoryStack;

public class BufferBinaire {
	public static Image importString(String chemin) {
		System.out.println(chemin);
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);
			IntBuffer pChannels = stack.mallocInt(1);
			ByteBuffer pixels = stbi_load(chemin, pWidth, pHeight, pChannels, STBI_rgb_alpha);
			int largeur = pWidth.get(0);
			int hauteur = pHeight.get(0);
			if(largeur==0||hauteur==0) {
				System.err.println("Fichier non trouvé : "+chemin);
				return null;
			}
				
			return new Image(pixels, pWidth.get(), pHeight.get(),pChannels.get());
		}
	}
	public static String cheminRelatif(String chemin) {
		try {
			URL che = getSystemClassLoader().getResource(chemin);
			if (che==null)
				throw new URISyntaxException(chemin,chemin);
			return Paths
			.get(new URI(getSystemClassLoader().getResource(chemin).toExternalForm()))
			.toString();
		} catch (URISyntaxException e) {
			System.err.println("Fichier non trouvé "+chemin);
			return null;
		}
	}
	public static void main(String[] args) {
		importString(cheminRelatif("test/suzane.png"));
	}

}
