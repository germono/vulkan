package oliv.lib.vulkan.apiDonnee;



import java.nio.ByteBuffer;

public record Texture(ByteBuffer image, int largeur, int hauteur) {

}