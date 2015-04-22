package terminal.dialogs;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import terminal.JavaCard;
import terminal.data.TerminalData;
/**
 * Wurzel alle Dialoge. Jeder Dialog enth�lt zwei JButtons und einen zugeh�rigen 
 * ActionListener. Dieser verweist auf die Standard-Methoden showDialog() und dialogConfirmed().
 * Diese beiden, sowie die Methode showSuccess() m�ssen von erbenden Klassen �berschrieben werden.
 * 
 * Zus�tzlich enth�lt die Klasse eine Methode zur Ausgabe einer Fehlermeldung und einen selbst geschriebenen 
 * LayoutManager. Dieser berechnet anhand der �bergebenen Parameter die Position des Elements und weist
 * selbstst�ndig einen Abstand zwischen den Element zu.
 * 
 * Die Standradwerte k�nnen �ber die Methode renewSize() ge�ndert werden.
 * @author Dennis Jandt
 *
 */
public abstract class Dialog_Basic implements ActionListener{
	
	// GUI-Elemente
	protected JDialog dialog;
	protected JButton button_ok;
	protected JButton button_cancel;
	protected JFrame mainwindow;
	
	// Referenzobjekte zu den Fachklassen
	protected JavaCard card;
	protected TerminalData data;

	
	// Positionierungsvariablen
	protected int startx,starty,defpadx,defpady,defwidth,defheight;
	
	/**
	 * Oberster Konstruktor. Dieser setzt die n�tigen Referenzobjekte, sowie den Titel.
	 * Die Parameter x und y stehen f�r die zu erzeugende Fenstergr��e, wobei x f�r die Anzahl der
	 * Elemente in der horizontalen und y f�r die Elemente in der vertikalen stehen.
	 * @param mainwindow Das Elternobjekt des Dialogs
	 * @param card Referenz auf das JavaCard-Objekt
	 * @param data Referenz zur Speicherklasse
	 * @param title Titel des Dialogfensters
	 * @param x Anzahl der Elemente in der horizontalen
	 * @param y Anzahl der Elemente in der vertikalen.
	 */
	public Dialog_Basic(JFrame mainwindow,JavaCard card,TerminalData data,String title, int x, int y){
		this.card= card;
		this.data = data;
		this.mainwindow = mainwindow;
		
		// Standardwerte f�r die Positionsvariablen
		mainwindow.setLayout(new GridLayout(x,y,5,5));
		startx = 10;
		starty = 15;
		defwidth=130;
		defheight=25;
		defpady=defheight+5;
		defpadx=defwidth+5;
		
		dialog			= new JDialog(mainwindow, title, true);
		button_ok 		= new JButton("OK");
		button_cancel 	= new JButton("Schlie�en");
		dialog.setLayout(null);
		dialog.setResizable(false);
		button_ok.addActionListener(this);
		button_cancel.addActionListener(this);		
		dialog.add(button_ok);
		dialog.add(button_cancel);
		setSize(x, y);
	}

	/**
	 * Legt die Abl�ufe zum erscheinen und schlie�en des Dialogs fest.
	 * @param auswahl Ob der Dialog angezeigt werden soll
	 */
	public abstract void showDialog(boolean auswahl);
	
	/**
	 * Legt die Meldung bei einer erfolgreichen Ausf�hrung des Dialogs fest.
	 * Sollte zudem showDialog(false) aufrufen.
	 */
	protected abstract void showSuccess();
	
	/**
	 * Wird nach best�tigung von button_ok aufgerufen. Diese
	 * soll die Aktionen nach einer Benutzerbest�tigung des Dialogs
	 * implementieren.
	 */
	protected abstract void dialogConfirmed();
	
	/**
	 * Gibt die in den Parametern �bergebene Fehlermeldung aus.
	 * @param meldung Nachricht der Fehlermeldung
	 * @param fensterTitel Titel des Fehlerfensters
	 */
	protected void showError(String meldung, String fensterTitel) {
		JOptionPane.showMessageDialog(dialog, meldung, fensterTitel, JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Erneuert die f�r die Elemente verwendeten Standardgr��en
	 * @param width Standardbreite eines Objekts
	 * @param height Standardh�he eines Objekts
	 * @param pad x und y Abstand zu anderen Objekten
	 */
	protected void renewSize(int width, int height,int pad){
		defwidth=width;
		defheight=height;
		defpady=defheight+pad;
		defpadx=defwidth+pad;
	}
	
	/**
	 * �ndert die Gr��e des angezeigten Dialogs auf die x * y Anzahl an Elementen.
	 * Generell ist der Aufbau hiermit Tabellenf�rmig.
	 * @param cols Anzahl der Spalten
	 * @param rows Anzahl der Reihen
	 */
	protected void setSize(int cols,int rows){
		dialog.setSize(startx*2+defpadx*cols, starty*2+startx*2+defpady*rows);
	}
	
	/**
	 * F�gt dem Standarddialog ein Element an der gew�hlten Stelle und der gew�nschten Ausdehnung hinzu.
	 * @param comp Die zu platzierende Komponente
	 * @param posx Die X-Position des Elements, ab 0
	 * @param posy Die Y-Position des Elements, ab 0
	 * @param xspan Die Anzahl an horizontalen Spalten �ber die sich das Objekt erstreckt
	 * @param yspan Die Anzahl an vertikalen Spalten �ber die sich das Objekt erstreckt
	 */
	protected void addComponent(Component comp,int posx,int posy,int xspan,int yspan){
		comp.setBounds(startx+defpadx*posx, starty+defpady*posy, defwidth+defpadx*(xspan-1), defheight+defpady*(yspan-1));
		dialog.add(comp);
	}
	
	/**
	 * F�gt dem �bergebenen Container Element ein Bedienelement an der gew�hlten Stelle hinzu.
	 * @param comp Die zu platzierende Komponente
	 * @param posx Die X-Position des Elements, ab 0
	 * @param posy Die Y-Position des Elements, ab 0
	 * @param xspan Die Anzahl an horizontalen Spalten �ber die sich das Objekt erstreckt
	 * @param yspan Die Anzahl an vertikalen Spalten �ber die sich das Objekt erstreckt
	 * @param cont Der Container, welcher das Objekt aufnehmen soll
	 */
	protected void addComponent(Component comp,int posx,int posy,int xspan, int yspan, Container cont){
		comp.setBounds(startx+defpadx*posx, starty+defpady*posy, defwidth+defpadx*xspan, defheight+defpady*yspan);
		cont.add(comp);
	}
	
	/**
	 * Standardimplementation des ActionListeners. 
	 * Besitzt eine abgeleitete Klasse mehr als zwei Buttons, m�ssen alle 
	 * ActionEvents neu gesetzt werden.
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == button_ok){
			dialogConfirmed();
		}
		if(e.getSource() == button_cancel){
			showDialog(false);
		}
	}
	
}
