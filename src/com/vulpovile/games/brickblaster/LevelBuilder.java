package com.vulpovile.games.brickblaster;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.vulpovile.games.brickblaster.combiner.LevelCombinerUI;

public class LevelBuilder extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	LevelPanel pnlLevel = new LevelPanel();
	private JMenuBar menuBar;
	private JMenu mnFile;
	JMenuItem mntmNew = new JMenuItem("New");
	JMenuItem mntmSave = new JMenuItem("Save");
	JMenuItem mntmLoad = new JMenuItem("Load");
	JMenuItem mntmLevelCombiner = new JMenuItem("Combine Levels...");
	JMenuItem mntmLoadLegacy = new JMenuItem("Load Legacy");
	JMenuItem mntmConvertDirectory = new JMenuItem("Convert Directory");
	
	private File lastFile = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try
				{
					LevelBuilder frame = new LevelBuilder();
					frame.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public LevelBuilder() {
		super("BrickBlaster Level Builder - a0.0.3");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		menuBar.add(mnFile);
		mnFile.add(mntmNew);
		mnFile.add(mntmSave);
		mnFile.add(mntmLoad);
		mnFile.add(mntmLoadLegacy);
		mnFile.add(mntmConvertDirectory);
		mnFile.add(mntmLevelCombiner);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel pnlColors = new JPanel();
		contentPane.add(pnlColors, BorderLayout.NORTH);

		for (int i = 0; i < pnlLevel.colors.length; i++)
		{
			final byte ibyte = (byte) i;
			JButton jButton = new JButton();
			jButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					pnlLevel.selectedVal = ibyte;
				}
			});
			jButton.setBackground(pnlLevel.colors[i]);
			pnlColors.add(jButton);
		}

		contentPane.add(pnlLevel, BorderLayout.CENTER);

		mntmNew.addActionListener(this);
		mntmSave.addActionListener(this);
		mntmLoad.addActionListener(this);
		mntmLoadLegacy.addActionListener(this);
		mntmConvertDirectory.addActionListener(this);
		mntmLevelCombiner.addActionListener(this);
	}

	public JFileChooser getDefaultChooser() {
		JFileChooser fileChooser = new JFileChooser(lastFile);
		fileChooser.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File arg0) {
				return arg0.isDirectory() || arg0.getName().toLowerCase().endsWith(".bbl");
			}

			@Override
			public String getDescription() {
				return "BrickBlaster Levels (*.bbl)";
			}
		});
		return fileChooser;
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == mntmNew)
		{
			if (!pnlLevel.hasChanges() || JOptionPane.showConfirmDialog(this, "You have unsaved changes! Continue?", "Unsaved Changes", JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
			{
				pnlLevel.clear();
			}
		}
		else if (arg0.getSource() == mntmConvertDirectory)
		{
			JFileChooser fileChooser = new JFileChooser(lastFile);
			if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			{
				File[] files = fileChooser.getSelectedFile().getParentFile().listFiles();
				for (int i = 0; i < files.length; i++)
				{
					if (files[i].getName().endsWith(".bbl"))
					{
						boolean errored = false;
						File backup = new File(files[i] + ".bak");
						File tmp = new File(files[i] + ".tmp");
						FileInputStream fis = null;
						FileOutputStream fos = null;
						try
						{
							fis = new FileInputStream(files[i]);
							fos = new FileOutputStream(tmp);
							byte[] oldBytes = new byte[128];
							fis.read(oldBytes, 0, oldBytes.length);
							for(int j = 0; j < oldBytes.length; j+=2)
							{
								fos.write((oldBytes[j] & 0x0F) | (oldBytes[j + 1] << 4 & 0xF0));
							}
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
							errored = true;
						}
						finally
						{
							cleanClose(fis);
							cleanClose(fos);
						}
						if(!errored)
						{
							files[i].renameTo(backup);
							tmp.renameTo(files[i]);
						}
					}
				}
				JOptionPane.showMessageDialog(this, "Converted!");
			}
		}
		else if (arg0.getSource() == mntmLoad)
		{
			if (!pnlLevel.hasChanges() || JOptionPane.showConfirmDialog(this, "You have unsaved changes! Continue?", "Unsaved Changes", JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
			{
				JFileChooser fileChooser = getDefaultChooser();
				if (fileChooser.showOpenDialog(LevelBuilder.this) == JFileChooser.APPROVE_OPTION)
				{
					File file = fileChooser.getSelectedFile();
					if (file != null)
					{
						lastFile = file;
						FileInputStream fos = null;
						try
						{
							fos = new FileInputStream(file);
							for (int i = 0; i < pnlLevel.field.length; i += 2)
							{
								int read = fos.read();
								if (read != -1)
								{
									pnlLevel.field[i] = (byte) (read & 0x0F);
									pnlLevel.field[i + 1] = (byte) ((read & 0xF0) >> 4);
								}
							}
							pnlLevel.setChangesSaved();
							pnlLevel.repaint();
						}
						catch (IOException e)
						{
							e.printStackTrace();
							JOptionPane.showMessageDialog(LevelBuilder.this, "Failed to load level:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
						finally
						{
							if (fos != null)
							{
								try
								{
									fos.close();
								}
								catch (IOException e)
								{
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
		else if (arg0.getSource() == mntmLoadLegacy)
		{
			if (!pnlLevel.hasChanges() || JOptionPane.showConfirmDialog(this, "You have unsaved changes! Continue?", "Unsaved Changes", JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
			{

				JFileChooser fileChooser = getDefaultChooser();
				if (fileChooser.showOpenDialog(LevelBuilder.this) == JFileChooser.APPROVE_OPTION)
				{
					File file = fileChooser.getSelectedFile();
					if (file != null)
					{
						lastFile = file;
						FileInputStream fos = null;
						try
						{
							fos = new FileInputStream(file);
							fos.read(pnlLevel.field, 0, pnlLevel.field.length);
							pnlLevel.setChangesSaved();
							pnlLevel.repaint();
						}
						catch (IOException e)
						{
							e.printStackTrace();
							JOptionPane.showMessageDialog(LevelBuilder.this, "Failed to load level:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
						finally
						{
							cleanClose(fos);
						}
					}
				}
			}
		}
		else if (arg0.getSource() == mntmSave)
		{
			JFileChooser fileChooser = getDefaultChooser();
			if (fileChooser.showSaveDialog(LevelBuilder.this) == JFileChooser.APPROVE_OPTION)
			{
				File file = fileChooser.getSelectedFile();
				if (file != null)
				{
					lastFile = file;
					if (!file.getName().toLowerCase().endsWith(".bbl"))
					{
						file = new File(file.getAbsoluteFile() + ".bbl");
					}
					FileOutputStream fos = null;
					try
					{
						fos = new FileOutputStream(file);
						for (int i = 0; i < pnlLevel.field.length; i += 2)
						{
							fos.write((pnlLevel.field[i] & 0x0F) | (pnlLevel.field[i + 1] << 4 & 0xF0));
						}
						pnlLevel.setChangesSaved();
					}
					catch (IOException e)
					{
						e.printStackTrace();
						JOptionPane.showMessageDialog(LevelBuilder.this, "Failed to save level:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
					finally
					{
						cleanClose(fos);
					}
				}
			}
		}
		else if(arg0.getSource() == mntmLevelCombiner)
		{
			LevelCombinerUI lcu = new LevelCombinerUI(this);
			lcu.setVisible(true);
		}
	}

	public static void cleanClose(Closeable c) {
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
