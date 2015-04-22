package terminal.data;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.spec.RSAPublicKeySpec;
import terminal.Terminal;
/**
 * Enthält einen gespeicherten PublicKey und Operationen um Informationen von diesem zu bekommen.
 * @author Dennis Jandt
 */
	 

public class PublicKeyObject implements Serializable{
	
	
	private static final long serialVersionUID = 1L;
	private String preName;
	private String name;
	private String modulusString,exponentString;
	private BigInteger publicModulus, publicExponent;
	
	/**
	 * Erstellt ein neues PublicKeyObject mit den übergebenen Daten.
	 * @param preName Vorname des Besitzers
	 * @param name Nachname des Besitzers
	 * @param modulusArray Der Modulus als Byte-Array
	 * @param exponentArray Der Exponent als Byte-Array
	 */
	public PublicKeyObject(String preName, String name, byte[] modulusArray, byte[] exponentArray){
		this.preName = preName;
		this.name = name;		
		String modulus = Terminal.bytesToHex(modulusArray);
		String exponent = Terminal.bytesToHex(exponentArray);
		modulusString = modulus;
		exponentString = exponent;
	}
	
	/**
	 * Erzeugt ein RSAPublicKeySpec-Objekt zum Verschlüsseln von Daten mit einem RSA-PublicKey.
	 * @return Das aus den gespeicherten Daten erzeugte RSAPublicKeySpec-Objekt
	 */
	public RSAPublicKeySpec getPublicKey(){		
		publicModulus = new BigInteger(modulusString, 16);
		publicExponent = new BigInteger(exponentString, 16);
		return new RSAPublicKeySpec(publicModulus, publicExponent);
	}
	
	/**
	 * Liefert den gespeicherten Modulus zurück.
	 * @return Modulus des PublicKeys
	 */
	public BigInteger getModulus(){
		return new BigInteger(modulusString, 16);
	}
	
	/**
	 * Liefert den gespeicherten Exponent zurück.
	 * @return Exponent des PublicKeys
	 */
	public BigInteger getExponent(){
		return new BigInteger(exponentString,16);
	}
	
	/**
	 * Liefert den Nachnamen des Besitzers.
	 * @return Nachname des Besitzers
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Liefert den Vornamen des Besitzers.
	 * @return Vorname des Besitzers
	 */
	public String getPreName(){
		return preName;
	}
}
