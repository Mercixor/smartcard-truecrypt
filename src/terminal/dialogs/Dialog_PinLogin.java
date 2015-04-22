package terminal.dialogs;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import terminal.JavaCard;
import terminal.Terminal;
import terminal.data.TerminalData;
/**
 * Dialog zum Anmelden des Benutzers im System.
 * Wird von Dialog_Basic_PIN abgeleitet.
 * @author Dennis Jandt
 * @see Dialog_Basic_Pin
 *
 */
public class Dialog_PinLogin extends Dialog_Basic_Pin{
	
	/**
	 * Ruft den Oberklassen Konstruktor auf und erzeugt die
	 * notwenidgen Zusatzelemente.
	 * @param mainwindow Das Elternobjekt des Dialogs
	 * @param card Referenz auf das JavaCard-Objekt
	 * @param data Referenz zur Speicherklasse
	 * @param terminal Referenz zum Hautobjekt um den Anmeldestatus zu setzen.
	 */
	public Dialog_PinLogin(JFrame mainwindow,JavaCard card,TerminalData data, Terminal terminal){
		super(mainwindow, card, data, "PIN Eingabe", 2, 2, terminal);	
		// Einstellung der Position und Größe der Elemente
		addComponent(new JLabel("Bitte PIN eingeben:"), 0, 0, 1, 1);
		// Hinzufügen der Komponenten zum Dialog und Anzeigen
		dialog.setLocation(100, 230);
	}
	
	/**
	 * Zeigt je nach Auswahl den Dialog an.
	 */
	public void showDialog(boolean auswahl){
		dialog.setVisible(auswahl);
		if(!auswahl)clearPasswordField();
	}
	
	/**
	 * Zeigt eine Erfolgsmeldung an und Beendet den Dialog.
	 */
	protected void showSuccess(){
		JOptionPane.showMessageDialog(dialog,						
                "Angemeldet!",
                "Erfolg",
                JOptionPane.INFORMATION_MESSAGE);
		clearPasswordField();
		terminal.angemeldet = true;
		terminal.wasLoggedIn = false;		
		showDialog(false);
		terminal.setUserPreferences();
	}

	/**
	 * Wird aufgerufen wenn der Benutzer den Dialog bestätigt.
	 * Überprüft die Syntax und sendet, wenn diese richitg ist,
	 * den PIN an die Karte.
	 */
	protected void dialogConfirmed() {
		if(checkInput(pin_field, 4)){
			if(checkPin(getPassword(pin_field))){
				terminal.setMaxIndex();
				card.getSavedContainer();
				data.loadUserData(card.getUserData());
				showSuccess();
			}else{
				
			}
		}
	}
}