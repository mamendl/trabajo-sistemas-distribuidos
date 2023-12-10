package main;

import java.io.*;
import java.net.*;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
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
							Element nom = (Element) e.getElementsByTagName("nombre").item(0);
							if (usuario.equals(nom.getTextContent())) {
								encontrado = true;
								// System.out.println(usuario + " " + nom.getTextContent());
								Element c = (Element) e.getElementsByTagName("clave").item(0);
								if (clave.equals(c.getTextContent())) {
									corresto = true;
									// System.out.println(clave.equals(c.getTextContent()));
								} else {
									oos.write("Contraseña incorrecta\r\n".getBytes());
									oos.flush();
								}
							}
							i++;
						} while (i < nodes.getLength() && !encontrado);

						if (corresto) {
							this.usuario = usuario;
							oos.write("Te has loggeado correctamente\r\n".getBytes());
							oos.flush();
						}

						if (!encontrado) {
							oos.write("Error: usuario no encontrado\r\n".getBytes());
							oos.flush();
						}

						oos.writeBoolean(corresto);
						oos.flush();

						if (!corresto) {
							usuario = ois.readLine();
							clave = ois.readLine();
							encontrado = false;
						}

					}
					break;
				case 2:
					while (!corresto) {

						if (!existeElElemento(usuario, "usuario", "src/datos/usuarios.xml")) {

							Element nuevoUsuario = d.createElement("usuario");
							Element nom = d.createElement("nombre");
							nom.setTextContent(usuario);
							Element c = d.createElement("clave");
							c.setTextContent(clave);
							nuevoUsuario.appendChild(nom);
							nuevoUsuario.appendChild(c);
							root.appendChild(nuevoUsuario);

							guardarElXml(root, "src/datos/usuarios.xml");

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
						if (!corresto) {
							usuario = ois.readLine();
							clave = ois.readLine();
						}

					}
					break;
				}
				oos.flush();
				// oos.writeBoolean(corresto);
				// oos.flush();

				String suXml = "src/datos/" + this.usuario + "/" + this.usuario + ".xml";
				boolean borrado = false;
				File xml = new File(suXml);
				Document docu = db.parse(xml);
				Element raiz = docu.getDocumentElement();

				while (op != 6 && !borrado) {

					op = ois.readInt();

					switch (op) {
					case 1:
						// enviar su xml
						oos.writeObject(docu);
						oos.flush();
						break;
					case 2:
						// recibir y añadir a su xml
						boolean cancelar = ois.readBoolean();

						if (cancelar)
							break;

						String nombre = ois.readLine();
						String tam = ois.readLine();

						boolean subido = existeElElemento(nombre, "archivo", suXml);

						if (subido) {
							oos.writeBoolean(false);
							oos.flush();
							oos.write("Ese archivo ya lo has subido fiera.\r\n".getBytes());
							oos.flush();
							break;
						}

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
							}
						}

						// añadir al xml:

						Element archivo = docu.createElement("archivo");
						Element nom = docu.createElement("nombre");
						nom.setTextContent(nombre);
						Element size = docu.createElement("size");
						size.setTextContent(tam);
						archivo.appendChild(nom);
						archivo.appendChild(size);
						raiz.appendChild(archivo);

						TransformerFactory tf = TransformerFactory.newInstance();
						Transformer t = tf.newTransformer();
						DOMSource source = new DOMSource(raiz);
						StreamResult result = new StreamResult(
								new File("src/datos/" + usuario + "/" + usuario + ".xml"));
						t.transform(source, result);

						oos.write("Archivo subido correctamente.\r\n".getBytes());
						oos.flush();

						break;
					case 3:
						cancelar = false;
						oos.writeObject(raiz);
						oos.flush();
						if (raiz.getElementsByTagName("archivo").getLength() == 0)
							break;

						String nombreArchivo = ois.readLine();
						File archivoAmandar = new File("src/datos/" + usuario + "/" + nombreArchivo);
						boolean existe = archivoAmandar.exists();
						oos.writeBoolean(existe);
						oos.flush();
						cancelar = ois.readBoolean();
						while (!existe && !cancelar) {
							nombreArchivo = ois.readLine();
							archivoAmandar = new File("src/datos/" + usuario + "/" + nombreArchivo);
							existe = archivoAmandar.exists();
							oos.writeBoolean(existe);
							oos.flush();
							cancelar = ois.readBoolean();
						}
						if (cancelar)
							break;
						// lo manda
						byte[] buff = new byte[1024 * 1024];
						try (FileInputStream fos = new FileInputStream(archivoAmandar)) {
							oos.write((archivoAmandar.length() + "\r\n").getBytes());
							int leidos = fos.read(buff);
							int enviados = 0;
							while (enviados < archivoAmandar.length()) {
								oos.write(buff, 0, leidos);
								enviados = enviados + leidos;
								leidos = fos.read(buff);
								oos.flush();
							}
							oos.write("\u001a".getBytes());
							oos.flush();
						}

						oos.write("Archivo mandado correctamente.\r\n".getBytes());
						oos.flush();

						break;
					case 4:
						// borrar archivo
						cancelar = false;
						raiz = docu.getDocumentElement();
						oos.writeObject(raiz);
						if (raiz.getElementsByTagName("archivo").getLength() == 0)
							break;

						nombreArchivo = ois.readLine();
						File archivoAborrar = new File("src/datos/" + usuario + "/" + nombreArchivo);
						existe = archivoAborrar.exists();
						oos.writeBoolean(existe);
						oos.flush(); // lee una linea escribe el booleano
						while (!existe && !cancelar) {
							cancelar = ois.readBoolean();
							if (cancelar)
								break;
							nombreArchivo = ois.readLine();
							archivoAborrar = new File("src/datos/" + usuario + "/" + nombreArchivo);
							existe = archivoAborrar.exists();
							oos.writeBoolean(existe);
							oos.flush();

						}

						oos.writeBoolean(archivoAborrar.delete());
						oos.flush();
						// eliminarlo del xml
						NodeList ar = raiz.getElementsByTagName("archivo");
						int i = 0;
						encontrado = false;
						do {
							Element arc = (Element) ar.item(i);
							if (arc.getElementsByTagName("nombre").item(0).getTextContent().equals(nombreArchivo)) {
								System.out.println(arc.getElementsByTagName("nombre").item(0).getTextContent());
								arc.getParentNode().removeChild(arc);
								encontrado = true;
							}
							i++;
						} while (i < ar.getLength() && !encontrado);
						guardarElXml(raiz, suXml);

						break;
					case 5:
						borrado = ois.readBoolean();

						if (!borrado)
							break;

						i = 0;

						File carpeta = new File("src/datos/" + this.usuario);

						for (File file : carpeta.listFiles()) {
							file.delete();
						}

						if (carpeta.delete()) {
							ar = root.getElementsByTagName("usuario");
							do {
								Element arc = (Element) ar.item(i);
								if (arc.getElementsByTagName("nombre").item(0).getTextContent().equals(this.usuario)) {
									arc.getParentNode().removeChild(arc);
									encontrado = true;
								}
								i++;
							} while (i < ar.getLength() && !encontrado);
							guardarElXml(root, "src/datos/usuarios.xml");
							oos.write("Usuario eliminado correctamente.\r\n".getBytes());
						} else {
							oos.write("Ha habido un problema borrando tu carpetita.\r\n".getBytes());
						}
						oos.flush();

						break;
					}

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

	private void guardarElXml(Element root, String xml) throws TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer();
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(root);
		StreamResult result = new StreamResult(new File(xml));
		t.transform(source, result);
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
