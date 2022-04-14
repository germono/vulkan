package oliv.lib.vulkan.objet.noeud;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

public class VertexNormalTexture {

    public static final int SIZEOF = (3 + 3 + 2) * Float.BYTES;
    static final int OFFSETOF_POS = 0;
    static final int OFFSETOF_NORMAL = 3 * Float.BYTES;
    static final int OFFSETOF_TEXTCOORDS = (3 + 3) * Float.BYTES;

    public Vector3fc pos;
    public Vector3fc normal;
    public Vector2fc texCoords;

    public VertexNormalTexture(Vector3fc pos, Vector3fc normal, Vector2fc texCoords) {
        this.pos = pos;
        this.normal = normal;
        this.texCoords = texCoords;
    }

    public static VkVertexInputBindingDescription.Buffer getBindingDescription() {

        VkVertexInputBindingDescription.Buffer bindingDescription =
                VkVertexInputBindingDescription.calloc(1);

        bindingDescription.binding(0);
        bindingDescription.stride(VertexNormalTexture.SIZEOF);
        bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

        return bindingDescription;
    }

    public  static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {

        VkVertexInputAttributeDescription.Buffer attributeDescriptions =
                VkVertexInputAttributeDescription.calloc(3);

        // Position
        VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
        posDescription.binding(0);
        posDescription.location(0);
        posDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
        posDescription.offset(OFFSETOF_POS);

        // Normal
        VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
        colorDescription.binding(0);
        colorDescription.location(1);
        colorDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
        colorDescription.offset(OFFSETOF_NORMAL);

        // Texture coordinates
        VkVertexInputAttributeDescription texCoordsDescription = attributeDescriptions.get(2);
        texCoordsDescription.binding(0);
        texCoordsDescription.location(2);
        texCoordsDescription.format(VK_FORMAT_R32G32_SFLOAT);
        texCoordsDescription.offset(OFFSETOF_TEXTCOORDS);

        return attributeDescriptions.rewind();
    }
}
