package com.mrboomdev.awery.desktop;

import com.mrboomdev.awery.desktop.ui.components.Button;
import com.mrboomdev.awery.ext.data.Image;

import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Main {

	public static void main(String[] args) {
		var toolkit = Toolkit.getDefaultToolkit();

		var frame = new JFrame();
		frame.setTitle("Awery");
		frame.setAutoRequestFocus(true);
		frame.setResizable(true);

		frame.setSize(
				Math.round(toolkit.getScreenSize().width * .75f),
				Math.round(toolkit.getScreenSize().height * .75f));

		frame.setLocation(
				toolkit.getScreenSize().width / 2 - frame.getWidth() / 2,
				toolkit.getScreenSize().height / 2 - frame.getHeight() / 2);

		var label = new JLabel("Welcome to Awery!");
		label.setBounds(200, 0, 200, 200);
		frame.add(label);

		var button = new Button("Hello, World!");
		button.setBounds(150, 200, 220, 50);
		frame.add(button);

		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}