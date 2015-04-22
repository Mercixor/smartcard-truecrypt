package terminal.dialogs;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import terminal.JavaCard;
import terminal.TrueCryptControl;
import terminal.data.TC_Container;
import terminal.data.TerminalData;
/**
 * Abstrakte Klasse die sich von der ebenfalls abstrakten Klasse Dialog_Basic ableitet. 
 * Sie ergänzt den Standarddialog um eine Tabelle, in der die SmartCard-Container 
 * angezeigt werden, sowie Operationen zum Füllen der Tabelle.
 * Zusätzlich wird hier die häufig benötigt Referenz zum TrueCryptControl Objekt gesetzt.
 * @author Dennis Jandt
 * @see Dialog_Basic
 */
public abstract class Dialog_Basic_SmartCard extends Dialog_Basic{

	protected JTable tableSmartCardContainer;
	protected JScrollPane scrollPaneSCContainers;
	protected TC_Container[] smartCardContainer;
	protected TrueCryptControl tcControl;
	
	protected Object[][] 	containerTableData;
	protected int maxIndex;
	protected JLabel labelSmartCardContainer;
	private String[] columnsSmartCard = {"Nr.","Bezeichner"};
	
	/**
	 * Erzeugt die nötigen Objekte und legt die Referenz zum TrueCryptControl-Objekt an.
	 * @param mainwindow Das Elternobjekt des Dialogs
	 * @param card Referenz auf das JavaCard-Objekt
	 * @param data Referenz zur Speicherklasse
	 * @param tcCont Referenz auf das TrueCryptControl Objekt
	 * @param title Titel des Dialogfensters
	 * @param x Anzahl der Elemente in der horizontalen
	 * @param y Anzahl der Elemente in der vertikalen.
	 */
	public Dialog_Basic_SmartCard(JFrame mainwindow, JavaCard card,
			TerminalData data,TrueCryptControl tcCont, String title, int x, int y) {
		super(mainwindow, card, data, title, x, y);
		this.tcControl=tcCont;
		tableSmartCardContainer 	= new JTable();
		scrollPaneSCContainers 		= new JScrollPane(tableSmartCardContainer);
		labelSmartCardContainer		= new JLabel("SmartCard Container");
		
		tableSmartCardContainer.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);	
	}
	
	/**
	 * Füllt die Tabelle mit den empfangenen Containernamen. Dazu ist die inner Klasse 
	 * DataModelSmartCardContainer notwendig.
	 */
	protected void fillTableSmartCardContainer() {
		smartCardContainer = data.getSavedSmartCardContainer();
		containerTableData = new Object[maxIndex][columnsSmartCard.length];
		for (int i=0;i<smartCardContainer.length;i++){
			containerTableData[i][0] = i+1;
			if(smartCardContainer[i]!=null)containerTableData[i][1] = smartCardContainer[i].getName();
			else containerTableData[i][1] = "Frei";
		}
		tableSmartCardContainer.setModel(new DataModelSmartCardContainer());
		tableSmartCardContainer.getColumnModel().getColumn(0).setMinWidth(5);
		tableSmartCardContainer.getColumnModel().getColumn(0).setPreferredWidth(5);
		tableSmartCardContainer.getColumnModel().getColumn(0).setWidth(5);
	}
	
	/**
	 * Benötigt um den maximalen Index für die Tabelle zu erhalten.
	 * @param maxIndex Der maximal anzuzeigende Index
	 */
	public void setMaxIndex(int maxIndex){
		this.maxIndex = maxIndex;
	}
	
	/**
	 * Innere Klasse, welche das AbstractTableModel erweitert. Diese ist
	 * zur Nutzung der JTable notwendig.
	 * @author Dennis Jandt
	 *
	 */
	protected class DataModelSmartCardContainer extends AbstractTableModel{

		private static final long serialVersionUID = 1L;
		@Override
		public String getColumnName(int col) {			
	        return columnsSmartCard[col];
	    }		
		@Override
		public int getColumnCount() {
			return columnsSmartCard.length;
		}
		@Override
		public int getRowCount() {
			return maxIndex;
		}
		@Override
		public Object getValueAt(int row, int col) {
			return containerTableData[row][col];
		}		
		@Override
		public boolean isCellEditable(int row, int col){
			return false; 
        }		
	}
}
