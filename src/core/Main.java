package core;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import lan.ZeroTier;
import lan.ZeroTierAPI;
import lan.ZeroTierCLI;

public class Main {
	
	public static JFrame frame;
	
	public static JLabel roomId;
	
	public static JButton createNetwork;
	public static JButton joinNetwork;
	public static JButton deleteNetwork;
	public static JButton leaveNetwork;
	public static JButton uninstall;
	
	public static JLabel leftCalanguinho;
	public static JLabel rightCalanguinho;
	
	public static void main(String[] args) throws Exception {
		System.setProperty("sun.java2d.uiScale", "1.0");
		System.setProperty("sun.java2d.uiScale.enabled", "false");
		
		if(!ZeroTier.isInstalled()) {
			JOptionPane pane = createOptionPane("Lan não instalada, deseja instalar ?", JOptionPane.ERROR_MESSAGE, JOptionPane.YES_NO_OPTION);
			if((int) pane.getValue() == 1) System.exit(1);
			
			JDialog dialog = createProgressPane("Instalador", "Instalando...");
			
			new Thread() {
				@Override
				public void run() {
					try {
						copyInstallerToTempFile();
						ZeroTier.install();
						dialog.dispose();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
			
			dialog.setVisible(true);
			createOptionPane("Lan instalada com sucesso, clique em OK para prosseguir.", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
		}
		
		frame = new JFrame("Connection Test");
		frame.setSize(250, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setLayout(null);

		int spacing = 50;
		createNetwork = new JButton("Criar Sala");
		createNetwork.setBounds(59, 10, 120, 35);
		createNetwork.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					disableAllButtons();
					frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					addLeftCalanguinho(Calanguinho.lightCalanguinho);
					
					String id = ZeroTierAPI.createNetwork().getAsJsonObject("data").get("id").getAsString();
					ZeroTierCLI.join(id);
					addRoomId(id);
					
					enableButton(deleteNetwork);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		
		joinNetwork = new JButton("Entrar na sala");
		joinNetwork.setBounds(createNetwork.getBounds());
		joinNetwork.setLocation(joinNetwork.getX(), joinNetwork.getY() + spacing);
		joinNetwork.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableAllButtons();
				frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				addLeftCalanguinho(Calanguinho.darkCalanguinho);
				
				String id = JOptionPane.showInputDialog(frame, "Insira o ID da sala:", "Entrar na sala", JOptionPane.QUESTION_MESSAGE);
				if(id == null || id.isEmpty()) {
					enableButton(createNetwork);
					enableButton(joinNetwork);
					enableButton(uninstall);
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					removeCalanguinhos();
					return;
				}
				
				String response = ZeroTierCLI.join(id).trim();
				if(!response.startsWith("200")) {
					createOptionPane("ID Inválido", JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION, frame);
					enableButton(createNetwork);
					enableButton(joinNetwork);
					enableButton(uninstall);
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					removeCalanguinhos();
					return;
				}
				
				addRoomId(id);
				enableButton(leaveNetwork);
			}
		});
		
		leaveNetwork = new JButton("Sair da sala");
		leaveNetwork.setBounds(joinNetwork.getBounds());
		leaveNetwork.setLocation(leaveNetwork.getX(), leaveNetwork.getY() + spacing);
		leaveNetwork.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableAllButtons();
				
				ZeroTierCLI.leave(roomId.getText());
				
				enableButton(createNetwork);
				enableButton(joinNetwork);
				enableButton(uninstall);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
				removeCalanguinhos();
			}
		});
		
		deleteNetwork = new JButton("Excluir Sala");
		deleteNetwork.setBounds(leaveNetwork.getBounds());
		deleteNetwork.setLocation(deleteNetwork.getX(), deleteNetwork.getY() + spacing);
		deleteNetwork.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					disableAllButtons();
					//Avisar ao coleguinha que a sala vai de F
					
					ZeroTierAPI.deleteNetwork(roomId.getText());
					ZeroTierCLI.leave(roomId.getText());
					
					enableButton(createNetwork);
					enableButton(joinNetwork);
					enableButton(uninstall);
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					
					removeCalanguinhos();
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		
		uninstall = new JButton("Desinstalar");
		uninstall.setBounds(deleteNetwork.getBounds());
		uninstall.setLocation(uninstall.getX(), uninstall.getY() + spacing);
		uninstall.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane pane = createOptionPane("Tem certeza ? ÒwÓ", JOptionPane.ERROR_MESSAGE, JOptionPane.YES_NO_OPTION, frame);
				if((int)pane.getValue() == 1) return;
				
				JDialog dialog = createProgressPane("Desinstalador", "Desinstalando...");
				
				new Thread() {
					@Override
					public void run() {
						try {
							copyInstallerToTempFile();
							ZeroTier.uninstall();
							dialog.dispose();
						} catch(Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				
				dialog.setVisible(true);
				
				createOptionPane("Lan desinstalado com sucesso, clique em OK para fechar.", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
				System.exit(1);
			}
		});	
		
		frame.add(createNetwork);
		frame.add(joinNetwork);
		frame.add(leaveNetwork);
		frame.add(deleteNetwork);
		frame.add(uninstall);
		
		disableButton(deleteNetwork);
		disableButton(leaveNetwork);
		
		frame.setVisible(true);
	}
	
	public static void disableButton(JButton button) {
		button.setEnabled(false);
		button.setBackground(Color.RED);
	}
	
	public static void enableButton(JButton button) {
		button.setEnabled(true);
		button.setBackground(null);
	}
	
	public static void disableAllButtons() {
		disableButton(createNetwork);
		disableButton(joinNetwork);
		disableButton(leaveNetwork);
		disableButton(deleteNetwork);
		disableButton(uninstall);
	}
	
	public static void addLeftCalanguinho(ImageIcon calanguinhoGif) {
		frame.setSize(600, 300);
		frame.setLocationRelativeTo(null);
		
		moveAllButtons(240);
		leftCalanguinho = new JLabel(calanguinhoGif);
		leftCalanguinho.setBounds(0, 60, 210, 200);
		frame.add(leftCalanguinho);
	}
	
	public static void addRightCalanguinho(ImageIcon calanguinhoGif) {
		frame.setSize(610, 300);
		frame.setLocationRelativeTo(null);
		
		moveAllButtons(240);
		
		rightCalanguinho = new JLabel(calanguinhoGif);
		rightCalanguinho.setBounds(395, 65, 210, 200);

		frame.add(rightCalanguinho);
	}
	
	public static void removeCalanguinhos() {
		frame.setSize(250, 300);
		moveAllButtons(59);

		if(leftCalanguinho != null)
			frame.remove(leftCalanguinho);
		
		if(rightCalanguinho != null)
			frame.remove(rightCalanguinho);
		
		if(roomId != null)
			frame.remove(roomId);
		
		frame.setLocationRelativeTo(null);
	}
	
	public static void moveAllButtons(int x) {
		createNetwork.setLocation(x, createNetwork.getY());
		joinNetwork.setLocation(x, joinNetwork.getY());
		leaveNetwork.setLocation(x, leaveNetwork.getY());
		deleteNetwork.setLocation(x, deleteNetwork.getY());
		uninstall.setLocation(x, uninstall.getY());
	}
	
	public static File copyInstallerToTempFile() throws IOException {
		InputStream source = ClassLoader.class.getResourceAsStream("/redist/ZeroTierOne.msi");
		File tempFile = File.createTempFile("zerotier-installer", ".msi");

		tempFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(tempFile);
		
		byte[] buffer = new byte[8192];
		int length;
		while((length = source.read(buffer)) != -1) {
			out.write(buffer, 0, length);
		}
		
		out.close();
		source.close();
		
		ZeroTier.INSTALLER_PATH = tempFile.getPath();
		
		return tempFile;
	}
	
	public static JOptionPane createOptionPane(String message, int messageType, int optionType, JFrame... frame) {
		Toolkit.getDefaultToolkit().beep();
		JOptionPane pane = new JOptionPane(message);
		pane.setMessageType(messageType);
		pane.setOptionType(optionType);

		JDialog dialog = pane.createDialog((frame.length == 0 ? null : frame[0]), "ConnectionTest");
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.setVisible(true);
		
		return pane;
	}
	
	public static void addRoomId(String id) {
		roomId = new JLabel(id);
		roomId.setFont(new Font("Arial", Font.BOLD, 13));
		roomId.setBounds(20, 10, 150, 40);
		roomId.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				roomId.setBorder(BorderFactory.createLineBorder(Color.RED));
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				roomId.setBorder(null);
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				Toolkit.getDefaultToolkit().beep();
				String myString = roomId.getText();
				StringSelection stringSelection = new StringSelection(myString);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);
			}
		});
		
		frame.add(roomId);
	}
	
	public static JDialog createProgressPane(String title, String message) {
		JOptionPane progressPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {}, null);
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressPane.add(progressBar);
		
		JDialog dialog = progressPane.createDialog(title);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.pack();
		
		return dialog;
	}
}
