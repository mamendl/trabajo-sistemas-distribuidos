package main;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Cliente {
	public static void main(String[] args) {
		try (Socket s = new Socket("localhost", 55555);
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(s.getInputStream());) {

			Scanner sc = new Scanner(System.in);
			String linea = "";
			int option = 0;

			System.out.println("1. Iniciar sesión");
			System.out.println("2. Darse de alta");
			System.out.println("3. Desconectar");
			System.out.print("Introduce una opción: ");
			option = 0;
			while (option > 3 || option <= 0) {
				try {
					linea = sc.nextLine();
					option = Integer.parseInt(linea);
				} catch (NumberFormatException nfe) {
				} finally {
					if (option > 3 || option <= 0)
						System.out.print("Introduce un número válido: ");
				}
			}

			oos.writeInt(option);
			oos.flush();
			if (option == 3)
				System.exit(0);

			boolean correcto = false;

			String mensaje = "";

			while (!correcto) {
				System.out.print("Usuario: ");
				String nom = sc.nextLine();
				System.out.print("Contraseña: ");
				String contrasena = sc.nextLine();
				oos.write((nom + "\r\n").getBytes());
				oos.write((contrasena + "\r\n").getBytes());
				oos.flush();

				mensaje = ois.readLine();
				System.out.println(mensaje);

				correcto = ois.readBoolean();
			}

			boolean borrado = false;

			if (correcto) {
				System.out.println("Bienvenido a Drive Safe.");
				while (option != 6 && !borrado) {
					option = 0;
					System.out.println("1. Consultar archivos");
					System.out.println("2. Subir archivo");
					System.out.println("3. Descargar archivo");
					System.out.println("4. Borrar un archivo");
					System.out.println("5. Borrar mi usuario");
					System.out.println("6. Desconectar");
					System.out.print("Introduce una opción: ");
					while (option > 6 || option <= 0) {

						try {
							linea = sc.nextLine();
							option = Integer.parseInt(linea);
						} catch (NumberFormatException nfe) {
						} finally {
							if (option > 6 || option <= 0)
								System.out.print("Introduce un número válido: ");
						}
					}

					oos.writeInt(option);
					oos.flush();

					boolean existe = false;

					switch (option) {
					case 1:
						// consultar archivos
						Document d = (Document) ois.readObject();
						Element raiz = d.getDocumentElement();
						NodeList nodes = raiz.getElementsByTagName("archivo");
						if (nodes.getLength() == 0) {
							System.out.println("Parece que no tienes ningún archivo subido.");
						} else {
							System.out.println("Estos son tus archivos:");
							for (int i = 0; i < nodes.getLength(); i++) {
								Element a = (Element) nodes.item(i);
								System.out.print(a.getElementsByTagName("nombre").item(0).getTextContent() + " ");
								System.out.print(a.getElementsByTagName("size").item(0).getTextContent() + " bytes\n");
							}
						}
						break;
					case 2:
						// subir archivo
						System.out.print("Introducir la ruta del archivo que desea subir: ");
						String ar = sc.nextLine();
						File arch = new File(ar);

						boolean cancelar = false;

						while ((!arch.exists() || arch.isDirectory()) && !cancelar) {
							System.out.print(
									"Error, archivo no encontrado. Introdúcleo de nuevo (escribe cancelar para volver al menú): ");
							ar = sc.nextLine();
							arch = new File(ar);
							if (ar.equalsIgnoreCase("cancelar")) {
								cancelar = true;
							}
						}

						oos.writeBoolean(cancelar);
						oos.flush();

						if (cancelar)
							break;

						if (arch.exists() && !arch.isDirectory()) {

							oos.write((arch.getName() + "\r\n").getBytes());
							oos.write((arch.length() + "\r\n").getBytes());
							oos.flush();

							boolean enviar = ois.readBoolean();

							if (enviar) {

								byte[] buff = new byte[1024 * 1024];

								try (FileInputStream fos = new FileInputStream(arch)) {
									int leidos = fos.read(buff);
									while (leidos != -1) {
										oos.write(buff, 0, leidos);
										leidos = fos.read(buff);
										oos.flush();
									}
									oos.write("\u001a".getBytes());
									oos.flush();
								}
							}

							mensaje = ois.readLine();
							System.out.println(mensaje);
						}
						break;
					case 3: // descargar un archivo
						cancelar = false;
						Element archivos = (Element) ois.readObject();
						if (archivos.getElementsByTagName("archivo").getLength() == 0) {
							System.out.println("No tienes archivos subidos todavía fiera.");
							break;
						}
						
						System.out.print("Introduce el nombre del archivo que quieres descargar: ");
						linea = sc.nextLine();
						oos.write((linea + "\r\n").getBytes());
						oos.flush();
						existe = ois.readBoolean();
						while (!existe && !cancelar) {
							System.out.print(
									"Error, ese archivo no existe. ¿Estás seguro de que lo has escrito bien? Introdúcelo de nuevo (escribe cancelar para volver al menú): ");
							linea = sc.nextLine();
							if (linea.equalsIgnoreCase("cancelar")) {
								cancelar = true;
								oos.writeBoolean(cancelar);
								oos.flush();
							} else {
								oos.writeBoolean(cancelar);
								oos.flush();
								oos.write((linea + "\r\n").getBytes());
								oos.flush();
								existe = ois.readBoolean();
							}
						}
						
						oos.writeBoolean(cancelar);
						oos.flush();
						
						if(cancelar) break;
						
						String archivoAdescargar = linea;
						System.out.print("Introduce el nombre del directorio donde quieras guardarlo: ");
						String direc = sc.nextLine();
						File dir = new File(direc);
						while (!dir.isDirectory()) {
							System.out.print("Introduce un directorio válido: ");
							direc = sc.nextLine();
							dir = new File(direc);
						}
						int size = Integer.parseInt(ois.readLine());
						byte[] buff = new byte[1024 * 1024];
						try (FileOutputStream fos = new FileOutputStream(direc + "/" + archivoAdescargar)) {
							int leidos = ois.read(buff);
							int enviados = 0;
							while (leidos != -1 && enviados < size) {
								fos.write(buff, 0, leidos);
								enviados = enviados + leidos;
								leidos = ois.read(buff);
							}
						}
						mensaje = ois.readLine();
						System.out.println(mensaje);
						break;
					case 4: // BORRAR UN ARCHIVO:
						cancelar = false;
						archivos = (Element) ois.readObject();
						if (archivos.getElementsByTagName("archivo").getLength() == 0) {
							System.out.println("No tienes archivos subidos todavía fiera.");
						} else {
							System.out.print("Introduce el nombre del archivo que quieres borrar: ");
							linea = sc.nextLine();
							oos.write((linea + "\r\n").getBytes());
							oos.flush();
							existe = ois.readBoolean();
							while (!existe && !cancelar) {
								System.out.print(
										"Error, ese archivo no existe. ¿Estás seguro de que lo has escrito bien? Introdúcelo de nuevo (escribe cancelar para volver al menú): ");
								linea = sc.nextLine();
								if (linea.equalsIgnoreCase("cancelar")) {
									cancelar = true;
									oos.writeBoolean(cancelar);
									oos.flush();
								} else {
									oos.writeBoolean(cancelar);
									oos.flush();
									oos.write((linea + "\r\n").getBytes());
									oos.flush();
									existe = ois.readBoolean();
								}
							}
							if (!cancelar && ois.readBoolean()) {
								System.out.println("Archivo borrado correctamente.");
							} else if (!cancelar) {
								System.out.println("Parece que no se ha podido borrar el archivo.");
							}
						}
						break;
					case 5:
						System.out.print("Vas a borrar tu usuario y todos sus datos para siempre (eso es mucho tiempo). ");
						String decision;
						boolean x = false;
						while (!x) {
							System.out.print("¿Seguro que quieres continuar? (S/n): ");
							decision = sc.nextLine();
							if (decision.equalsIgnoreCase("s") || decision.equalsIgnoreCase("sí")
									|| decision.equalsIgnoreCase("si")) {
								x = true;
								borrado = true;
								oos.writeBoolean(borrado);
								oos.flush();
								mensaje = ois.readLine();
								System.out.println(mensaje);
							} else if (decision.equalsIgnoreCase("n") || decision.equalsIgnoreCase("no")) {
								x = true;
								oos.writeBoolean(borrado);
								oos.flush();
							}
						}
						break;
					}
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
