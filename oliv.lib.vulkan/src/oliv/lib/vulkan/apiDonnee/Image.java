package oliv.lib.vulkan.apiDonnee;

import java.nio.ByteBuffer;

public record Image(ByteBuffer image, int largeur, int hauteur,int channel) {

}
