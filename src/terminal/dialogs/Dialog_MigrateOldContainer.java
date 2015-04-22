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
	 * Erzeugt die für die Passwortgenerierung notwendige SecureRandom-Objekt 
	 * und das Array, auf das die später erzeugten Zufallszahlen abgebildet werden.
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
		openFile			= new JButton("Datei wählen");
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
		// Actionlistener zu den Interaktionselemente hinzufügen
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
	 * Wird nach Bestätigung von button_ok aufgerufen. Das Passwort wird
	 * in die Zwischenablage kopiert und der Benutzer muss dies anschließend im 
	 * TrueCrypt-Dialog einfügen. Bestätigt der Benutzer die Eingabe, wird der
	 * Container zu laden versucht. Dies soll dem Programm die Bestätigung liefern, 
	 * dass das neue Passwort gesetzt wurde.
	 */
	protected void dialogConfirmed() {
		if(requestConfirmation()){
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
		            new StringSelection(getPassword()), null);
			JOptionPane.showMessageDialog(dialog, "Das neue Passwort wurde in die Zwischenablage kopiert.\n"
					+ "Dies bitte im TrueCrypt Dialog zum ändern des Passworts einfügen.","Information",JOptionPane.INFORMATION_MESSAGE);
			tcControl.migrateOldContainer(file.getAbsolutePath());
			JOptionPane.showMessageDialog(dialog, "Bitte erste Bestätigen, wenn das neue Passowort in TrueCrypt eingesetzt wurde", "TrueCrypt Passwortänderung abgeschlossen", JOptionPane.INFORMATION_MESSAGE);
			if(tcControl.mountContainer(file.getAbsolutePath(), getPassword())){
				card.writeContainerData(getPassword(), textFieldContName.getText(), tableSmartCardContainer.getSelectedRow());
				showSuccess();
				card.getSavedContainer();
			}
			else{
				showError("Fehler bei der Migration. TrueCrypt.exe Richtig gewählt?", "Fehler");
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
	 * Überprüft ob ein bereits existierender SmartCard-Container überschrieben wird.
	 * Falls Ja wird der Benutzer zu einer Bestätigung aufgefordert, 
	 * ob er den gewählten Platz wirklich überschreiben will.
	 * @return Liefert bei einem leeren PLatz true und nach getätigter Bestätigung
	 */
	public boolean requestConfirmation(){
		int index = tableSmartCardContainer.getSelectedRow();
		if(file!=null){
			if(smartCardContainer[index]!=null){
				int result = JOptionPane.showConfirmDialog(dialog, 
						"Wirklich bestehendes Passwort vom Container '"+Terminal.trimName(smartCardContainer[index].getName())+
						"' überschreiben?\n\n          !!!WARNUNG!!!: Das alte Passwort geht dabei verloren!", 
						"Abfrage", 
						JOptionPane.YES_NO_OPTION);
				if(result==JOptionPane.YES_OPTION)return true;
				else return false;
			}
			return true;
		}else{
			showError("Es wurde keine Containerdatei ausgewählt! Bitte eine Datei wählen und wiederholen.", "Keine Datei gewählt");
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
	 * Erzeugt 64 Zufallszahlen und nimmt diese als Index für ein Array,
	 * welches alle Groß- und Kleinbuchstaben enthält, sowie die 
	 * Zahlen 0-9.
	 * Schreibt anschließend das erstellte Passwort in das JPasswordField.
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
	 * Löscht die eingegebenen Daten.
	 */
	private void clearFields(){
		textFieldContName.setText("");
		passwordField.setText("");
	}
	
	/**
	 * Öffnet den FileInput-Dialog zur Auswahl eines TrueCrypt-Containers.
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
	 * Überschreibung der geerbten actionPerformed Methode um
	 * den notwendigen FileInput-Dialog zu öffnen und das
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