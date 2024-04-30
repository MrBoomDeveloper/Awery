package com.mrboomdev.awery.desktop;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Main {
	private static final int SIZE_MULTIPLIER = 75;

	public static void main(String[] args) {
		var frame = new JFrame();
		frame.setTitle("Awery");
		frame.setSize(16 * SIZE_MULTIPLIER, 9 * SIZE_MULTIPLIER);
		frame.setResizable(true);

		var label = new JLabel("Welcome to Awery!");
		label.setBounds(200, 0, 200, 200);
		frame.add(label);

		var button = new JButton("Hello, World!");
		button.setBounds(150, 200, 220, 50);
		frame.add(button);

		frame.setLayout(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}