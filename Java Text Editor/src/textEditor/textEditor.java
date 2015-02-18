/**
 * Application Name : Java Text Editor
 * Author : Zachary Wells
 * Version : 0.1
 * Additional Notes : This is a very simple text editor made as a
 * little side project. There is nothing fancy about this program,
 * but it's fun to play around with.
 */

package textEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.undo.UndoManager;

public class textEditor extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	
	private String title = "Java Text Editor";
	
	private JMenuBar bar;
	private JMenu file, edit, view;
	private JMenuItem New, Open, Save, SaveAs; //File Items
	private JMenuItem Undo, Redo;			   //File Items
	private JCheckBoxMenuItem onTop;           //View Items
	private JLabel XButton, MinButton, MaxButton;
	
	private JTextPane text;
	private Font font = new Font("Verdana", 0, 14);
	
	private UndoManager undo = new UndoManager();
	private boolean OnTop = false;
	private int xoff = 0, yoff = 0, xoffScreen, yoffScreen;
	
	private boolean maximised = false; private int origW, origH, origX, origY;
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	double width = screenSize.getWidth();
	double height = screenSize.getHeight();
	
	public textEditor() {
		super("Java Text Editor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(title);
		setUndecorated(true);
		
		bar = new JMenuBar();
		bar.setBorder(BorderFactory.createTitledBorder(null, title, 2, 3, font, Color.BLACK));
		bar.setBackground(new Color(0xFFFFFF));
		setJMenuBar(bar);
		
		bar.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) { 
				xoff = e.getX();
				yoff = e.getY();
				xoffScreen = e.getXOnScreen();
				yoffScreen = e.getYOnScreen();
			}
			public void mouseReleased(MouseEvent e) {
				if (e.getYOnScreen() == 0)
					toggleMax(false, maxX | maxY);
				if (getY() < 0) setLocation(getX(), 0);
			}
		});
		bar.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {}
			public void mouseDragged(MouseEvent e) {
				if (!maximised) {
					setLocation(e.getX()+getX()-xoff, e.getY()+getY()-yoff);
				}
				else
					toggleMax(true, maxX | maxY);
			}
		});
		
		file = new JMenu("File"); file.setFont(font); file.setForeground(Color.BLACK); bar.add(file);
		edit = new JMenu("Edit"); edit.setFont(font); edit.setForeground(Color.BLACK); bar.add(edit);
		view = new JMenu("View"); view.setFont(font); view.setForeground(Color.BLACK); bar.add(view);
		
		XButton = new JLabel(" X ");
		XButton.setForeground(Color.GRAY);
		XButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		XButton.setBorder(BorderFactory.createTitledBorder(""));
		XButton.addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				dispose();
				System.exit(0);
			}
		});
		MaxButton = new JLabel(" \u1010 ");
		MaxButton.setForeground(Color.GRAY);
		MaxButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		MaxButton.setBorder(BorderFactory.createTitledBorder(""));
		MaxButton.addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				toggleMax(false, maxX | maxY);
			}
		});
		MinButton = new JLabel(" _ ");
		MinButton.setForeground(Color.GRAY);
		MinButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		MinButton.setBorder(BorderFactory.createTitledBorder(""));
		MinButton.addMouseListener(new MouseListener() {
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				setState(JFrame.ICONIFIED);
			}
		});
		
		bar.add(Box.createGlue());
		JLabel version = new JLabel("v0.1  "); version.setForeground(Color.GRAY);
		bar.add(version);
		
		bar.add(MinButton);
		bar.add(new JLabel(" "));
		bar.add(MaxButton);
		bar.add(new JLabel(" "));
		bar.add(XButton);
		
		New = new JMenuItem("New"); file.add(New);  //File Items
		New.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { New(); } });
		Open = new JMenuItem("Open"); file.add(Open);
		Open.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { Open(); } });
		Save = new JMenuItem("Save"); file.add(Save);
		Save.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { Save(); } });
		SaveAs = new JMenuItem("Save As"); file.add(SaveAs);
		SaveAs.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { SaveAs(); } });
		
		Undo = new JMenuItem("Undo"); edit.add(Undo); //Edit Items
		Undo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (undo.canUndo())
					undo.undo();
				else
					Toolkit.getDefaultToolkit().beep();
			}
		});
		Redo = new JMenuItem("Redo"); edit.add(Redo);
		Redo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (undo.canRedo())
					undo.redo();
				else
					Toolkit.getDefaultToolkit().beep();
			}
		});
		//edit.add(new JSeparator());
		
		onTop = new JCheckBoxMenuItem("Always On Top"); view.add(onTop); //View Items
		onTop.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { toggleOnTop(); } });
		
		contentPane = new JPanel();
		contentPane.setLayout(new GridBagLayout());
		contentPane.setBorder(BorderFactory.createTitledBorder(null, "", 3, 1));
		setContentPane(contentPane);
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.weightx = 1;
		gbc.weighty = 1;
		
		text = new JTextPane();
		text.setFont(font);
		text.setBackground(new Color(0xFEFEFE));
		text.setForeground(new Color(0));
		JPanel noWrap = new JPanel(new BorderLayout());
		noWrap.add(text, BorderLayout.CENTER);
		JScrollPane scroll = new JScrollPane(noWrap);
		scroll.setBorder(null);
		contentPane.add(scroll, gbc);
		
	    text.getDocument().addUndoableEditListener(undo);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.weightx = -1;
		
		JSeparator dragLeft = new JSeparator(1);
		dragLeft.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
		dragLeft.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {}
			public void mouseDragged(MouseEvent e) {
				int size = getWidth()-e.getX();
				if (size < 105) { size = 105; return; }
				setSize(size, getHeight());
				setLocation(getX()+e.getX(), getY());
			}
		});
		contentPane.add(dragLeft, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.weightx = -1;
		
		JSeparator dragRight = new JSeparator(1);
		dragRight.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
		dragRight.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {}
			public void mouseDragged(MouseEvent e) {
				int size = getWidth()+e.getX();
				if (size < 105) size = 105;
				setSize(size, getHeight());
			}
		});
		contentPane.add(dragRight, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.PAGE_END;
		gbc.weighty = -1;
		
		JSeparator dragDown = new JSeparator(0);
		dragDown.setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
		dragDown.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {}
			public void mouseDragged(MouseEvent e) {
				int size = getHeight() + e.getY();
				if (size < 75) size = 75;
				setSize(getWidth(), size);
				
				if (e.getYOnScreen() == 0)
					toggleMax(false, maxY);
			}
		});
		contentPane.add(dragDown, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.LAST_LINE_END;
		gbc.weightx = -0.1;
		gbc.weighty = -0.1;
		
		JLabel dragSE = new JLabel("");
		dragSE.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
		dragSE.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {}
			public void mouseDragged(MouseEvent e) {
				int sizex = getWidth() + e.getX();
				int sizey = getHeight() + e.getY();
				if (sizex < 105) sizex = 105;
				if (sizey < 75) sizey = 75;
				setSize(sizex, sizey);
			}
		});
		contentPane.add(dragSE, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.LAST_LINE_START;
		gbc.weightx = -0.1;
		gbc.weighty = -0.1;
		
		JLabel dragSW = new JLabel("");
		dragSW.setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR));
		dragSW.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {}
			public void mouseDragged(MouseEvent e) {
				int sizex = getWidth() - e.getX(),
						sizey = getHeight() + e.getY(),
						movex = getX() + e.getX();
				if (sizex < 105) { sizex = 105; movex = getX(); }
				if (sizey < 75) sizey = 75;
				setSize(sizex, sizey);
				setLocation(movex, getY());
			}
		});
		contentPane.add(dragSW, gbc);
		
		addWindowListener(new WindowListener() {
			public void windowClosed(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
			public void windowActivated(WindowEvent e) {
				bar.setBorder(BorderFactory.createTitledBorder(null, title, 2, 3, font, Color.BLACK));
				file.setForeground(Color.BLACK);
				edit.setForeground(Color.BLACK);
				view.setForeground(Color.BLACK);
				if (OnTop)
					e.getWindow().setOpacity(1f);
			}
			public void windowDeactivated(WindowEvent e) {
					bar.setBorder(BorderFactory.createTitledBorder(null, title, 2, 3, font, Color.GRAY));
					file.setForeground(Color.GRAY);
					edit.setForeground(Color.GRAY);
					view.setForeground(Color.GRAY);
				if (OnTop)
					e.getWindow().setOpacity(0.8f);
			}
		});
		
		pack();
		setSize(500, 350);
		setLocationRelativeTo(null);		
		setVisible(true);
	}
	
	public void setTitleAll(String title) {
		this.title = title;
		bar.setBorder(BorderFactory.createTitledBorder(null, title, 2, 3, font, Color.BLACK));
		setTitle(title);
	}
	
	public void toggleOnTop() {
		OnTop = !OnTop;
		setAlwaysOnTop(OnTop);
	}
	
	static int maxX = 0b10, maxY = 0b01;
	public void toggleMax(boolean drag, int direction) {
		if (!maximised) {
			origW = getWidth();
			origH = getHeight();
			origX = getX();
			origY = getY();
			switch(direction) {
			case 0b10 : // x
				setExtendedState(JFrame.MAXIMIZED_HORIZ);
				break;
			case 0b01 : // y
				setExtendedState(JFrame.MAXIMIZED_VERT);
				break;
			case 0b11 : //  x | y
				setExtendedState(JFrame.MAXIMIZED_BOTH);
				break;
			}
			setState(JFrame.NORMAL);
			maximised = true;
		} else if (!drag) {
			setState(JFrame.NORMAL);
			setSize(origW, origH);
			setLocation(origX, (origY < 0) ? 0 : origY);
			maximised = false;
		} else {
			setState(JFrame.NORMAL);
			setSize(origW, origH);
			setLocation(xoffScreen - origW/2, yoffScreen);
			xoff = xoffScreen - xoff + origW/2; yoff = yoffScreen;
			maximised = false;
		}
	}
	
	//IO
	boolean changed = false;
	String filePath = null;
	public void New() {
		filePath = null;
		text.setText("");
		setTitleAll("Java Text Editor - New File");
	}
	public void Open() {
		if (OnTop)
			setAlwaysOnTop(false);
		FileDialog dialog = new FileDialog(this, "Open", FileDialog.LOAD);
		dialog.setVisible(true);
		if (dialog.getFile() != null) {
			filePath = dialog.getDirectory() + dialog.getFile();
            FileReader input = null;
			try {
				input = new FileReader(filePath);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
            BufferedReader bufferReader = new BufferedReader(input);
            String content = "", line = "";
            
            try {
				while ((line = bufferReader.readLine()) != null)   {
					content += line + "\n";
				}
				bufferReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
            text.setText(content);
            setTitleAll("Java Text Editor - " + dialog.getFile());
		}
		if (OnTop)
			setAlwaysOnTop(true);
	}
	public void Save() {
		if (filePath == null) {
			if (OnTop)
				setAlwaysOnTop(false);
			FileDialog dialog = new FileDialog(this, "Save", FileDialog.SAVE);
			dialog.setVisible(true);
			if (dialog.getFile() != null) {
				filePath = dialog.getDirectory() + dialog.getFile();
				FileWriter fw;
				try {
					File file = new File(filePath);
					file.createNewFile();
					fw = new FileWriter(filePath);
					BufferedWriter bw = new BufferedWriter(fw);
				    bw.write(text.getText());
				    bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				setTitleAll("Java Text Editor - " + dialog.getFile());
			}
			if (OnTop)
				setAlwaysOnTop(true);
		} else {
			FileWriter fw;
			try {
				fw = new FileWriter(filePath);
				BufferedWriter bw = new BufferedWriter(fw);
			    bw.write(text.getText());
			    bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void SaveAs() {
		if (OnTop)
			setAlwaysOnTop(false);
		FileDialog dialog = new FileDialog(this, "Save as", FileDialog.SAVE);
		dialog.setVisible(true);
		if (dialog.getFile() != null) {
			filePath = dialog.getDirectory() + dialog.getFile();
			FileWriter fw;
			try {
				File file = new File(filePath);
				if (file.exists()) file.createNewFile();
				fw = new FileWriter(filePath);
				BufferedWriter bw = new BufferedWriter(fw);
			    bw.write(text.getText());
			    bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			setTitleAll("Java Text Editor - " + dialog.getFile());
		}
		if (OnTop)
			setAlwaysOnTop(true);
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new textEditor();
			}
		});
	}
}
