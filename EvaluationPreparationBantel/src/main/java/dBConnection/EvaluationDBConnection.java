package dBConnection;

import java.sql.SQLException;

public class EvaluationDBConnection extends SQLiteConnection{
	private  static EvaluationDBConnection singleton;
	private static String dbLocation = "D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\Bantel\\Services\\EvaluationPreparation\\Evaluation.db";
	
	private EvaluationDBConnection(String passPhrase) throws ClassNotFoundException {
		super(passPhrase, dbLocation);
	}
	
	public static EvaluationDBConnection getInstance(String passPhrase) throws ClassNotFoundException {

		if (singleton==null || (singleton.getPassphrase()!=passPhrase)) {
			singleton = new EvaluationDBConnection(passPhrase);
		}
		
		return singleton;
	}
	public static EvaluationDBConnection getInstance() {
		return singleton;
	}
	public void close() throws SQLException {
		super.close();
		singleton = null;
	}
	

}
