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

public class Cliente {
	public static void main(String[] args) {
		try(Socket s = new Socket("localhost",55555);
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
				) {
			//Ventana t = (Ventana) ois.readObject();
			//System.out.println("¿Interfaz o consola?");
			//System.out.println("1. Interfaz");
			//System.out.println("2. Consola");
			//System.out.println("3. No quiero seguir me da amsiedad tomar decisiones :(");
			//Scanner sc = new Scanner(System.in);
			//int

			System.out.println("1. Loggearse");
			System.out.println("2. Darse de alta");
			System.out.println("3. Desconectar");
			int option = 0;
			Scanner sc = new Scanner(System.in);
			String linea;
			while(option==0||option>3||option<0) {
				try{
					linea = sc.nextLine();
					option = Integer.parseInt(linea);
				} catch(NumberFormatException nfe){
					System.out.println("Introduce un número válido imbécil");
				}
			}

			oos.writeInt(option);
			oos.flush();
			if(option==3) System.exit(0);
						
			System.out.print("Usuario: ");
			String nom = sc.nextLine();
			System.out.print("Contraseña: ");
			String contrasena = sc.nextLine(); 
			oos.write((nom+"\r\n"+contrasena+"\r\n").getBytes());
			oos.flush();
			System.out.println("Usuario: "+nom+"\nContraseña: "+contrasena);

			boolean correcto;
			correcto = ois.readBoolean();
			switch (option) {
			case 1:		//loggearse:
				if(correcto) System.out.println("enhorabuena, te has loggeado con éxito");
				else System.out.println("parece que ha habido un error con tu usuario y/o contraseña");
				break;
			case 2:		//darse de alta:
				if(correcto) System.out.println("Te has registrado correctamente");
				break;
			}
			
			option = 0;
			if(correcto) {System.out.println(":D");}
			System.out.println("Bienvenido a Drive Safe. Seleccione una opción:");
			System.out.println("1. Consultar archivos");
			System.out.println("2. Subir archivo");
			System.out.println("3. Descargar archivo");
			System.out.println("4. Desconectar");
			while(option==0||option>4||option<0) {
				try{
					linea = sc.nextLine();
					option = Integer.parseInt(linea);
				} catch(NumberFormatException nfe){
					System.out.println("Introduce un número válido imbécil");
				}
			}
			switch (option) {
			case 1: 
				//consultar archivos
				
				break;
			case 2:
				
				break;
			case 3:
				
				break;
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
