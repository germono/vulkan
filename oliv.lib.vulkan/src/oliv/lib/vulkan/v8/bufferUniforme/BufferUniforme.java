package oliv.lib.vulkan.v8.bufferUniforme;

import static oliv.lib.vulkan.fonction.AlignmentUtils.alignas;
import static oliv.lib.vulkan.fonction.AlignmentUtils.alignof;
import static org.joml.Intersectionf.intersectRayPlane;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindBufferMemory;
import static org.lwjgl.vulkan.VK10.vkCreateBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetBufferMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.MouseEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4x3f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import oliv.lib.vulkan.apiGraphique.InteractionHumaine;
import oliv.lib.vulkan.objet.UniformModelVueProj;
import oliv.lib.vulkan.v4.devicePhysique.GestionPhysicalDevice;
import oliv.lib.vulkan.v5.deviceLogique.GestionDevice;
import oliv.lib.vulkan.v8.s1.swapChain.GestionSwapChain;

public class BufferUniforme  implements GestionBufferUniforme{
	GestionDevice device;
	GestionSwapChain swapChain;
	GestionPhysicalDevice physicalDevice;
    private List<Long> uniformBuffersMemory;
	private List<Long> uniformBuffers;
	

	@Override
	public void cree(GestionDevice device,
			GestionSwapChain swapChain,GestionPhysicalDevice physicalDevice) {
		this.device=device;
		this.swapChain=swapChain;
		this.physicalDevice=physicalDevice;
	}
	@Override
	public void creeSwanp() {
		uniformBuffersMemory = new ArrayList<>(swapChain.swapChainImages().size());
	}
	@Override
	public void netoyeSwanp() {
		uniformBuffersMemory.forEach(uboMemory -> vkFreeMemory(device.device(), uboMemory, null));

	}
	@Override
	public long get(int i) {
		return uniformBuffers.get(i);
	}
	@Override
	public void createUniformBuffers() {

		try (MemoryStack stack = stackPush()) {

			uniformBuffers = new ArrayList<>(swapChain.swapChainImages().size());
			this.creeSwanp();

			LongBuffer pBuffer = stack.mallocLong(1);
			LongBuffer pBufferMemory = stack.mallocLong(1);

			for (int i = 0; i < swapChain.swapChainImages().size(); i++) {
				createBuffer(UniformModelVueProj.SIZEOF, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
						VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, pBuffer,
						pBufferMemory);

				uniformBuffers.add(pBuffer.get(0));
				this.add(pBufferMemory.get(0));
			}

		}
	}
	@Override
	public void cleanupSwapChain() {
		uniformBuffers.forEach(ubo -> vkDestroyBuffer(device.device(), ubo, null));
		this.netoyeSwanp();
	}
	private void createBuffer(long size, int usage, int properties, LongBuffer pBuffer, LongBuffer pBufferMemory) {

		try (MemoryStack stack = stackPush()) {

			VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.calloc(stack);
			bufferInfo.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
			bufferInfo.size(size);
			bufferInfo.usage(usage);
			bufferInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

			if (vkCreateBuffer(device.device(), bufferInfo, null, pBuffer) != VK_SUCCESS) {
				throw new RuntimeException("Failed to create vertex buffer");
			}

			VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
			vkGetBufferMemoryRequirements(device.device(), pBuffer.get(0), memRequirements);

			VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack);
			allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
			allocInfo.allocationSize(memRequirements.size());
			allocInfo.memoryTypeIndex(findMemoryType(memRequirements.memoryTypeBits(), properties));

			if (vkAllocateMemory(device.device(), allocInfo, null, pBufferMemory) != VK_SUCCESS) {
				throw new RuntimeException("Failed to allocate vertex buffer memory");
			}

			vkBindBufferMemory(device.device(), pBuffer.get(0), pBufferMemory.get(0), 0);
		}
	}
	private int findMemoryType(int typeFilter, int properties) {

		VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.malloc();
		vkGetPhysicalDeviceMemoryProperties(physicalDevice.physicalDevice(), memProperties);

		for (int i = 0; i < memProperties.memoryTypeCount(); i++) {
			if ((typeFilter & (1 << i)) != 0
					&& (memProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
				return i;
			}
		}

		throw new RuntimeException("Failed to find suitable memory type");
	}
	@Override
	public void detruit() {
		
	}
	@Override
	public void updateUniformBuffer(int currentImage) {

            try(MemoryStack stack = stackPush()) {

                UniformModelVueProj ubo = new UniformModelVueProj();
                ubo.model.translate(0.0f, 0.0f,0.0f);
                ubo.view
                .translate(0, 0, -radius)
                .rotateX(xAngle)
                .rotateY(yAngle)
                .rotateZ(zAngle)
                .translate(translation)
                ;
                float DebutDuChamp=0.1f;
                float FinDuChamp=1000.0f;
                double angle=Math.toRadians(35.7538);
                float aspecRatio=(float)(swapChain.swapChainExtent().width()*1.0 
                        / swapChain.swapChainExtent().height());
                float x=(float) (Math.tan(angle)*radius*0.75);
                if(ortho)
                	ubo.proj.ortho(-x,x, -x/aspecRatio, x/aspecRatio, -1000, 1000,true);
                else
                	ubo.proj.perspective((float) angle,aspecRatio, DebutDuChamp, FinDuChamp);
                ubo.proj.m11(ubo.proj.m11() * -1);
                ubo.proj.mul(ubo.view,vpMat);

                PointerBuffer data = stack.mallocPointer(1);
                vkMapMemory(device.device(), uniformBuffersMemory.get(currentImage), 0, UniformModelVueProj.SIZEOF, 0, data);
                {
                    memcpy(data.getByteBuffer(0, UniformModelVueProj.SIZEOF), ubo);
                }
                vkUnmapMemory(device.device(), uniformBuffersMemory.get(currentImage));
            }
        }
	private void memcpy(ByteBuffer buffer, UniformModelVueProj ubo) {

		final int mat4Size = 16 * Float.BYTES;

		ubo.model.get(0, buffer);
		ubo.view.get(alignas(mat4Size, alignof(ubo.view)), buffer);
		ubo.proj.get(alignas(mat4Size * 2, alignof(ubo.view)), buffer);
	}
	boolean ortho=true;
	@Override
	public void basculeProjetction() {
		ortho=!ortho;
	}
	@Override
	public void autoCentre() {
		
	}
	
    private int mouseX, mouseY;
    private boolean dragging, viewing;
    private Vector3f dragStartWorldPos = new Vector3f();
    private Vector3f dragCamNormal = new Vector3f();
    private Vector3f dragRayOrigin = new Vector3f();
    private Vector3f dragRayDir = new Vector3f();
    private Vector3f translation = new Vector3f();
    private final Matrix4x3f vMat = new Matrix4x3f();
    private final Matrix4f vpMat = new Matrix4f();
    private float xAngle = (float)-Math.toRadians(80), 
    		yAngle =(float)-Math.toRadians(0), 
    	    		zAngle =(float)-Math.toRadians(-120), 
    		radius = 3;
    boolean xpress,ypress,zpress;
	
    private int width,height;
    @Override
	public void onMouseMove(MouseEvent e) {
	        if (dragging)
	            drag(e.x, e.y);
	        else if (viewing)
	            rotate(e.x, e.y);
	        mouseX = e.x;
	        mouseY = e.y;
	    }
    @Override
	  public   void dragBegin() {
	        dragging = true;
	        width= swapChain.swapChainExtent().width();
	        height= swapChain.swapChainExtent().height();
	        // find "picked" point on the grid
	        vpMat.unprojectRay(mouseX,mouseY, new int[] {0, 0, width, height}, dragRayOrigin, dragRayDir);
//	        float t = intersectRayPlane(dragRayOrigin, dragRayDir, new Vector3f(), new Vector3f(0, dragRayOrigin.y > 0 ? 1 : -1, 0), 1E-5f);
	        float t = intersectRayPlane(dragRayOrigin, dragRayDir, new Vector3f(), new Vector3f(0, 1, 0), 1E-5f);
//	        System.out.println("Depart "+t+" "+(dragRayOrigin.y > 0));
	        dragStartWorldPos.set(dragRayDir).mul(t).add(dragRayOrigin);
	        vMat.positiveZ(dragCamNormal);
	    }
    @Override
	     public void transReset() {
			translation.mul(0.0f);
		}
    @Override
		public void rotReset() {
	    	 xAngle=0;
	    	 yAngle=0;
	    	 zAngle=0;
		}
	@Override
	public void c_setRotation(int xAngle, int yAngle, int zAngle) {
		 this.xAngle=(float)Math.toRadians(xAngle);
		 this.yAngle=(float)Math.toRadians(yAngle);
		 this.zAngle=(float)Math.toRadians(zAngle);
	}
		void drag(double xpos, double ypos) {
	        // find new position of the picked point
	        vpMat.unprojectRay((float) xpos, (float) ypos, new int[] {0, 0, width, height}, dragRayOrigin, dragRayDir);
	        float t = intersectRayPlane(dragRayOrigin, dragRayDir, dragStartWorldPos, dragCamNormal, 1E-5f);
//	        float t2 = intersectRayPlane( dragRayOrigin, dragRayDir, dragStartWorldPos, dragCamNormal, 1E-5f);
////	        System.out.println(t+" "+t2);
//	        t=Math.max(t,t2);
	        Vector3f dragWorldPosition = new Vector3f(dragRayDir).mul(t).add(dragRayOrigin);
	        translation.add(dragWorldPosition.sub(dragStartWorldPos));
	    }
		@Override
	   public void dragEnd() {
	        dragging = false;
	    }

	    void rotate(double xpos, double ypos) {
	        float deltaX = (float) xpos - mouseX;
	        float deltaY = (float) ypos - mouseY;
	        if(xpress||ypress||zpress) {
	        	double delta = Math.sqrt(deltaX*deltaX+deltaY*deltaY);
	        	if(Math.abs(deltaY)>Math.abs(deltaX))
	        		delta *= Math.signum(deltaY);
	        	else
	        		delta *= Math.signum(deltaX);
	        	if(xpress)
	        		xAngle+=delta*0.005f;
	        	if(ypress)
	        		yAngle+=delta*0.005f;
	        	if(zpress)
	        		zAngle+=delta*0.005f;
//	        	System.out.println(Math.toDegrees(xAngle)+" "+Math.toDegrees(yAngle)+" "+Math.toDegrees(zAngle));
	        }else {
	        xAngle += deltaY * 0.005f;
	        yAngle += deltaX * 0.005f;
	        }		       
	    }
	    @Override
	   public void onScroll(MouseEvent e) {
	        radius *= e.count > 0 ? 1f/1.1f : 1.1f;
	        radius=Math.min(1000,radius);
//	        System.out.println(radius);
	    }
	    @Override
	    public void viewEnd() {
	        viewing = false;
	    }
	    @Override
	    public void viewBegin() {
	        viewing = true;
	    }
		@Override
		public void add(long l) {
            uniformBuffersMemory.add(l);
		}
		@Override
		public void xpress(boolean b) {
			xpress=b;
		}
		@Override
		public void ypress(boolean b) {
			ypress=b;
		}
		@Override
		public void zpress(boolean b) {
			zpress=b;
		}
}