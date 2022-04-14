package oliv.lib.vulkan.objet;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.assimp.Assimp.aiGetErrorString;
import static org.lwjgl.assimp.Assimp.aiImportFile;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;

import oliv.lib.vulkan.apiDonnee.LecteurMaillageNonStructure;
import oliv.lib.vulkan.apiDonnee.LecteurMaillageNonStructure.ExecuteMaillageNonStructure;
import oliv.lib.vulkan.apiDonnee.Vecteur;

public class ModelLoader {

	public static Model loadModel(File file, int flags) {

		try (AIScene scene = aiImportFile(file.getAbsolutePath(), flags)) {

			Logger logger = Logger.getLogger(ModelLoader.class.getSimpleName());

			logger.info("Loading model " + file.getPath() + "...");

			if (scene == null || scene.mRootNode() == null) {
				throw new RuntimeException("Could not load model: " + aiGetErrorString());
			}

			Model model = new Model();

			long startTime = System.nanoTime();

			processNode(scene.mRootNode(), scene, model);

			logger.info("Model loaded in " + ((System.nanoTime() - startTime) / 1e6) + "ms");

			return model;
		}
	}

	private static void processNode(AINode node, AIScene scene, Model model) {

		if (node.mMeshes() != null) {
			processNodeMeshes(scene, node, model);
		}

		if (node.mChildren() != null) {

			PointerBuffer children = node.mChildren();

			for (int i = 0; i < node.mNumChildren(); i++) {
				processNode(AINode.create(children.get(i)), scene, model);
			}

		}

	}

	private static void processNodeMeshes(AIScene scene, AINode node, Model model) {

		PointerBuffer pMeshes = scene.mMeshes();
		IntBuffer meshIndices = node.mMeshes();

		for (int i = 0; i < meshIndices.capacity(); i++) {
			AIMesh mesh = AIMesh.create(pMeshes.get(meshIndices.get(i)));
			processMesh(scene, mesh, model);
		}

	}

	private static void processMesh(AIScene scene, AIMesh mesh, Model model) {

		processPositions(mesh, model.positions);
		processTexCoords(mesh, model.texCoords);

		processIndices(mesh, model.indices);
	}

	private static void processPositions(AIMesh mesh, List<Vector3fc> positions) {

		AIVector3D.Buffer vertices = requireNonNull(mesh.mVertices());

		for (int i = 0; i < vertices.capacity(); i++) {
			AIVector3D position = vertices.get(i);
			positions.add(new Vector3f(position.x(), position.y(), position.z()));
		}

	}

	private static void processTexCoords(AIMesh mesh, List<Vector2fc> texCoords) {

		AIVector3D.Buffer aiTexCoords = requireNonNull(mesh.mTextureCoords(0));

		for (int i = 0; i < aiTexCoords.capacity(); i++) {
			final AIVector3D coords = aiTexCoords.get(i);
			texCoords.add(new Vector2f(coords.x(), coords.y()));
		}

	}

	private static void processIndices(AIMesh mesh, List<Integer> indices) {

		AIFace.Buffer aiFaces = mesh.mFaces();

		for (int i = 0; i < mesh.mNumFaces(); i++) {
			AIFace face = aiFaces.get(i);
			IntBuffer pIndices = face.mIndices();
			for (int j = 0; j < face.mNumIndices(); j++) {
				indices.add(pIndices.get(j));
			}
		}

	}

	public static class Model {

		public final List<Vector3fc> positions;
		public final List<Vector3fc> normals;
		public final List<Vector2fc> texCoords;
		public final List<Integer> indices;
		public final List<Integer> indicesLines;

		public Model() {
			this.positions = new ArrayList<>();
			this.texCoords = new ArrayList<>();
			this.normals = new ArrayList<>();
			this.indices = new ArrayList<>();
			this.indicesLines= new ArrayList<>();
		}
	}

//	public static Model plan() {
//		Model m = new Model();
//		for (int i = 0; i < 10; i++) {
//			for (int j = 0; j < 10; j++) {
//				m.positions.add(new Vector3f(i / 10.0f, j / 10.0f, 0.0f));
//				m.normals.add(new Vector3f(0.0f,0.0f, 1.0f));
//				m.texCoords.add(new Vector2f(i / 10.0f, j / 10.0f));
//			}
//		}
//		for (int i = 0; i < 9; i++) {
//			for (int j = 0; j < 9; j++) {
//
//				m.indices.add(j+i*10);
//				m.indices.add(j+(i+1)*10);
//				m.indices.add(j+1+i*10);
//				m.indices.add(j+1+i*10);
//				m.indices.add(j+(i+1)*10);
//				m.indices.add(j+1+(i+1)*10);
//
//			}
//		}
//		return m;
//	}

	public static Model lecteur(LecteurMaillageNonStructure modele) {
//		if(modele==null)
//			return plan();

		Model m = new Model();
		ExecuteMaillageNonStructure lecteur=new ExecuteMaillageNonStructure() {
 
			@Override
			public void positionNoeud(double x, double y, double z) {
				m.positions.add(new Vector3f((float)x,(float)y,(float)z));
			}

			@Override
			public void positionTexture(double u, double v) {
				m.texCoords.add(new Vector2f((float)u,(float)v));
			}
			@Override
			public void positionNormal(double nx, double ny, double nz) {
				m.normals.add(new Vector3f((float)nx,(float)ny,(float)nz));
			}
			@Override
			public void triangle(int n1, int n2, int n3) {
				m.indices.add(n1);
				m.indices.add(n2);
				m.indices.add(n3);
				m.indices.add(0xFFFFFFFF);
			}
			@Override
			public void quadrangle(int n1, int n2, int n3, int n4) {
				m.indices.add(n1);
				m.indices.add(n2);
				m.indices.add(n4);
				m.indices.add(n3);
				m.indices.add(0xFFFFFFFF);
				
			}
			@Override
			public void quadrangleNormee(Vecteur n1, Vecteur n2, Vecteur n3,Vecteur n4, Vecteur normal,double u, double v) {
				int id=m.positions.size();
				m.positions.add(new Vector3f((float)n1.x,(float)n1.y,(float)n1.z));
				m.positions.add(new Vector3f((float)n2.x,(float)n2.y,(float)n2.z));
				m.positions.add(new Vector3f((float)n3.x,(float)n3.y,(float)n3.z));
				m.positions.add(new Vector3f((float)n4.x,(float)n4.y,(float)n4.z));
				m.texCoords.add(new Vector2f((float)u,(float)v));
				m.texCoords.add(new Vector2f((float)u,(float)v));
				m.texCoords.add(new Vector2f((float)u,(float)v));
				m.texCoords.add(new Vector2f((float)u,(float)v));
				m.normals.add(new Vector3f((float)normal.x,(float)normal.y,(float)normal.z));
				m.normals.add(new Vector3f((float)normal.x,(float)normal.y,(float)normal.z));
				m.normals.add(new Vector3f((float)normal.x,(float)normal.y,(float)normal.z));
				m.normals.add(new Vector3f((float)normal.x,(float)normal.y,(float)normal.z));
				
				//Mode list
//				m.indices.add(id);
//				m.indices.add(id+1);
//				m.indices.add(id+2);
//				m.indices.add(id);
//				m.indices.add(id+2);
//				m.indices.add(id+3);
				//Mode Strip
				m.indices.add(id);
				m.indices.add(id+1);
				m.indices.add(id+3);
				m.indices.add(id+2);
				m.indices.add(0xFFFFFFFF);
				
			}

			@Override
			public void ligne(int n1, int n2) {
				m.indicesLines.add(n1);
				m.indicesLines.add(n2);
			}
			
		};
		modele.lectureNoeud(lecteur);
		modele.lectureElementTriangle(lecteur);
		modele.lectureElementLigne(lecteur);
		return m;
	}

}
