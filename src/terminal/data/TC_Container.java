package terminal.data;

import java.io.Serializable;
/**
 * Verwaltet die Informationen für einen TrueCrypt-Container.
 * @author Dennis Jandt
 */
public class TC_Container implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String containerName, fileName, filePath;
	private int connectedSmartCardContainerIndex;
	
	/**
	 * Erstellt ein Container-Objekt mit dem übergebenen Namen.
	 * @param name Name des Containers.
	 */
	public TC_Container(String name){
		this.containerName = name;
	}
	
	/**
	 * Liefert den Namen der Containerdatei.
	 * @return Name der Containerdatei
	 */
	public String getfileName(){
		return fileName;
	}
	
	/**
	 * Liefert den absoluten Pfad zur Datei.
	 * @return Absolute Pfad der Datei.
	 */
	public String getFilePath(){
		return filePath;
	}
	
	/**
	 * Liefert den mit dem Container verbundenen Passwortindex zurück.
	 * @return SmartCard-Container Index
	 */
	public int getConnectedIndex(){
		return connectedSmartCardContainerIndex;
	}
	
	/**
	 * Liefert den SmartCard-Containernamen zurück.
	 * @return SmartCard-Containername
	 */
	public String getName(){
		return containerName;
	}
	
	/**
	 * Setzt den aktuellen Dateinamen.
	 * @param name Dateiname
	 */
	public void setFileName(String name){
		fileName = name;
	}
	
	/**
	 * Setzt den zugehörigen absoluten Pfad.
	 * @param path Absoluter Pfad zur Datei
	 */
	public void setFilePath(String path){
		filePath = path;
	}
	
	/**
	 * Setzt den mit diesem Container verbundenen SmartCard-Container Index.
	 * @param index Der Referenzindex für das Passwort auf der SmartCard
	 */
	public void setRefferedIndex(int index){
		connectedSmartCardContainerIndex = index;
	}
}
