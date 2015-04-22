package terminal.dialogs;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import terminal.JavaCard;
import terminal.TrueCryptControl;
import terminal.data.TC_Container;
import terminal.data.TerminalData;
/**
 * Dialog zum Laden der gespeicherten TrueCrypt-Container mit
 * den Passwörter auf der SmartCard.
 * Wird von Dialog_Basic_SmartCard abgeleitet.
 * @author Dennis Jandt
 * @see Dialog_Basic_SmartCard
 */
public class Dialog_MountContainers extends Dialog_Basic_SmartCard{
	protected JTable 		tableLocalContainer;
	public JButton 			openContainer, connectLocal, deleteItem;
	protected JScrollPane 	scrollPaneLocal;
	protected String[] 		columnNamesLocalContainer 		= {"Nr.","Dateiname","Dateipfad","Card-Nr.","Card-Bez."};
	protected Object[][] 	connectorsTableData;
	private File[] openedData;
	
	private TC_Container[] localContainer;
	
	/**Ruft den Oberklassen Konstruktor auf und 
	 * initGUI().
	 * @param mainwindow Das Elternobjekt des Dialogs
	 * @param card Referenz auf das JavaCard-Objekt
	 * @param data Referenz zur Speicherklasse
	 * @param tcCont Referenz auf das TrueCryptControl Objekt
	 */
	public Dialog_MountContainers(JFrame mainwindow, JavaCard card, TerminalData data,TrueCryptControl tcCont) {
		super(mainwindow, card, data, tcCont,"TrueCrypt Container mounten", 4, 8);
		initGUI();
	}
	
	/**
	 * Erzeugt die notwendigen Bedienelemente und platziert diese im Dialog.
	 */
	private void initGUI(){
		// GUI-Elemente erzeugen
		openContainer			= new JButton("Container auswählen");
		connectLocal			= new JButton("Datei und Passwort verbinden");
		deleteItem				= new JButton("Verbindung löschen");		
		tableLocalContainer 	= new JTable();
		scrollPaneLocal			= new JScrollPane(tableLocalContainer);
		// Ändern der Standardgröße
		renewSize(160, 25, 5);
		// Ändern der Fenstergröße
		setSize(4, 8);
		// Ändern der Fensterposition
		dialog.setLocation(100, 100);
		// GUI-Elemente platzieren
		addComponent(new JLabel("Lokal gespeicherte Verbindungen zwischen Container und Karte"),0,0,3,1);
		addComponent(labelSmartCardContainer, 3, 0, 1, 1);
		tableLocalContainer.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		addComponent(scrollPaneLocal, 0, 1, 3, 5);
		addComponent(scrollPaneSCContainers, 3, 1, 1, 7);
		addComponent(openContainer, 0, 6, 1, 1);		
		addComponent(connectLocal, 1, 6, 2, 1);
		addComponent(deleteItem, 0, 7, 1, 1);	
		addComponent(button_cancel, 1, 7, 1, 1);
		addComponent(button_ok, 2, 7, 1, 1);			
		// ActionListener hinzufügen
		openContainer.addActionListener(this);
		connectLocal.addActionListener(this);	
		deleteItem.addActionListener(this);		
	}
	
	/**
	 * Zeigt je nach Auswahl den Dialog an. Wird der Dialog angezeigt, wird die
	 * Oberklassenmethode fillTableSmartCardContainer() aufgerufen, sowie 
	 * fillLocalSavedContainerArray().
	 */
	public void showDialog(boolean auswahl){
		if(auswahl){			
			fillTableSmartCardContainer();						
			fillLocalSavedContainerArray();
			openedData = new File[maxIndex];
		}
		dialog.setVisible(auswahl);
	}	

	/**
	 * Zeigt eine Erfolgsmeldung an und Beendet den Dialog.
	 */
	public void showSuccess(){
		JOptionPane.showMessageDialog(dialog,						
                "Container erfolgreich gemountet",
                "Erfolg",
                JOptionPane.INFORMATION_MESSAGE);
		showDialog(false);
	}
	
	protected void dialogConfirmed() {
		String failures = "";
		int deviceCount = 0, failCount=0,tableCount=0;
		for(int i=0;i<localContainer.length;i++){
			deviceCount = File.listRoots().length;
			if(localContainer[i] != null){
				tableCount++;
				tcControl.mountContainer(localContainer[i].getFilePath(), card.getSelectedPassword(localContainer[i].getConnectedIndex()));
				if(deviceCount==File.listRoots().length){
					failures = failures +"Fehler beim Laden von: "+localContainer[i].getfileName()+"\n";
					failCount++;
				}
			}
		}
		if(failures.equals(""))showSuccess();
		else{
			if(failCount==tableCount){
				showError("Fehler beim Laden sämtlicher Container. TrueCrypt.exe richtig gewählt?", "Fehler");
			}else{
				JOptionPane.showMessageDialog(dialog, failures+"Fehlerursache bitte der TrueCrypt Meldung entnehmen", "Fehler beim Laden", JOptionPane.OK_OPTION);
			}
					
		}
	}
	
	/**
	 * Befüllen der Tabelle für die lokal gespeicherten
	 * Verbinduingen von SmartCard-Container und 
	 * TrueCrypt-Containerdateien.
	 */
	private void fillLocalSavedContainerArray() {
		localContainer = data.getSavedUserContainer();
		connectorsTableData = new Object[maxIndex][columnNamesLocalContainer.length];
		for (int i=0;i<maxIndex;i++){
			connectorsTableData[i][0] = i+1;
			if(localContainer[i] != null)
				if(localContainer[i].getConnectedIndex() <= maxIndex){				
					connectorsTableData[i][1] = localContainer[i].getfileName();
					connectorsTableData[i][2] = localContainer[i].getFilePath();
					connectorsTableData[i][3] = localContainer[i].getConnectedIndex()+1;
					connectorsTableData[i][4] = localContainer[i].getName();
				}			
		}
		setLocalContainerModel();
	}
	
	/**
	 * AKtualisiert die Daten in den Tabellen.
	 */
	private void setLocalContainerModel(){
		tableLocalContainer.setModel(new DataModelLocalContainers());
		tableLocalContainer.getColumnModel().getColumn(0).setMinWidth(3);
		tableLocalContainer.getColumnModel().getColumn(0).setPreferredWidth(3);
		tableLocalContainer.getColumnModel().getColumn(0).setWidth(3);
		tableLocalContainer.getColumnModel().getColumn(3).setMinWidth(3);
		tableLocalContainer.getColumnModel().getColumn(3).setPreferredWidth(3);
		tableLocalContainer.getColumnModel().getColumn(3).setWidth(3);
	}
	
	/**
	 * Verbindet ein Element der lokalen Liste mit einem 
	 * Passwort-Index von der SmartCard Tabelle.
	 */
	public void connectContainerAndPassword(){
		int selectedContainer = tableSmartCardContainer.getSelectedRow();
		int selectedFile	= tableLocalContainer.getSelectedRow();
		if(selectedFile == -1 || selectedContainer== -1){
			JOptionPane.showMessageDialog(dialog, "Keine Element der Tabelle zum speichern gewählt!", "Kein Element gewählt", JOptionPane.OK_OPTION);
			return;
		}
		File file = openedData[selectedFile];
		if (file == null){
			if(localContainer[selectedFile] == null)return;
			file = new File(localContainer[selectedFile].getFilePath());
		}
		if(smartCardContainer[selectedContainer]==null){
			JOptionPane.showMessageDialog(dialog, "Bitte ein Containerelement mit Passwort auswählen", "Leerer Container", JOptionPane.OK_OPTION);
			return;
		}
		localContainer[selectedFile] = new TC_Container(smartCardContainer[selectedContainer].getName());
		localContainer[selectedFile].setFileName(file.getName());
		localContainer[selectedFile].setFilePath(file.getPath());
		localContainer[selectedFile].setRefferedIndex(selectedContainer);
		renewData();
	}
	
	/**
	 * Füllt die Daten in der Tabelle neu auf.
	 */
	public void renewData(){
		fillTableSmartCardContainer();
		fillLocalSavedContainerArray();
	}
	
	/**
	 * Löschen einer gespeicherten Verbindung.
	 */
	public void deleteDataRow(){
		data.deleteLocalContainer(tableLocalContainer.getSelectedRow());
		renewData();
	}
	
	/**
	 * Öffnet den FileInput-Dialog zur Auswahl des zu ladenenen
	 * TrueCrypt-Containers.
	 */
	public void openFileDialog(){
		File file = new File("C:/");
		JFileChooser fc = new JFileChooser(new File(".*"));
		fc.setCurrentDirectory(file);
		int option = fc.showOpenDialog(dialog);
		file = fc.getSelectedFile();		
		if(option==JFileChooser.APPROVE_OPTION){
			int dateiZeile = tableLocalContainer.getSelectedRow();
			connectorsTableData[dateiZeile][1]=file.getName();
			connectorsTableData[dateiZeile][2]=file.getPath();
			openedData[dateiZeile] = (File) file;
			setLocalContainerModel();
			tableLocalContainer.setRowSelectionInterval(dateiZeile, dateiZeile);
		}else{
			file = null;
		}
	}
	
	/**
	 * Überschreibung der geerbten actionPerformed Methode um
	 * den notwendigen FileInput-Dialog zu öffnen. Zusätzlich 
	 * werden die Buttons zum Löschen und Setzen einer Verbindung
	 * belegt.
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == deleteItem){
			if(tableLocalContainer.getSelectedRow()!=-1)deleteDataRow();
		}
		if(e.getSource() == connectLocal){
			connectContainerAndPassword();
		}
		if(e.getSource() == openContainer){
			if(tableLocalContainer.getSelectedRow()!=-1){
				openFileDialog();
			}else{
				showError("Keine Element aus der lokalen Liste zum Speichern gewählt!", "Kein Element gewählt");
			}			
		}
		if(e.getSource() == button_ok){
			dialogConfirmed();
		}
		if(e.getSource() == button_cancel){
			showDialog(false);
		}
	}


	/**
	 * Innere Klasse, welche die Datenstruktur für die JTable
	 * bereitstellt.
	 * @author Dennis Jandt
	 *
	 */
	public class DataModelLocalContainers extends AbstractTableModel{

		private static final long serialVersionUID = 1L;
		@Override
		public String getColumnName(int col) {
	        return columnNamesLocalContainer[col].toString();
	    }		
		@Override
		public int getColumnCount() {
			return columnNamesLocalContainer.length;
		}
		@Override
		public int getRowCount() {
			return connectorsTableData.length;
		}
		@Override
		public Object getValueAt(int row, int col) {
			return connectorsTableData[row][col];
		}		
		@Override
		public boolean isCellEditable(int row, int col){
			return false; 
        }		
	}	
}
