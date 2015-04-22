package terminal.dialogs;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

import terminal.JavaCard;
import terminal.TrueCryptControl;
import terminal.data.TerminalData;
/**
 * Zeigt die gespeicherten PublicKeys des Rechners an.
 * Wird von Dialog_Basic_SmartCard_PublicKey abgeleitet.
 * @author Dennis Jandt
 * @see Dialog_Basic_SmartCard_PublicKey
 */
public class Dialog_ShowSavedPublicKeys extends Dialog_Basic_SmartCard_PublicKey{
	private JButton button_savePublicKey;
	
	/**
	 * Ruft den Oberklassen Konstruktor auf und erstellt die 
	 * notwendigen Zusatzelemente.
	 * @param mainwindow Das Elternobjekt des Dialogs
	 * @param card Referenz auf das JavaCard-Objekt
	 * @param data Referenz zur Speicherklasse
	 * @param tcCont Referenz auf das TrueCryptControl Objekt
	 */
	public Dialog_ShowSavedPublicKeys(JFrame mainwindow, JavaCard card,TerminalData data,TrueCryptControl tcCont){
		super(mainwindow, card, data, tcCont,"Gespeicherte PublicKeys", 2, 7);	
		button_savePublicKey = new JButton("PublicKey speichern");
		renewSize(210, 25, 5);
		setSize(2, 7);
		addComponent(labelPublicKey, 0, 0, 2, 1);
		addComponent(scrollPanePublicKeys, 0, 1, 2, 4);
		addComponent(button_cancel, 0, 6, 1, 1);
		addComponent(button_savePublicKey, 0, 5, 1, 1);
		addComponent(button_ok, 1, 5, 1, 1);
		button_ok.setText("PublicKey Löschen");
		button_savePublicKey.addActionListener(this);
		dialog.setLocation(10, 120);	
	}
	
	/**
	 * Zeigt je nach Auswahl den Dialog an. Wird der Dialog angezeigt, wird die
	 * Oberklassenmethode fillPublicKeyArray() aufgerufen.
	 */
	public void showDialog(boolean auswahl){
		fillPublicKeyArray();
		dialog.setVisible(auswahl);	
	}
	
	/**
	 * Speichert den von der Karte eingesteckten PublicKey ab.
	 */
	public void savePublicKey(){
		if(card.isCardPresent())card.getPublicKey();
		else showError("Keine Karte eingesteckt", "Keine Karte vorhanden");
		renewData();
	}

	/**
	 * Leere Methode, da keine Erfolgsmeldung benötigt wird.
	 */
	protected void showSuccess() {
	}

	/**
	 * Erneuert die angezeigten Daten.
	 */
	private void renewData(){
		fillPublicKeyArray();
	}
	
	/**
	 * Wird durch die Bestätigung des Dialogs aufgerufen.
	 * Löscht den gewählten PublicKey aus der Liste.
	 */
	protected void dialogConfirmed() {
		int index = tablePublicKeys.getSelectedRow();
		if (index == -1){
			showError("Kein Element der Liste zum Löschen gewählt! Bitte eins auswählen!","Fehler");
		}else{
			data.deletePublicKey(index);
			renewData();
		}
	}
	
	/**
	 * Überschreibung der geerbten actionPerformed Methode um
	 * den PublicKey abspeichern zu können.
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == button_savePublicKey){
			savePublicKey();
		}
		if(e.getSource() == button_cancel){
			showDialog(false);
		}
		if(e.getSource() == button_ok){
			dialogConfirmed();
		}
	}
}
