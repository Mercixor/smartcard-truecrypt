package terminal.dialogs;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import terminal.JavaCard;
import terminal.TrueCryptControl;
import terminal.data.TerminalData;
/**
 * Dialog zum Anzeigen der gespeicherten Passwörter auf der SmartCard.
 * Wird von Dialog_Basic_SmartCard abgeleitet.
 * @author Dennis Jandt
 * @see Dialog_Basic_SmartCard
 */
public class Dialog_ShowSavedPasswords extends Dialog_Basic_SmartCard{
	private JTextField containerName, pwTextfield;
	
	/**
	 * Ruft den Oberklassen Konstruktor auf und erstellt die 
	 * notwendigen Zusatzelemente.
	 * @param mainwindow Das Elternobjekt des Dialogs
	 * @param card Referenz auf das JavaCard-Objekt
	 * @param data Referenz zur Speicherklasse
	 * @param tcCont Referenz auf das TrueCryptControl Objekt
	 */
	public Dialog_ShowSavedPasswords(JFrame mainwindow, JavaCard card,TerminalData data,TrueCryptControl tcCont){
		super(mainwindow, card, data, tcCont,"Containerpasswort von Karte anzeigen lassen", 3, 5);
		
		// Dialogelemente erzeugen
		pwTextfield			= new JTextField();
		containerName		= new JTextField();
		
		// Werte der Dialogelemente einstellen	
		addComponent(new JLabel("Container Name:"), 0, 0, 1, 1);
		addComponent(containerName, 1, 0, 1, 1);
		containerName.setEnabled(false);
		addComponent(labelSmartCardContainer, 2, 0, 1, 1);
		addComponent(new JLabel("Passwort:"), 0, 1, 1, 1);
		addComponent(pwTextfield, 1, 1, 1, 1);
		
		addComponent(button_ok, 0, 3, 2, 1);
		button_ok.setText("Passwort anzeigen");
		addComponent(button_cancel, 0, 4, 2, 1);			
		addComponent(scrollPaneSCContainers, 2, 1, 1, 4);
		dialog.setLocation(100, 120);
	}
	
	/**
	 * Zeigt je nach Auswahl den Dialog an. Wird der Dialog angezeigt, wird die
	 * Oberklassenmethode fillTableSmartCardContainer() aufgerufen.
	 */
	public void showDialog(boolean auswahl){		
		if(auswahl){			
			fillTableSmartCardContainer();
		}else{
			clearFields();			
		}
		dialog.setVisible(auswahl);
	}

	/**
	 * Löscht die eingegebenen Daten.
	 */
	private void clearFields(){
		containerName.setText("");
		pwTextfield.setText("");
	}
	
	private void showPassword(){
		int index = tableSmartCardContainer.getSelectedRow();
		if(index ==-1){
			JOptionPane.showMessageDialog(dialog, "Keinen Container aus der Liste gewählt", "Kein Element gewählt", JOptionPane.OK_OPTION);
			return;
		}
		if(smartCardContainer[index]!=null){
			containerName.setText(smartCardContainer[index].getName());
			pwTextfield.setText(card.getSelectedPassword(index));
		}else{
			JOptionPane.showMessageDialog(dialog, "Es wurde ein leerer Container gewählt. Bitte einen mit Passwort wählen",
					"Leeres Element gewählt", JOptionPane.OK_OPTION);
		}		
	}
	
	/**
	 * Leere Methode, da diese in diesem Dialog nicht benötigt wird.
	 */
	protected void showSuccess() {}

	/**
	 * Zeigt das gewählte Passwort an.
	 */
	protected void dialogConfirmed() {
		showPassword();
	}	
}