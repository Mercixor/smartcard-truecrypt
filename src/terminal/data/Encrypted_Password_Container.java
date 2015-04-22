package terminal.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import terminal.Terminal;
/**
 * Enth�lt das mit dem PublicKey verschl�sselte Passwort.
 * @author Dennis Jandt
 *
 */
public class Encrypted_Password_Container implements Serializable {
	
	private static final long serialVersionUID = 1L;
	// Das verschl�sselte Passwort in Byte
	private byte[] encryptedPW;
	
	/**
	 * Erstellt ein neues Containerpasswort-Objekt, mit dem entsprechenden Passwort in Byte.
	 * @param encryptedPW Das verschl�sselte Passwort in Byte.
	 */
	public Encrypted_Password_Container(byte[] encryptedPW){
		this.encryptedPW = encryptedPW;
	}	

	/**
	 * Liefert das gespeicherte verschl�sselte Passwort in Byte zur�ck.
	 * @return Das verschl�sselte Passwort in einem Byte-Array.
	 */
	public byte[] getEncrPassword(){
		return encryptedPW;
	}
	
	/**
	 * Schreibt das Objekt auf den Datentr�ger.
	 * Dazu werden die �bergebenen Parameter ben�tigt, um eine eindeutige Bezeichnung zu erhalten.
	 * 'containerName_from_urheber_to_empf�nger'
	 * @param owner Der das Passwort teilt
	 * @param containerName Name des zu teilenden Containers
	 * @param reciever Name des Empf�ngers
	 * @return Ob das Objekt erfolgreich geschrieben wurde
	 */
	public boolean writeObject(String owner, String containerName, String reciever) {
		FileOutputStream saveFile;
		ObjectOutputStream save;
		try {
			File file = new File(Terminal.publicKeyPath);
			file.mkdirs();
			String fileName = Terminal.publicKeyPath+"/"+containerName+"_from_"+owner+"_to_"+reciever+".cont";
			saveFile = new FileOutputStream(fileName);
			save = new ObjectOutputStream(saveFile);
			save.writeObject(this);
			save.close();
			Runtime.getRuntime().exec("explorer.exe \""+file.getAbsolutePath()+"\"");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
