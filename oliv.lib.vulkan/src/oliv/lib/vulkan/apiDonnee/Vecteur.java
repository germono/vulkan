package oliv.lib.vulkan.apiDonnee;



public class Vecteur {
	public double x;
	public double y;
	public double z;
	   private double norme;

	   public final double getNorme() {
		   return norme;
	   }
	   private void initNorme() {
		   norme=Math.sqrt(x * x + y * y + z * z);
	   }
	   
	   public Vecteur plus( Vecteur b) {
	      return new Vecteur(this.x + b.x, this.y + b.y, this.z + b.z);
	   }
	   public final void plusModif( Vecteur b) {
		      x+= b.x;
		      y+= b.y;
		      z+= b.z;
		      initNorme();
		   }
		public final void minChaqueModif(Vecteur n) {
			x=Math.min(x,n.x);
			y=Math.min(y,n.y);
			z=Math.min(z,n.z);
		    initNorme();
			
		}
		public final void maxChaqueModif(Vecteur n) {
			x=Math.max(x,n.x);
			y=Math.max(y,n.y);
			z=Math.max(z,n.z);
		    initNorme();
			
		}
	   public final Vecteur minus( Vecteur b) {
	      return new Vecteur(this.x - b.x, this.y - b.y, this.z - b.z);
	   }
	   public void add(double x2, double y2, double z2, int i) {
			setX((getX()*i+x2)/(i+1));
			setY((getY()*i+y2)/(i+1));
			setZ((getZ()*i+z2)/(i+1));
		}
	   
	   public final Vecteur times(double b) {
	      return new Vecteur(this.x * b, this.y * b, this.z * b);
	   }

	   
	   public final Vecteur div(double b) {
	      return new Vecteur(this.x / b, this.y / b, this.z / b);
	   }

	   
	   public final Vecteur div(int b) {
	      return new Vecteur(this.x / (double)b, this.y / (double)b, this.z / (double)b);
	   }
	   
	   public final Vecteur divModif(double b) {
		   x/=b;
		   y/=b;
		   z/=b;
		      initNorme();
		      return this;
		   }
	   public String toString() {
	      return "" + '|' + this.x + " , " + this.y + " , " + this.z + " | " + this.getNorme();
	   }

	   public final double times( Vecteur b) {
	      return this.x * b.x + b.y * this.y + this.z * b.z;
	   }

	   public final double produitScalaire( Vecteur b) {
	      return this.times(b);
	   }

	   public final double dot( Vecteur b) {
	      return this.times(b);
	   }
	   public final double projeterNorme(double x,double y,double z) {
		   
		   return (this.x * x + y * this.y + this.z * z)/(getNorme());
	   }
	   
	   public final Vecteur cross( Vecteur b) {
	      return new Vecteur(this.y * b.z - b.y * this.z, this.z * b.x - this.x * b.z, this.x * b.y - b.x * this.y);
	   }

	   public final Vecteur normalise() {
	      double n = this.getNorme();
	      this.x /= n;
	      this.y /= n;
	      this.z /= n;
	      initNorme();
	      
	      return this;
	   }
	   /**
	    * Formule de rodrigues
	    * @param alpha en rad
	    * @param u normee
	    * @return
	    */
	   public Vecteur rotation(double alpha, Vecteur u ) {
		   return this.times(Math.cos(alpha))
		   .plus(u.times(this.dot(u)).times(1-Math.cos(alpha)))
		   .plus(u.cross(this).times(Math.sin(alpha)));
				   
	   }

	   public final void tourneDx(int dx) {
	      double rad = (double)(dx * 180) / Math.PI;
	      this.tourneDx(rad);
	   }

	   public final void tourneDx(double dx) {
	      double var7 = this.y;
	      double var9 = Math.cos(dx);
	      double var10000 = var7 * var9;
	      var9 = this.z;
	      var7 = var10000;
	      double var11 = Math.sin(dx);
	      double ty = var7 - var9 * var11;
	      var7 = this.z;
	      var9 = Math.cos(dx);
	      var10000 = var7 * var9;
	      var9 = this.y;
	      var7 = var10000;
	      var11 = Math.sin(dx);
	      double tz = var7 + var9 * var11;
	      this.y = ty;
	      this.z = tz;
	      initNorme();
	   }

	   public final void tourneDy(int dy) {
	      double rad = (double)(dy * 180) / Math.PI;
	      this.tourneDy(rad);
	   }

	   public final void tourneDy(double dy) {
	      double var7 = this.x;
	      double var9 = Math.cos(dy);
	      double var10000 = var7 * var9;
	      var9 = this.z;
	      var7 = var10000;
	      double var11 = Math.sin(dy);
	      double tx = var7 + var9 * var11;
	      var7 = this.z;
	      var9 = Math.cos(dy);
	      var10000 = var7 * var9;
	      var9 = this.x;
	      var7 = var10000;
	      var11 = Math.sin(dy);
	      double tz = var7 - var9 * var11;
	      this.x = tx;
	      this.z = tz;
	      initNorme();
	   }

	   public final void tourneDz(int dz) {
	      double rad = (double)(dz * 180) / Math.PI;
	      this.tourneDz(rad);
	   }

	   public final void tourneDz(double dz) {
	      double var7 = this.x;
	      double var9 = Math.cos(dz);
	      double var10000 = var7 * var9;
	      var9 = this.y;
	      var7 = var10000;
	      double var11 = Math.sin(dz);
	      double tx = var7 - var9 * var11;
	      var7 = this.y;
	      var9 = Math.cos(dz);
	      var10000 = var7 * var9;
	      var9 = this.x;
	      var7 = var10000;
	      var11 = Math.sin(dz);
	      double ty = var7 + var9 * var11;
	      this.y = ty;
	      this.x = tx;
	      initNorme();
	   }

	   public final double getX() {
	      return this.x;
	   }

	   public final void setX(double var1) {
	      this.x = var1;
	      initNorme();
	   }

	   public final double getY() {
	      return this.y;
	   }

	   public final void setY(double var1) {
	      this.y = var1;
	      initNorme();
	   }

	   public final double getZ() {
	      return this.z;
	   }

	   public final void setZ(double var1) {
	      this.z = var1;
	      initNorme();
	   }

	   public Vecteur(double x, double y, double z) {
	      this.x = x;
	      this.y = y;
	      this.z = z;
	      initNorme();
	   }
	   public Vecteur(double[] xyz) {
		      this.x = xyz[0];
		      this.y = xyz[1];
		      this.z = xyz[2];
		      initNorme();
		   }
	   public static Vecteur getNormal(Vecteur a,Vecteur b,Vecteur c) {
		   Vecteur ab=b.minus(a).normalise();
		   Vecteur ac=c.minus(a).normalise();
		   return ab.cross(ac).normalise();
		  
	   }

	  
	}
