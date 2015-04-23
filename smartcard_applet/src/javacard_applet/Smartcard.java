package javacard_applet;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.OwnerPIN;
import javacard.framework.Util;
import javacard.security.CryptoException;
import javacard.security.KeyBuilder;
import javacard.security.KeyPair;
import javacard.security.RSAPublicKey;
import javacardx.crypto.Cipher;

/**
 * Hauptapplet zur Realisierung der SmartCard Funktionen und Instruktionsabarbeitung.
 * Zudem Verwaltung der gespeicherten Benutzerdaten und die Authorisierung dieses mit dem System.
 * 
 * @author Dennis Jandt	
 */
public class Smartcard extends Applet{

	private OwnerPIN pin;
	private OwnerPIN puk;
	private KeyPair keypair;
	private final short MAX_INDEX					= (short) 10;
	private final short PW_LENGTH					= (short) 64;
	// Maximale APDU Länge beachten! MAX_INDEX * MAX_NAME_LENGTH < 256 Byte!
	private final short MAX_CONTAINER_NAME_LENGTH	= (short) 10;
	private final short MAX_USERNAME_LENGTH			= (short) 20;
	// Festlegen der RSA-Schlüssellänge
	private final short	RSA_KEY_LENGTH				= KeyBuilder.LENGTH_RSA_2048;
	// Benutzer Daten Struktur
	// user_data[0..19] = Benutzer Name
	private byte[] user_data 						= new byte[MAX_USERNAME_LENGTH];
	private byte[] cipherBuf						= new byte[256];
	
	private PasswordArray[] passwordList 			= new PasswordArray[MAX_INDEX];
	
	// Standard PIN für eine neue Karte: 1234
	private static byte[] default_pin		={(byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34};
	// PIN Unblock (PUK) für die Karte setzen: 123456
	private static byte[] default_puk		={(byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34,(byte) 0x35, (byte) 0x36};
	
	
	// SW-Word falls die Verfication fehlschlägt.
	// Das letzte Nibbel enthält die verbleibende Anzahl an Versuchen.
	final static short   SW_PIN_FAILED 		=(short) 0x63C0;   														
	
	// CLA WORDS
	private final byte CLA_COMMAND			= (byte) 0x80;
	
	// INS WORDS
	private final byte INS_CHECKPIN 			= (byte) 0x00;
	private final byte INS_WRITE_CONTAINER_DATA	= (byte) 0x01;
	private final byte INS_WRITE_USER_DATA 		= (byte) 0x02;
	private final byte INS_READ_USER_DATA 		= (byte) 0x03;
	private final byte INS_READ_CONTAINER_NAME	= (byte) 0x04;
	private final byte INS_UPDATE_PIN 			= (byte) 0x05;
	private final byte INS_PIN_TRIES			= (byte) 0x06;
	private final byte INS_GET_PUBLIC			= (byte) 0x07;
	private final byte INS_GET_MAX_INDEX		= (byte) 0x08;
	private final byte INS_GET_PASSWORD			= (byte) 0x09;
	private final byte INS_UNBLOCK_PIN			= (byte) 0x0A;
	
	// P2 BYTES
	private final byte P1_GET_FIRST_200				= (byte) 0x01;
	private final byte P1_GET_LAST					= (byte) 0x02;
	private final byte P2_WRITE_PW					= (byte) 0x01;
	private final byte P2_DECRYPT_AND_WRITE_PART1 	= (byte) 0x02;
	private final byte P2_DECRYPT_AND_WRITE_PART2 	= (byte) 0x03;
	
	/**
	 * Klassenkonstruktor welcher die Erzeugung der PIN und PUK startet, sowie
	 * den nötigen RSA-Schlüssel generiert.
	 * 
	 * Bei einem 2048 Bit RSA-Schlüssel ist die Ladezeit des Applets ~30 Sekunden,
	 * danach aber für den Benutzer nicht weiter spürbar.
	 *
	 */
	public Smartcard(){
		byte retries = (byte) 3;
		byte maxLength = (byte) 4;
		// Erzeugen des Standardpins mit maximal drei Fehlversuchen
		pin = new OwnerPIN(retries, maxLength);
		puk = new OwnerPIN((byte)6,(byte)6);
		puk.update(default_puk,(short)0,(byte)6);
		pin.update(default_pin,(short) 0,(byte) 4);
		keypair = new KeyPair(KeyPair.ALG_RSA_CRT, RSA_KEY_LENGTH);
		keypair.genKeyPair();
		for(short i=0;i<MAX_INDEX;i++){
			passwordList[i]= new PasswordArray(PW_LENGTH, MAX_CONTAINER_NAME_LENGTH);
		}
	}
	
	/**
	 * Installiert und registriert das Applet beim JavaCard Kartenmanager.
	 */
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		// GP-compliant JavaCard applet registration
		new Smartcard().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	/**
	 * Erkennung des gesendeten Befehls im INS Field der APDU.
	 * @params apdu - Empfangene APDU.
	 */
	public void process(APDU apdu) {
		//	Gibt bei erfolgreicher Selektierung 9000 zurück
		if (selectingApplet()){
			return;
		}
		
		// 	Lesen des Buffers zur Erkennung des CLA und INS Bytes.
		byte[] buf = apdu.getBuffer();
		if(buf[ISO7816.OFFSET_CLA]==CLA_COMMAND){
			switch(buf[ISO7816.OFFSET_INS]){
			case INS_CHECKPIN:
				commandCheckPin(apdu);
				break;
			case INS_WRITE_CONTAINER_DATA:
				commandWriteData(apdu);
				break;
			case INS_WRITE_USER_DATA:
				commandWriteUserData(apdu);
				break;
			case INS_READ_USER_DATA:
				commandReadUserData(apdu);
				break;
			case INS_READ_CONTAINER_NAME:
				commandReadContainerData(apdu);
				break;
			case INS_UPDATE_PIN:
				commandUpdatePin(apdu);
				break;
			case INS_PIN_TRIES:
				commandPinTries();
				break;
			case INS_GET_PUBLIC:
				commandGetPublic(apdu);
				break;
			case INS_GET_MAX_INDEX:
				commandGetMaxIndex(apdu);
				break;
			case INS_GET_PASSWORD:
				commandGetPassword(apdu);
				break;
			case INS_UNBLOCK_PIN:
				commandUnblockPin(apdu);
				break;
			default:
				ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
			}
		}
	}	

	/**
	 * 		INS_CHECKPIN:				Überprüft den in der APDU gespeicherten PIN.
	 * 									Gibt bei Fehlversuch die verbleibenden Versuche zurück.
	 * 
	 * 		@param apdu 				Empfangene APDU. Enthält die zu prüfenden Daten.
	 * 
	 * 		@return						SW_OK oder 0x69C0, wobei im letztern Fall das letzte Nibble die verbleibenden Versuche enthält
	 */
	private void commandCheckPin(APDU apdu) {
		// 	Lesen des Buffers
		byte[] buf = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		if(pin.check(buf,ISO7816.OFFSET_CDATA,(byte)4)){
			return;
		}else{
			short tries = pin.getTriesRemaining();
			ISOException.throwIt( (short) (SW_PIN_FAILED + tries));
		}		
	}
	
	/**
	 * 		INS_WRITE_CONTAINER_DATA: 	Update alter Daten oder schreiben neuer Daten. Zusätzlich die Möglichkeit
	 * 									ein von anderen Benutzern, mit dem eigenen Public Key, verschlüsselten Passwörter
	 * 									einzulesen.
	 * 		
	 *									Byte[0..63]	= TC_Container Password
	 *									Byte[64..MAX_CONTAINER_NAME_LENGTH]= TC_Container_Name
	 *
	 *		@param P1 					Index der zu schreibenden Daten.
	 *		@param P2					Art der Operation - Klartext schreiben oder vorher Entschlüsseln
	 *		@param apdu 				Empfangene APDU. Enthält die empfangenen Daten.
	 */ 
	private void commandWriteData(APDU apdu) {		
		if (pin.isValidated()){
			// Lesen des Buffers
			byte[] buf = apdu.getBuffer();
			apdu.setIncomingAndReceive();
			byte index = buf[ISO7816.OFFSET_P1];
			if(index < MAX_INDEX){
				if(buf[ISO7816.OFFSET_P2]==P2_WRITE_PW){
					// Kopieren der Daten auf die SmartCard mit einer atomaren Operation
					Util.arrayCopy(buf,(short) ISO7816.OFFSET_CDATA, passwordList[index].containerPassword,(short)0,(short)PW_LENGTH);
					Util.arrayCopy(buf,(short) (ISO7816.OFFSET_CDATA+PW_LENGTH), passwordList[index].containerName,(short)0,(short)MAX_CONTAINER_NAME_LENGTH);
					return;						
				}
				if(buf[ISO7816.OFFSET_P2]==P2_DECRYPT_AND_WRITE_PART1){
					Util.arrayCopy(buf,(short) ISO7816.OFFSET_CDATA, cipherBuf,(short)0,(short)200);
					return;
				}
				if(buf[ISO7816.OFFSET_P2]==P2_DECRYPT_AND_WRITE_PART2){
					Util.arrayCopy(buf,(short) ISO7816.OFFSET_CDATA, cipherBuf,(short)200,(short)56);
					Util.arrayCopy(buf,(short) (ISO7816.OFFSET_CDATA+56), passwordList[index].containerName,(short)0,MAX_CONTAINER_NAME_LENGTH);
					try{
						Cipher cipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);
						cipher.init(keypair.getPrivate(), Cipher.MODE_DECRYPT);
						short ciLen = cipher.doFinal(cipherBuf,(short)0,(short)256,buf,(short)0);
						Util.arrayCopy(buf,(short)0,passwordList[index].containerPassword,(short)0,ciLen);
						return;
					}catch (CryptoException e){
						apdu.setOutgoing();
						apdu.setOutgoingLength((short)1);
						switch(e.getReason()){
						case CryptoException.ILLEGAL_VALUE:
							buf[0] = (byte) 0x01;
							break;
						case CryptoException.UNINITIALIZED_KEY:
							buf[0] = (byte) 0x02;
							break;
						case CryptoException.NO_SUCH_ALGORITHM:
							buf[0] = (byte) 0x03;
							break;
						case CryptoException.INVALID_INIT:
							buf[0] = (byte) 0x04;
							break;
						case CryptoException.ILLEGAL_USE:
							buf[0] = (byte) 0x05;
							break;
						default:
							buf[0] = (byte) 0xFF;
						}
						apdu.sendBytes((short)0,(short)1);			
					}					
				}					
			}else{
				ISOException.throwIt(ISO7816.SW_WRONG_P1P2);	
			}				
		}else{
		ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
		}
	}
	
	/**
	 * 		INS_WRITE_USER_DATA: 		Ändern der gespeicherten Benutzerdaten.
	 * 									Der Benutzer muss sich für diese Operation vorher authorisieren. 
	 * 
	 * 		@param apdu 				Empfangene APDU. Enthält die zu schreibenden Daten.
	 */
	private void commandWriteUserData(APDU apdu) {
		// Lesen des Buffers
		byte[] buf = apdu.getBuffer();
		short len = apdu.setIncomingAndReceive();
		if (pin.isValidated()){
			Util.arrayCopy(buf,(short) ISO7816.OFFSET_CDATA, user_data,(short)0,(short) (len));
			return;
		}else{
			ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
		}
	}

	/**
	 * 		INS_READ_USER_DATA: 		Auslesen der gespeicherten Benutzerdaten: Vor- und Nachname
	 * 
	 * 		@param apdu 				APDU zum senden der Daten.
	 * 
	 * 		@return						Liefert Nach- und Vorname wie folgt: Nachname+0x20+Vorname+0xDE
	 */
	private void commandReadUserData(APDU apdu) {
		short len = (short) user_data.length;
		apdu.setOutgoing();
		apdu.setOutgoingLength(len);
		apdu.sendBytesLong(user_data,(short) 0, (short) len);
		return;
	}
	
	/**
	 * 		INS_READ_CONTAINER_NAME:	Auslesen der gespeicherten Containernamen und zurücksenden an das Terminal.
	 * 									Der Benutzer muss sich für diese Operation vorher authorisieren.
	 * 
	 * 		@param apdu					APDU zum senden der Daten.
	 * 
	 * 		@return						Liste mit den gespeicherten Containernamen.
	 */
	private void commandReadContainerData(APDU apdu) {
		if(pin.isValidated()){
			short len = (short) passwordList.length;
			byte[] buf = apdu.getBuffer();
			for (short i=0;i<len;i++){
				Util.arrayCopy(passwordList[i].containerName,(short)0,buf ,(short)(0+i*MAX_CONTAINER_NAME_LENGTH),MAX_CONTAINER_NAME_LENGTH);
			}
			len =(short)(passwordList.length*MAX_CONTAINER_NAME_LENGTH);
			apdu.setOutgoing();
			apdu.setOutgoingLength(len);
			apdu.sendBytes((short)0,(short)len);
			return;
		}
	}
	
	/**
	 * 		INS_UPDATE_PIN:		Setzt eines neuen Benutzer PINs.
	 * 							Der Benutzer muss sich für diese Operation vorher authorisieren.
	 * 
	 * 		@param apdu 		Empfangene APDU. Enthält die empfangenen Daten.
	 */
	private void commandUpdatePin(APDU apdu) {
		// 	Lesen des Buffers
		byte[] buf = apdu.getBuffer();
		if(pin.isValidated()){
			byte byteRead = (byte)(apdu.setIncomingAndReceive());
			if(byteRead == 0x04){			
				pin.update(buf,ISO7816.OFFSET_CDATA,(byte)4);
				return;
			}else{
				ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
			}
		}else{
		ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
		}
	}
	
	/**
	 * 		INS_PIN_TRIES:		Rückgabe der Anzahl der verbleibenden Versuche.
	 * 
	 * 		@param apdu 		APDU zum senden der Daten.
	 * 
	 * 		@return				0x63C0, wobei das letzte Nibble mit den verbleibenden Versuchen befüllt ist.
	 */
	private void commandPinTries() {
		short tries = pin.getTriesRemaining();
		ISOException.throwIt( (short) (SW_PIN_FAILED + tries));	
	}
	
	/**
	 * 	 	INS_GET_PUBLIC:		Senden des öffentlichen Schlüsselteils.
	 * 
	 * 		@param P1 			Auswahl ob Modulus oder Exponent teil geschickt werden soll.
	 * 		@param apdu 		APDU zum senden der Daten.
	 * 
	 * 		@return 			Modulus und Exponent des RSA-Karten-Schlüssels
	 */
	private void commandGetPublic(APDU apdu) {
		// Lesen des Buffers
		byte[] buf = apdu.getBuffer();
		RSAPublicKey pubKey = (RSAPublicKey) keypair.getPublic();
		byte[] modulus = new byte[256];
		byte[] exponent = new byte[20];
		pubKey.getModulus(modulus,(short)0);
		short expLen = pubKey.getExponent(exponent,(short)0);
		short out = 0;
		if(buf[ISO7816.OFFSET_P1]==P1_GET_FIRST_200){
			out = (short) 200;
			Util.arrayCopy(modulus,(short)0,buf,(short)0,(short)200);
		}
		if(buf[ISO7816.OFFSET_P1]==P1_GET_LAST){
			out = (short)(56+expLen);
			Util.arrayCopy(modulus,(short)200,buf,(short)0,(short)56);
			Util.arrayCopy(exponent,(short)0,buf,(short)56,(short)expLen);
		}
		apdu.setOutgoing();
		apdu.setOutgoingLength(out);
		apdu.sendBytes((short)0,out);
		return;
	}
	
	/**
	 * 		INS_GET_MAX_INDEX:	Liefert maximale Anzahl zu speichernden Daten zurück.
	 * 
	 * 		@param apdu			Empfangene APDU. Enthält die empfangenen Daten.
	 * 
	 * 		@return Maximal ansteuerbarer Index
	 */
	private void commandGetMaxIndex(APDU apdu) {
		byte[] buf = apdu.getBuffer();
		buf[0]=MAX_INDEX;
		apdu.setOutgoing();
		apdu.setOutgoingLength((short)1);
		apdu.sendBytes((short)0,(short)1);
		return;
	}
	
	
	/**
	 * 		INS_GET_PASSWORD:	Liefert das an dem Index gespeicherte Passwort zurück.
	 * 							Der Benutzer muss sich für diese Operation vorher authorisieren.
	 * 
	 * 		@param apdu			Empfangene APDU. Enthält die empfangenen Daten.
	 * 		@param P1			Enthält den Index, welcher ausgelesen werden soll.
	 * 
	 * 		@return				Passwort an dem Index von P1
	 */
	private void commandGetPassword(APDU apdu) {
		if(pin.isValidated()){
			byte[] buf = apdu.getBuffer();
			byte index = (byte) buf[ISO7816.OFFSET_P1];
			if(index<MAX_INDEX){
				Util.arrayCopy(passwordList[(short)index].containerPassword,(short)0,buf,(short)0,(short)64);
				apdu.setOutgoing();
				apdu.setOutgoingLength((short)64);
				apdu.sendBytes((short)0,(short)64);
				return;
			}else{
				ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
			}
		}else{
			ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
		}
	}
	
	/**
	 * 		INS_UNBLOCK_PIN:	Überprüft die eingegebene PUK und entsperrt den PIN falls diese richtig ist.
	 * 							Bei Falscheingabe werden die verbleibenden Versuche zurückgeliefert.
	 * 
	 * 		@param apdu Empfangene APDU. Enthält die empfangene PUK.
	 */
	private void commandUnblockPin(APDU apdu) {
		byte[] buf = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		if(puk.check(buf,ISO7816.OFFSET_CDATA,(byte)6)){
			pin.resetAndUnblock();
			return;
		}else{
			short tries = puk.getTriesRemaining();
			ISOException.throwIt( (short) (SW_PIN_FAILED + tries));
		}
	}
}
