package main;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

public class Servidor {
	public static void main(String[] args) {
		ExecutorService pool = Executors.newCachedThreadPool();
		try(ServerSocket ss = new ServerSocket(55555)){
			Socket cliente = null;
			while(true) {
				try{
					cliente = ss.accept();
					AtenderPeticion r = new AtenderPeticion(cliente);
					pool.execute(r);
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
