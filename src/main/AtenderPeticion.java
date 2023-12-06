package main;

import java.io.*;
import java.net.*;

public class Hilo implements Runnable {

	private Socket s;
	
	public Hilo(Socket s) {
		this.s = s;
	}
	
	@Override
	public void run() {
		try(BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()))){
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
