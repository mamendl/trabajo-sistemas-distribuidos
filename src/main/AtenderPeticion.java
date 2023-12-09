package main;

import java.io.*;
import java.net.*;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AtenderPeticion implements Runnable {

	private Socket s;
	private String usuario;

	public AtenderPeticion(Socket s) {
		this.s = s;
	}

	@Override
	public void run() {
		try (ObjectOutputStream oos = new ObjectOutputStream(this.s.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(this.s.getInputStream())) {
			int op = ois.readInt();

			if (op != 3) {

				String usuario = ois.readLine();
				String clave = ois.readLine();

				File f = new File("src/datos/usuarios.xml");
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document d = db.parse(f);
				Element root = d.getDocumentElement();

				boolean corresto = false;
				boolean encontrado = false;
				switch (op) {
				case 1:
					while (!corresto) {
						
						NodeList nodes = root.getElementsByTagName("usuario");
						int i = 0;
						do {
							Element e = (Element) nodes.item(i);
							Element nom = (Element) e.getElementsByTagName("nombre").item(i);
							if (usuario.equals(nom.getTextContent())) {
								encontrado = true;
								System.out.println(usuario + " " + nom.getTextContent());
								Element c = (Element) e.getElementsByTagName("clave").item(i);
								if (clave.equals(c.getTextContent())) {
									corresto = true;
									System.out.println(clave.equals(c.getTextContent()));
								} else {
									oos.write("Contraseña incorrecta\r\n".getBytes());
								}
							}
							i++;
						} while (i < nodes.getLength() && !encontrado);

						if (corresto) {
							this.usuario = usuario;
							oos.write("Te has loggeado correctamente\r\n".getBytes());
						}
						if (!encontrado) {
							oos.write("Error: usuario no encontrado\r\n".getBytes());
						}
						
						oos.writeBoolean(corresto);
						oos.flush();
						
						if(!corresto) {
							usuario = ois.readLine();
							clave = ois.readLine();
						}
					}
					break;
				case 2:
					while(!corresto) {
						if (!existeElElemento(usuario, "usuario", "src/datos/usuarios.xml")) {
							Element nuevoUsuario = d.createElement("usuario");
							Element nom = d.createElement("nombre");
							nom.setTextContent(usuario);
							Element c = d.createElement("clave");
							c.setTextContent(clave);
							nuevoUsuario.appendChild(nom);
							nuevoUsuario.appendChild(c);
							root.appendChild(nuevoUsuario);
	
							TransformerFactory tf = TransformerFactory.newInstance();
							Transformer t = tf.newTransformer();
							DOMSource source = new DOMSource(root);
							StreamResult result = new StreamResult(new File("src/datos/usuarios.xml"));
							t.transform(source, result);
							
							File carpeta = new File("src/datos/" + usuario);
							carpeta.mkdir();
							try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
									new FileOutputStream(new File("src/datos/" + usuario + "/" + usuario + ".xml"))))) {
								bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
										+ "<!DOCTYPE archivos SYSTEM \"../archivos.dtd\" >\r\n" + "<archivos>\r\n"
										+ "</archivos>");
							}
	
							this.usuario = usuario;
							
							oos.write("Te has registrado correctamente\r\n".getBytes());
							
							corresto = true;
						} else {
	
							oos.write("El nombre que has introducido ya existe\r\n".getBytes());
	
							corresto = false;
						}
						oos.writeBoolean(corresto);
						oos.flush();
						if(!corresto) {
							usuario = ois.readLine();
							clave = ois.readLine();
						}
						
					}
					break;
				}
				oos.flush();
				//oos.writeBoolean(corresto);
				//oos.flush();
				op = ois.readInt();
				String suXml = "src/datos/" + this.usuario + "/" + this.usuario + ".xml";
				while (op != 6 && op != 5) {
					switch (op) {
					case 1:
						// enviar su xml
						File fich = new File(suXml);
						Document doc = db.parse(fich);
						oos.writeObject(doc);
						break;
					case 2:
						// recibir y añadir a su xml
						String nombre = ois.readLine();
						String tam = ois.readLine();
						// System.out.println(nombre);
						// System.out.println(tam);

						File xml = new File(suXml);
						Document docu = db.parse(xml);
						Element r = docu.getDocumentElement();

						boolean subido = existeElElemento(nombre, "archivo", suXml);

						/*
						 * NodeList nodelits = r.getElementsByTagName("archivo"); for(int i = 0; i <
						 * nodelits.getLength(); i++) { Element e = (Element) nodelits.item(i); NodeList
						 * no = e.getElementsByTagName("nombre");
						 * if(nombre.equals(no.item(0).getTextContent())) subido = true; }
						 */

						if (!subido) {
							oos.writeBoolean(true);
							oos.flush();
							try (FileOutputStream fos = new FileOutputStream(
									new File("src/datos/" + usuario + "/" + nombre))) {
								byte[] buff = new byte[1024 * 1024];
								int leidos = ois.read(buff);
								int escritos = 0;
								while (leidos != -1 && escritos < Integer.parseInt(tam)) { //
									escritos = escritos + leidos;
									fos.write(buff, 0, leidos);
									leidos = ois.read(buff);
									System.out.println(leidos + " " + escritos);
								}
								System.out.println("aleluia");
							}

							// añadir al xml:

							Element archivo = docu.createElement("archivo");
							Element nom = docu.createElement("nombre");
							nom.setTextContent(nombre);
							Element size = docu.createElement("size");
							size.setTextContent(tam);
							archivo.appendChild(nom);
							archivo.appendChild(size);
							r.appendChild(archivo);

							TransformerFactory tf = TransformerFactory.newInstance();
							Transformer t = tf.newTransformer();
							DOMSource source = new DOMSource(r);
							StreamResult result = new StreamResult(
									new File("src/datos/" + usuario + "/" + usuario + ".xml"));
							t.transform(source, result);

							oos.write("Archivo subido correctamente.\r\n".getBytes());
						} else {
							oos.writeBoolean(false);
							oos.write("Ese archivo ya lo has subido fiera.\r\n".getBytes());
						}
						oos.flush();

						break;
					case 3:
						// recibir el nombre, buscarlo, enviar un mensaje si existe o no y enviar el
						// archivo
						String nombreArchivo = ois.readLine();
						boolean existe = false;
						File archivoAmandar = new File("src/datos/" + usuario + "/" + nombreArchivo);
						existe = archivoAmandar.exists();
						oos.writeBoolean(existe);
						if (existe) {
							// lo manda
							oos.writeObject(archivoAmandar);
							oos.write("Archivo mandado con éxito.\r\n".getBytes());
							oos.flush();
						} else {
							oos.write("Error archivo no encontrado.\r\n".getBytes());
							oos.flush();
						}
						break;
					case 4:
						// borrar archivo
						// recibe el nombre
						String nombreArchivoABorrar = ois.readLine();
						File archivoABorrar = new File("src/datos/" + usuario + "/" + nombreArchivoABorrar);
						if (archivoABorrar.delete()) {
							oos.write("Archivo eliminado con éxito".getBytes());
							// AQUÍ FALTARÍA ELIMINAR EL ARCHIVO DEL XML
						} else
							oos.write("Error: no se ha podido eliminar el archivo".getBytes());
						break;
					case 5:
						//
						break;
					}
					op = ois.readInt();
				}
			}
		} catch (IOException | ParserConfigurationException | SAXException | TransformerException e) {
			e.printStackTrace();
		} finally {
			if (this.s != null)
				try {
					this.s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public boolean existeElElemento(String nombreElemento, String elemento, String archivoxml)
			throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		File xml = new File(archivoxml);
		Document docu = db.parse(xml);
		Element r = docu.getDocumentElement();
		NodeList nodelits = r.getElementsByTagName(elemento);
		for (int i = 0; i < nodelits.getLength(); i++) {
			Element e = (Element) nodelits.item(i);
			NodeList no = e.getElementsByTagName("nombre");
			if (nombreElemento.equals(no.item(0).getTextContent()))
				return true;
		}
		return false;
	}

}
