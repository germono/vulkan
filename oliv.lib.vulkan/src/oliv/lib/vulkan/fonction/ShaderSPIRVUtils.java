package oliv.lib.vulkan.fonction;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_compilation_status_success;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_compile_into_spv;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_compiler_initialize;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_compiler_release;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_glsl_fragment_shader;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_glsl_geometry_shader;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_glsl_vertex_shader;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_result_get_bytes;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_result_get_compilation_status;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_result_get_error_message;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_result_release;

import java.nio.ByteBuffer;

import org.lwjgl.system.NativeResource;

import oliv.lib.vulkan.apiDonnee.ChargementFichierE4;

public class ShaderSPIRVUtils {

    public static SPIRV compileShaderFile(String shaderFile, ShaderKind shaderKind) {
    	 return compileShaderAbsoluteFile(shaderFile, shaderKind);
    }

    public static SPIRV compileShaderAbsoluteFile(String shaderFile, ShaderKind shaderKind) {
     
        	String source = ChargementFichierE4.chargeURL(ShaderSPIRVUtils.class,shaderFile);
            return compileShader(shaderFile, source, shaderKind);
        
    }

    public static SPIRV compileShader(String filename, String source, ShaderKind shaderKind) {

        long compiler = shaderc_compiler_initialize();

        if(compiler == NULL) {
            throw new RuntimeException("Failed to create shader compiler");
        }

        long result = shaderc_compile_into_spv(compiler, source, shaderKind.kind, filename, "main", NULL);

        if(result == NULL) {
            throw new RuntimeException("Failed to compile shader " + filename + " into SPIR-V");
        }

        if(shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success) {
            throw new RuntimeException("Failed to compile shader " + filename + "into SPIR-V:\n " + shaderc_result_get_error_message(result));
        }

        shaderc_compiler_release(compiler);

        return new SPIRV(result, shaderc_result_get_bytes(result));
    }

    public enum ShaderKind {

        VERTEX_SHADER(shaderc_glsl_vertex_shader),
        GEOMETRY_SHADER(shaderc_glsl_geometry_shader),
        FRAGMENT_SHADER(shaderc_glsl_fragment_shader);

        private final int kind;

        ShaderKind(int kind) {
            this.kind = kind;
        }
    }

    public static final class SPIRV implements NativeResource {

        private final long handle;
        private ByteBuffer bytecode;

        public SPIRV(long handle, ByteBuffer bytecode) {
            this.handle = handle;
            this.bytecode = bytecode;
        }

        public ByteBuffer bytecode() {
            return bytecode;
        }

        @Override
        public void free() {
            shaderc_result_release(handle);
            bytecode = null; // Help the GC
        }
    }

}
