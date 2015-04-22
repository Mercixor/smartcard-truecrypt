package terminal.dialogs;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import terminal.JavaCard;
import terminal.TrueCryptControl;
import terminal.data.PublicKeyObject;
import terminal.data.TerminalData;
/**
 * Erweitert die abstrakte Klasse Dialog_Basic_SmartCard um 
 * eine Tabelle zur Anzeige der gespeicherten PublicKeys.
 * @author Dennis Jandt
 * @see Dialog_Basic_SmartCard
 */
public abstract class Dialog_Basic_SmartCard_PublicKey extends Dialog_Basic_SmartCard{

	protected JTable 		tablePublicKeys;
	protected JScrollPane 	scrollPanePublicKeys;
	private String[] 		columnNamesPublicKeys 	= {"Vorname","Nachname","Modulus","Exponent"};
	protected Object[][]	publicKeyData;
	protected JLabel 		labelPublicKey;
	
	/**
	 * Fügt dem Dialog eine Tabelle zur Anzeige der gespeicherten PublicKeys hinzu und
	 * Operation zur Befüllung dieser mit Daten.
	 * @param mainwindow Das Elternobjekt des Dialogs
	 * @param card Referenz auf das JavaCard-Objekt
	 * @param data Referenz zur Speicherklasse
	 * @param tcCont Referenz auf das TrueCryptControl Objekt
	 * @param title Titel des Dialogfensters
	 * @param x Anzahl der Elemente in der horizontalen
	 * @param y Anzahl der Elemente in der vertikalen.
	 */
	public Dialog_Basic_SmartCard_PublicKey(JFrame mainwindow,
			JavaCard card, TerminalData data, TrueCryptControl tcCont,String title, int x, int y) {
		super(mainwindow, card, data, tcCont,title, x, y);
		labelPublicKey			= new JLabel("Lokal gespeicherte Benutzer-PublicKeys");
		tablePublicKeys 		= new JTable();
		scrollPanePublicKeys	= new JScrollPane(tablePublicKeys);		
	}
	
	/**
	 * Befüllt die Tabelle mit den gespeicherten Daten. Dazu wird
	 * die innere Klasse DataModelPublicKeys benötigt.
	 */
	protected void fillPublicKeyArray() {
		int anzahl = data.getPublicCount();
		publicKeyData = new Object[anzahl][columnNamesPublicKeys.length];
		PublicKeyObject[] pubkeyList = data.getSavedPublicKeys();
		for (int i=0;i<anzahl;i++){
			publicKeyData[i][0] = pubkeyList[i].getPreName();
			publicKeyData[i][1] = pubkeyList[i].getName();
			publicKeyData[i][2] = pubkeyList[i].getModulus();
			publicKeyData[i][3] = pubkeyList[i].getExponent();
		}
		tablePublicKeys.setModel(new DataModelPublicKeys());
	}
	
	/**
	 * Inner Klasse, welche das AbstractTableModel erweitert. Diese ist
	 * zur Nutzung der JTable notwendig.
	 * @author Dennis Jandt
	 *
	 */
	public class DataModelPublicKeys extends AbstractTableModel{

		private static final long serialVersionUID = 1L;
		@Override
		public String getColumnName(int col) {
	        return columnNamesPublicKeys[col].toString();
	    }		
		@Override
		public int getColumnCount() {
			return columnNamesPublicKeys.length;
		}
		@Override
		public int getRowCount() {
			return publicKeyData.length;
		}
		@Override
		public Object getValueAt(int row, int col) {
			return publicKeyData[row][col];
		}		
		@Override
		public boolean isCellEditable(int row, int col){
			return false; 
        }		
	}

}
