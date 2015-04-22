package terminal.data;

import java.io.Serializable;
import java.math.BigInteger;
import terminal.Terminal;
/**
 * Verwaltet die Daten und stellt Methoden zum Auslesen dieser zur Verfügung.
 * @author Dennis Jandt
 */
public class TerminalData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private PublicKeyObject[] publicKeyList;
	private TC_Container[] smartCardContainer, localSavedContainer;
	private UserData[] userData;
	public String trueCryptPath;
	private int currentUser, maxIndex;
	
	/**
	 * Erzeugt das Objekt und initialisiert die entsprechenden Objekte.
	 */
	public TerminalData(){
		trueCryptPath = "";
		publicKeyList = new PublicKeyObject[0];
		smartCardContainer = new TC_Container[0];
		localSavedContainer = new TC_Container[maxIndex];
		userData = new UserData[0];
		currentUser = -1;
	}
	
	
	/**
	 * Schreibt einen neuen PublicKey in das Datenobjekt, wenn dieser noch nicht vorhanden ist.
	 * @param name Nachname des Besitzers 
	 * @param prename Vorname des Besitzers
	 * @param modulus Der Modulus als Byte-Array
	 * @param exponent Der Exponent als Byte-Array
	 */	 	
 	public void newPublicKey(String name, String prename, byte[] modulus, byte[] exponent){
		if(checkDuplicate(modulus)){
			int count = getPublicCount();
			PublicKeyObject[] temp_array = new PublicKeyObject[count+1];
			for (int i=0;i<count;i++){
				temp_array[i]=publicKeyList[i];
			}
			temp_array[count] = new PublicKeyObject(prename,name, modulus,exponent);
			publicKeyList = new PublicKeyObject[count+1];
			publicKeyList = temp_array;
		}
	}
	
 	/**
 	 * Löscht den im Parameter gewählten PublicKey.
 	 * @param index Der zu löschende PublicKey
 	 */
	public void deletePublicKey(int index){
		int count = getPublicCount();
		int helpPointer = 0;
		PublicKeyObject[] temp_array = new PublicKeyObject[count-1];
		for(int i=0;i<count;i++){
			if(i==index)continue;
			temp_array[helpPointer] = publicKeyList[i];
			helpPointer++;
		}
		publicKeyList = temp_array;
	}
	
	/**
	 * Löscht die lokal gespeicherte Verbindung zwischen Container und SmartCard-Passwort
	 * @param index Der zu löschende Index
	 */
	public void deleteLocalContainer(int index){
		localSavedContainer[index]=null;
	}
	
	/**
	 * Überprüft ob der PublicKey bereits in den Daten gespeichert ist.
	 * @param publicModulusToCheck Der Modulus der überprüft werden soll
	 * @return Ob der Modulus bereits vorhanden ist
	 */
	private boolean checkDuplicate(byte[] publicModulusToCheck){
		BigInteger modulus = new BigInteger(Terminal.bytesToHex(publicModulusToCheck),16);
		for(int i=0;i<getPublicCount();i++){			
			if(publicKeyList[i].getModulus().equals(modulus)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Speichert die übergebenen Benutzerverbindungen.
	 * @param cont Die zu speicherenden Verbindungen.
	 */
	public void saveUserContainerData(TC_Container[] cont){
		userData[currentUser].setUserData(cont);
	}
	
	/**
	 * Speichert die Benutzerdaten.
	 * @param user Der zu speichernde Benutzer
	 */
	public void saveUserData(UserData user){
		userData[currentUser]=user;
	}
	
	/**
	 * Liefert die aktuelle geladenen Benutzerdaten zurück.
	 * @return Der aktuell angemeldete Benutzer
	 */
	public UserData getUserData(){
		return userData[currentUser];
	}
	
	/**
	 * 	Liefert alle gespeicherten PublicKeys zurück.		
	 * 
	 * @return	Existiert das Array, wird ein Array vom Typ User_object zurückgeliefert, ansonsten null.
	 */
	public PublicKeyObject[] getSavedPublicKeys(){
		try{
			return publicKeyList;
		} catch (Exception e){
			return null;
		}
	}
	
	/**
	 * Die nach dem Anmelden abgerufenen Containernamen.
	 * @return TC_Container-Objekte, welche die auf der SmartCard abgespeicherten Namen der Container enthalten.
	 */
	public TC_Container[] getSavedSmartCardContainer(){
		return smartCardContainer;
	}
	
	/**
	 * Liefert eine Liste der lokal gespeicherten BenutzerContainer zurück.
	 * @return Liste der lokal gespeicherten BenutzerContainer
	 */
	public TC_Container[] getSavedUserContainer(){
		return localSavedContainer;
	}
	
	/**
	 * 	Liefert einen speziellen PublicKey zurück.
	 * 
	 * @param selectedUser	Index des zu ladenen PublicKeys
	 * @return Existiert die Benutzer-ID wird das entsprechende Objekt zurückgeliefert, ansonsten null.
	 */
	public PublicKeyObject getSpecificUser(int selectedUser){
		try{
			return publicKeyList[selectedUser];
		}catch (Exception e){
			return null;
		}
	}
	
	/**
	 * Liefert die Anzahl der gespeicherten PublicKeys.
	 * @return Anzahl der gespeicherten PublicKeys
	 */
	public int getPublicCount(){
		return publicKeyList.length;
	}
	
	/**
	 * Speichert die nach dem Anmelden empfangenen SmartCard-Containernamen ab.
	 * @param cont Die empfangenen SmartCard-Containernamen
	 */
	public void saveSmartCardContainer(TC_Container[] cont){
		smartCardContainer = cont;
	}
	
	/**
	 * Setzt den Maximal zulässige Array Größe.
	 * Dieser wird zur Generierung der maximalen Arraygröße der in den Benutzerdaten gespeicherten Container benutzt. 
	 * @param index Der größte zulässige Index.
	 */
	public void setMaxIndex(int index){
		maxIndex = index;
	}
	
	/**
	 * Prüft die übergebenen Benutzerdaten und legt, 
	 * falls kein existierender Benutzer gefunden wurde, einen neuen Benutzer an.
	 * @param user Der Name des Benutzers, welcher sich angemeldet hat.
	 */
	public void loadUserData(UserData user){
		int userCount = userData.length;
		for(int i=0;i<userCount;i++){
			if(user.getName().equals(userData[i].getName()) && user.getPreName().equals(userData[i].getPreName())){
				localSavedContainer = userData[i].getUserData();
				currentUser=i;
				return;
			}
		}
		UserData[] tempUserArray = new UserData[userCount+1];
		for(int i=0;i<userCount;i++)tempUserArray[i] = userData[i];
		userData = new UserData[userCount+1];
		System.arraycopy(tempUserArray, 0, userData, 0, userCount);
		userData[userCount]= new UserData(user.getName(), user.getPreName(), maxIndex);
		localSavedContainer = userData[userCount].getUserData();
		currentUser=userCount;
	}
}