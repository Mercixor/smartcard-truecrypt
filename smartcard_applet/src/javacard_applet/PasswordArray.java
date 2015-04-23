package javacard_applet;
/**
 * Klasse zum speichern der TrueCrypt Container Daten.
 * 	
 * @author Dennis Jandt
 *
 */
public class PasswordArray {
	byte[] containerPassword;
	byte[] containerName;
	public PasswordArray(short lengData, short lengName){
		containerPassword = new byte[lengData];
		containerName = new byte[lengName];
	}
}
