package terminal.dialogs;

import java.awt.Container;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.security.SecureRandom;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import terminal.JavaCard;
import terminal.Terminal;
import terminal.TrueCryptControl;
import terminal.data.TerminalData;
import terminal.data.UserData_Preferences;
/**
 * Dialog zum Erstellen eines neuen TrueCrypt-Containers.
 * Dies kann durch die Klasse Robots oder durch den Benutzer
 * manuell erfolgen.
 * @author Dennis Jandt
 * @see Dialog_Basic_SmartCard
 * @see Robot
 */
public class Dialog_NewContainer extends Dialog_Basic_SmartCard{
	
	protected JPasswordField pwfield;
	protected JTextField textFieldContName, textFieldPw,textFieldPath,textFieldSize;
	
	protected JButton buttonOpenFile;
	protected JComboBox<String> comboBoxEncryp,comboBoxHash,comboBoxDataSys,comboBoxCluster;
	protected JPanel panelSmartCard = new JPanel(null), panelTCContainers = new JPanel(null);
	
	protected SecureRandom random;
	protected char[] alphanumerics;
	
	protected Robot robot;
	protected boolean robotstatus, fileexist;
	protected File file;
	protected UserData_Preferences pref;
	
	
	protected JCheckBox checkBoxPassword, checkBoxDynamic;
	
	/**
	 * Erzeugt die für die Passwortgenerierung notwendige SecureRandom-Objekt 
	 * und das Array, auf das die Zufallszahlen abgebildet wird.
	 * @param mainwindow Das Elternobjekt des Dialogs
	 * @param card Referenz auf das JavaCard-Objekt
	 * @param data Referenz zur Speicherklasse
	 * @param tcCont Referenz auf das TrueCryptControl Objekt
	 */
	public Dialog_NewContainer(JFrame mainwindow, JavaCard card,TerminalData data,TrueCryptControl tcCont) {
		super(mainwindow, card, data,tcCont, "Neuen Container erstellen", 1, 1);
		random = new SecureRandom();	
		
		// TrueCrypt Robots Optionen
		TitledBorder titleTC = BorderFactory.createTitledBorder("TrueCrypt Container Optionen");
		panelTCContainers.setBorder(titleTC);		
		addTCContainerComponents(panelTCContainers);
		// SmartCard Optionen
		TitledBorder titleSC = BorderFactory.createTitledBorder("SmartCard Optionen");		
		panelSmartCard.setBorder(titleSC);	
		addSmartCardComponents(panelSmartCard);
		dialog.setLocation(100, 120);
		dialog.add(panelTCContainers);
		dialog.add(panelSmartCard);
		dialog.setSize(420, 400);
		
		// Actionlistener zu den Interaktionselemente hinzufügen
		checkBoxPassword.addActionListener(this);
		
		// Array für Passwortgenerierung
		StringBuilder tmp = new StringBuilder();
	    for (char ch = '0'; ch <= '9'; ++ch)
	      tmp.append(ch);
	    for (char ch = 'a'; ch <= 'z'; ++ch)
	      tmp.append(ch);
	    for (char ch = 'A'; ch <= 'Z';ch++)
	    	tmp.append(ch);
	    alphanumerics = tmp.toString().toCharArray();
	}
	
	/**
	 * Zeigt je nach Auswahl den Dialog an. Wird der Dialog angezeigt, wird die
	 * Oberklassenmethode fillTableSmartCardContainer() und
	 *  generatePassword() aufgerufen. Dann wird geprüft ob Robot vom Benutzer erlaubt ist
	 *  und falls ja, ob das System die Benutzung von Robot zulässt.
	 *  Wenn Robot nicht erlaub ist, wird nur ein beschränkter Dialog angezeigt.
	 */
	public void showDialog(boolean auswahl){		
		if(auswahl){
			pref = data.getUserData().getUserPreferences();
			int dialogx=0,dialogy=0;
			fillTableSmartCardContainer();			
			generatePassword();	
			if(pref.isRobotSet()){
				try{
				robot = new Robot();
				int tcX=(startx+defpadx*4+startx);
				int tcY=(starty+defpady*4+5);
				int scX=(startx+defpadx*3+startx);
				int scY=(starty+defpady*5+5);
				panelTCContainers.setBounds(startx, startx, tcX, tcY);
				panelSmartCard.setBounds(startx, startx+tcY,scX,scY);
				dialogx = startx*3+tcX;
				dialogy	= startx*5+tcY+scY;
				dialog.setSize(dialogx, dialogy);
				robotstatus=true;
				throw new Exception();
				}catch(Exception awte){}
			}else{
				dialog.remove(panelTCContainers);
				dialog.remove(panelSmartCard);
				int scX=(startx+defpadx*3+startx);
				int scY=(starty+defpady*5+5);
				panelSmartCard.setBounds(startx,startx,scX,scY);
				dialogx	= startx*3+scX;
				dialogy	= starty*2+startx+scY;
				dialog.setSize(dialogx,dialogy);
				dialog.add(panelSmartCard);
				robotstatus=false;
			}
		}
		dialog.setVisible(auswahl);
	}
	
	/**
	 * Zeigt eine Erfolgsmeldung an und Beendet den Dialog.
	 */
	protected void showSuccess(){		
		if(robotstatus){
			JOptionPane.showMessageDialog(dialog,						
	                "Neuer Container wurde erfolgreich erstellt.",
	                "Erfolg",
	                JOptionPane.INFORMATION_MESSAGE);
			clearFields();
			showDialog(false);
		}else{
			JOptionPane.showMessageDialog(dialog,						
	                "Bitte Container nun laden über den Dialog 'Container mounten',\n"
	                + "um festzustellen, ob der Container erfolgreich verbunden wurde.",
	                "Information",
	                JOptionPane.INFORMATION_MESSAGE);
			clearFields();
			showDialog(false);
		}		
	}
	
	/**
	 * Wird nach Bestätigung durch den Benutzer aufgerufen.
	 * Überprüft ob die Klasse Robot aktiviert ist und ruft
	 * je nach Auswahl die TrueCrypt-Containererstellung mit
	 * entsprechenden Parametern auf.
	 * Zusätzlich gibt es eine angepasste Informationsmeldung zur 
	 * Benutzung aus.
	 */
	protected void dialogConfirmed() {
		if(requestConfirmation())
		{
			// Abhandlung falls Robots API erlaubt ist
			if(robotstatus){
				JOptionPane.showMessageDialog(dialog, "Ihre Auswahl wird gleich automatisch in TrueCrypt eingefügt.\n"
						+ "Bitte warten Sie, bis das Programm ihnen die bestätigt, dass alle Schritte ausgeführt wurden.", "Information",
						JOptionPane.INFORMATION_MESSAGE);
				
				doRobotsCommands();
				if(checkSucces()){
					if(card.writeContainerData(getPassword(),
							textFieldContName.getText(),
							tableSmartCardContainer.getSelectedRow())){
						showSuccess();
						card.getSavedContainer();
					}
					else showError("Fehler beim Schreiben des neuen Containers auf die SmartCard", "Schreib Fehler");
				}
			}else{
				if(card.writeContainerData(getPassword(),
					textFieldContName.getText(),
					tableSmartCardContainer.getSelectedRow()))
				{
					card.getSavedContainer();
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
	                        new StringSelection(getPassword()), null);
					JOptionPane.showMessageDialog(dialog, "Passwort wurde in die Zwischenablage kopiert."
							+ " Dieses bitte in das entsprechende Passwort Feld via 'Strg+V' einfügen.", "Hinweis", JOptionPane.INFORMATION_MESSAGE);
					tcControl.openNewContainerWizard(true);
					if(checkSucces())showSuccess();
				}else showError("Fehler beim Schreiben des neuen Containers auf die SmartCard", "Schreib Fehler");
			
			}
		}
	}
	
	/**
	 * Führt die notwendigen Schritte zur Befüllung
	 * der TrueCrypt Containererstellung aus.
	 */
	private void doRobotsCommands(){
		int i,defaultDelay=pref.getRobotDelay();
		int mbsize = Integer.parseInt(textFieldSize.getText());
		int delay=0;
		if(mbsize<200)delay=6000;
		else if(mbsize<500) delay=8000;
			else if(mbsize<1000) delay=18000;
				else delay=25000;
		tcControl.openNewContainerWizard(false);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(file.getAbsolutePath()), null);
		robot.setAutoDelay(defaultDelay);
		robot.delay(300);
		altN();
		altN();
		strgV();
		altN();
		altN();
		for(i=0;i<comboBoxEncryp.getSelectedIndex();i++)keyUpDown(false);
		for(i=0;i<3;i++)tab();
		for(i=0;i<comboBoxHash.getSelectedIndex();i++)keyUpDown(false);
		altN();
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(textFieldSize.getText()), null);
		strgV();
		altN();
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(getPassword()), null);
		strgV();
		tab();
		strgV();
		altN();
		tab();
		tab();
		int indexSys = comboBoxDataSys.getSelectedIndex();
		if(indexSys==1)keyUpDown(true);
		if(indexSys==2)keyUpDown(false);
		tab();		
		for(i=0;i<comboBoxCluster.getSelectedIndex();i++)keyUpDown(false);
		tab();
		if(checkBoxDynamic.isSelected())robot.keyPress(KeyEvent.VK_SPACE);
		// Abfrage wenn Datei existiert
		robot.delay(100);
		altF();
		if(fileexist){
			robot.delay(100);
			tab();
			space();
		}		
		// Die notwendige Wartezeit zum Formatieren, wurde am Anfang berechnet
		robot.delay(delay);
		space();
		if(checkBoxDynamic.isSelected())space();
		robot.delay(50);
		robot.keyPress(KeyEvent.VK_ESCAPE);
	}
	
	/**
	 * Löst eine Pfeil auf oder Peil ab Eingabe aus.
	 * @param up True wenn Pfeil auf und False wenn Pfeil ab gedrückt werden soll
	 */
	private void keyUpDown(boolean up){
		if(up){
			robot.keyPress(KeyEvent.VK_UP);
			robot.keyRelease(KeyEvent.VK_UP);
		}else{
			robot.keyPress(KeyEvent.VK_DOWN);
			robot.keyRelease(KeyEvent.VK_DOWN);
		}
	}
	
	/**
	 * Simuliert eine Leerzeichen Eingabe.
	 */
	private void space(){
		robot.keyPress(KeyEvent.VK_SPACE);
		robot.keyRelease(KeyEvent.VK_SPACE);
	}
	
	/**
	 * Simuliert eine Tabulator Eingabe.
	 */
	private void tab(){
		robot.keyPress(KeyEvent.VK_TAB);
		robot.keyRelease(KeyEvent.VK_TAB);
	}
	
	/**
	 * Simuliert die Tastenkombination 'Alt+F'
	 */
	private void altF(){
		robot.keyPress(KeyEvent.VK_ALT);
		robot.keyPress(KeyEvent.VK_F);
		robot.keyRelease(KeyEvent.VK_ALT);
		robot.keyRelease(KeyEvent.VK_F);
	}
	
	/**
	 * Simuliert die Tastenkombination 'Alt+N'
	 */
	private void altN(){
		robot.keyPress(KeyEvent.VK_ALT);
		robot.keyPress(KeyEvent.VK_N);
		robot.keyRelease(KeyEvent.VK_ALT);
		robot.keyRelease(KeyEvent.VK_N);
	}
	
	/**
	 * Simuliert die Tastenkombination 'Strg+V'
	 */
	private void strgV(){
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_CONTROL);
		robot.keyRelease(KeyEvent.VK_V);
	}
	
	/**
	 * Öffnet den FileInput-Dialog zur Auswahl des Containerspeicherorts.
	 */
	private void openFileDialog(){
		File file = new File("C:/");
		JFileChooser fc = new JFileChooser(new File(".*"));
		fc.setCurrentDirectory(file);
		int option = fc.showOpenDialog(dialog);
		file = fc.getSelectedFile();		
		if(option==JFileChooser.APPROVE_OPTION){
			this.file = file;
			textFieldPath.setText(file.getAbsolutePath());
		}else{
			file = null;
		}
	}
	
	/**
	 * Überprüft ob der Container erfolgreich erstellt wurde.
	 * Falls nicht wird eine Fehlermeldung ausgegeben.
	 * @return Ob der Container erstellt wurde
	 */
	private boolean checkSucces(){
		if(robotstatus){
			int contsBefore = File.listRoots().length;
			tcControl.mountContainer(file.getAbsolutePath(), getPassword());
			if(contsBefore!=File.listRoots().length){
				tcControl.dismountAll();
				return true;
			}
			else{
				showError("Fehler beim Erstellen des Containers. TrueCrypt.exe richtig gewählt?", "Fehler beim Erstellen");
				tcControl.dismountAll();
				return false;
			}	
		}else{
			return true;
		}
			
	}
	
	/**
	 * Fügt die TrueCrypt-Container Optionen dem JPanel hinzu.
	 * @param pane Das JPanel Objekt
	 */
	protected void addTCContainerComponents(Container pane){
		
		// ComboxBox Elemente
		String[] encrypt = {"AES","Serpent","Twofish","AES-Twofish","AES-Twofish-Serpent","Serpent-AES","Serpent-Twofish-AES","Twofish-Serpent"};
		String[] hash	= {"Ripemd-160","SHA-512","Whirlpool"};
		String[] dataSys= {"FAT","NTFS","None"};
		String[] cluster ={"Default","0.5KB","1KB","2KB","4KB","8KB","16KB","32KB","64KB"};
		
		// Panelelemente
		JLabel labelPfad 	= new JLabel("Pfad:");
		JLabel labelEncryp 	= new JLabel("Verschlüsselung:");
		JLabel labelHash 	= new JLabel("Hashalgorithmus:");
		JLabel labelDataSys = new JLabel("Dateisystem:");
		JLabel labelCluster = new JLabel("Clustergröße");
		JLabel labelsize	= new JLabel("Containergröße (MB):");
		checkBoxDynamic 	= new JCheckBox("Dynamische Volumengröße");
		buttonOpenFile 		= new JButton("Speicherort wählen");
		textFieldPath 		= new JTextField();
		textFieldSize		= new JTextField();
		comboBoxEncryp 		= new JComboBox<String>(encrypt);
		comboBoxHash 		= new JComboBox<String>(hash);
		comboBoxDataSys 	= new JComboBox<String>(dataSys);
		comboBoxCluster 	= new JComboBox<String>(cluster);
		buttonOpenFile.setBounds(startx+defpadx*2, starty, defwidth+defpadx, defheight);
		buttonOpenFile.addActionListener(this);	
		comboBoxEncryp.setBounds(startx+defpadx, starty+defpady, defwidth, defheight);
		comboBoxHash.setBounds(startx+defpadx, starty+defpady*2, defwidth, defheight);
		comboBoxDataSys.setBounds(startx+defpadx*3, starty+defpady, defwidth, defheight);
		comboBoxCluster.setBounds(startx+defpadx*3,starty+defpady*2,defwidth,defheight);		
		checkBoxDynamic.setBounds(startx+defpadx*2, starty+defpady*3, defwidth*2, defheight);
		textFieldPath.setBounds(startx+defpadx, starty, defwidth, defheight);
		textFieldSize.setBounds(startx+defpadx, starty+defpady*3, defwidth, defheight);
		
		labelPfad.setBounds(startx, starty, defwidth, defheight);
		labelEncryp.setBounds(startx, starty+defpady, defwidth, defheight);
		labelHash.setBounds(startx, starty+defpady*2, defwidth, defheight);
		labelsize.setBounds(startx, starty+defpady*3, defwidth, defheight);
		labelDataSys.setBounds(startx+defpadx*2, starty+defpady, defwidth, defheight);
		labelCluster.setBounds(startx+defpadx*2, starty+defpady*2, defwidth, defheight);	
		
		pane.add(labelPfad);
		pane.add(labelEncryp);
		pane.add(labelHash);
		pane.add(labelsize);
		pane.add(textFieldSize);
		pane.add(textFieldPath);
		pane.add(comboBoxEncryp);
		pane.add(comboBoxHash);
		pane.add(comboBoxDataSys);
		pane.add(comboBoxCluster);
		pane.add(buttonOpenFile);
		pane.add(labelDataSys);
		pane.add(labelCluster);
		pane.add(checkBoxDynamic);
		
	}
	
	/**
	 * Fügt die SmartCard-Container Optionen dem JPanel hinzu.
	 * @param pane Das JPanel Objekt
	 */
	protected void addSmartCardComponents(Container pane){
		// Dialogelemente erzeugen
		pwfield 			= new JPasswordField();
		textFieldPw			= new JTextField();
		textFieldContName	= new JTextField();
		checkBoxPassword	= new JCheckBox("Containerpasswort ausblenden");
		JLabel labelCont 	= new JLabel("Container Name:");
		JLabel labelPW 		= new JLabel("Passwort:");
		
		labelCont.setBounds(startx, starty, defwidth, defheight);
		labelPW.setBounds(startx,starty+defpady,defwidth,defheight);
		textFieldContName.setBounds(startx+defpadx, starty, defwidth, defheight);
		labelSmartCardContainer.setBounds(startx+defpadx*2,starty,130,25);
		textFieldPw.setBounds(startx+defpadx, starty+defpady, defwidth, defheight);
		pwfield.setBounds(startx+defpadx, starty+defpady, defwidth, defheight);
		checkBoxPassword.setBounds(startx, starty+defpady*2, defwidth+defpadx, defheight);
		button_ok.setBounds(startx, starty+defpady*3, defwidth+defpadx, defheight);		
		button_cancel.setBounds(startx, starty+120, defwidth+defpadx, defheight);	
		scrollPaneSCContainers.setBounds(startx+defpadx*2, starty+defpady, defwidth,defheight+defpady*3);
		
		textFieldPw.setVisible(false);
		pwfield.setEnabled(false);		
		checkBoxPassword.setSelected(true);			
		tableSmartCardContainer.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);		
		
		pane.add(labelCont);
		pane.add(labelPW);
		pane.add(textFieldContName);
		pane.add(pwfield);
		pane.add(checkBoxPassword);
		pane.add(textFieldPw);
		pane.add(button_ok);
		pane.add(button_cancel);
		pane.add(scrollPaneSCContainers);
		pane.add(labelSmartCardContainer);
	}
	
	/**
	 * Zeigt das erstellte Passwort an oder versteckt es, je
	 * nachdem ob showPasswordBox gesetzt ist.
	 */
	protected void setUserOption(){
		if(checkBoxPassword.isSelected()){
			pwfield.setVisible(true);
			textFieldPw.setText("");
			textFieldPw.setVisible(false);
		}else{
			pwfield.setVisible(false);
			textFieldPw.setText(getPassword());
			textFieldPw.setVisible(true);
		}
	}
	
	/**
	 * Fragt ab, ob alle notwenigen Informationen vom Benutzer eingegeben wurden.
	 * @return True wenn alle Informationen vorliegen, sonst False
	 */
	protected boolean requestConfirmation(){
		int scIndex = tableSmartCardContainer.getSelectedRow();
		// Wurde ein Speicherplatz gewählt
		if(scIndex!=-1){
			if(robotstatus){
				// Wurde ein Pfad gewählt
				if(file!=null){
					// Existierende Datei überschreiben
					if(file.exists()){
						int result = JOptionPane.showConfirmDialog(dialog, 
								"Wirklich bestehende Datei '"+file.getName()+"' in '"+file.getAbsolutePath()+"'"+
								"' überschreiben?\n\n          !!!WARNUNG!!!: Die bestehende Datei geht verloren!", 
								"Abfrage", 
								JOptionPane.YES_NO_OPTION);
						if(result!=JOptionPane.YES_OPTION){
							file = null;
							textFieldPath.setText("");
							return false;
						}else{
							fileexist=true;}
					}
					// Wurden im Feld "Containergröße" nur Zahlen eingegeben bzw. eine Größe eingegeben
					if(Pattern.matches("\\d*",textFieldSize.getText()) && !textFieldSize.getText().equals("")){
						// Enthält der gewählte Speicherplatz bereits ein Passwort
						if(smartCardContainer[scIndex]!=null){
							int result = JOptionPane.showConfirmDialog(dialog, 
									"Wirklich bestehendes Passwort vom Container '"+Terminal.trimName(smartCardContainer[scIndex].getName())+
									"' überschreiben?\n\n          !!!WARNUNG!!!: Das alte Passwort geht dabei verloren!", 
									"Abfrage", 
									JOptionPane.YES_NO_OPTION);
							if(result!=JOptionPane.YES_OPTION)return false;
						}
					}else{
						showError("Bitte nur Zahlen für die Größe des Containers eingeben", "Falsche Eingabe");
						textFieldSize.requestFocus();
						return false;
					}
					if(textFieldContName.equals("")){
						int result = JOptionPane.showConfirmDialog(dialog, 
								"Es wurde kein Containername angegeben.\nDies wird nicht empfohlen.\nTrotzdem fortfahren?", 
								"Abfrage", 
								JOptionPane.YES_NO_OPTION);
						if(result!=JOptionPane.YES_OPTION)return false;
					}
					return true;
				}else{
					showError("Es wurde kein Pfad für den zu erstellenden Container gewählt!", "Keine Datei/Pfad gewählt");
					return false;
				}
			}else{
				if(smartCardContainer[scIndex]!=null){
					int result = JOptionPane.showConfirmDialog(dialog, 
							"Wirklich bestehendes Passwort vom Container '"+Terminal.trimName(smartCardContainer[scIndex].getName())+
							"' überschreiben?\n\n          !!!WARNUNG!!!: Das alte Passwort geht dabei verloren!", 
							"Abfrage", 
							JOptionPane.YES_NO_OPTION);
					if(result==JOptionPane.YES_OPTION)return true;
					else return false;
				}
				return true;
			}			
		}else{
			showError("Es wurde kein Speicherplatz auf der SmartCard ausgewählt!", "Kein Speicherplatz gewählt");
		}
		return false;
	}
	
	/**
	 * Liefert das erzeugte Passwort als String.
	 * @return Generierte Passwort
	 */
	protected String getPassword(){
		return String.valueOf(pwfield.getPassword());
	}
	
	/**
	 * Erzeugt 64 Zufallszahlen und nimmt diese als Index für ein Array,
	 * welches alle Groß- und Kleinbuchstaben enthält, sowie die 
	 * Zahlen 0-9.
	 * Schreibt anschließend das erstellte Passwort in das JPasswordField.
	 */
	protected void generatePassword(){
		char[] temp = new char[64];
		for (int i=0;i<temp.length;i++){
			temp[i] = alphanumerics[random.nextInt(alphanumerics.length)];
		}
		pwfield.setText(new String(temp));
		byte[] bytetest = getPassword().getBytes();
		String ausgabe = "";
		for(int i=0;i<bytetest.length;i++){
			ausgabe = ausgabe + (char) bytetest[i];
		}
	}
	
	/**
	 * Löscht die eingegebenen Daten.
	 */
	protected void clearFields(){
		textFieldContName.setText("");
		pwfield.setText("");
		textFieldPath.setText("");
	}
	
	/**
	 * Überschreibung der geerbten actionPerformed Methode um
	 * den notwendigen FileInput-Dialog zu öffnen und das 
	 * generierte Passwort anzuzeigen.
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == button_ok){
			dialogConfirmed();
		}
		if(e.getSource() == button_cancel){
			showDialog(false);
		}
		if(e.getSource() == checkBoxPassword){
			setUserOption();
		}
		if(e.getSource() == buttonOpenFile){
			openFileDialog();
		}			
	}
}