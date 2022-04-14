package oliv.lib.vulkan.objet.noeud;

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SFLOAT;


public class Vertex {


    public static final int SIZEOF = (3 ) * Float.BYTES;
    static final int OFFSETOF_POS = 0;

    public Vector3fc pos;

    public Vertex(Vector3fc pos) {
        this.pos = pos;
    }

    public static VkVertexInputBindingDescription.Buffer getBindingDescription() {

        VkVertexInputBindingDescription.Buffer bindingDescription =
                VkVertexInputBindingDescription.calloc(1);

        bindingDescription.binding(0);
        bindingDescription.stride(Vertex.SIZEOF);
        bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

        return bindingDescription;
    }

    public  static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {

        VkVertexInputAttributeDescription.Buffer attributeDescriptions =
                VkVertexInputAttributeDescription.calloc(1);

        // Position
        VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
        posDescription.binding(0);
        posDescription.location(0);
        posDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
        posDescription.offset(OFFSETOF_POS);


        return attributeDescriptions.rewind();
    }
  
}
