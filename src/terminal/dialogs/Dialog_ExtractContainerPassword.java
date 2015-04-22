package terminal.dialogs;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import terminal.JavaCard;
import terminal.Terminal;
import terminal.TrueCryptControl;
import terminal.data.Encrypted_Password_Container;
import terminal.data.TerminalData;
/**
 * Dialog zur Extraktion eines Passwortes aus einer verschlüsselten Passwortdatei.
 * Wird von Dialog_Basic_SmartCard abgeleitet.
 * @author Dennis Jandt
 * @see Dialog_Basic_SmartCard
 */
public class Dialog_ExtractContainerPassword extends Dialog_Basic_SmartCard{
	
	private JPasswordField pwfield;
	private JTextField containerName;
	private JTextField selectedFile;
	private byte[] passwordByte;
	private File file = null;
	
	private JButton button_chooseFile;
	
	/**
	 * Ruft den Oberkonstruktor auf und die Methode initGUI().
	 * @param mainwindow Das Elternobjekt des Dialogs
	 * @param card Referenz auf das JavaCard-Objekt
	 * @param data Referenz zur Speicherklasse
	 * @param tcCont Referenz auf das TrueCryptControl Objekt
	 */
	public Dialog_ExtractContainerPassword(JFrame mainwindow, JavaCard card,TerminalData data,TrueCryptControl tcCont) {
		super(mainwindow, card, data,tcCont, "Containerpasswort aus Datei extrahieren", 3, 6);
		initGUI();
	}
	
	/**
	 * Erzeugt die notwendigen Bedienelemente und platziert diese im Dialog.
	 */
	protected void initGUI(){
		// Dialogelemente erzeugen
		button_chooseFile	= new JButton("Datei wählen...");
		pwfield 			= new JPasswordField(64);
		containerName		= new JTextField();
		selectedFile		= new JTextField();
		// Positionierung des Dialogs
		dialog.setLocation(100, 115);
		// Werte der Dialogelemente einstellen
		addComponent(new JLabel("Container Name:"), 0, 0, 1, 1);
		addComponent(new JLabel("Passwort:"), 0, 1, 1, 1);
		addComponent(new JLabel("Datei:"), 0, 2, 1, 1);
		addComponent(containerName, 1, 0, 1, 1);
		addComponent(labelSmartCardContainer, 2, 0, 1, 1);
		addComponent(scrollPaneSCContainers, 2, 1, 1, 5);
		addComponent(button_chooseFile, 0, 3, 2, 1);
		addComponent(button_ok, 0, 4, 2, 1);	
		addComponent(button_cancel, 0, 5, 2, 1);
		addComponent(pwfield, 1, 1, 1, 1);
		addComponent(selectedFile, 1, 2, 1, 1);
		// INIT-Werte zuweisen
		pwfield.setEnabled(false);		
		selectedFile.setEnabled(false);		
		// Actionlistener zu den Interaktionselemente hinzufügen
		button_chooseFile.addActionListener(this);		
	}
	
	/**
	 * Zeigt je nach Auswahl den Dialog an. Wird der Dialog angezeigt, wird die
	 * Oberklassenmethode fillTableSmartCardContainer() aufgerufen.
	 */
	public void showDialog(boolean auswahl){
		if(auswahl){
			clearFields();
			fillTableSmartCardContainer();
		}
		dialog.setVisible(auswahl);		
	}
	
	/**
	 * Zeigt eine Erfolgsmeldung an und Beendet den Dialog.
	 */
	protected void showSuccess(){
		JOptionPane.showMessageDialog(dialog,						
                "Neuer Container wurde erfolgreich erstellt.",
                "Erfolg",
                JOptionPane.INFORMATION_MESSAGE);
		clearFields();
		showDialog(false);
	}
	
	/**
	 * Wird nach Bestätigung von button_ok aufgerufen.
	 * Überprüft ob in den Tabellen Elemente gewählt wurden und gibt
	 * andernfalls eine Fehlermeldung an den Benutzer aus.
	 *
	 * Wenn der Dialog korrekt ausgefüllt ist, werden die Daten zur Karte gesendet.
	 */
	protected void dialogConfirmed() {
		int index = tableSmartCardContainer.getSelectedRow();
		if(index == -1){
			showError("Kein Element ausgewählt! Bitte zum schreiben auf der SmartCard einen Container aus der Liste wählen!","Fehler" );
		}else{
			if(requestConfirmation()){
				if(card.writePublicContainerData(passwordByte,containerName.getText(),
						index)){
					showSuccess();
					card.getSavedContainer();
				}else{
					showError("Fehler beim Entschlüsseln der Passwortdatei. Datei eventuell nicht für den Benutzer verschlüsselt.","Fehler");
				}
			}
		}
	}
	
	/**
	 * Überprüft ob ein bereits existierender SmartCard-Container überschrieben wird.
	 * Falls Ja wird der Benutzer zu einer Bestätigung aufgefordert, 
	 * ob er den gewählten Platz wirklich überschreiben will.
	 * @return Liefert bei einem leeren PLatz true und nach getätigter Bestätigung
	 */
	private boolean requestConfirmation(){
		if(file==null){
			showError("Keine Passwortdatei '*.cont' ausgewählt. Zum Fortfahren bitte zuerst eine entsprechende Datei wählen!", "Fehler");
			return false;
		}else{
			int scIndex = tableSmartCardContainer.getSelectedRow();
			if(smartCardContainer[scIndex]!=null){
				int result = JOptionPane.showConfirmDialog(dialog, 
						"Wirklich bestehendes Passwort vom Container '"+Terminal.trimName(smartCardContainer[scIndex].getName())+
						"' überschreiben?\n\n          !!!WARNUNG!!!: Das alte Passwort geht dabei verloren!", 
						"Abfrage", 
						JOptionPane.YES_NO_OPTION);
				if(result==JOptionPane.YES_OPTION)return true;
				else return false;
			}	
		}
		return true;
	}
	
	/**
	 * Löscht die eingegebenen Daten.
	 */
	private void clearFields(){
		containerName.setText("");
		pwfield.setText("");
	}
	
	/**
	 * Öffnet den FileInput-Dialog zur Auswahl der gespeicherten Passwortdatei.
	 * Wenn der Benutzer eine Datei gewählt hat, wird überprüft, ob es sich dabei
	 * um eine Passwortdatei handelt.
	 */
	private void openFileDialog() {
		file = new File(Terminal.datapath);
		JFileChooser fc = new JFileChooser(new File("*.cont"));
		fc.setCurrentDirectory(file);
		int option = fc.showOpenDialog(dialog);
		file = fc.getSelectedFile();		
		if(option==JFileChooser.APPROVE_OPTION){
			try{
				selectedFile.setText(file.getPath());
				FileInputStream fis = new FileInputStream(file.getPath());
				ObjectInputStream restore = new ObjectInputStream(fis);
				Encrypted_Password_Container encr = (Encrypted_Password_Container) restore.readObject();
				restore.close();
				pwfield.setText(encr.getEncrPassword().toString());
				passwordByte = encr.getEncrPassword();
			} catch (Exception e){
				JOptionPane.showMessageDialog(dialog,						
		                "Fehler beim Einlesen der Datei - keine verschlüsselte Passwortdatei!",
		                "Fehler",
		                JOptionPane.ERROR_MESSAGE);
				selectedFile.setText("");
				pwfield.setText("");
				file = null;
			}
		}else{
			file = null;
		}		
	}
	
	/**
	 * Überschreibung der geerbten actionPerformed Methode um
	 * den notwendigen FileInput-Dialog zu öffnen.
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == button_chooseFile){
			openFileDialog();
		}
		if(e.getSource() == button_ok){
			dialogConfirmed();
		}
		if(e.getSource() == button_cancel){
			showDialog(false);
		}
	}	
}