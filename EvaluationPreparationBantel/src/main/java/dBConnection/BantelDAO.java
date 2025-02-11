package dBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class BantelDAO {
	DBConnection dBConnection = null;

	public BantelDAO(String passPhrase) throws ClassNotFoundException {
		dBConnection = BantelDBConnection.getInstance(passPhrase);
	}


	public BantelDAO() {
		dBConnection = BantelDBConnection.getInstance();
	}

	
	public JSONObject getSalesAmounts(String fromDate, String toDate) throws SQLException{
		Statement statement = null;
		ResultSet resultSet = null;
		JSONObject salesamounts = new  JSONObject();
		String sql= "select svp.skbez, sum(pos.menge/pv.ProdFaktor * kart.menge) As Menge From Lieferscheine ls \r\n" + 
				"join LS_Positionen pos on ls.ls_No = pos.LS_No \r\n" + 
				"left join VerpacktEndproduktPaarung vep on vep.pkbez=pos.pkbez \r\n" + 
				"join  SorteVerpacktPaarung svp on svp.vkbez=vep.vkbez \r\n" + 
				"join EndproduktKartonagePaarung ekp on ekp.pkbez = vep.pkbez \r\n" + 
				"join VerpacktKartonagePaarung vkp on vkp.vkbez = vep.vkbez \r\n" + 
				"join ProdukteVerpackt pv on pv.vkbez = vep.vkbez \r\n" + 
				"join Kartonagen kart on kart.verpID = ekp.verpID \r\n" + 
				"where ls.Datum>='" + fromDate + "' and ls.Datum <= '" + toDate + "' \r\n" + 
				"group by svp.skbez \r\n" + 
				"order by svp.skbez asc";
		/*String sql = "select svp.skbez, sum(pos.menge*kart.menge) From Lieferscheine ls \r\n" + 
				"join LS_Positionen pos on ls.ls_No = pos.LS_No \r\n" + 
				"left join VerpacktEndproduktPaarung vep on vep.pkbez=pos.pkbez \r\n" + 
				"join  SorteVerpacktPaarung svp on svp.vkbez=vep.vkbez \r\n" + 
				"join EndproduktKartonagePaarung ekp on ekp.pkbez = vep.pkbez \r\n" + 
				"join Kartonagen kart on kart.verpID = ekp.verpID \r\n" + 
				"where ls.Datum>='" + fromDate + "' and ls.Datum <= '" + toDate + "' \r\n" + 
				"group by svp.skbez \r\n" + 
				"order by svp.skbez asc";*/
		Connection connection = dBConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String skbez = resultSet.getString(1);
				salesamounts.put(skbez, resultSet.getDouble(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return salesamounts;
	}
	
	public JSONObject getCampaignAmounts(String fromDate, String toDate) throws SQLException{
		Statement statement = null;
		ResultSet resultSet = null;
		JSONObject salesamounts = new  JSONObject();
		String sql= "select svp.SKbez, sum(Menge)"
				+ " from Aktionen akt"
				+ " join VerpacktEndproduktPaarung vep on vep.pkbez = akt.pkbez"
				+ " join SorteVerpacktPaarung svp on svp.vkbez=vep.vkbez"
				+ " where akt.AktionsDatum >='" + fromDate + "' and akt.AktionsDatum <= '" + toDate + "'"
				+ " group by svp.skbez" 
				+ " order by svp.skbez asc";
		/*String sql = "select svp.skbez, sum(pos.menge*kart.menge) From Lieferscheine ls \r\n" + 
				"join LS_Positionen pos on ls.ls_No = pos.LS_No \r\n" + 
				"left join VerpacktEndproduktPaarung vep on vep.pkbez=pos.pkbez \r\n" + 
				"join  SorteVerpacktPaarung svp on svp.vkbez=vep.vkbez \r\n" + 
				"join EndproduktKartonagePaarung ekp on ekp.pkbez = vep.pkbez \r\n" + 
				"join Kartonagen kart on kart.verpID = ekp.verpID \r\n" + 
				"where ls.Datum>='" + fromDate + "' and ls.Datum <= '" + toDate + "' \r\n" + 
				"group by svp.skbez \r\n" + 
				"order by svp.skbez asc";*/
		Connection connection = dBConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String skbez = resultSet.getString(1);
				salesamounts.put(skbez, resultSet.getDouble(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return salesamounts;
	}
}