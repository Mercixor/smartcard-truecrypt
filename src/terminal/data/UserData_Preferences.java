package terminal.data;

import java.io.Serializable;
/**
 * Klasse zur Speicherung der Benutzereinstellungen.
 * Sie enthält den Pfad zu TrueCrypt, Verzögerungszeit von Robots, sowie
 * ob diese überhaupt aktiv sein soll.
 * @author Dennis Jandt
 */
public class UserData_Preferences implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String trueCryptPath;
	private int robotDelay;
	private boolean robotsActiv, securityOptionActiv;
	
	/**
	 * Erstellt ein Standard-Einstellungsobjekt.
	 */
	public UserData_Preferences(){
		robotDelay=30;
		robotsActiv=true;
		securityOptionActiv=true;
		trueCryptPath="";
	}
	
	/**
	 * Setzt den im Argument übergebenen Delaywert fest.
	 * @param delay Der einzustellende Delay für die Robot-Klasse
	 */
	public void setRobotDelay(int delay){
		robotDelay=delay;
	}
	
	/**
	 * Setzt die Aktiverung der Robot-Klasse fest.
	 * @param bool True wenn Robot genutzt werden soll, ansonsten false
	 */
	public void setRobotStatus(boolean bool){
		robotsActiv=bool;
	}
	
	/**
	 * Legt die Sicherheitsoption fest.
	 * @param bool Ob die Sicherheitsoption aktiviert sein soll
	 */
	public void setSecurityOption(boolean bool){
		securityOptionActiv=bool;
	}
	
	/**
	 * Setzt den übergebenen TrueCrypt-Pfad.
	 * @param path Absoluter Pfad zur TrueCrypt.exe
	 */
	public void setTrueCryptPath(String path){
		trueCryptPath=path;
	}
	
	/**
	 * Liefert den absoluten Pfad zur TrueCrypt.exe zurück.
	 * @return Absoluter Pfad zur TrueCrypt.exe
	 */
	public String getTrueCryptPath(){
		return trueCryptPath;
	}
	
	/**
	 * Liefert den gesetzten Verzögerungswert für die Klasse Robot.
	 * @return Gesetzte Verzögerung in ms.
	 */
	public int getRobotDelay(){
		return robotDelay;
	}
	
	/**
	 * Liefert den gesetzten Robot-Status.
	 * @return Ob die Klasse Robot genutzt werden soll
	 */
	public boolean isRobotSet(){
		return robotsActiv;
	}
	
	/**
	 * Liefert den gesetzten Sicherheitsstatus zurpck.
	 * @return Ob die Sicherheitsoption aktiviert ist.
	 */
	public boolean isSecuritySet(){
		return securityOptionActiv;		
	}	
}
