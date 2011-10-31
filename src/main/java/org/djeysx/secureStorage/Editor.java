package org.djeysx.secureStorage;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.djeysx.secureStorage.io.IOUtils;

public class Editor implements ActionListener, DropTargetListener {
	private static final String TITLE_APPNAME = " - AES Editor";
	private final JFrame jframe = new JFrame("New File" + TITLE_APPNAME);
	private final JTextArea jtextarea = new JTextArea("");
	private File currentFile = null;
	private String currentPassword;
	private boolean modified = false;

	public static void main(String[] args) {
		new Editor().run(args);
	}

	public void run(String[] args) {

		jframe.setSize(600, 400);
		jframe.setPreferredSize(new Dimension(600, 400));
		jframe.setMinimumSize(new Dimension(200, 200));

		jframe.addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) {
			}

			public void windowClosed(WindowEvent e) {
			}

			public void windowClosing(WindowEvent e) {
				jframe.dispose();
			}

			public void windowDeactivated(WindowEvent e) {
			}

			public void windowDeiconified(WindowEvent e) {
			}

			public void windowIconified(WindowEvent e) {
			}

			public void windowOpened(WindowEvent e) {
			}
		});

		jtextarea.setAutoscrolls(true);
		jtextarea.setEditable(true);
		jtextarea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		jtextarea.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
				if (modified == false) {
					if (((e.getModifiers() & ActionEvent.CTRL_MASK) != 0)
							|| ((e.getModifiers() & ActionEvent.ALT_MASK) != 0)) {
						// do nothing
					} else {
						modified = true;
						jframe.setTitle("* " + (currentFile != null ? currentFile.getName() : "new file")
								+ TITLE_APPNAME);
					}
				}
			}
		});
		DropTarget dt = new DropTarget(jtextarea, this);

		JScrollPane jscrollPane = new JScrollPane(jtextarea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		jframe.add(jscrollPane);
		jframe.setLocationByPlatform(true);

		// Where the GUI is created:
		JMenuBar menuBar;
		JMenu menu;
		JMenuItem menuItem;

		// Create the menu bar.
		menuBar = new JMenuBar();

		// Build the first menu.
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);

		// a group of JMenuItems
		menuItem = new JMenuItem("Open", KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuItem.setActionCommand("open");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		menuItem = new JMenuItem("Save", KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		menuItem.setActionCommand("save");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		menuItem = new JMenuItem("Save as...", KeyEvent.VK_A);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
		menuItem.setActionCommand("save as");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		jframe.setJMenuBar(menuBar);
		jframe.setVisible(true);
		processParameters(args);
	}
	
	

	private void processParameters(String[] args) {
		if(args.length>0){
			File file = new File(args[0]);
			if(file.exists() && file.isFile())
				openFile(file);
		}
	}



	private FileFilter fileFilter_aes = new FileFilter() {
		@Override
		public boolean accept(File f) {
			return f.isDirectory() | f.getName().toLowerCase().endsWith(".aes");
		}

		@Override
		public String getDescription() {
			return "*.aes";
		}
	};

	private void openFile(File selected) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			// TODO am�liorer la s�curit�. hash etc
			String password = askPassword(selected.getName());
			IOUtils.streamDecryptFromFile(selected, baos, password);
			byte[] textdata = checkMD5(baos.toByteArray());
			currentFile = selected;
			currentPassword = password;
			jframe.setTitle(selected.getName() + TITLE_APPNAME);
			jtextarea.setText(new String(textdata, "UTF8"));
			jtextarea.setCaretPosition(0);
			modified = false;
		} catch (IOException e1) {
			jframe.setTitle("! " + e1.getMessage() + TITLE_APPNAME);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if ("open".equals(e.getActionCommand())) {
			JFileChooser jfc = new JFileChooser();
			jfc.setDialogType(JFileChooser.OPEN_DIALOG);
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jfc.setMultiSelectionEnabled(false);
			jfc.setFileFilter(fileFilter_aes);
			int r = jfc.showOpenDialog(jframe);
			if (r == JFileChooser.APPROVE_OPTION) {
				File selected = jfc.getSelectedFile();
				openFile(selected);
			}

		} else if ("save".equals(e.getActionCommand())) {
			if (currentFile != null)
				try {
					File selected = currentFile;
					ByteArrayInputStream bais = new ByteArrayInputStream(addMD5(jtextarea.getText().getBytes("UTF8")));
					// TODO am�liorer la s�curit�. hash etc
					String password = currentPassword;
					IOUtils.streamEncryptToFile(bais, selected, password);
					String ts = new Timestamp(System.currentTimeMillis()).toString();
					ts = ts.substring(0, ts.lastIndexOf("."));
					jframe.setTitle(selected.getName() + " (saved " + ts + ")" + TITLE_APPNAME);
					modified = false;
				} catch (IOException e1) {
					jframe.setTitle("! " + e1.getMessage() + TITLE_APPNAME);
				}

		} else if ("save as".equals(e.getActionCommand())) {
			JFileChooser jfc = new JFileChooser();
			jfc.setDialogType(JFileChooser.SAVE_DIALOG);
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jfc.setMultiSelectionEnabled(false);
			jfc.setFileFilter(fileFilter_aes);
			int r = jfc.showSaveDialog(jframe);
			if (r == JFileChooser.APPROVE_OPTION) {
				try {
					File selected = jfc.getSelectedFile();
					ByteArrayInputStream bais = new ByteArrayInputStream(addMD5(jtextarea.getText().getBytes("UTF8")));
					// TODO am�liorer la s�curit�. hash etc
					String password = askPassword(selected.getName());
					IOUtils.streamEncryptToFile(bais, selected, password);
					currentFile = selected;
					currentPassword = password;
					String ts = new Timestamp(System.currentTimeMillis()).toString();
					ts = ts.substring(0, ts.lastIndexOf("."));
					jframe.setTitle(selected.getName() + " (saved " + ts + ")" + TITLE_APPNAME);
					modified = false;
				} catch (IOException e1) {
					jframe.setTitle("! " + e1.getMessage() + TITLE_APPNAME);
				}
			}

		}
	}

	public String askPassword(String filename) {
		final JPasswordField jpf = new JPasswordField();
		JOptionPane jop = new JOptionPane(jpf, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = jop.createDialog("Password for " + filename);
		dialog.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				jpf.requestFocusInWindow();
			}
		});
		dialog.setVisible(true);
		Integer result = (Integer) jop.getValue();
		dialog.dispose();
		char[] password = null;
		if (result != null && result == JOptionPane.OK_OPTION) {
			password = jpf.getPassword();
		}
		return password != null ? new String(password) : null;
	}

	private byte[] checkMD5(byte[] data) throws IOException {
		byte[] md5 = new byte[16];
		byte[] textdata = new byte[data.length - md5.length];
		System.arraycopy(data, 0, md5, 0, md5.length);
		System.arraycopy(data, md5.length, textdata, 0, textdata.length);
		byte[] controlMd5 = null;
		try {
			controlMd5 = MessageDigest.getInstance("MD5").digest(textdata);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
		if (!Arrays.equals(md5, controlMd5))
			throw new IOException("Erreur d�codage");
		return textdata;

	}

	private byte[] addMD5(byte[] data) throws IOException {
		byte[] md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5").digest(data);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
		byte[] result = new byte[md5.length + data.length];
		System.arraycopy(md5, 0, result, 0, md5.length);
		System.arraycopy(data, 0, result, md5.length, data.length);
		return result;
	}

	public void dragEnter(DropTargetDragEvent dtde) {
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void dragOver(DropTargetDragEvent dtde) {
	}

	public void drop(DropTargetDropEvent dtde) {
		Transferable tr = dtde.getTransferable();
		if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			try {
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				List<File> fl = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
				openFile(fl.get(0));
			} catch (UnsupportedFlavorException e) {
			} catch (IOException e) {
			}
		}
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

}
