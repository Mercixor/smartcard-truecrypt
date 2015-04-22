/**
 * Diese Klasse erstellt ein Fenster zur Verbindung mit einer SmartCard,
 * welche das Passwort für einen lokal liegenden TrueCrypt Container gespeichert hat.
 * Der Benutzer muss sich mit einem PIN autorisieren, damit dieser auf die Daten
 * auf der SmartCard zugreifen kann.
 * 
 * Der Administrator hat darüber hinaus die Rechte neue Benutzer anzulegen und alte Benutzer
 * zu sperren.
 */
package terminal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import terminal.data.TerminalData;
import terminal.data.UserData_Preferences;
import terminal.dialogs.Dialog_ChangeUserData;
import terminal.dialogs.Dialog_ExtractContainerPassword;
import terminal.dialogs.Dialog_MigrateOldContainer;
import terminal.dialogs.Dialog_MountContainers;
import terminal.dialogs.Dialog_NewContainer;
import terminal.dialogs.Dialog_PinChange;
import terminal.dialogs.Dialog_PinLogin;
import terminal.dialogs.Dialog_PinReset;
import terminal.dialogs.Dialog_Share_Container_Password;
import terminal.dialogs.Dialog_ShowSavedPasswords;
import terminal.dialogs.Dialog_ShowSavedPublicKeys;
import terminal.dialogs.Dialog_UserPreferences;

/**Klasse zur Erstellung des Hauptfensters und seiner Unterelemente.
 * 
 * Diese Klasse erstellt ein Fenster zur Verbindung mit einer SmartCard,
 * welche die Passwörter für lokal gespeicherte TrueCrypt Container enthält.
 * Der Benutzer muss sich mit einem PIN autorisieren, damit dieser auf die Daten
 * auf der SmartCard zugreifen kann.
 * 
 * @author Dennis Jandt
 * @version 1.0
 */
public class Terminal {

	// Datenobjekt für das Terminal Programm
	private TerminalData data;
	private EreignisBeobachter ereignisbeo;
	private JavaCard card;
	private TrueCryptControl tcControl;
	private UserData_Preferences userPreferences;
	
	// GUI Elemente
	private JLabel				currentUser;
	private JCheckBox 			statusReader,statusCard,statusAuth;
	private JCheckBoxMenuItem 	statusSecurity;
	private JComboBox<String>	comboxBoxTerminals;
	private JButton 			button_anmelden,button_abmelden,button_mountContainer, button_reconnect;
	private JFrame 				mainwindow;	
	private JMenuBar 			menuleiste;	
	// Deklaration der Dialoge
	private Dialog_PinLogin 					dialog_PinLogin;
	private Dialog_PinChange 					dialog_PinChange;
	private Dialog_PinReset 					dialog_PinReset;
	private Dialog_NewContainer 				dialogNewContainer;
	private Dialog_Share_Container_Password 	dialogShareContainerPassword;
	private Dialog_ShowSavedPublicKeys 			dialogShowPublicKeyList;
	private Dialog_ChangeUserData 				dialogUpdateUserData;
	private Dialog_ExtractContainerPassword 	dialogExtractPassword;
	private Dialog_MountContainers 				dialogMountContainers;
	private Dialog_ShowSavedPasswords 			dialogShowSavedPasswords;
	private Dialog_MigrateOldContainer 			dialogMigrateOldContainer;
	private Dialog_UserPreferences				dialogUserPreferences;
	
	public static final int MAX_RETRIES = 3;
	public static final char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String datapath	= "../Multiuserapplikation Daten";
	public static String publicKeyPath = datapath+"/Encrypted Containerpasswords";
	public static String pfadTerminalData = "/terminalData.sav";
	
	public boolean angemeldet, wasLoggedIn =false;
	
	/**Konstruktor erzeugt das Hautpfenster zur Anzeige des Programms
	 * und ruft die Methode initGUI() und initTerminal() auf.
	 */
	public Terminal() {
		initGUI();
		initTerminal();		
	}
	
	
	/**Main Methode zur Erzeugung des Programms.
	 * 
	 * @param args An das Programm übergebene Parameter
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				new Terminal();				
			}
		});
	}
	
	/**Lädt die gespeicherten Daten ein und erstellt die Dialog Objekte.
	 */
	private void initTerminal() {
		// Gespeicherte Daten laden
		loadSavedData();
		// Erzeugen der Klasse zur Verbindung mit der SmartCard
		card = new JavaCard(data,this);
		// Starten des Thread Überprüfung der Bedingungen,
		// welche zum arbeiten mit dem Programm erfüllt sein müssen
		new check_Cond().start();
		tcControl					= new TrueCryptControl();
		
		dialog_PinLogin 			= new Dialog_PinLogin(mainwindow,card, data, this);
		dialog_PinChange 			= new Dialog_PinChange(mainwindow,card, data, this);
		dialog_PinReset				= new Dialog_PinReset(mainwindow,card, data, this);
		dialogUpdateUserData		= new Dialog_ChangeUserData(mainwindow, card, data);
		dialogShowPublicKeyList 	= new Dialog_ShowSavedPublicKeys(mainwindow, card, data, tcControl);
		dialogNewContainer 			= new Dialog_NewContainer(mainwindow, card, data, tcControl);
		dialogShareContainerPassword= new Dialog_Share_Container_Password(mainwindow, card, data, tcControl);		
		dialogExtractPassword		= new Dialog_ExtractContainerPassword(mainwindow, card, data, tcControl);
		dialogMountContainers		= new Dialog_MountContainers(mainwindow, card, data, tcControl);
		dialogShowSavedPasswords 	= new Dialog_ShowSavedPasswords(mainwindow, card, data, tcControl);
		dialogMigrateOldContainer 	= new Dialog_MigrateOldContainer(mainwindow, card, data, tcControl);
		dialogUserPreferences		= new Dialog_UserPreferences(mainwindow, card, data, this);
	}
	
	
	
	/**
	 * Erzeugung der GUI-Elemente und Platzierung dieser auf dem Hauptframe
	 */
	private void initGUI() {		
		// Benötigte GUI Komponenten erstellen
		statusReader 			= new JCheckBox("Kartenlesegerät angeschlossen");
		statusCard 				= new JCheckBox("SmartCard eingesteckt");
		statusAuth 				= new JCheckBox("Angemeldet");
		statusSecurity			= new JCheckBoxMenuItem("Container automatisch auswerfen");
		button_anmelden 		= new JButton("Anmelden");
		button_abmelden 		= new JButton("Abmelden");
		button_mountContainer	= new JButton("TrueCrypt Container laden");
		button_reconnect		= new JButton("Neuverbinden mit SmartCard");
		comboxBoxTerminals		= new JComboBox<String>();
		mainwindow 				= new JFrame("Multiuserapplikation für TrueCrypt");
		menuleiste 				= new JMenuBar();
		currentUser				= new JLabel("Benutzer: - keiner Angemeldet -");
		JMenu haupt_menu 		= new JMenu("Datei");
		JMenu container_menu	= new JMenu("Containermenü");
		JMenu hilfe_menu 		= new JMenu("Hilfe");		
		JMenuItem eineOperation;		
		
		// ActionListener erzeugen -> Innere Klasse
		ereignisbeo 		= new EreignisBeobachter();	
		
		// ActionListener zu den Buttons hinzufügen
		button_anmelden.addActionListener(ereignisbeo);
		button_abmelden.addActionListener(ereignisbeo);
		button_mountContainer.addActionListener(ereignisbeo);
		button_reconnect.addActionListener(ereignisbeo);
		
		// Positionierung der Komponenten zur Startzeit
		mainwindow.setLayout(null);
		currentUser.setBounds(14, 3, 200, 25);
		statusReader.setBounds(10, 30, 250, 25);
		statusCard.setBounds(10, 60, 250, 25);
		statusAuth.setBounds(10, 90, 110, 25);
		comboxBoxTerminals.setBounds(10, 120, 220, 25);		
		button_anmelden.setBounds(50, 150, 150, 50);		
		button_mountContainer.setBounds(10, 150, 220, 25);	
		button_reconnect.setBounds(10, 150, 220, 25);		
		button_abmelden.setBounds(10, 180, 220, 25);
		
		// Initialisierung der Komponenten zur Startzeit
		button_anmelden.setEnabled(false);
		button_mountContainer.setVisible(false);
		button_reconnect.setVisible(false);
		button_abmelden.setVisible(false);
		comboxBoxTerminals.setVisible(false);				
		statusReader.setEnabled(false);
		statusCard.setEnabled(false);
		statusAuth.setEnabled(false);
		statusSecurity.setEnabled(false);
		statusSecurity.setSelected(true);	
		
		// Komponenten hinzufügen zum Fenster
		mainwindow.setSize(250, 270);
		mainwindow.setLocation(100, 100);
		mainwindow.setResizable(false);
		mainwindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainwindow.addWindowListener(ereignisbeo);
		
		// GUI-Elemente zum Fenster hinzufügen
		mainwindow.add(currentUser);
		mainwindow.add(statusReader);
		mainwindow.add(statusCard);
		mainwindow.add(statusAuth);
		mainwindow.add(comboxBoxTerminals);
		mainwindow.add(button_anmelden);
		mainwindow.add(button_abmelden);
		mainwindow.add(button_mountContainer);
		mainwindow.add(button_reconnect);
		mainwindow.setJMenuBar(menuleiste);	
		
		// Aufbau des Hauptmenüs		
		eineOperation = new JMenuItem("PublicKey-Liste anzeigen");
		haupt_menu.add(eineOperation);
		eineOperation.addActionListener(ereignisbeo);
		eineOperation =  new JMenuItem("Einstellungen...");
		haupt_menu.add(eineOperation);
		eineOperation.addActionListener(ereignisbeo);
		eineOperation = new JMenuItem("Benutzerdaten ändern");
		eineOperation.setEnabled(false);
		haupt_menu.add(eineOperation);
		eineOperation.addActionListener(ereignisbeo);
		eineOperation = new JMenuItem("Neuen PIN festlegen");
		eineOperation.setEnabled(false);
		haupt_menu.add(eineOperation);
		eineOperation.addActionListener(ereignisbeo);
		haupt_menu.addSeparator();
		eineOperation = new JMenuItem("Beenden");
		haupt_menu.add(eineOperation);
		eineOperation.addActionListener(ereignisbeo);
		
		// Aufbau des Containermenüs		
		eineOperation = new JMenuItem("PublicKey abspeichern");
		container_menu.add(eineOperation);
		eineOperation.addActionListener(ereignisbeo);
		container_menu.addSeparator();
		eineOperation = new JMenuItem("Neuen Container erstellen");
		container_menu.add(eineOperation);
		eineOperation.addActionListener(ereignisbeo);
		eineOperation = new JMenuItem("Container-Passwort teilen");
		eineOperation.setEnabled(false);
		container_menu.add(eineOperation);
		eineOperation.addActionListener(ereignisbeo);
		eineOperation = new JMenuItem("Container-Passwort aus Datei extrahieren");
		eineOperation.setEnabled(false);
		container_menu.add(eineOperation);
		eineOperation.addActionListener(ereignisbeo);
		eineOperation = new JMenuItem("Container-Passwort von der SmartCard anzeigen");
		eineOperation.setEnabled(false);
		container_menu.add(eineOperation);
		eineOperation.addActionListener(ereignisbeo);
		eineOperation = new JMenuItem("Alten Container in das System portieren");
		eineOperation.setEnabled(false);
		container_menu.add(eineOperation);
		eineOperation.addActionListener(ereignisbeo);
		container_menu.addSeparator();
		container_menu.add(statusSecurity);		
		
		// Aufbau Hilfemenüs
		eineOperation = new JMenuItem("Hilfe");
		hilfe_menu.add(eineOperation);
		eineOperation.addActionListener(ereignisbeo);
		eineOperation = new JMenuItem("Karte entsperren");
		hilfe_menu.add(eineOperation);
		eineOperation.addActionListener(ereignisbeo);		
		
		// Menüleiste mit Komponenten füllen		
		menuleiste.add(haupt_menu);
		menuleiste.add(container_menu);
		menuleiste.add(hilfe_menu);	
		
		// Anzeigen des Hauptfensters
		mainwindow.setVisible(true);
	}
	
	
	/**
	 * Lädt die gespeicherten Daten.
	 * 
	 * Versucht die gespeicherten Daten zu laden. 
	 * Sind keine Daten vorhanden wird ein neues TerminalData-Objekt erzeugt.
	 * Sollte dabei ein Fehler auftreten, wird eine Fehlermeldung an den Benutzer ausgegeben.
	 */
	private void loadSavedData(){
		OutputStream fos = null;
		ObjectOutputStream oos = null;
		InputStream fis = null;
		new File(datapath).mkdirs();
		File terminalData = new File(datapath+pfadTerminalData);
		if(terminalData.isFile()){
			try {
				fis = new FileInputStream(datapath+pfadTerminalData);
				ObjectInputStream restore = new ObjectInputStream(fis);
				data = (TerminalData) restore.readObject();
				restore.close();
			} catch (Exception e){
				JOptionPane.showMessageDialog(null, "Fehler beim Öffnen der gespeicherten Daten.","Fehler", JOptionPane.ERROR_MESSAGE);
			}
		}else{			
			try {
				data = new TerminalData();
				fos = new FileOutputStream(datapath+pfadTerminalData);
				oos = new ObjectOutputStream(fos);
				oos.writeObject(data);
				oos.close();
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(mainwindow, "Fehler beim erstellen der Speicherdatei.","Fehler", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Setzt den aktuellen Benutzer in das im Hauptfenster angezeigte Label.
	 * @param name Name des angemeldeten Benutzers
	 */
	protected void setCurrentUser(String name){
		if(name!="")currentUser.setText("Benutzer: "+name);
		else currentUser.setText("Benutzer: - keiner Angemeldet -");
	}
	
	/**
	 * Setzt die vom Benutzer gewählten Statusoptionen um.
	 */
	public void setUserPreferences(){
		userPreferences = data.getUserData().getUserPreferences();
		statusSecurity.setSelected(userPreferences.isSecuritySet());
		String path = userPreferences.getTrueCryptPath();
		if(path.equals("")){
			if(data.trueCryptPath.equals("")){
				dialogUserPreferences.showDialog(true);
			}else{
				userPreferences.setTrueCryptPath(data.trueCryptPath);
			}
		}else{
			data.trueCryptPath=path;
			tcControl.setTrueCryptPath(path);
		}
		
	}
	
	/**
	 * Zeigt eine kurze Hilfe zum Programm an.
	 */
	private void showHelp(){
		JOptionPane.showMessageDialog(mainwindow, "Hilfsseite für 'Multiuserapplikation für TrueCrypt'\n\n"
				+ "1. Zum korrekten Ablauf des Programms, muss der Pfad zur\n"
				+ "TrueCrypt.exe korrekt gesetzt sein. Zusätzlich muss sich\n"
				+ "die TrueCrypt Format.exe in dem Ordner befinden.\n\n"
				+ "2. Die Standardpin lautet '1234'. Es wird empfohlen diese\n"
				+ " zu ändern unter: Datei -> PIN ändern\n\n"
				+ "3. Die Standard-PUK lautet: '123456'\n\n"
				+ "4. Robot ist eine automatisierte Eingabehilfe. Sollten\n"
				+ "Probleme auftreten die Eingabeverzögerung unter den\n"
				+ "Einstellungen erhöhen oder Robot deaktivieren.\n\n"
				+ "5. Im Containermenü ist die Sicherheitsoption\n"
				+ "'Container automatisch entladen' zu finden.\n"
				+ "Diese entlädt sämtliche Container, wenn\n"
				+ "die SmartCard entfernt wird. Wenn dies nicht\n"
				+ "gewünscht ist, diese dort deaktivieren.\n", "Hilfe", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Meldet den Benutzer ab und entlädt alle Container aus dem Speicher.
	 */
	private void commandLogoff(){
		currentUser.setText("Benutzer: - keiner Angemeldet -");
		angemeldet = false;
		tcControl.dismountAll();
	}
	
	/**
	 * Entfernung aller Container aus dem Speicher und speichern der
	 * Daten.
	 */
	private void commandShutdown(){
		commandLogoff();
		FileOutputStream saveFile;
		ObjectOutputStream save;
		try {
			saveFile = new FileOutputStream(datapath+pfadTerminalData);
			save = new ObjectOutputStream(saveFile);
			save.writeObject(data);
			save.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	/**
	 * Fragt die verbleibenden PIN-Versuche ab.
	 * @return Ob der Fehlschlagzähler 0 erreicht hat.
	 */
	private boolean checkRemainingTries(){
		if(card.getRemainingTries()==0){
			JOptionPane.showMessageDialog(mainwindow,						
	                "PIN gesperrt. Zum Entsperren mit der PUK: 'Hilfe' -> 'Karte entsperren'",
	                "PIN gesperrt",
	                JOptionPane.WARNING_MESSAGE);
			return false;
		}
		return true;
	}
	
	/**
	 * Setzt den maximalen von der Karte empfangenen Index.
	 * Dies ist notwendig, um die angezeigte Größe der Tabellen zu bestimmen. 
	 * Zudem garantiert dies eine Kompatibilität zu andern Karten mit größeren Speicher.
	 */
	public void setMaxIndex(){
		int maxIndex = card.getMaxIndex();
		data.setMaxIndex(maxIndex);
		dialogNewContainer.setMaxIndex(maxIndex);
		dialogExtractPassword.setMaxIndex(maxIndex);
		dialogShareContainerPassword.setMaxIndex(maxIndex);
		dialogMountContainers.setMaxIndex(maxIndex);
		dialogShowSavedPasswords.setMaxIndex(maxIndex);		
		dialogMigrateOldContainer.setMaxIndex(maxIndex);
	}
	
	/**
	 * Statische Operation, um aus dem Containernamen 0x00 zu entfernen.
	 * @param containerName Name des übergebenen Containers mit Trenzeichen
	 * @return Name des Containers als String
	 */
	public static String trimName(String containerName) {
		byte[] temp = containerName.getBytes();
		String ausgabe = "";
		for (int i=0;i<temp.length;i++){
			if(temp[i]==0x00)break;
			ausgabe = ausgabe + (char) temp[i];
		}
		return ausgabe;
	}
	
	/**
	 * Gibt die in den Parametern übergebene Fehlermeldung aus.
	 * @param meldung Nachricht der Fehlermeldung
	 * @param fensterTitel Titel des Fehlerfensters
	 */
	protected void showError(String meldung, String fensterTitel) {
		JOptionPane.showMessageDialog(mainwindow, meldung, fensterTitel, JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Erzeugt aus einem Byte-Array einen HexString.
	 * @param bytes Das Byte-Array.
	 * @return Hex-String des Byte-Arrays.
	 */
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	/**Innere Klasse welche die Ereignisbehandlung abwickelt.
	 * Es werden die Interfaces ActionListener und WindowListener implementiert.
	 * Der ActionListener reagiert auf die Benutzeraktionen im Hauptfesnter.
	 * Der WindowListener dient zur sicheren Abmeldung, falls der Benutzer das Fenster
	 * über den 'X'-Knopf schließt.
	 * 
	 * @author Dennis Jandt
	 * @version vom 21.11.2014
	 */
	public class EreignisBeobachter implements ActionListener,WindowListener{

		/**
		 * Implementierung des ActionListener Interfaces zur Steuerung des Hauptfensters.
		 */
		public void actionPerformed(ActionEvent e) {
			
			// Aktionen der JMenuBar			
			// Option: Hauptmenü -> PublicKey-Liste anzeigen
			if (e.getSource() == menuleiste.getMenu(0).getMenuComponent(0)){
				dialogShowPublicKeyList.showDialog(true);
			}
			// Option: Hauptmenü -> Einstellungen
			if (e.getSource() == menuleiste.getMenu(0).getMenuComponent(1)){
				dialogUserPreferences.showDialog(true);
			}
			// Option: Hauptmenü -> Benutzerdaten ändern
			if (e.getSource() == menuleiste.getMenu(0).getMenuComponent(2)){
				dialogUpdateUserData.showDialog(true);
			}
			// Option: Hauptmenü -> Neuen PIN festlegen
			if (e.getSource() == menuleiste.getMenu(0).getMenuComponent(3)){
				if(checkRemainingTries())dialog_PinChange.showDialog(true);
			}
			// Option: Hauptmenü -> Beenden
			if (e.getSource() == menuleiste.getMenu(0).getMenuComponent(5)){
				commandShutdown();
			}
			
			// Option: Containermenü -> PublicKey speichern
			if (e.getSource() == menuleiste.getMenu(1).getMenuComponent(0)){
				dialogShowPublicKeyList.savePublicKey();
			}
			// Option: Containermenü -> Neuen Container erstellen
			if (e.getSource() == menuleiste.getMenu(1).getMenuComponent(2)){
				dialogNewContainer.showDialog(true);
			}
			// Option: Containermenü -> Container-Passwort teilen
			if (e.getSource() == menuleiste.getMenu(1).getMenuComponent(3)){
				dialogShareContainerPassword.showDialog(true);
			}
			// Option: Containermenü -> Container-Passwort aus Datei extrahieren
			if (e.getSource() == menuleiste.getMenu(1).getMenuComponent(4)){
				dialogExtractPassword.showDialog(true);
			}
			// Option: Containermenü -> Container-Passwort von der SmartCard anzeigen
			if (e.getSource() == menuleiste.getMenu(1).getMenuComponent(5)){
				dialogShowSavedPasswords.showDialog(true);
			}
			// Option: Containermenü -> Alt-Container migrieren
			if (e.getSource() == menuleiste.getMenu(1).getMenuComponent(6)){
				dialogMigrateOldContainer.showDialog(true);
			}
			// Option: Containermenü -> Sicherheitsoption gewechselt
			if (e.getSource() == menuleiste.getMenu(1).getMenuComponent(8)){
				userPreferences.setSecurityOption(statusSecurity.isSelected());
			}
			// Option: Hilfe -> Hilfe
			if (e.getSource() == menuleiste.getMenu(2).getMenuComponent(0)){
				showHelp();
			}
			// Option: Hilfe -> Karte entsperren
			if (e.getSource() == menuleiste.getMenu(2).getMenuComponent(1)){
				dialog_PinReset.showDialog(true);
			}
			
			// Anmelden gedrückt
			if (e.getSource() == button_anmelden) {				
				if(checkRemainingTries())dialog_PinLogin.showDialog(true);
			}
			
			// Abmelden gedrückt
			if (e.getSource() == button_abmelden){
				commandLogoff();
			}
			
			// TrueCrypt Container mounten gedrückt
			if (e.getSource() == button_mountContainer){
				dialogMountContainers.showDialog(true);
			}
			// TrueCrypt Container mounten gedrückt
			if (e.getSource() == button_reconnect){
				if(checkRemainingTries())dialog_PinLogin.showDialog(true);
			}	
		}
		
		
		/**
		 * Implementierung des WindowListener Interfaces, welches aufgerufen wird,
		 * falls das Hauptfenster geschlossen wird.
		 */
		public void windowClosing(WindowEvent arg0) {
			commandShutdown();
		}
		// Leere Methodenrümpfe. Diese Ereignisse werden nicht gebraucht.
		public void windowActivated(WindowEvent arg0) {}
		public void windowClosed(WindowEvent arg0) {}
		public void windowDeactivated(WindowEvent arg0) {}
		public void windowDeiconified(WindowEvent arg0) {}
		public void windowIconified(WindowEvent arg0) {}
		public void windowOpened(WindowEvent arg0) {}
	}
	
	/** 
	 * Implementiert einen Thread, welcher die Bedingungen zum Anmelden
	 * überprüft und weitere Rechte gewährt.
	 * 
	 * @author Dennis Jandt
	 * @version vom 26.11.2014
	 */
	public class check_Cond extends Thread{
		boolean maxIndexSet,verbunden = false;
		int terminalCount, oldTerminalsValue = 0, oldIndex = 0, newIndex = 0;
		
		/**
		 * Schleife des Threads.
		 */
		public void run() {
			while(true){
				try{
					statusReader.setSelected(checkTerminal());
					statusCard.setSelected(card.isCardPresent());
					statusAuth.setSelected(angemeldet);														
					if(statusCard.isSelected()){
						button_anmelden.setEnabled(true);
						button_reconnect.setEnabled(true);
						if(statusAuth.isSelected()){							
							if(!wasLoggedIn)enableMenues(true);
							else button_reconnect.setVisible(true);
							wasLoggedIn = true;
						}else{
							enableMenues(false);
							wasLoggedIn = false;
							button_reconnect.setVisible(false);
						}												
					}else{
						if(statusAuth.isSelected()){
							if(statusSecurity.isSelected())angemeldet=false;
							else{
								enableCardRelatedMenues(false);
								verbunden=false;
								button_reconnect.setEnabled(false);
							}							
						}else{
							button_anmelden.setEnabled(false);
							enableMenues(false);
							angemeldet = false;
							verbunden = false;
							button_reconnect.setVisible(false);
							commandLogoff();
						}						
					}
					// Wartezeit am Ende der Routine
			        sleep(500);
			    // Fehlerbehandlung des Threads
				} catch (Exception e){
					e.printStackTrace();
					JOptionPane.showMessageDialog(mainwindow, "Fehler bei der Ausführung des Hautpthreads! Bitte Programm neustarten!", "Thread Fehler", JOptionPane.ERROR_MESSAGE);
					this.interrupt();
				}
			}
		}
		
		/**
		 * Prüft ob ein oder mehrere Terminals angeschlossen sind.
		 * Sind mehrere Terminals angeschlossen, wird ein Auswahlfenster angezeigt.
		 * @return Liefert Wahr wenn mindestens ein Terminal angschlossen ist
		 */
		private boolean checkTerminal(){
			terminalCount = card.getTerminalSize();
			List<String> liste = card.getTerminals();
			if(terminalCount>=1){
				statusReader.setSelected(true);						
				if(terminalCount==1){
					if(!verbunden)verbunden = card.connect(0);
					comboxBoxTerminals.setVisible(false);
				}else{
					newIndex = comboxBoxTerminals.getSelectedIndex();
					if(!verbunden || oldIndex != newIndex){								
						verbunden = card.connect(newIndex);
						oldIndex = newIndex;
					}
					comboxBoxTerminals.setVisible(true);							
					if(!(liste.size()==oldTerminalsValue)){
						comboxBoxTerminals.removeAllItems();
						for(int i=0;i<liste.size();i++)comboxBoxTerminals.addItem(liste.get(i));
						oldTerminalsValue = liste.size();
					}
				}
				return true;
			}
			return false;
		}
		
		/**
		 * De- oder Aktiviert Kartenspezifische Kommandos.
		 * Dies soll verhindern, dass der Benutzer eine Funktionalität erwartet,
		 * die ohne Karte nicht möglich ist.
		 * @param auswahl Ob die Kartenspezifischen Dialoge aktiviert werden sollen
		 */
		private void enableCardRelatedMenues(boolean auswahl) {
        	menuleiste.getMenu(0).getMenuComponent(2).setEnabled(auswahl);
			menuleiste.getMenu(0).getMenuComponent(3).setEnabled(auswahl);
        	menuleiste.getMenu(1).getMenuComponent(2).setEnabled(auswahl);
			menuleiste.getMenu(1).getMenuComponent(3).setEnabled(auswahl);
			menuleiste.getMenu(1).getMenuComponent(4).setEnabled(auswahl);
			menuleiste.getMenu(1).getMenuComponent(5).setEnabled(auswahl);
			menuleiste.getMenu(1).getMenuComponent(6).setEnabled(auswahl);
			button_mountContainer.setVisible(auswahl);
			button_reconnect.setVisible(!auswahl);
		}

		/**
		 * De- oder Aktiviert die Meüoptionen, falls der Benutzer angemeldet wurde.
		 * @param auswahl Ob die Post-Anmeldungsspezifischen Menüs aktiviert werden soll
		 */
		private void enableMenues(boolean auswahl){
			button_anmelden.setVisible(!auswahl);
			button_abmelden.setVisible(auswahl);
			button_mountContainer.setVisible(auswahl);
			menuleiste.getMenu(0).getMenuComponent(1).setEnabled(auswahl);
			menuleiste.getMenu(0).getMenuComponent(2).setEnabled(auswahl);
			menuleiste.getMenu(0).getMenuComponent(3).setEnabled(auswahl);        	
        	menuleiste.getMenu(1).getMenuComponent(2).setEnabled(auswahl);
			menuleiste.getMenu(1).getMenuComponent(3).setEnabled(auswahl);
			menuleiste.getMenu(1).getMenuComponent(4).setEnabled(auswahl);
			menuleiste.getMenu(1).getMenuComponent(5).setEnabled(auswahl);
			menuleiste.getMenu(1).getMenuComponent(6).setEnabled(auswahl);
			menuleiste.getMenu(1).getMenuComponent(8).setEnabled(auswahl);
		}
	}
}