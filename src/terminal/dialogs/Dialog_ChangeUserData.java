package terminal.dialogs;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import terminal.JavaCard;
import terminal.data.TerminalData;
/**
 * Dialog zur Änderung der auf der Karte gespeicherten Benutzerdaten.
 * Wird von Dialog_Basic abgeleitet.
 * @author Dennis Jandt
 * @see Dialog_Basic
 */
public class Dialog_ChangeUserData extends Dialog_Basic{
	private JTextField name;
	private JTextField preName;
	
	/**
	 * Erzeugt das Objekt die zusätzlichen Textfelder.
	 * @param mainwindow Das Elternobjekt des Dialogs
	 * @param card Referenz auf das JavaCard-Objekt
	 * @param data Referenz zur Speicherklasse
	 */
	public Dialog_ChangeUserData(JFrame mainwindow, JavaCard card,
			TerminalData data) {
		super(mainwindow, card, data, "Benutzedaten ändern", 2, 3);
		// Dialogelemente erzeugen
		name 				= new JTextField();
		preName				= new JTextField();
		
		// Werte der Dialogelemente einstellen	
		addComponent(new JLabel("Vorname:"), 0, 0, 1, 1);
		addComponent(new JLabel("Nachname:"), 0, 1, 1, 1);
		addComponent(preName, 1, 0, 1, 1);
		addComponent(name, 1, 1, 1, 1);
		addComponent(button_ok, 0, 2, 1, 1);	
		addComponent(button_cancel, 1, 2, 1, 1);
	}
	
	/**
	 * Zeigt, je nach Auswahl, den Dialog an. Zusätzlich
	 * wird beim Schließen des Dialogs die eingegebenen Daten gelöscht.
	 */
	public void showDialog(boolean auswahl){
		if(!auswahl) clearFields();	
		dialog.setVisible(auswahl);
	}
	
	/**
	 * Zeigt dem Benutzer eine Erfolgsmeldung an und beendet nach Bestätigung 
	 * den Dialog.
	 */
	protected void showSuccess(){
		JOptionPane.showMessageDialog(dialog,						
                "Benutzerdaten aktualisiert!",
                "Erfolg",
                JOptionPane.INFORMATION_MESSAGE);
		clearFields();
		showDialog(false);
	}
	
	/**
	 * Wird durch die Betätigung des button_ok ausgelöst. 
	 * Wenn die Benutzerdaten erfolgreich auf der Karte geschrieben wurden,
	 * wird showSuccess() aufgerufen. Andernfalls eine Fehlermeldung ausgegeben.
	 */
	protected void dialogConfirmed() {
		if(card.writeUserData(preName.getText(), name.getText()))showSuccess();
		else showError("Fehler beim Schreiben der neuen Benutzerdaten", "Fehler");
	}
	
	/**
	 * Löscht alle eingegebenen Daten.
	 */
	public void clearFields(){
		name.setText("");
		preName.setText("");
	}	
}