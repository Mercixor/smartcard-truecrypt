package terminal.dialogs;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import terminal.JavaCard;
import terminal.Terminal;
import terminal.TrueCryptControl;
import terminal.data.Encrypted_Password_Container;
import terminal.data.TerminalData;
/**
 * Dialog zum Teilen eines Containerpasswortes von
 * der SmartCard.
 * Wird von Dialog_Basic_SmartCard_PublicKey abgeleitet.
 * @author Dennis Jandt
 * @see Dialog_Basic_SmartCard_PublicKey
 */
public class Dialog_Share_Container_Password extends Dialog_Basic_SmartCard_PublicKey{
	
	/**
	 * Ruft den Oberklassen Konstruktor auf und erstellt die 
	 * notwendigen Zusatzelemente.
	 * @param mainwindow Das Elternobjekt des Dialogs
	 * @param card Referenz auf das JavaCard-Objekt
	 * @param data Referenz zur Speicherklasse
	 * @param tcCont Referenz auf das TrueCryptControl Objekt
	 */
	public Dialog_Share_Container_Password(JFrame mainwindow,
			JavaCard card, TerminalData data,TrueCryptControl tcCont) {
		super(mainwindow, card, data, tcCont,"Container-Passwort teilen", 4,6);

		// Werte zuweisen
		addComponent(labelPublicKey, 0, 0, 3, 1);
		addComponent(labelSmartCardContainer, 3, 0, 1, 1);
		addComponent(scrollPanePublicKeys, 0, 1, 3, 4);
		addComponent(scrollPaneSCContainers, 3, 1, 1, 4);
		addComponent(button_cancel, 0, 5, 1, 1);	
		addComponent(button_ok, 1, 5, 1, 1);
		
		// GUI-Elemente zum Dialog hinzufügen
		dialog.setLocation(100, 100);
	}
	
	/**
	 * Zeigt je nach Auswahl den Dialog an. Wird der Dialog angezeigt, wird die
	 * Oberklassenmethode fillTableSmartCardContainer() aufgerufen, sowie 
	 * fillPublicKeyArray().
	 */
	public void showDialog(boolean auswahl){
		if(auswahl){			
			fillTableSmartCardContainer();						
			fillPublicKeyArray();			
		}
		dialog.setVisible(auswahl);
	}	

	/**
	 * Zeigt eine Erfolgsmeldung an und Beendet den Dialog.
	 */
	public void showSuccess(){
		JOptionPane.showMessageDialog(dialog,						
                "Passwort-Datei erfolgreich erstellt!",
                "Erfolg",
                JOptionPane.INFORMATION_MESSAGE);
		showDialog(false);
	}

	/**
	 * Wird durch die Bestätigung des Dialogs aufgerufen.
	 * Überprüft ob alle notwendigen Elemente ausgewählt wurden
	 * und falls ja, wird das gewählte Containerpasswort mit
	 * dem gewählten PublicKey verschlüsselt.
	 * Danach wird das verschlüsselte Passwort abgespeichert.
	 */
	protected void dialogConfirmed() {
		try{
			int indexSmartCard = tableSmartCardContainer.getSelectedRow();
			int indexPublicKey = tablePublicKeys.getSelectedRow();
			String pw = card.getSelectedPassword(indexSmartCard);
			if(indexSmartCard!=-1){
				if(indexPublicKey!=-1){			
					if(pw==null)showError("Fehler beim Abrufen des Containerpassworts von der SmartCard.","Fehler");
					else{
						RSAPublicKeySpec keyspec = data.getSpecificUser(tablePublicKeys.getSelectedRow()).getPublicKey();
						Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");			
						PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(keyspec);
						cipher.init(Cipher.ENCRYPT_MODE,publicKey);	
						byte[] ciphertext = cipher.doFinal(pw.getBytes());
						Encrypted_Password_Container encrCont = new Encrypted_Password_Container( ciphertext);
						if(encrCont.writeObject(card.preName+card.name,
								Terminal.trimName(smartCardContainer[indexSmartCard].getName()),
								(String) tablePublicKeys.getValueAt(indexPublicKey, 1)+
								(String) tablePublicKeys.getValueAt(indexPublicKey, 0)))showSuccess();
					}
					}else showError("Kein PublicKey aus der Liste gewählt!", "Kein Element gewählt");
				}else showError("Kein Passwort von der SmartCard gewählt", "Kein Element gewählt");
		}catch (Exception e){
			e.printStackTrace();
			showError("Fehler beim Schreiben der Passwortdatei.", "Dateifehler");
		}
	}
}
