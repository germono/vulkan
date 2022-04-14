package oliv.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import oliv.lib.vulkan.apiDonnee.BufferBinaire;
import oliv.lib.vulkan.apiDonnee.Image;
import oliv.lib.vulkan.apiDonnee.LecteurMaillageNonStructure;
import oliv.lib.vulkan.apiDonnee.Texture;
import oliv.lib.vulkan.apiDonnee.Vecteur;
import oliv.lib.vulkan.apiGraphique.Canvas3d;

public class Fenetre {
	Fenetre(LecteurMaillageNonStructure modele) {
		Display display = new Display();
		Shell shell = new Shell(display, SWT.SHELL_TRIM | SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE);
		shell.setLayout(new FillLayout());
		Canvas3d canvas = new Canvas3d(shell);
		canvas.chargeModele(modele);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	static LecteurMaillageNonStructure cube(int nb) {
		double a=1/Math.sqrt(3);
		double delta=0.1/nb;
		LecteurMaillageNonStructure obj = new LecteurMaillageNonStructure() {
			@Override
			public void lectureNoeud(ExecuteMaillageNonStructure lecteur) {
				for (int i = 0; i < nb; i++) {
					for (int j = 0; j < nb; j++) {
						for (int k = 0; k < nb; k++) {
							Vecteur n1 = new Vecteur(delta + (i * 1.0 / (nb + 1)), delta + (j * 1.0 / (nb + 1)),
									delta + (k * 1.0 / (nb + 1)));// d,d,d
							Vecteur n2 = new Vecteur(-delta + ((i + 1) * 1.0 / (nb + 1)), delta + (j * 1.0 / (nb + 1)),
									delta + (k * 1.0 / (nb + 1)));// -d,d,d
							Vecteur n3 = new Vecteur(-delta + ((i + 1) * 1.0 / (nb + 1)),
									-delta + ((j + 1) * 1.0 / (nb + 1)), delta + (k * 1.0 / (nb + 1)));// -d,-d,d
							Vecteur n4 = new Vecteur(delta + (i * 1.0 / (nb + 1)), -delta + ((j + 1) * 1.0 / (nb + 1)),
									delta + (k * 1.0 / (nb + 1)));// d,-d,d
							lecteur.quadrangleNormee(n1, n2, n3, n4, new Vecteur(0, 0, -1),
									(k + j * nb + i * nb * nb) * 1.0 / (nb * nb * nb), 0.5);

							Vecteur n5 = new Vecteur(delta + (i * 1.0 / (nb + 1)), delta + (j * 1.0 / (nb + 1)),
									-delta + ((k + 1) * 1.0 / (nb + 1)));// d,d,-d
							Vecteur n6 = new Vecteur(-delta + ((i + 1) * 1.0 / (nb + 1)), delta + (j * 1.0 / (nb + 1)),
									-delta + ((k + 1) * 1.0 / (nb + 1)));// -d,d,-d
							Vecteur n7 = new Vecteur(-delta + ((i + 1) * 1.0 / (nb + 1)),
									-delta + ((j + 1) * 1.0 / (nb + 1)), -delta + ((k + 1) * 1.0 / (nb + 1)));// -d,-d,-d
							Vecteur n8 = new Vecteur(delta + (i * 1.0 / (nb + 1)), -delta + ((j + 1) * 1.0 / (nb + 1)),
									-delta + ((k + 1) * 1.0 / (nb + 1)));// d,-d,-d
							lecteur.quadrangleNormee(n5, n6, n7, n8, new Vecteur(0, 0, 1),
									(k + j * nb + i * nb * nb) * 1.0 / (nb * nb * nb), 0.5);

							lecteur.quadrangleNormee(n1, n2, n6, n5, new Vecteur(0, -1, 0),
									(k + j * nb + i * nb * nb) * 1.0 / (nb * nb * nb), 0.5);

							lecteur.quadrangleNormee(n3, n4, n8, n7, new Vecteur(0, 1, 0),
									(k + j * nb + i * nb * nb) * 1.0 / (nb * nb * nb), 0.5);

							lecteur.quadrangleNormee(n2, n3, n7, n6, new Vecteur(1, 0, 0),
									(k + j * nb + i * nb * nb) * 1.0 / (nb * nb * nb), 0.5);

							lecteur.quadrangleNormee(n4, n1, n5, n8, new Vecteur(-1, 0, 0),
									(k + j * nb + i * nb * nb) * 1.0 / (nb * nb * nb), 0.5);

						}
					}
				}

			}

			@Override
			public void lectureElementTriangle(ExecuteMaillageNonStructure lecteur) {
			}

			@Override
			public void lectureElementLigne(ExecuteMaillageNonStructure lecteur) {
			}

			@Override
			public Texture lectureTexture() {
				int width = 254;
				int height = 2;
				int BYTES_PER_PIXEL = 4;

				ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * BYTES_PER_PIXEL)
						.order(ByteOrder.nativeOrder());

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						buffer.put((byte) 254); // Red component
						buffer.put((byte) x); // Green component
						buffer.put((byte) x); // Blue component
						buffer.put((byte) x); // Alpha component. Only for RGBA
					}
				}

				buffer.flip();

				return new Texture(buffer, width, height);
			}

		};
		return obj;

	}

	static LecteurMaillageNonStructure lectureObj(String chemin) {
		try {
			System.out.println(Path.of(chemin).toAbsolutePath().toString());
			try (var fichier = Files.lines(Path.of(chemin))) {
				class Lire {
					List<double[]> points = new ArrayList<>();
					List<double[]> normal = new ArrayList<>();
					List<double[]> uv = new ArrayList<>();
					List<int[]> element = new ArrayList<>();
					List<int[]> elementLines = new ArrayList<>();
					Map<String, Integer> ids = new HashMap<>();

					public void collecte(String l) {
						if (l.startsWith("v ")) {
							var ss = l.split(" ");
							points.add(new double[] { Double.parseDouble(ss[1]), Double.parseDouble(ss[2]),
									Double.parseDouble(ss[3]) });
						} else if (l.startsWith("vn ")) {
							var ss = l.split(" ");
							normal.add(new double[] { Double.parseDouble(ss[1]), Double.parseDouble(ss[2]),
									Double.parseDouble(ss[3]) });
						} else if (l.startsWith("vt ")) {
							var ss = l.split(" ");
							uv.add(new double[] { Double.parseDouble(ss[1]), Double.parseDouble(ss[2]) });
						} else if (l.startsWith("f ")) {
							var ss = l.split(" ");
							for (int i = 1; i < ss.length; i++) {
								ids.putIfAbsent(ss[i], ids.size());
							}
							element.add(Arrays.stream(ss).skip(1).mapToInt(s -> ids.get(s).intValue()).toArray());

						} else if (l.startsWith("l ")) {
							var ss = l.split(" ");
							for (int i = 1; i < ss.length; i++) {
								ids.putIfAbsent(ss[i] + "//", ids.size());
							}
							elementLines.add(
									Arrays.stream(ss).skip(1).mapToInt(s -> ids.get(s + "//").intValue()).toArray());

						}

					}

					public void regroupe(Lire autre) {
						points.addAll(autre.points);
						normal.addAll(autre.normal);
						uv.addAll(autre.uv);
						ids.putAll(autre.ids);
						element.addAll(autre.element);
						elementLines.addAll(autre.elementLines);
					}
				}

				Lire lec = fichier.collect(() -> new Lire(), Lire::collecte, Lire::regroupe);
				LecteurMaillageNonStructure obj = new LecteurMaillageNonStructure() {
					@Override
					public void lectureNoeud(ExecuteMaillageNonStructure lecteur) {
						lec.ids.entrySet().stream().sorted(Comparator.comparing(Entry<String, Integer>::getValue))
								.forEachOrdered(e -> {
									int[] idss = Arrays.stream((e.getKey() + " ").split("/")).map(s -> s.trim())
											.map(s -> s.length() == 0 ? "-1" : s).mapToInt(Integer::parseInt).toArray();
									double[] point = lec.points.get(idss[0] - 1);
									double[] normal;
									if (idss[2] != -1)
										normal = lec.normal.get(idss[2] - 1);
									else
										normal = new double[] { 0.0, 0.0, 0.0 };
									double[] uv;
									if (idss[1] != -1)
										uv = lec.uv.get(idss[1] - 1);
									else
										uv = new double[] { 0.0, 0.0 };
									lecteur.positionNoeud(point[0], point[1], point[2]);
									lecteur.positionTexture(uv[0], 1 - uv[1]);
									lecteur.positionNormal(normal[0], normal[1], normal[2]);
								});

					}

					@Override
					public void lectureElementTriangle(ExecuteMaillageNonStructure lecteur) {
						for (int[] tri : lec.element) {
							if (tri.length == 3)
								lecteur.triangle(tri[0], tri[1], tri[2]);
							else if (tri.length == 4)
								lecteur.quadrangle(tri[0], tri[1], tri[2], tri[3]);
							else {
								System.err.println("Attention polyedre");
								for (int i = 0; i < tri.length - 2; i++) {
									lecteur.triangle(tri[0], tri[i + 1], tri[i + 2]);
								}
							}
						}

					}

					@Override
					public void lectureElementLigne(ExecuteMaillageNonStructure lecteur) {
						for (int[] ligne : lec.elementLines) {
							for (int i = 0; i < ligne.length - 1; i++) {
								lecteur.ligne(ligne[i], ligne[i + 1]);
							}
						}
					}

					@Override
					public Texture lectureTexture() {
						Image suzane = BufferBinaire.importString(BufferBinaire.cheminRelatif("test/suzane4k.png"));
						System.out.println(suzane.largeur() + " " + suzane.hauteur());
						return new Texture(suzane.image(), suzane.largeur(), suzane.hauteur());
					}

				};
				return obj;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static ByteBuffer createByteBuffer(int capacity) {
		return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
	}


	public static void main(String[] args) {
//		new Fenetre(lectureObj("bin/test/plusieurs2.obj"));
		new Fenetre(cube(40));
	}

}
