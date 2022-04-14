package oliv.lib.vulkan.apiDonnee;

import static java.lang.ClassLoader.getSystemClassLoader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class ChargementFichierE4 {
//	public static File charge(String nomBundle, String cheminFichier) {
//		Bundle bundle = Platform.getBundle(nomBundle);
//		URL fileURL = bundle.getEntry(cheminFichier);
//		File file = null;
//		try {
//		    file = new File(FileLocator.resolve(fileURL).toURI());
//		    return file;
//		} catch (URISyntaxException e1) {
//		    e1.printStackTrace();
//		} catch (IOException e1) {
//		    e1.printStackTrace();
//		}
//		return null;
//	}
	public static String chargeURL(Class<?> dossiers, String cheminFichier) {
		try {
			return new String(Files.readAllBytes(
					Paths.get(new URI(getSystemClassLoader().getResource(cheminFichier).toExternalForm()))));
		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
