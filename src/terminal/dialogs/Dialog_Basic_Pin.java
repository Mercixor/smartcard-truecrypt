package terminal.dialogs;

import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import terminal.JavaCard;
import terminal.Terminal;
import terminal.data.TerminalData;
/**
 * Abstrakte Klasse die den Dialog_Basic um ein JPasswordField erweitert.
 * @author Dennis Jandt
 * @see Dialog_Basic
 */
public abstract class Dialog_Basic_Pin extends Dialog_Basic{

	protected JPasswordField pin_field;
	protected Terminal terminal;
	
	/**
	 * Platziert die Elemente in einem Standardmuster. Das JLabel muss
	 * in der erbenden Klasse hinzugefügt werden.
	 * @param mainwindow Das Elternobjekt des Dialogs
	 * @param card Referenz auf das JavaCard-Objekt
	 * @param data Referenz zur Speicherklasse
	 * @param title Titel des Dialogfensters
	 * @param x Anzahl der Elemente in der horizontalen
	 * @param y Anzahl der Elemente in der vertikalen.
	 * @param terminal Referenz zum Hautobjekt um den Anmeldestatus zu setzen.
	 */
	public Dialog_Basic_Pin(JFrame mainwindow,JavaCard card,TerminalData data, String title,int x,int y, Terminal terminal){
		super(mainwindow,card,data,title,x,y);
		this.terminal = terminal;
		addComponent(button_ok, 0, 1, 1, 1);
		addComponent(button_cancel, 1, 1, 1, 1);
		pin_field = new JPasswordField(4);
		pin_field.setEchoChar('*');
		addComponent(pin_field, 1, 0, 1, 1);
	}
	
	/**
	 * Löscht die eingetragenen Wert im Passwortfeld und setzt den Focus auf dieses.
	 */
	protected void clearPasswordField(){
		pin_field.setText("");
		pin_field.requestFocus();
	}
	
	/**
	 * Liefert das in dem JPasswordField enthaltene Passwort als String zurück.
	 * @param pwfield Enthält das zu extrahierende Passwort
	 * @return Das Passwort als String
	 */
	protected String getPassword(JPasswordField pwfield){
		return String.valueOf(pin_field.getPassword());
	}
	
	/**	Überprüft den übergebenen PIN
	 * 	
	 * @param pin Der vom Benutzer eingegebene PIN
	 * @return Ergebnis der PIN-Überprüfung
	 */
	protected boolean checkPin(String pin){
		int pinversuche  = card.getRemainingTries();
		int ergebnisdialog = 0;
		if(pinversuche == -1){
			showError("Fehler", "Fehler beim Abrufen der verbleibenden Versuche.");
			return false;
		}
		// Wenn bereits Fehleingaben getätigt wurden, wird eine Warnung mit den verbleibenden Versuchen angezeigt
		if(pinversuche < Terminal.MAX_RETRIES){
			String warnung = ("Nur noch "+pinversuche+" Versuche! Bei "+Terminal.MAX_RETRIES+" Fehlschlägen Sperrung der Karte\nFortfahren?");
			ergebnisdialog = JOptionPane.showConfirmDialog(dialog, warnung, "Warnung", JOptionPane.YES_NO_OPTION);
		}
		// Abfrage bestätigt bzw. noch keine Fehlversuche getätigt
		if(ergebnisdialog == JOptionPane.YES_OPTION || pinversuche == Terminal.MAX_RETRIES){
			// Übergabe der PIN an die Klasse JavaCard
			// Bei dem Rückgabewert 'true' wird der Benutzer als angemeldet
			// betrachtet und der Dialog ausgeblendet
			if (card.checkPin(pin)){
				terminal.angemeldet = true;
				// Löschen des gespeicherten PINs
				pin = "";
				return true;
			}else{
				// Schlägt die Authorisierung fehl, wird der Fehlschlagszähler um
				// eins erhöht und eine Rückmeldung an den Benutzer ausgegeben
				// Erreich der Zähler drei, was die maximale Fehlereingabe darstellt
				// erhält der Benutzer die Rückmeldung das seine Karte gesperrt wurde
				// Falls noch Versuche übrig sind, wird der JDialog erneut angezeigt
				pinversuche = card.getRemainingTries();
				if (pinversuche == 0){
					showDialogCardDisabled();
					clearPasswordField();
					showDialog(false);
				}else{
					showError("Falscher PIN - Versuch Nr: "+(Terminal.MAX_RETRIES-pinversuche),"Falscher PIN");
					clearPasswordField();
				}
			}
		// Löschen des gespeicherten PINs
		pin = "";
		return false;
		}else{
			showDialog(false);
			clearPasswordField();
			return false;
		}
	}

	/**
	 * 	/**
	 * Überprüft die Eingabe des Benutzers im Dialog auf korrekte Syntax.
	 * 
	 * @param pwfield Das zu prüfende JPasswordField-Objekt
	 * @param length Die korrekte Länge
	 * @return Ergebnis der Syntax-Überprüfung
	 */
	protected boolean checkInput(JPasswordField pwfield, int length){
		String pin = String.valueOf(pwfield.getPassword());
		// Überprüfung ob die Eingabe der Länge entspricht
		if (pin.length() == length){
			// Überprüfung ob nur Zahlen eingegeben wurden
			if(Pattern.matches("\\d*",pin)){
				return true;
			} else{
				showError("Falsche Eingabe - Nur Zahlen, keine Buchstaben  o.ä.","Fehler bei der Eingabe");
				pwfield.setText("");
				pwfield.requestFocus();
			}
		// Fehlermeldung falls eine falsche Eingabelänge vorliegt
		}else{
			showError("Falsche Eingabelänge - Es müssen "+length+" Zahlen sein.","Fehler bei der Eingabe");
			pwfield.setText("");
			pwfield.requestFocus();
		}
		return false;
	}
	
	/**
	 * Zeigt dem Benutzer die Meldung, dass seine PIN gesperrt ist.
	 */
	private void showDialogCardDisabled(){
		JOptionPane.showMessageDialog(dialog,						
                "PIN gesperrt. Zum Entsperren mit der PUK: 'Hilfe' -> 'Karte entsperren'",
                "PIN gesperrt",
                JOptionPane.WARNING_MESSAGE);	
	}
}
