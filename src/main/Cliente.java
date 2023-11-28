package main;

import java.io.*;
import java.net.*;

public class Cliente {
	public static void main(String[] args) {
		try(Socket s = new Socket("localhost",55555);
				ObjectInputStream ois = new ObjectInputStream(s.getInputStream());) {
			
			
			
			//Ventana t = (Ventana) ois.readObject();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} /*catch (ClassNotFoundException e) {
			e.printStackTrace();
		}*/
	}
}
