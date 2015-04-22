/**
 * Die Klasse Connect_Card realisiert die Verbindung mit dem Terminal und der SmartCard.
 * Dabei sollen alle relevanten Aufgaben, wie Verbinden und Abfragen, über diese Klasse laufen, sodass
 * ander Klassen diese nicht implementieren müssen und die Klassen Thematisch getrennt sind.
 */
package terminal;

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import terminal.data.TC_Container;
import terminal.data.TerminalData;
import terminal.data.UserData;

/**
 * Stellt die Funktionen zur Kommunikation mit der JavaCard zur Verfügung.
 * 
 * @author Dennis Jandt
 * @version 1.0
 */
public class JavaCard {
	private TerminalFactory factory;
	private List<CardTerminal> terminals;
	private CardTerminal cardTerminal;
	private CardChannel basicChannel;
	private CommandAPDU cmd;
	private ResponseAPDU rpd;
	private TerminalData tData;
	private Terminal terminal;
	public String preName = "",name ="";
	
	// AID des Applets
	private static final byte [] APPLET_AID 	= "smartapplet".getBytes();
	
	// CLA WORDS
	private final byte CLA_SELECT 		= (byte) 0x00;	
	private final byte CLA_APPLET 		= (byte) 0x80;
	
	// INS WORDS
	private final byte INS_SELECT 				= (byte) 0xA4;
	private final byte INS_CHECKPIN 			= (byte) 0x00;
	private final byte INS_WRITE_CONTAINER_DATA = (byte) 0x01;
	private final byte INS_WRITE_USER_DATA 		= (byte) 0x02;
	private final byte INS_READ_USER_DATA 		= (byte) 0x03;
	private final byte INS_READ_CONTAINER_NAME	= (byte) 0x04;
	private final byte INS_UPDATE_PIN 			= (byte) 0x05;
	private final byte INS_PIN_TRIES			= (byte) 0x06;
	private final byte INS_GET_PUBLIC			= (byte) 0x07;
	private final byte INS_GET_MAX_INDEX		= (byte) 0x08;
	private final byte INS_GET_PASSWORD			= (byte) 0x09;
	private final byte INS_UNBLOCK_PIN			= (byte) 0x0A;
	
	// P1 & P2 WORDS
	private final byte P1_GET_FIRST_200				= (byte) 0x01;
	private final byte P1_GET_LAST					= (byte) 0x02;
	private final byte P2_WRITE_PW					= (byte) 0x01;
	private final byte P2_DECRYPT_AND_WRITE_PART1	= (byte) 0x02;
	private final byte P2_DECRYPT_AND_WRITE_PART2 	= (byte) 0x03;
	
	// SW WORDS
	private static final int SW_OK 					= 0x9000;
	private final short PW_LENGTH					= (short) 64;
	// Maximale APDU Länge beachten! MAX_INDEX * MAX_NAME_LENGTH < 256 Byte!
	private final short MAX_CONTAINERNAME_LENGTH	= (short) 10;
	private final short MAX_USERNAME_LENGTH			= (short) 20;
	
	/**
	 * Konstruktor der Klasse. Bekommt als Parameter eine Referenz auf die Daten
	 * und das Hauptprogramm.
	 * @param data Referenz zum Datenhaltungsobjekt
	 * @param terminal Referenz zum Hauptprogramm
	 */
	protected JavaCard(TerminalData data, Terminal terminal){
		this.tData = data;	
		this.terminal = terminal;
		getTerminals();
	}
	
	/**
	 * Erfasst alle am System angeschlossenen Kartenlesegeräte und liefert diese als Stringliste zurück.
	 * @return Liste mit den Angeschlossenen Kartenlesegeräten
	 */
	protected List<String> getTerminals() {
		List<String> liste = new ArrayList<String>();
		factory = TerminalFactory.getDefault();
		try {
			terminals = factory.terminals().list();			
		} catch (CardException e) {
			//terminal.showError("Fehler beim Erkennen der anschlossenen Terminals", "Fehler");
			return null;
		}
		for(int i=0;i<terminals.size();i++)liste.add(terminals.get(i).getName());
	    return liste;
	}

	/**
	 * Baut eine Verbindung zum gewählten Kartenleser auf.
	 * @param terminalIndex gewählter Kartenleser
	 * @return Ob die Verbindung erfolgreich hergestellt wurde
	 */
	public boolean connect(int terminalIndex){
		try{
			cardTerminal = terminals.get(terminalIndex);
			// Verbinden mit der Karte mit dem T1 Protkoll
			Card card = cardTerminal.connect("T=1");
			basicChannel = card.getBasicChannel();
			return selectApplet();
		} catch (Exception e){
			return false;
		}
		
	}	
	
	/**
	 * Senden des eingegebenen PINs an die SmartCard, welche diesen überprüft.
	 * 
	 * @param pin	Übergebener PIN
	 * @return		Ob der PIN akzeptiert wurde
	 */
	public boolean checkPin(String pin){
		try{
			// CMD Aufbau: (CLA, INS, P1, P2, Data)
			cmd = new CommandAPDU(CLA_APPLET,INS_CHECKPIN,0,0,pin.getBytes());
			rpd = basicChannel.transmit(cmd);
			if(rpd.getSW()==SW_OK) return true;
		} catch (CardException e){
			return false;
		}
		return false;
	}
	
	/**
	 * Senden des neuen PINs an die SmartCard.
	 * 
	 * @param pin	Neuer PIN, welcher den alten ersetzt
	 * @return 		Ob der PIN geändert wurde
	 */
	public boolean updatePin(String pin){
		try{			
			// CMD Aufbau: (CLA, INS, P1, P2, Data)
			cmd = new CommandAPDU(CLA_APPLET,INS_UPDATE_PIN,0,0,pin.getBytes());
			rpd = basicChannel.transmit(cmd);
			return (rpd.getSW()==SW_OK);
		} catch (CardException e){
			return false;
		}
	}
	
	/**
	 * Sendet die PUK an die Karte.
	 * @param puk Übergebener Personal Unblocking Key
	 * @return Ob der PIN-Fehlerzähler zurückgesetzt wurde
	 */
	public int resetPin(String puk){
		try{
			cmd = new CommandAPDU(CLA_APPLET,INS_UNBLOCK_PIN,0,0,puk.getBytes());
			rpd = basicChannel.transmit(cmd);
			if(rpd.getSW()==SW_OK){
				return 99;
			}else{
				return rpd.getSW2()&0x0F;
			}
		} catch (CardException e){
			return -1;
		}
	}
	
	/**
	 *Fragt die Anzahl der verbleibenden Versuche zur Pin-Eingabe ab und liefert diese an den Aufrufer zurück.
	 * 
	 * @return 	Anzahl der verbleibenden Versuche. -1 bei einem Fehler bei der Datenübertragung.
	 */
	public int getRemainingTries(){
		try{
			cmd = new CommandAPDU(CLA_APPLET,INS_PIN_TRIES,0,0);
			rpd = basicChannel.transmit(cmd);
			int leftTries = rpd.getSW2()&0x0F;
			return leftTries;
		}catch (Exception e){
			return -1;
		}		
	}	
		
	/**
	 *Fragt die gespeicherten Benutzerdaten der SmartCard ab und setzt
	 *den aktuellen Benutzernamen.
	 * @return	die Benutzedaten als String
	 */
	public UserData getUserData(){
		try{
			name=""; preName="";
			cmd = new CommandAPDU(CLA_APPLET,INS_READ_USER_DATA,0,0);
			rpd = basicChannel.transmit(cmd);
			byte[] data = rpd.getData();
			int maxLength = data.length;
			int initCount = 0;
			for(int i=0;i<2;i++){
				for(int r=initCount;r<maxLength;r++){
					// Trenner zwischen Vor- und Nachname erreicht
					if(data[r]==0x20){
						initCount = name.length();
						initCount++;
						break;
					}
					// Nachname
					if(i==0){
						name = name + (char) data[r];
						continue;						
					}
					// Vorname und überprüfen des Ende Zeichens
					if(i==1 && data[r] != -34){
						preName = preName + (char) data[r];
						continue;
					}else{
						break;
					}
				}
			}
			terminal.setCurrentUser(preName+" "+name);
			UserData userdata = new UserData(name, preName, 1);  
			return (userdata);
		} catch (Exception e){
			terminal.showError("Fehler beim Empfangen der Kartendaten", "Fehler");
			return null;
		}
	}
	
	/**
	 * 	Fragt die auf der SmartCard gespeicherten Containernamen ab und speichert diese in der Klasse 'Terminal_data'.
	 * 	Der Benutzer muss sich vorher authorisieren.
	 */
	public void getSavedContainer(){
		try{
			cmd = new CommandAPDU(CLA_APPLET,INS_READ_CONTAINER_NAME,0,0);
			rpd = basicChannel.transmit(cmd);
			byte[] data = rpd.getData();
			byte[] temp = new byte[MAX_CONTAINERNAME_LENGTH];
			int anzahlDaten = data.length/MAX_CONTAINERNAME_LENGTH;
			TC_Container[] cont = new TC_Container[anzahlDaten];
			for(int i=0;i<anzahlDaten;i++){
				System.arraycopy(data, (i*MAX_CONTAINERNAME_LENGTH), temp,0, MAX_CONTAINERNAME_LENGTH);
				if(temp[0]!=0)cont[i] = new TC_Container(parseString(temp));
				else cont[i]= null;
			}
			tData.saveSmartCardContainer(cont);
		} catch (Exception e){
			terminal.showError("Fehler beim Empfangen der gespeicherten Containerdaten","Fehler beim Abruf der Container");
		}
	}
	
	/**
	 * 	Ruft den auf der SmartCard gespeicherten RSA-PublicKey ab. Dieser wird als Modulus und Exponent in
	 * 	zwei APDUs übertragen, aufgrund der maximalen Datenlänge der APDU. Die JavaCard unterstütz zwar Extended APDUs, aber
	 * 	das JCOP Entwicklungstool nicht.
	 * 	Speichert den PublicKey anschließend lokal in den Terminaldaten unter dem in der Karte gespeicherten Nach- und Vornamen ab.
	 */
	public void getPublicKey(){
		try{
			getUserData();
			cmd = new CommandAPDU(CLA_APPLET,INS_GET_PUBLIC,P1_GET_FIRST_200,0);
			rpd = basicChannel.transmit(cmd);			
			byte[] data = rpd.getData();
			byte[] dataModulus = new byte[256];
			System.arraycopy(data,0,dataModulus,0,200);
			cmd = new CommandAPDU(CLA_APPLET,INS_GET_PUBLIC,P1_GET_LAST,0);
			rpd = basicChannel.transmit(cmd);
			data = rpd.getData();
			System.arraycopy(data,0,dataModulus,200,56);
			byte[] dataExponent = new byte[3];
			System.arraycopy(data,56,dataExponent,0,3);
			tData.newPublicKey(name,preName,dataModulus,dataExponent);
		}catch (CardException e) {
			terminal.showError("Fehler beim Abrufen des PublicKeys. Bitte erneut versuchen", "Fehler beim Abruf des PublicKeys");
		}
		
	}
	
	/**
	 * Fragt den maximal ansprechbaren Index des auf der SmartCard gespeicherten Datenarrays ab.
	 * 
	 * @return Maximale Zahl der Container in Integer.
	 */
	public int getMaxIndex(){
		try {
			cmd = new CommandAPDU(CLA_APPLET,INS_GET_MAX_INDEX,0,0);
			rpd = basicChannel.transmit(cmd);
			byte[] data = rpd.getData();
			return (int) data[0];
		} catch (CardException e) {
			return 0;
		}
	}
	
	/**
	 * 	Liefert das auf der SmartCard gespeicherte Containerpasswort, für den gewählten Containerindex, zurück.
	 * 	Der Benutzer muss sich für diese Operation vorher authorisieren.
	 * 
	 * @param index		Der abzufragende Datenpunkt. 
	 * @return			Das Passwort in Klartext als String.
	 */
	public String getSelectedPassword(int index){
		try{
			getUserData();
			cmd = new CommandAPDU(CLA_APPLET,INS_GET_PASSWORD,(byte)index,0);
			rpd = basicChannel.transmit(cmd);			
			byte[] data = rpd.getData();
			return parseString(data);
		}catch (CardException e) {
			return null;
		}
	}
	
	/**
	 * 	Schreibt neue Benutzerdaten auf die Karte.
	 * 	Der Benutzer muss sich für diese Operation vorher authorisieren.
	 * 
	 * @param preName	Vorname des Benutzers
	 * @param name		Nachname des Benutzers	 * 
	 * @return 			'True' wenn SW_OK ansonsten 'False'
	 */
	public boolean writeUserData(String preName, String name){
		try {
			int length = (preName.length()+name.length()+2);
			int left = preName.length();
			if(length > MAX_USERNAME_LENGTH){
				left = MAX_USERNAME_LENGTH - name.length()-2;
				length = MAX_USERNAME_LENGTH;
			}
			byte[] data = new byte[length];
			byte[] temp = name.getBytes();
			System.arraycopy(temp, 0, data, 0, name.length());
			// Trennerzeichen zwischen Nach- und Vorname
			data[name.length()]=0x20;
			temp = preName.getBytes();
			System.arraycopy(temp, 0, data, name.length()+1, left);
			// Endezeichen setzten
			data[length-1] = (byte) 0xDE;
			cmd = new CommandAPDU(CLA_APPLET,INS_WRITE_USER_DATA,0,0,data);
			rpd = basicChannel.transmit(cmd);
			return (rpd.getSW()==SW_OK);		
		} catch (CardException e) {
			return false;
		}
	}
	
	/**
	 * Schreibt neue Containerdaten auf die Karte.
	 * Der Benutzer muss sich für diese Operation vorher authorisieren.
	 * 
	 * @param password			Das übergebene Passwort zum speichern auf der SmartCard
	 * @param tcContainerName	Der vom Benutzer gewählte Containername, damit dieser das Passwort einem Container zuordnen kann
	 * @param index				Der vom Benutzer gewählte Containername, damit dieser das Passwort einem Container zuordnen kann
	 * 
	 * @return					'True' falls die Operation ohne Fehler durchgeführt wurde, 'False' bei einem Fehler.
	 */
	public boolean writeContainerData(String password, String tcContainerName, int index){
			try {
				byte[] data = new byte[PW_LENGTH+MAX_CONTAINERNAME_LENGTH];
				byte[] temp = password.getBytes();
				System.arraycopy(temp, 0, data, 0, PW_LENGTH);
				temp = tcContainerName.getBytes();
				if (temp.length <= MAX_CONTAINERNAME_LENGTH) System.arraycopy(temp, 0, data, PW_LENGTH, temp.length);
				else System.arraycopy(temp, 0, data, PW_LENGTH, MAX_CONTAINERNAME_LENGTH);
				cmd = new CommandAPDU(CLA_APPLET,INS_WRITE_CONTAINER_DATA,index,P2_WRITE_PW,data);
				rpd = basicChannel.transmit(cmd);
				return (rpd.getSW()==SW_OK);		
			} catch (CardException e) {
				return false;
			}
	}
	
	/**
	 * Schreiben neuer Containerdaten, die von einem anderen Benutzer mit dem eigenen PublicKey verschlüsselt wurden.
	 * Diese werden auf der Karte dann mit dem zugehörigen PrivateKey entschlüsselt und dann gespeichert.
	 * Der Benutzer muss sich für diese Operation vorher authorisieren.
	 * 
	 * @param passwordByte		Das verschlüsselte Passwort in einem Bytearray
	 * @param tcContainerName	Der vom Benutzer gewählte Containername, damit dieser das Passwort einem Container zuordnen kann
	 * @param index				Der vom Benutzer gewählte Containername, damit dieser das Passwort einem Container zuordnen kann
	 * 
	 * @return					'True' falls die Operation ohne Fehler durchgeführt wurde;
	 * 							'False' bei einem Fehler, wobei dies in der Regel auf eine falsch verschlüsselte Datei hindeutet
	 */
	public boolean writePublicContainerData(byte[] passwordByte, String tcContainerName, int index){
		try {
			byte[] data = passwordByte;
			byte[] temp = new byte[200];
			System.arraycopy(data, 0, temp, 0, 200);
			cmd = new CommandAPDU(CLA_APPLET,INS_WRITE_CONTAINER_DATA,index,P2_DECRYPT_AND_WRITE_PART1,temp);
			rpd = basicChannel.transmit(cmd);
			temp = new byte[56+tcContainerName.length()];
			System.arraycopy(data, 200, temp, 0, 56);
			if(tcContainerName.length() <=MAX_CONTAINERNAME_LENGTH)System.arraycopy(tcContainerName.getBytes(), 0, temp, 56, tcContainerName.length());
			else System.arraycopy(tcContainerName.getBytes(), 0, temp, 56, MAX_CONTAINERNAME_LENGTH);
			cmd = new CommandAPDU(CLA_APPLET,INS_WRITE_CONTAINER_DATA,index,P2_DECRYPT_AND_WRITE_PART2,temp);
			rpd = basicChannel.transmit(cmd);
			return (rpd.getSW()==SW_OK);		
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Wandelt einen Hex-String in ASCII Zeichen um.
	 * @param daten				Das übergebene Bytearray mit den Hex-Zeichen.
	 * 
	 * @return					Die extrahierten ASCII-Zeichen als String
	 */
	private String parseString(byte[] daten){
		String ergebnis = "";
		for (int i=0;i<daten.length;i++){
			ergebnis = ergebnis+(char) daten[i];
		}
		return ergebnis;
	}
	
	/**
	 * Wählt das Multiuserapplet auf der Karte als aktives Applet aus.
	 * 
	 * @return					'True' falls das Applet gewählt wurde;
	 * 							'False' falls ein Fehler auftrat. In der Regel, falls das Applet nicht vorhanden ist.
	 */
	public boolean selectApplet(){
		try{
			cmd = new CommandAPDU(CLA_SELECT,INS_SELECT,4,0,APPLET_AID);
			rpd = basicChannel.transmit(cmd);
			return (rpd.getSW()==SW_OK);
		} catch (Exception e){
			return false;
		}		
	}
	
	/**
	 * Rückgabe ob ein Terminal an dem Benutzer-PC angeschlossen ist.
	 * 
	 * @return					'True' falls ein Kartenleser gefunden wurde;
	 * 							'False' falls keiner gefunden wurde.
	 */
	public int getTerminalSize(){
		try{
			if (terminals.isEmpty()) return 0;
			else return terminals.size();
		}catch (Exception e){
			return 0;
		}
		
	}
	
	/**
	 * Rückgabe ob im gewählten Terminal eine Karte eingesteckt ist.
	 * 
	 * @return					'True' falls eine Karte gefunden wurde;
	 * 							'False' falls keine gefunden wurde.
	 */
	public boolean isCardPresent(){
		try{
			if (cardTerminal.isCardPresent()) return true;
			else return false;
		}catch(Exception e){
			return false;
		}
		
	}
}