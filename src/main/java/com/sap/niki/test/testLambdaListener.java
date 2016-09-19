package com.sap.niki.test;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

public class testLambdaListener {
	  public static void main(String[] args) {
		           
		       JButton testButton = new JButton("Test Button");
		       testButton.addActionListener(new ActionListener(){
		    	   public void actionPerformed(ActionEvent ae){
		    		   System.out.println("Click Detected by Anon Class");
		    	   }
		       });
		       
		       testButton.addActionListener(e -> System.out.println("Click Detected by Lambda Listner"));
		       
		       // Swing stuff
		       JFrame frame = new JFrame("Listener Test");
		       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		       frame.add(testButton, BorderLayout.CENTER);
		       frame.pack();
		       frame.setVisible(true);
		       
		     }
		   }
