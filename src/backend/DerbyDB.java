package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

public class DerbyDB {
	public String					driver		= "org.apache.derby.jdbc.EmbeddedDriver";
	public String					protocol	= "jdbc:derby:";
	String							dbName;												// Name
																							// der
																							// Datenbank

	// SQL Variablen
	private Statement				s			= null;
	private PreparedStatement		psInsert;
	private PreparedStatement		psUpdate;
	private Connection				conn		= null;
	private ArrayList<Statement>	statements	= new ArrayList<Statement>();

	/**
	 * Erstellt ein neues Datenbankobjekt, mit dem gewählten Namen.
	 *
	 * @param databaseName Name der Datenbank.
	 * @param embedded Ob Lokal oder Netzwerk - noch ohne Funktion.
	 */
	public DerbyDB(String databaseName, boolean embedded) {
		this.dbName = databaseName;
	}

	/**
	 * Startet die Datenbank mit den Standardbenutzer.
	 *
	 * @return True oder False, je nach Erfolg des Startens.
	 */
	public boolean startDB() {
		try {
			Properties props = new Properties(); // Verbindungs-Einstellungen
			// Stellt einen Benutzernamen und Passwort zur Verfügung
			// In der embedded Version optional
			props.put("user", "user1");
			props.put("password", "user1");
			conn = DriverManager.getConnection(protocol + dbName
					+ ";create=true", props);
			System.out.println("Connected to and created database " + dbName);
			// Übertragungen müssen manuell bestätigt werden
			conn.setAutoCommit(false);
			// Erstellen eines Statement Objekts um SQL Befehle an die Datenbank
			// zu schicken
			s = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			statements.add(s);

			return true;
		} catch (SQLException e) {
			printSQLException(e);
			return false;
		}
	}

	/**
	 * Fährt die Datenbank herunter.
	 *
	 * @return True oder False, je nach Status des Herunterfahrens.
	 */
	public boolean stopDB() {
		try {
			// Das Attribute 'shutdown=true' fährt die DB herunter
			// Um eine spezifische DB herunterzufahren muss folgender Befehl
			// verwendet werden
			// DriverManager.getConnection("jdbc:derby:" + dbName +
			// ";shutdown=true");
			DriverManager.getConnection("jdbc:derby:" + dbName
					+ ";shutdown=true");
			return true;
		} catch (SQLException e) {
			if (((e.getErrorCode() == 50000) && ("XJ015"
					.equals(e.getSQLState())))
					|| (e.getErrorCode() == 45000)
					&& ("08006".equals(e.getSQLState()))) {
				// Erwartete Excepion
				System.out.println("Derby wurde normal heruntergefahren");
				return true;
			} else {
				// Wenn der Fehlercode oder der SQL Status ein andererer ist,
				// wurde
				// Derby nicht normal heruntergefahren
				System.err
						.println("Derby wurde nicht normal hertuntergefahren");
				printSQLException(e);
				return false;
			}
		}
	}

	public void dropTable() {
		try {
			// Löschen der Datensätze der Datenbank
			s.execute("DROP table Filme");
			conn.commit();
		} catch (SQLException e) {
			printSQLException(e);
		}
	}

	public boolean setTestData(int id) {
		try {
			psInsert = conn
					.prepareStatement("insert into Filme (Name, Genre, Release, Description, IMDB, Speicherort, Trailerort) values (?, ?, ?, ?, ?, ?, ?)");
			statements.add(psInsert);

			psInsert.setString(1, "xXx"); // Name
			psInsert.setString(2, "Action"); // Genre
			psInsert.setDate(3, Date.valueOf("2000-11-09")); // Release
			psInsert.setString(4, "Ein Film mit Vin Diesel"); // Description
			psInsert.setDouble(5, 8.8); // IMDB
			psInsert.setString(6, "C:Filme"); // Speicherort
			psInsert.setString(7, "C:Trailer"); // Trailerort
			psInsert.executeUpdate();
			conn.commit();
			return true;
		} catch (SQLException e) {
			System.out.println("Fehler beim Einfügen der Testdaten");
			printSQLException(e);
			return false;
		}
	}

	public boolean setData(String name, String genre, String release,
			String summary, double imdb, String filmPath, String trailerPath) {
		try {
			psInsert = conn
					.prepareStatement("insert into Filme (Name, Genre, Release, Description, IMDB, Speicherort, Trailerort) values (?, ?, ?, ?, ?, ?, ?)");
			statements.add(psInsert);

			System.out.println("INSERT Aufruf: Name=" + name + " | Genre="
					+ genre + " | Date=" + release + " | Summary=" + summary
					+ " | IMDB=" + imdb + " | FilmPfad=" + filmPath
					+ " | TrailPfad=" + trailerPath);

			psInsert.setString(1, name); // Name
			psInsert.setString(2, genre); // Genre
			psInsert.setDate(3, Date.valueOf(release)); // Release
			psInsert.setString(4, summary); // Description
			psInsert.setDouble(5, imdb); // IMDB
			psInsert.setString(6, filmPath); // Speicherort
			psInsert.setString(7, trailerPath); // Trailerort
			psInsert.executeUpdate();
			conn.commit();
			return true;
		} catch (SQLException e) {
			System.out.println("Fehler beim Einfügen der neuen Filmdaten");
			printSQLException(e);
			return false;
		}
	}

	/**
	 * Liefert die Ergebnisgröße des Querys zurück.
	 *
	 * @param rs Das ResultSet-Objekt des Querys.
	 * @return Die Größe des Querys. -1 falls ein Fehler beim Abruf auftrat.
	 */
	public int getResultSize(ResultSet rs) {
		int resultSize = -1;
		try {
			rs.setFetchDirection(ResultSet.TYPE_SCROLL_INSENSITIVE);
			boolean data = rs.last();
			if (data) {
				resultSize = rs.getRow();
			}
			System.out.println("Ergebnisgröße: " + resultSize);
		} catch (SQLException e) {
			System.out.println("Fehler beim Abruf der Query-Größe");
			printSQLException(e);
		}
		return resultSize;
	}

	/**
	 * Führt den übergebenen Query an die Datenbank aus.
	 *
	 * @param sqlQuery SQL-Query für die Datenbank.
	 * @return
	 */
	public ResultSet getSavedData(String sqlQuery) {
		ResultSet rs = null;
		try {
			rs = s.executeQuery(sqlQuery);
		} catch (SQLException e) {
			printSQLException(e);
		}
		return rs;
	}

	/**
	 * Prints details of an SQLException chain to <code>System.err</code>.
	 * Details included are SQL State, Error code, Exception message.
	 *
	 * @param e
	 *            the SQLException from which to print details.
	 */
	public static void printSQLException(SQLException e) {
		// Unwraps the entire exception chain to unveil the real cause of the
		// Exception.
		while (e != null) {
			System.err.println("\n----- SQLException -----");
			System.err.println("  SQL State:  " + e.getSQLState());
			System.err.println("  Error Code: " + e.getErrorCode());
			System.err.println("  Message:    " + e.getMessage());
			// for stack traces, refer to derby.log or uncomment this:
			// e.printStackTrace(System.err);
			e = e.getNextException();
		}
	}
}
