package com.mrboomdev.awery.desktop.ui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JComponent;

public class Button extends JComponent {
	private Color textColor, backgroundColor;
	private String text;

	public Button(String text) {
		setText(text);
		setTextColor(Color.BLACK);
		setBackgroundColor(Color.BLUE);
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(backgroundColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(textColor);
		g.setFont(Font.getFont("Monospaced"));
		g.drawString(text, 0, 0);
	}
}