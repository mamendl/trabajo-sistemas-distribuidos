package main;

import java.net.*;

public class Hilo implements Runnable {

	private Socket s;
	
	public Hilo(Socket s) {
		this.s = s;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//finally{serrar el socket del cliente}
	}
	
}
