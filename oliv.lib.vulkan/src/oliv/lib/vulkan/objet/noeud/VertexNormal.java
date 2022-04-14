package oliv.lib.vulkan.objet.noeud;

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SFLOAT;


public class VertexNormal {


    public static final int SIZEOF = (3 + 3 ) * Float.BYTES;
    static final int OFFSETOF_POS = 0;
    static final int OFFSETOF_NORMAL = 3 * Float.BYTES;

    public Vector3fc pos;
    public Vector3fc normal;

    public VertexNormal(Vector3fc pos, Vector3fc normal) {
        this.pos = pos;
        this.normal = normal;
    }

    public static VkVertexInputBindingDescription.Buffer getBindingDescription() {

        VkVertexInputBindingDescription.Buffer bindingDescription =
                VkVertexInputBindingDescription.calloc(1);

        bindingDescription.binding(0);
        bindingDescription.stride(VertexNormal.SIZEOF);
        bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

        return bindingDescription;
    }

    public  static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {

        VkVertexInputAttributeDescription.Buffer attributeDescriptions =
                VkVertexInputAttributeDescription.calloc(2);

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

        return attributeDescriptions.rewind();
    }
}
