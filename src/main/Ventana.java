package main;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

public class Ventana extends javax.swing.JFrame{
	
	private Component jl = new JLabel("Arrastra un archivo putito");
	
	public Ventana() {
		JFrame frame = new JFrame();
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new GridLayout(0,1));
				
		//
		
		frame.add(panel,BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setTitle("juju");
		frame.add(this.jl);
		frame.pack();
		frame.setVisible(true);
		
	}
	
	public static File[] getDropFiles(DropTargetDropEvent dtde) {
		try {
			if(dtde.getDropAction()==DnDConstants.ACTION_MOVE) {
				dtde.acceptDrop(dtde.getDropAction());
				final Transferable transferable = dtde.getTransferable();
				if(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					ArrayList<File> listFiles = (ArrayList) transferable.getTransferData(DataFlavor.javaFileListFlavor);
					dtde.dropComplete(true);
					return listFiles.toArray(File[]::new);
				}
			}
			return null;
		} catch (UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void drop(DropTargetDropEvent evt) {
		File[] files = Ventana.getDropFiles(evt);
		JLabel jl = (JLabel) this.jl;
		for (File file : files) {
			jl.setText(file.getName());
		}
		
	}
	
	public static void main(String[] args) {
		new Ventana();
	}
}
