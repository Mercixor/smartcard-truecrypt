package terminal.dialogs;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import terminal.JavaCard;
import terminal.Terminal;
import terminal.data.TerminalData;
/**
 * Dialog zur Entsperrung des PIN's durch die PUK.
 * Wird von Dialog_Basic_PIN abgeleitet.
 * @author Dennis Jandt
 * @see Dialog_Basic_Pin
 *
 */
public class Dialog_PinReset extends Dialog_Basic_Pin{
	
	/**
	 * Ruft den Oberklassen Konstruktor auf und erzeugt die
	 * notwenidgen Zusatzelemente.
	 * @param mainwindow Das Elternobjekt des Dialogs
	 * @param card Referenz auf das JavaCard-Objekt
	 * @param data Referenz zur Speicherklasse
	 * @param terminal Referenz zum Hautobjekt um den Anmeldestatus zu setzen.
	 */
	public Dialog_PinReset(JFrame mainwindow,JavaCard card,TerminalData data, Terminal terminal){
		super(mainwindow, card, data, "PIN-Entsperren", 2, 2, terminal);
		
		// Einstellung der Position und Größe der Elemente
		addComponent(new JLabel("Bitte PUK eingeben:"), 0, 0, 1, 1);
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
                "PIN erfolgreich entsperrt. Bitte nun normal anmelden.",
                "Erfolg",
                JOptionPane.INFORMATION_MESSAGE);
		clearPasswordField();
		showDialog(false);
	}
	/**
	 * Wird aufgerufen wenn der Benutzer den Dialog bestätigt.
	 * Überprüft die Syntax und sendet die PUK an die Karte.
	 */
	protected void dialogConfirmed() {
		if(checkInput(pin_field, 6)){
			int ergebnis = card.resetPin(getPassword(pin_field));
			if(ergebnis==99)showSuccess();
			else{
				showError("Falsche PUK eingegeben! Noch "+ergebnis+" Versuche.", "Falscher PUK");
			}
		}
	}
}