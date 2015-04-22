package terminal;

import java.io.File;
import java.util.Map;
/**
 * Realisiert die Kommunikation mit TrueCrypt. Zu beachten ist, 
 * dass nur die Windows Version von TrueCrypt unterstützt wird. Dies hat
 * den Grund, dass auf die Kommandoshell von Windows zugegriffen wird.
 * Dadurch geht die Platformunabhängigkeit verloren.
 * @author Dennis Jandt
 * @version 1.0
 */
public class TrueCryptControl {	
	public String trueCrypPath="";

	/**
	 * Lädt den unter dem Pfad gelisteten Container mit dem Passwort.
	 * @param path Pfad der zu öffnenden Datei
	 * @param password Passwort des Containers
	 * @return Ob der Container geladen wurde
	 */
	public boolean mountContainer(String path, String password){
		String[] command = {"cmd", "/c",trueCrypPath,"/q","/v"+path,"/a","/p"+password};
		ProcessBuilder pb = new ProcessBuilder(command);
		int oldCount = File.listRoots().length;
		try{
			Process p = pb.start();
			p.waitFor();
			if(oldCount==File.listRoots().length)return false;
			else return true;
		}catch(Exception e){
			System.out.println("Fehler");
		}
		return false;
	}
	
	/**
	 * Entlädt sämtliche Container aus dem Speicher.
	 */
	public void dismountAll(){
		
		String[] command = {"cmd", "/c", trueCrypPath,"/s","/q","/d", "/w"};
		ProcessBuilder pb = new ProcessBuilder(command);
		Map<String, String> env = pb.environment();
		 env.put("truecrypt", trueCrypPath);
		try{
			pb.start();
		}catch(Exception e){
			System.out.println("Fehler");
		}
	}
	
	/**
	 * Öffnet den GuideAgent von TrueCrypt für den neuen Container.
	 * @param wait Ob auf das Ende des Prozess gewartet werden soll
	 */
	public void openNewContainerWizard(boolean wait){
		String tcformat = trueCrypPath.replace(".exe", " Format.exe");
		String[] command = {"cmd", "/c",tcformat};
		ProcessBuilder pb = new ProcessBuilder(command);
		try{
			Process p = pb.start();
			if(wait)p.waitFor();
		}catch(Exception e){
			System.out.println("Fehler");
		}
	}
	
	/**
	 * Öffnet TrueCrypt mit der gewählten Datei als Argument
	 * @param path Absoluter Pfad zur Datei.
	 */
	public void migrateOldContainer(String path){
		String[] command = {"cmd", "/c",trueCrypPath,"/v"+path};
		ProcessBuilder pb = new ProcessBuilder(command);
		try{
			pb.start();
		}catch(Exception e){	
			System.out.println("Fehler");
		}
	}
	
	
	/**
	 * Setzt den aktuellen TrueCrypt-Pfad.
	 * Dieser ist notwendig, um die Funktionen zu nutzen.
	 * @param path Pfad von TrueCrypt.
	 */
	public void setTrueCryptPath(String path){
		trueCrypPath=path;
	}
}
