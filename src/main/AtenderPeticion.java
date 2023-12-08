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

	public AtenderPeticion(Socket s) {
		this.s = s;
	}

	@Override
	public void run() {
		try (ObjectOutputStream oos = new ObjectOutputStream(this.s.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(this.s.getInputStream())) {
			int op = ois.readInt();

			if (op != 3) {

				op = ois.readInt();

				String usuario = ois.readLine();
				System.out.println(usuario);
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

					NodeList nodes = root.getElementsByTagName("usuario");
					int i = 0;
					do {
						Element e = (Element) nodes.item(i);
						Element nom = (Element) e.getElementsByTagName("nombre").item(0);
						if (usuario.equals(nom.getTextContent())) {
							encontrado = true;
							Element c = (Element) e.getElementsByTagName("clave").item(0);
							if (clave.equals(c.getTextContent()))
								corresto = true;
							else
								oos.write("Contraseña incorrecta\r\n".getBytes());
						}
						i++;
					} while (i < nodes.getLength() && !encontrado);

					if (corresto)
						oos.write("Te has loggeado correctamente\r\n".getBytes());
					if (!encontrado)
						oos.write("Error: usuario no encontrado\r\n".getBytes());
					break;
				case 2:
					if (root.getElementsByTagName(usuario).getLength() == 0) {
						Element nuevoUsuario = d.createElement("usuario");
						Element nom = d.createElement("nombre");
						nom.setTextContent(usuario);
						Element c = d.createElement("clave");
						c.setTextContent(clave);
						// root.insertBefore(c, d)
						// Element s = (Element) d.createTextNode("\r\n\t");
						nuevoUsuario.appendChild(nom);
						nuevoUsuario.appendChild(c);
						// nuevoUsuario.setTextContent("\r\n\t");
						root.appendChild(nuevoUsuario);

						TransformerFactory tf = TransformerFactory.newInstance();
						Transformer t = tf.newTransformer();
						DOMSource source = new DOMSource(root);
						StreamResult result = new StreamResult(new File("src/datos/usuarios.xml"));
						t.transform(source, result);

						try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream(new File("src/datos/" + usuario + "/" + usuario + ".xml"))))) {
							bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
									+ "<!DOCTYPE archivos SYSTEM \"../archivos.dtd\" >\r\n" + "<archivos>\r\n"
									+ "</archivos>");
						}

						oos.write("Te has registrado correctamente\r\n".getBytes());

						corresto = true;
					} else {

						oos.write("El nombre que has introducido ya existe\r\n".getBytes());

						corresto = false;
					}
					break;
				}
				oos.flush();
				oos.writeBoolean(corresto);
				oos.flush();
				op = ois.readInt();
				while(op!=6&&op!=5) {
					switch (op) {
					case 1:
						// enviar su xml
						File fich = new File("src/datos/" + usuario + "/" + usuario + ".xml");
						Document doc = db.parse(fich);
						oos.writeObject(doc);
						break;
					case 2:
						// recibir y añadir a su xml
						break;
					case 3:
						// recibir el nombre, buscarlo, enviar un mensaje si existe o no y enviar el
						// archivo
						break;
					case 4:
						break;
					case 5:
						break;
					case 6:
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

}
