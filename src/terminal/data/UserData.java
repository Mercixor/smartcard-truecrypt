package terminal.data;

import java.io.Serializable;
/**
 * Speichert die spezifischen Benutzerdaten ab, damit die von ihm gewählten Zuordnungen 
 * nicht wiederholt ausgewählt werden müssen.
 * @author Dennis Jandt
 */
public class UserData implements Serializable{

	
	private static final long serialVersionUID = 1L;
	private TC_Container[] userContainer;
	private UserData_Preferences preferences;
	private String name,preName;
	
	/**
	 * Erstellt einen neuen Benutzer mit dem übergebenen Namen und maximalen Index der gespeicherten Daten.
	 * @param name Nachname des Benutzers
	 * @param preName Vorname des Benutzers
	 * @param maxIndex Der maximale Index der zu speichernden Daten
	 */
	public UserData(String name, String preName, int maxIndex){
		this.name = name;
		this.preName = preName;
		preferences = new UserData_Preferences();
		userContainer = new TC_Container[maxIndex];
	}
	
	/**
	 * Liefert die von diesem Benutzer verbundenen Container zurück.
	 * @return TC_Container-Array der gespeicherten Verbindungen.
	 */
	public TC_Container[] getUserData(){
		return userContainer;
	}
	
	/**
	 * Liefert den gespeicherten Nachnamen. 
	 * @return Gespeicherter Nachname
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Liefert den gespeicherten Vornamen.
	 * @return Gespeicherter Vorname
	 */
	public String getPreName(){
		return preName;
	}
	
	/**
	 * Speichert die vom Benutzer getätigten Eingaben ab.
	 * @param data Das zu speichernden Container-Array
	 */
	public void setUserData(TC_Container[] data){
		userContainer = data;
	}
	
	/**
	 * Liefert die unter dem Benutzer gespeicherten Einstellungen zurück.
	 * @return Referenz auf das Benutzereinstellungsobjekt
	 */
	public UserData_Preferences getUserPreferences(){
		return preferences;
	}
}
