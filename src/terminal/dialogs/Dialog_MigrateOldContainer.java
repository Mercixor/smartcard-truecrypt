package terminal.dialogs;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.security.SecureRandom;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import terminal.JavaCard;
import terminal.Terminal;
import terminal.TrueCryptControl;
import terminal.data.TerminalData;
/**
 * Dialog zur Migration eines existierenden TrueCrypt-Containers.
 * Wird von Dialog_Basic_SmartCard abgeleitet.
 * @author Dennis Jandt
 * @see Dialog_Basic_SmartCard
 */
public class Dialog_MigrateOldContainer extends Dialog_Basic_SmartCard{
	

	private JButton openFile;
	private JTextField textFieldPW,textFieldContName, textFieldFile;
	private JPasswordField passwordField;
	private JCheckBox showPasswordBox;

	private SecureRandom random;
	private File file=null;
	private char[] alphanumerics;
	protected Object[][] 	tabelData;
	
	/**
	 * Erzeugt die f�r die Passwortgenerierung notwendige SecureRandom-Objekt 
	 * und das Array, auf das die sp�ter erzeugten Zufallszahlen abgebildet werden.
	 * Ruft zudem die Methode initGUI() auf.
	 * @param mainwindow Das Elternobjekt des Dialogs
	 * @param card Referenz auf das JavaCard-Objekt
	 * @param data Referenz zur Speicherklasse
	 * @param tcCont Referenz auf das TrueCryptControl Objekt
	 */
	public Dialog_MigrateOldContainer(JFrame mainwindow, JavaCard card,
			TerminalData data,TrueCryptControl tcCont) {
		super(mainwindow, card, data,tcCont, "Alten Container portieren", 3, 7);
		random = new SecureRandom();	
		initGUI();	
		
		// Array zur Passwortgenerierung
		StringBuilder tmp = new StringBuilder();
	    for (char ch = '0'; ch <= '9'; ++ch)
	      tmp.append(ch);
	    for (char ch = 'a'; ch <= 'z'; ++ch)
	      tmp.append(ch);
	    for (char ch = 'A'; ch <= 'Z';ch++)
	    	tmp.append(ch);
	    alphanumerics = tmp.toString().toCharArray();
	}
	
	/**
	 * Erzeugt die notwendigen Bedienelemente und platziert diese im Dialog.
	 */
	private void initGUI(){
		// Dialogelemente erzeugen
		openFile			= new JButton("Datei w�hlen");
		passwordField 		= new JPasswordField();
		textFieldPW			= new JTextField();
		textFieldContName	= new JTextField();
		textFieldFile		= new JTextField();
		showPasswordBox		= new JCheckBox("Containerpasswort ausblenden");
		
		// Positionierung der GUI-Elemente
		// 1.Spalte
		addComponent(new JLabel("Container Name:"), 0, 0, 1, 1);
		addComponent(new JLabel("Passwort:"), 0, 1, 1, 1);
		addComponent(new JLabel("Datei:"), 0, 2, 1, 1);
		addComponent(showPasswordBox, 0, 3, 2, 1);
		addComponent(openFile, 0, 4, 2, 1);
		addComponent(button_ok, 0, 5, 2, 1);	
		addComponent(button_cancel, 0, 6, 2, 1);	
		// 2.Spalte
		addComponent(textFieldContName, 1, 0, 1, 1);
		addComponent(textFieldPW, 1, 1, 1, 1);
		addComponent(passwordField, 1, 1, 1, 1);
		addComponent(textFieldFile, 1, 2, 1, 1);	
		// 3.Spalte		
		addComponent(labelSmartCardContainer, 2, 0, 1, 1);
		addComponent(scrollPaneSCContainers, 2, 1, 1, 6);
		// Positionierung des Fensters
		dialog.setLocation(100, 120);
		// Sichtbarkeit und Init-Status der GUI-Elemente
		textFieldPW.setVisible(false);
		passwordField.setEnabled(false);
		textFieldFile.setEnabled(false);
		showPasswordBox.setSelected(true);
		// Actionlistener zu den Interaktionselemente hinzuf�gen
		showPasswordBox.addActionListener(this);
		openFile.addActionListener(this);		
	}
	
	/**
	 * Zeigt je nach Auswahl den Dialog an. Wird der Dialog angezeigt, wird die
	 * Oberklassenmethode fillTableSmartCardContainer() aufgerufen und
	 * generatePassword().
	 */
	public void showDialog(boolean auswahl){
		if(auswahl){
			fillTableSmartCardContainer();			
			generatePassword();
		}
		dialog.setVisible(auswahl);
	}
	
	/**
	 * Wird nach Best�tigung von button_ok aufgerufen. Das Passwort wird
	 * in die Zwischenablage kopiert und der Benutzer muss dies anschlie�end im 
	 * TrueCrypt-Dialog einf�gen. Best�tigt der Benutzer die Eingabe, wird der
	 * Container zu laden versucht. Dies soll dem Programm die Best�tigung liefern, 
	 * dass das neue Passwort gesetzt wurde.
	 */
	protected void dialogConfirmed() {
		if(requestConfirmation()){
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
		            new StringSelection(getPassword()), null);
			JOptionPane.showMessageDialog(dialog, "Das neue Passwort wurde in die Zwischenablage kopiert.\n"
					+ "Dies bitte im TrueCrypt Dialog zum �ndern des Passworts einf�gen.","Information",JOptionPane.INFORMATION_MESSAGE);
			tcControl.migrateOldContainer(file.getAbsolutePath());
			JOptionPane.showMessageDialog(dialog, "Bitte erste Best�tigen, wenn das neue Passowort in TrueCrypt eingesetzt wurde", "TrueCrypt Passwort�nderung abgeschlossen", JOptionPane.INFORMATION_MESSAGE);
			if(tcControl.mountContainer(file.getAbsolutePath(), getPassword())){
				card.writeContainerData(getPassword(), textFieldContName.getText(), tableSmartCardContainer.getSelectedRow());
				showSuccess();
				card.getSavedContainer();
			}
			else{
				showError("Fehler bei der Migration. TrueCrypt.exe Richtig gew�hlt?", "Fehler");
			}
		}
	}	
	
	/**
	 * Zeigt eine Erfolgsmeldung an und Beendet den Dialog.
	 */
	public void showSuccess(){
		JOptionPane.showMessageDialog(dialog,						
                "Neuer Container wurde erfolgreich erstellt.",
                "Erfolg",
                JOptionPane.INFORMATION_MESSAGE);
		clearFields();
		showDialog(false);
	}
	
	/**
	 * Zeigt das erstellte Passwort an oder versteckt es, je
	 * nachdem ob showPasswordBox gesetzt ist.
	 */
	private void setUserOption(){
		if(showPasswordBox.isSelected()){
			passwordField.setVisible(true);
			textFieldPW.setText("");
			textFieldPW.setVisible(false);
		}else{
			passwordField.setVisible(false);
			textFieldPW.setVisible(true);
			textFieldPW.setText(getPassword());
		}
		textFieldContName.requestFocus();
	}
	
	/**
	 * �berpr�ft ob ein bereits existierender SmartCard-Container �berschrieben wird.
	 * Falls Ja wird der Benutzer zu einer Best�tigung aufgefordert, 
	 * ob er den gew�hlten Platz wirklich �berschreiben will.
	 * @return Liefert bei einem leeren PLatz true und nach get�tigter Best�tigung
	 */
	public boolean requestConfirmation(){
		int index = tableSmartCardContainer.getSelectedRow();
		if(file!=null){
			if(smartCardContainer[index]!=null){
				int result = JOptionPane.showConfirmDialog(dialog, 
						"Wirklich bestehendes Passwort vom Container '"+Terminal.trimName(smartCardContainer[index].getName())+
						"' �berschreiben?\n\n          !!!WARNUNG!!!: Das alte Passwort geht dabei verloren!", 
						"Abfrage", 
						JOptionPane.YES_NO_OPTION);
				if(result==JOptionPane.YES_OPTION)return true;
				else return false;
			}
			return true;
		}else{
			showError("Es wurde keine Containerdatei ausgew�hlt! Bitte eine Datei w�hlen und wiederholen.", "Keine Datei gew�hlt");
		}
		return false;
	}
	
	/**
	 * Liefert das erzeugte Passwort als String.
	 * @return Generierte Passwort
	 */
	private String getPassword(){
		return String.valueOf(passwordField.getPassword());
	}
	
	/**
	 * Erzeugt 64 Zufallszahlen und nimmt diese als Index f�r ein Array,
	 * welches alle Gro�- und Kleinbuchstaben enth�lt, sowie die 
	 * Zahlen 0-9.
	 * Schreibt anschlie�end das erstellte Passwort in das JPasswordField.
	 */
	private void generatePassword(){
		char[] temp = new char[64];
		for (int i=0;i<temp.length;i++){
			temp[i] = alphanumerics[random.nextInt(alphanumerics.length)];
		}
		passwordField.setText(new String(temp));
		byte[] bytetest = getPassword().getBytes();
		String ausgabe = "";
		for(int i=0;i<bytetest.length;i++){
			ausgabe = ausgabe + (char) bytetest[i];
		}
	}
	
	/**
	 * L�scht die eingegebenen Daten.
	 */
	private void clearFields(){
		textFieldContName.setText("");
		passwordField.setText("");
	}
	
	/**
	 * �ffnet den FileInput-Dialog zur Auswahl eines TrueCrypt-Containers.
	 */
	private void openFileDialog(){
		File file = new File(Terminal.datapath);
		JFileChooser fc = new JFileChooser(new File(".*"));
		fc.setCurrentDirectory(file);
		int option = fc.showOpenDialog(dialog);
		file = fc.getSelectedFile();		
		if(option==JFileChooser.APPROVE_OPTION){
			this.file = file;
			textFieldFile.setText(file.getAbsolutePath());
		}else{
			file = null;
		}
	}
	
	/**
	 * �berschreibung der geerbten actionPerformed Methode um
	 * den notwendigen FileInput-Dialog zu �ffnen und das
	 * Passwort anzuzeigen.
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == button_cancel){
			showDialog(false);
		}
		if(e.getSource() == openFile){
			openFileDialog();
		}
		if(e.getSource() == button_ok){
			dialogConfirmed();
		}
		if(e.getSource() == showPasswordBox){
			setUserOption();
		}
	}

	
}