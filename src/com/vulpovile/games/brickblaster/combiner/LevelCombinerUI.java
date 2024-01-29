package com.vulpovile.games.brickblaster.combiner;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JList;

public class LevelCombinerUI extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;

	private ArrayList<File> levels = new ArrayList<File>();
	private ArrayList<String> titles = new ArrayList<String>();

	private JButton btnUp = new JButton("Up");
	private JButton btnDown = new JButton("Down");
	private JButton btnAdd = new JButton("Add");
	private JButton btnRemove = new JButton("Remove");
	private JButton btnCombine = new JButton("Combine");
	private JList list = new JList();

	private File lastFile = null;

	/**
	 * Create the frame.
	 */
	public LevelCombinerUI(JFrame parent) {
		super(parent, "BrickBlaster Level Combiner");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 400, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);

		panel.add(btnUp);
		panel.add(btnDown);
		panel.add(btnAdd);
		panel.add(btnRemove);
		panel.add(btnCombine);

		btnUp.addActionListener(this);
		btnDown.addActionListener(this);
		btnAdd.addActionListener(this);
		btnRemove.addActionListener(this);
		btnCombine.addActionListener(this);

		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);

		scrollPane.setViewportView(list);
	}

	public void updateLists() {
		list.setListData(titles.toArray());
	}

	public boolean swapInList(int x, int y) {
		if (x >= 0 && y >= 0 && x < titles.size() && y < titles.size())
		{
			String title = titles.remove(x);
			File file = levels.remove(x);
			titles.add(y, title);
			levels.add(y, file);
			return true;
		}
		return false;
	}

	public boolean removeFromList(int i) {
		if (i >= 0 && i < titles.size())
		{
			titles.remove(i);
			levels.remove(i);
			return true;
		}
		return false;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnAdd)
		{
			if(levels.size() >= 256)
			{
				JOptionPane.showMessageDialog(this, "You can not have more than 256 levels!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			JFileChooser jFileChooser = new JFileChooser(lastFile);
			jFileChooser.setMultiSelectionEnabled(true);
			jFileChooser.setFileFilter(new FileExtentionFilter("BrickBlaster Level", "bbl"));
			if (jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			{
				for (int i = 0; i < jFileChooser.getSelectedFiles().length; i++)
				{
					lastFile = jFileChooser.getSelectedFiles()[i];
					levels.add(lastFile);
					titles.add(lastFile.getName());
				}
				updateLists();
			}
		}
		else if (e.getSource() == btnUp)
		{
			int idx = list.getSelectedIndex();
			if (idx > -1 && swapInList(idx, idx - 1))
			{
				updateLists();
				list.setSelectedIndex(Math.max(0, idx - 1));
			}
		}
		else if (e.getSource() == btnDown)
		{
			int idx = list.getSelectedIndex();
			if (idx > -1 && swapInList(idx, idx + 1))
			{
				updateLists();
				list.setSelectedIndex(Math.min(titles.size() - 1, idx + 1));
			}
		}
		else if (e.getSource() == btnRemove)
		{
			int idx = list.getSelectedIndex();
			if (idx > -1 && removeFromList(idx))
			{
				updateLists();
				list.setSelectedIndex(Math.max(0, idx - 1));
			}
		}
		else if (e.getSource() == btnCombine)
		{
			if(levels.size() == 0)
			{
				JOptionPane.showMessageDialog(this, "You did not select any levels!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			JFileChooser jFileChooser = new JFileChooser(lastFile);
			jFileChooser.setMultiSelectionEnabled(false);
			jFileChooser.setFileFilter(new FileExtentionFilter("Combined BrickBlaster Level", "bcl"));
			if (jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION && jFileChooser.getSelectedFile() != null)
			{
				File file = jFileChooser.getSelectedFile();
				if (!file.toString().endsWith(".bcl"))
					file = new File(file.toString() + ".bcl");

				FileOutputStream fos = null;
				FileInputStream fis = null;

				try
				{
					fos = new FileOutputStream(file);
					//Magic number
					fos.write(0xCA);
					fos.write(0xFE);
					fos.write(0xBA);
					fos.write(0x11);
					//Number of levels
					fos.write(Math.min(255, levels.size()-1));
					byte[] buffer = new byte[64];
					for(int i = 0; i < Math.min(256, levels.size()); i++)
					{
						try{
						fis = new FileInputStream(levels.get(i));
						fis.read(buffer, 0, buffer.length);
						fos.write(buffer, 0, buffer.length);
						} finally {
							cleanClose(fis);
							fis = null;
						}
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				finally
				{
					cleanClose(fos);
					cleanClose(fis);
				}
			}
		}
	}

	public void cleanClose(Closeable c) {
		if (c != null)
		{
			try
			{
				c.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
