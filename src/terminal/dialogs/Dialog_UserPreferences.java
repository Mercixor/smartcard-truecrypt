package terminal.dialogs;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import terminal.JavaCard;
import terminal.Terminal;
import terminal.data.TerminalData;
import terminal.data.UserData_Preferences;
/**
 * Dialog zur Änderung der Benutzereinstellungen.
 * Wird von Dialog_Basic abgeleitet.
 * @author Dennis Jandt
 * @see Dialog_Basic
 */
public class Dialog_UserPreferences extends Dialog_Basic implements ChangeListener{
	private JTextField textFieldPath;
	private JButton button_openPath,button_robotsActiv,button_robotsNotActiv;
	private JSlider sliderRobotsDelay;
	private JLabel labelRobotsDelay;
	private Terminal terminal;
	
	/**
	 * Ruft den Oberklassen Konstruktor auf und die Methode
	 * initGUI().
	 * @param mainwindow Das Elternobjekt des Dialogs
	 * @param card Referenz auf das JavaCard-Objekt
	 * @param data Referenz zur Speicherklasse
	 * @param terminal Referenz zum Hautprogramm
	 */
	public Dialog_UserPreferences(JFrame mainwindow, JavaCard card,
			TerminalData data, Terminal terminal) {
		super(mainwindow, card, data, "Benutzer Einstellungen", 2,7);
		this.terminal = terminal;
		initGUI();
	}
	
	/**
	 * Erzeugt und platziert die notwendigen Zusatzelemente
	 * im Dialog.
	 */
	private void initGUI(){
		textFieldPath = new JTextField();
		button_openPath=new JButton("TrueCrypt Pfad ändern");
		button_robotsActiv=new JButton("Robots Deaktivieren");
		button_robotsNotActiv=new JButton("Robots Aktivieren");
		sliderRobotsDelay=new JSlider(SwingConstants.HORIZONTAL, 10, 500, 60);
		labelRobotsDelay = new JLabel("Robots-Eingabe-Verzögerung: 60 ms");
		button_ok.setText("Speichern");
		button_cancel.setText("Abbrechen");
		addComponent(new JLabel("TrueCrypt-Pfad:"), 0, 0, 1, 1);
		addComponent(textFieldPath, 1, 0, 1, 1);
		addComponent(button_openPath, 0, 1, 2, 1);
		addComponent(labelRobotsDelay, 0,2, 2, 1);
		addComponent(sliderRobotsDelay, 0, 3, 2, 2);
		addComponent(button_robotsActiv, 0, 5, 2, 1);
		addComponent(button_robotsNotActiv, 0, 5, 2, 1);
		addComponent(button_cancel, 0, 6, 1, 1);
		addComponent(button_ok, 1, 6, 1, 1);
		dialog.setLocation(100, 100);
		// Listener hinzufügen
		sliderRobotsDelay.addChangeListener(this);
		button_openPath.addActionListener(this);
		button_robotsActiv.addActionListener(this);
		button_robotsNotActiv.addActionListener(this);
		// JSlider initialisieren
		sliderRobotsDelay.setMajorTickSpacing(100);
		sliderRobotsDelay.setMinorTickSpacing(10);
		sliderRobotsDelay.setPaintTicks(true);
		sliderRobotsDelay.setPaintLabels(true);
	}

	/**
	 * Zeigt je nach Auswahl den Dialog an. Wird der Dialog angezeigt, 
	 * werden die gespeicherten Benutzeroptionen eingesetzt.
	 */
	public void showDialog(boolean auswahl) {
		UserData_Preferences pref = data.getUserData().getUserPreferences();
		if(auswahl){					
			if(pref.isRobotSet()){
				button_robotsActiv.setVisible(true);
				button_robotsNotActiv.setVisible(false);
			}else{
				button_robotsNotActiv.setVisible(true);
				button_robotsActiv.setVisible(false);
			}
			sliderRobotsDelay.setValue(pref.getRobotDelay());
			labelRobotsDelay.setText("Robots-Eingabe-Verzögerung: "+sliderRobotsDelay.getValue()+" ms");
			textFieldPath.setText(pref.getTrueCryptPath());
			if(textFieldPath.getText().equals(""))JOptionPane.showMessageDialog(mainwindow, "Bitte den Pfad zur TrueCrypt.exe angeben.\n"
					+ "Dieser wird zur Funktion des Programms benötigt!", "TrueCrypt-Pfad nicht gefunden",JOptionPane.INFORMATION_MESSAGE);
			dialog.setVisible(true);
		}else{
			if(pref.getTrueCryptPath().equals("")){
				JOptionPane.showMessageDialog(dialog, "Bitte die TrueCrypt.exe Datei auf dem System wählen.\n"
						+ "Ohne diese ist das Programm nicht funktionsfähig.","Kein Pfad angegeben",JOptionPane.INFORMATION_MESSAGE);
			}else{
				dialog.setVisible(false);
			}
		}
	}

	/**
	 * Zeigt eine Erfolgsmeldung an und Beendet den Dialog.
	 * Zusätzlich werden die gewählten Optionen gespeichert
	 * und im Hautporgramm aktualisiert.
	 */
	protected void showSuccess() {
		JOptionPane.showMessageDialog(dialog, "Einstellungen gespeichert.","Einstellungen gespeichert",JOptionPane.INFORMATION_MESSAGE);
		terminal.setUserPreferences();
		showDialog(false);
	}

	/**
	 * Wird durch die Bestätigung des Dialogs aufgerufen.
	 * Speichert die Eingegebenen Daten in den Benutzereinstellungen.
	 */
	protected void dialogConfirmed() {
		UserData_Preferences pref = data.getUserData().getUserPreferences();
		pref.setTrueCryptPath(textFieldPath.getText());
		pref.setRobotDelay(sliderRobotsDelay.getValue());
		pref.setRobotStatus(button_robotsActiv.isVisible());
		if(pref.getTrueCryptPath().equals("")){
			JOptionPane.showMessageDialog(dialog, "Bitte die TrueCrypt.exe Datei auf dem System wählen.\n"
					+ "Ohne diese ist das Programm nicht funktionsfähig.","Kein Pfad angegeben",JOptionPane.INFORMATION_MESSAGE);
		}else{
			showSuccess();
		}
	}
	
	/**
	 * Öffnet den FileInput-Dialog zur Auswahl der TrueCrypt.exe
	 */
	private void openFileDialog(){
		File file = new File("C:/");
		JFileChooser fc = new JFileChooser(new File(".exe"));
		fc.setCurrentDirectory(file);
		int option = fc.showOpenDialog(dialog);
		file = fc.getSelectedFile();		
		if(option==JFileChooser.APPROVE_OPTION){
			textFieldPath.setText(file.getAbsolutePath());			
		}else{
			file = null;
		}
	}
	
	/**
	 * Überschreibung der geerbten actionPerformed Methode um
	 * den notwendigen FileInput-Dialog zu öffnen und 
	 * den Status der Robot-Aktivierung zu setzen.
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == button_ok){
			dialogConfirmed();
		}
		if(e.getSource() == button_cancel){
			showDialog(false);
		}
		if(e.getSource() == button_openPath){
			openFileDialog();
		}
		if(e.getSource() == button_robotsActiv){
			button_robotsActiv.setVisible(false);
			button_robotsNotActiv.setVisible(true);
		}
		if(e.getSource() == button_robotsNotActiv){
			button_robotsActiv.setVisible(true);
			button_robotsNotActiv.setVisible(false);
		}
	}

	/**
	 * Implementierung des ChangeListeners. Dieser überträgt die Änderungen des
	 * Benutzers am Slide-Objekt auf das Label.
	 */
	public void stateChanged(ChangeEvent e) {
		labelRobotsDelay.setText("Robots-Eingabe-Verzögerung: "+sliderRobotsDelay.getValue()+" ms");
	}

}
