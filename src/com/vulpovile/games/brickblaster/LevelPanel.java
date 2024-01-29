package com.vulpovile.games.brickblaster;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;

import javax.swing.JPanel;

public class LevelPanel extends JPanel implements MouseListener, MouseMotionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	byte[] field = new byte[128];

	/**
	 * Create the panel.
	 */
	public LevelPanel() {
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	Color[] colors = new Color[] { Color.BLACK, Color.BLUE, Color.GREEN, Color.CYAN, Color.RED, Color.MAGENTA, Color.YELLOW, Color.GRAY };

	byte selectedVal = 1;

	int lastPressedButton = -1;

	private byte mousey, mousex;

	private boolean unsavedChanges = false;

	public void paintComponent(Graphics g) {
		g.setColor(Color.darkGray);
		g.fillRect(0, 0, getWidth(), getHeight());
		int w = getWidth() / 8;
		int h = (getHeight() - getHeight() / 4) / 16;

		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 16; j++)
			{
				byte block = field[(j * 8) + i];
				g.setColor(colors[block]);
				g.fillRect(i * w, j * h, w - (getWidth() / 128), h - (getWidth() / 128));
			}
		}

		g.setColor(colors[selectedVal]);
		if(mousex >= 0 && mousex < 8 && mousey >= 0 && mousey < 16)
			g.drawRect(mousex * w, mousey * h, w - (getWidth() / 128), h - (getWidth() / 128));
	}

	public void mouseDragged(MouseEvent arg0) {
		int w = getWidth() / 8;
		int h = (getHeight() - getHeight() / 4) / 16;
		mousex = (byte) (arg0.getX() / w);
		mousey = (byte) (arg0.getY() / h);

		int idx = (mousey * 8) + mousex;
		if (idx >= 0 && idx < field.length && mousex < 8)
		{
			if (lastPressedButton == MouseEvent.BUTTON3)
				field[idx] = 0;
			else if (lastPressedButton == MouseEvent.BUTTON1)
				field[idx] = selectedVal;
			unsavedChanges = true;
		}

		repaint();
	}

	public void mouseMoved(MouseEvent arg0) {
		int w = getWidth() / 8;
		int h = (getHeight() - getHeight() / 4) / 16;
		mousex = (byte) (arg0.getX() / w);
		mousey = (byte) (arg0.getY() / h);
		repaint();
	}

	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent arg0) {
		int idx = (mousey * 8) + mousex;
		if (idx >= 0 && idx < field.length && mousex < 8)
		{
			lastPressedButton = arg0.getButton();
			if (lastPressedButton == MouseEvent.BUTTON3)
			{
				field[idx] = 0;
				unsavedChanges = true;
			}
			else if (lastPressedButton == MouseEvent.BUTTON1)
			{
				field[idx] = selectedVal;
				unsavedChanges = true;
			}
			else selectedVal = field[idx];
			repaint();
		}
	}

	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void clear() {
		setChangesSaved();
		Arrays.fill(field, (byte) 0);
		repaint();
	}

	public void setChangesSaved() {
		this.unsavedChanges = false;
	}

	public boolean hasChanges() {
		return unsavedChanges;
	}

}
