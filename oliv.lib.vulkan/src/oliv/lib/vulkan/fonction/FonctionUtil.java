package oliv.lib.vulkan.fonction;

import static org.lwjgl.system.MemoryStack.stackGet;

import java.util.Collection;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;


public interface FonctionUtil {
    default PointerBuffer asPointerBuffer(Collection<String> collection) {

        MemoryStack stack = stackGet();

        PointerBuffer buffer = stack.mallocPointer(collection.size());

        collection.stream()
                .map(stack::UTF8)
                .forEach(buffer::put);

        return buffer.rewind();
    }
    default PointerBuffer asPointerBuffer(List<? extends Pointer> list) {
    	if (list==null)
    			return null;
        MemoryStack stack = stackGet();

        PointerBuffer buffer = stack.mallocPointer(list.size());

        list.forEach(buffer::put);

        return buffer.rewind();
    }
    
   
}
