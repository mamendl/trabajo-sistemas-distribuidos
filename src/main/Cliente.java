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
			// Ventana t = (Ventana) ois.readObject();
			/*
			 * System.out.println("¿Interfaz o consola?");
			 * System.out.println("1. Interfaz"); System.out.println("2. Consola");
			 * System.out.println("3. No quiero seguir me da amsiedad tomar decisiones :(");
			 * 
			 * 
			 * while(option==0||option>3||option<0) { try{ linea = sc.nextLine(); option =
			 * Integer.parseInt(linea); } catch(NumberFormatException nfe){
			 * System.out.println("Introduce un número válido"); } }
			 * 
			 * oos.writeInt(option); oos.flush(); if(option==3) System.exit(0);
			 */

			Scanner sc = new Scanner(System.in);
			String linea = "";
			int option = 0;

			System.out.println("1. Loggearse");
			System.out.println("2. Darse de alta");
			System.out.println("3. Desconectar");
			option = 0;
			while (option == 0 || option > 3 || option < 0) {
				try {
					linea = sc.nextLine();
					option = Integer.parseInt(linea);
				} catch (NumberFormatException nfe) {
					System.out.println("Introduce un número válido");
				}
			}

			oos.writeInt(option);
			oos.flush();
			if (option == 3)
				System.exit(0);

			System.out.print("Usuario: ");
			String nom = sc.nextLine();
			System.out.print("Contraseña: ");
			String contrasena = sc.nextLine();
			oos.write((nom + "\r\n").getBytes());
			oos.write((contrasena + "\r\n").getBytes());
			oos.flush();

			boolean correcto;

			String mensaje = ois.readLine();
			System.out.println(mensaje);

			correcto = ois.readBoolean();

			/*
			 * while(!correcto&&!linea.equalsIgnoreCase("Desconectar")) { linea =
			 * sc.nextLine(); System.out.print("Usuario: "); String nom = sc.nextLine();
			 * System.out.print("Contraseña: "); String contrasena = sc.nextLine();
			 * oos.write((nom+"\r\n").getBytes());
			 * oos.write((contrasena+"\r\n").getBytes()); oos.flush(); }
			 */

			if (correcto) {
				System.out.print("Bienvenido a Drive Safe.");
				while (option != 6 && option != 5) {
					option = 0;
					System.out.print("Seleccione una opción:\n");
					System.out.println("1. Consultar archivos");
					System.out.println("2. Subir archivo");
					System.out.println("3. Descargar archivo");
					System.out.println("4. Borrar un archivo");
					System.out.println("5. Borrar mi usuario");
					System.out.println("6. Desconectar");
					while (option == 0 || option > 6 || option < 0) {
						try {
							linea = sc.nextLine();
							option = Integer.parseInt(linea);
						} catch (NumberFormatException nfe) {
							System.out.println("Introduce un número válido");
						}
					}
					oos.writeInt(option);
					oos.flush();
					switch (option) {
					case 1:
						// consultar archivos
						Document d = (Document) ois.readObject();
						Element raiz = d.getDocumentElement();
						NodeList nodes = raiz.getElementsByTagName("archivo");
						if (nodes.getLength() == 0) {
							System.out.println("Parece que aún no has subido nada.");
						} else {
							for (int i = 0; i < nodes.getLength(); i++) {
								Element a = (Element) nodes.item(i);
								System.out.print(a.getElementsByTagName("nombre").item(0).getTextContent() + " ");
								System.out.print(a.getElementsByTagName("size").item(0).getTextContent() + " bytes\n");
							}
						}
						break;
					case 2:
						// subir archivo
						System.out.println("Introducir la ruta del archivo que desea subir:");
						String ar = sc.nextLine();
						File arch = new File(ar);
						if (arch.exists() && !arch.isDirectory()) {
							System.out.println("uy eso si que existe");

						} else {
							System.out.println("Error, archivo no encontrado.");
							// ...
						}
						break;
					case 3:
						System.out.println("Introduce el nombre del archivo que quieres descargar:");
						linea = sc.nextLine();
						oos.write((linea + "\r\n").getBytes());
						break;
					case 5:
						System.out.println(
								"Vas a borrar tu usuario y todos sus datos para siempre (eso es mucho tiempo)");
						String decision;
						boolean x = false;
						while (!x) {
							System.out.println("¿Seguro que quieres continuar?(S/n)");
							decision = sc.nextLine();
							if (decision.equalsIgnoreCase("s") || decision.equalsIgnoreCase("sí")
									|| decision.equalsIgnoreCase("si")) {
								x = true;
								// borreision
							} else if (decision.equalsIgnoreCase("n") || decision.equalsIgnoreCase("no")) {
								x = true;
							}
						}
						break;
					}
				}
				oos.writeInt(6);
				oos.flush();
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
