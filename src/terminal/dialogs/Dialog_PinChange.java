package terminal.dialogs;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import terminal.JavaCard;
import terminal.Terminal;
import terminal.data.TerminalData;
/**
 * Dialog zur Änderung des PIN's.
 * Wird von Dialog_Basic_PIN abgeleitet.
 * @author Dennis Jandt
 * @see Dialog_Basic_Pin
 *
 */
public class Dialog_PinChange extends Dialog_Basic_Pin{
	// Erstellen der GUI-Elemente für die PIN-Abfrage
	
	private JPasswordField new_pin_field;
	
	private static int OLD_PIN = 0;
	private static int NEW_PIN = 1;
	
	/**
	 * Ruft den Oberklassen Konstruktor auf und erzeugt die
	 * notwenidgen Zusatzelemente.
	 * @param mainwindow Das Elternobjekt des Dialogs
	 * @param card Referenz auf das JavaCard-Objekt
	 * @param data Referenz zur Speicherklasse
	 * @param terminal Referenz zum Hautobjekt um den Anmeldestatus zu setzen.
	 */
	public Dialog_PinChange(JFrame mainwindow,JavaCard card,TerminalData data, Terminal terminal){
		super(mainwindow,card,data,"PIN Ändern",3,2,terminal);
		new_pin_field = new JPasswordField(4);
		
		// Einstellung der Position und Größe der Elemente
		addComponent(new JLabel("Alten PIN eingeben:"),0,0,1,1);
		addComponent(new JLabel("Neuen PIN eingeben:"), 0, 1, 1, 1);
		addComponent(new_pin_field, 1, 1, 1, 1);
		addComponent(button_ok, 0, 2, 1, 1);
		addComponent(button_cancel, 1, 2, 1, 1);
		
		new_pin_field.setEchoChar('*');
		
		// Hizufügen zum JDialog
		dialog.setLocation(110, 180);
	}
	
	/**
	 * Zeigt je nach Auswahl den Dialog an.
	 */
	public void showDialog(boolean auswahl){
		dialog.setVisible(auswahl);
		if(auswahl){
			pin_field.requestFocus();
		}else{
			clearPasswordField();
		}
	}
	
	/**
	 * Überprüft die Syntax der eingebenen Strings auf Stimmigkeit.
	 * @return Ob die Syntax stimmt.
	 */
	private boolean checkInputPassword(){
		return (checkInput(pin_field,4) && checkInput(new_pin_field,4));
	}
	
	/**
	 * Zeigt eine Erfolgsmeldung an und Beendet den Dialog.
	 */
	protected void showSuccess() {
		JOptionPane.showMessageDialog(dialog,						
                "Pin erfolgreich geändert",
                "Erfolg",
                JOptionPane.INFORMATION_MESSAGE);
		clearPasswordField();
		showDialog(false);
	}
	
	/**
	 * Liefert das Passwort des jeweiligen Feldes zurück.
	 * @param nummer Ob OLD_PIN oder NEW_PIN
	 * @return Passwort des Feldes.
	 */
	private String getPassword(int nummer){
		if(nummer==OLD_PIN){
			return String.valueOf(pin_field.getPassword());
		}else{
			return String.valueOf(new_pin_field.getPassword());
		}
	}
	
	/**
	 * Löscht die eingegebenen Daten.
	 */
	protected void clearPasswordField(){
		pin_field.setText("");
		new_pin_field.setText("");
	}

	/**
	 * Wird aufgerufen wenn der Benutzer den Dialog bestätigt.
	 * Überprüft den alten PIN und bei Erfolg schreibt
	 * dieser den neuen PIN auf die Karte.
	 */
	protected void dialogConfirmed() {
		if(checkInputPassword()){
			if(checkPin(getPassword(OLD_PIN))){
				if(card.updatePin(getPassword(NEW_PIN))){
					showSuccess();
				}else{
					showError("Fehler", "Fehler bei der PIN-Änderung.");
				}
			}
		}
	}
}