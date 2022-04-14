package oliv.lib.vulkan.apiDonnee;




public interface LecteurMaillageNonStructure {
	public interface ExecuteMaillageNonStructure{
		public void positionNoeud(double x, double y, double z);
		public void positionTexture(double u, double v);
		public void positionNormal(double nx, double ny, double nz);
		public void triangle(int n1, int n2, int n3);
		public void quadrangle(int n1, int n2, int n3, int n4);
		public void quadrangleNormee(Vecteur n1, Vecteur n2, Vecteur n3,Vecteur n4, Vecteur normal,double u, double v);
		public void ligne(int n1, int n2) ;
	}
	public void lectureNoeud(ExecuteMaillageNonStructure lecteur);
	public void lectureElementTriangle(ExecuteMaillageNonStructure lecteur);
	public void lectureElementLigne(ExecuteMaillageNonStructure lecteur);
	public Texture lectureTexture();
}
