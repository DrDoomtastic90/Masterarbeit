package dBConnections;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.glassfish.jersey.internal.util.collection.DataStructures;
import org.json.JSONException;
import org.json.JSONObject;

import errorHandler.UniqueConstraintException;
import serviceImplementation.Datastruct;
import serviceImplementation.Sorte;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class SorteDAO {
	DBConnection authenticationConnection = null;

	public SorteDAO(String passPhrase) throws ClassNotFoundException {
		authenticationConnection = BantelDBConnection.getInstance(passPhrase);
	}


	public SorteDAO() {
		authenticationConnection = BantelDBConnection.getInstance();
	}

	public Map<String, Sorte> getSorteMasterData() throws SQLException{
		Map<String, Sorte> sorteMap = new LinkedHashMap<String, Sorte>();
		Statement statement = null;
		ResultSet resultSet = null;
		Connection connection = authenticationConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(
				"SELECT ks.SKBez, ks.Bezeichnung, ks.MengePW, ks.MilchbedarfPW, ks.PrioritaetProd, ks.PrioritaetZut, ksZus.StueckelungWanne, ksZus.Sollbestand, ksZus.Laender, ksZus.Saisonalitaet, msp.MKBez from Kaesesorten ks join MilchSortePaarung msp on msp.SKBez=ks.SKBez join KaesesortenZusatz ksZus on ksZus.skbez=ks.skbez order by msp.MKBez asc, ks.SKBez asc;");
		while (resultSet.next()) {
			String skbez = resultSet.getString(1);
			String sorteBez = resultSet.getString(2);
			double kaesePW = resultSet.getDouble(3);
			double milchbedarfPW = resultSet.getDouble(4);
			int prioritaetProd = resultSet.getInt(5);
			int prioritaetZut = resultSet.getInt(6);
			int stueckelungWA = resultSet.getInt(7);
			double sollbestand = resultSet.getDouble(8);	
			String landArr = resultSet.getString(9).replaceAll("\\s","");
			landArr = landArr.replace("[", "");
			landArr = landArr.replace("]", "");
			ArrayList<String> laender = new ArrayList<String>(Arrays.asList(landArr.split(",")));
			Double milchbedarf = milchbedarfPW / kaesePW;
			double saisonalitaet = resultSet.getDouble(10);
			String mkbez = resultSet.getString(11);
			sorteMap.put(skbez, new Sorte(skbez, sorteBez, mkbez, milchbedarf, prioritaetProd, prioritaetZut, stueckelungWA, sollbestand, kaesePW, laender, saisonalitaet));
		}
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} finally {
		if (connection != null) {
			try {
				statement.close();
				resultSet.close();
				connection.close();
				connection = null;
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	/*for(String entry:sorteMap.keySet()) {
		sorteMap.get(entry).printSorte();
		
	}*/
	return sorteMap;
}

	public Map<String, String> getSortePKBezMapping() throws SQLException {
		Statement statement = null;
		ResultSet resultSet = null;
		Map<String, String> SortePKBezMap = new LinkedHashMap<String, String>();
		Connection connection = authenticationConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(
					"SELECT svp.SKBez, vep.PKBez from VerpacktEndproduktPaarung vep join SorteVerpacktPaarung svp on vep.VKBez = svp.VKBez order by vep.PKBez asc, svp.SKBez asc;");
			while (resultSet.next()) {
				String sorte = resultSet.getString(1);
				String pKBez = resultSet.getString(2);
				SortePKBezMap.put(pKBez, sorte);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					statement.close();
					resultSet.close();
					connection.close();
					connection = null;
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return SortePKBezMap;
	}
	

	public double getAmountPerPackage(String pKBez) throws UniqueConstraintException, SQLException {
		Statement statement = null;
		ResultSet resultSet = null;
		double amountPerPackage = 1;
		Connection connection = authenticationConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(
					"SELECT kart.Menge from Kartonagen kart join EndproduktKartonagePaarung ekp on ekp.VerpID = kart.VerpID where ekp.PKBez='"+ pKBez + "'");
			while (resultSet.next()) {
				amountPerPackage = resultSet.getDouble(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					statement.close();
					resultSet.close();
					connection.close();
					connection = null;
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return amountPerPackage;
	}

	public Map<String, Datastruct> getCustomerDataDaily(String pKBez, String kNo, String fromDate,
			String toDate/* , boolean nA */) throws UniqueConstraintException, SQLException {
		String sql = "SELECT ls.Datum, pos.Menge, pos.LS_No, pos.Pos_No from LS_Positionen pos join Lieferscheine ls on pos.LS_No = ls.LS_NO where pos.PKBez = '"
				+ pKBez + "' and ls.K_No = '" + kNo + "' order by ls.Datum asc";
		return getData(pKBez, sql, fromDate, toDate/* , nA */);
	}

	public Map<String, Datastruct> getDataDaily(String pKBez, String fromDate, String toDate)
			throws UniqueConstraintException, SQLException {
		String sql = "SELECT ls.Datum, pos.Menge, pos.LS_No, pos.Pos_No from LS_Positionen pos join Lieferscheine ls on pos.LS_No = ls.LS_NO where pos.PKBez = '"
				+ pKBez + "' and ls.Datum>='" + fromDate + "' and ls.Datum <= '" + toDate + "' order by ls.Datum asc";
		return getData(pKBez, sql, fromDate, toDate/* , nA */);
	}
	public Map<String, Double> getDirectSales(JSONObject configurations) throws ParseException, SQLException {
		Map<String, Double> directSalesMap = new LinkedHashMap<String, Double>();
		Statement statement = null;
		ResultSet resultSet = null;
		try {	
			String today = configurations.getJSONObject("data").getString("to");
			//String today = configurations.getJSONObject("data").getString("toDate");
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
			Calendar calendar = new GregorianCalendar(Locale.GERMAN);
			calendar.setTime(dateFormat.parse(today));
			calendar.add(Calendar.DAY_OF_MONTH, +7);
			int kw = calendar.get(Calendar.WEEK_OF_YEAR);
			calendar.set(Calendar.WEEK_OF_YEAR, kw); 
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			String fromDate = dateFormat.format(calendar.getTime()); 
			calendar.add(Calendar.DAY_OF_MONTH, +6);
			String toDate = dateFormat.format(calendar.getTime()); 	
			directSalesMap = getDirectSalesWeekly(fromDate, toDate);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return directSalesMap;
	}
	
	
	public Map<String, Double> getCampaigns(JSONObject configurations) throws ParseException {
		Map<String, Double> campaignMap = new LinkedHashMap<String, Double>();
		Statement statement = null;
		ResultSet resultSet = null;
	try {	
		//String today = configurations.getJSONObject("data").getString("toDate");
		String today = configurations.getJSONObject("data").getString("to");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setTime(dateFormat.parse(today));
		calendar.add(Calendar.DAY_OF_MONTH, +7);
		int kw = calendar.get(Calendar.WEEK_OF_YEAR);
		calendar.set(Calendar.WEEK_OF_YEAR, kw); 
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		String fromDate = dateFormat.format(calendar.getTime()); 
		calendar.add(Calendar.DAY_OF_MONTH, +6);
		String toDate = dateFormat.format(calendar.getTime()); 
		String sql = "select svp.skbez, sum(akt.Menge) from Aktionen akt join EndproduktKartonagePaarung ekp on akt.PKBez = ekp.PKBez join VerpacktEndproduktPaarung vep on vep.pkbez = akt.PKBez join SorteVerpacktPaarung svp on svp.vkbez=vep.vkbez join Kartonagen kart on ekp.VerpId = kart.VerpID where akt.ProduktionsDatum>='" + fromDate + "' and akt.ProduktionsDatum <= '" + toDate + "'  Group BY svp.sKBez order by svp.sKBez asc";
		Connection connection = authenticationConnection.checkConnectivity();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
				while (resultSet.next()) {
					String pKBez =resultSet.getString(1);
					double mengeStueck = resultSet.getDouble(2);
					campaignMap.put(pKBez, mengeStueck);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	/*for(String skbez: inventoryMap.keySet()) {
		System.out.println("SKBEZ: " + skbez + ": INV: " + inventoryMap.get(skbez));
	}*/
		return campaignMap;
	}
	
	public Map<String, Double> getSaisonality(JSONObject configurations) throws ParseException, SQLException {
		Map<String, Double> saisonMap = new LinkedHashMap<String, Double>();
		Statement statement = null;
		ResultSet resultSet = null;
		Connection connection = authenticationConnection.checkConnectivity();	
		String sql = "select saison.SKBez, saison.ProdPercentage from Saisonality saison order by saison.SKBez asc";

		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
				while (resultSet.next()) {
					String sKBez =resultSet.getString(1);
					double percentage =resultSet.getDouble(2);
					saisonMap.put(sKBez, percentage);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	/*for(String skbez: inventoryMap.keySet()) {
		System.out.println("SKBEZ: " + skbez + ": INV: " + inventoryMap.get(skbez));
	}*/
		return saisonMap;
	}
	
	public Map<String, Double> getInventory(Map<String, Sorte> sorteMap, JSONObject configurations) throws ParseException, SQLException {
		Map<String, Double> inventoryMap = new LinkedHashMap<String, Double>();
		String datumLetzteZaehlung = getDatumLetzteZaehlung(configurations);
		//String toDate = configurations.getJSONObject("data").getString("toDate");
		String toDate = configurations.getJSONObject("data").getString("to");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setTime(dateFormat.parse(datumLetzteZaehlung));
		calendar.add(Calendar.DAY_OF_MONTH, +1);
		String fromDate = dateFormat.format(calendar.getTime());
		Map<String, Double> zaehlungMap = getAktuelleZaehlung(datumLetzteZaehlung);
		Map<String, Double> verpacktMap = getStueckVerpacktSKBez(fromDate, toDate);
		Map<String, Double> verkaufMap = getSalesAmounts(fromDate, toDate); //getVerkaeufeSKBez(datumLetzteZaehlung, toDate);
		for(String skbez: sorteMap.keySet()) {
			double menge = 0;
			if (zaehlungMap.containsKey(skbez)){
				menge = menge + zaehlungMap.get(skbez);
			}
			if (verpacktMap.containsKey(skbez)){
				menge = menge + verpacktMap.get(skbez);
			}
			if (verkaufMap.containsKey(skbez)){
				menge = menge - verkaufMap.get(skbez);
			}
			inventoryMap.put(skbez, menge);
		}

		return inventoryMap;
	}
		
	private Map<String, Double> getStueckVerpacktSKBez(String fromDate, String toDate) throws SQLException {
		String sql = "Select svp.SKbez, sum(ve.Menge/pv.ProdFaktor) from VerpacktBestand vb \r\n" + 
				"join SorteVerpacktPaarung svp on svp.vkbez=vb.vkbez \r\n" + 
				"left join VerpackungsErfassung ve on ve.vkbez=vb.vkbez and ve.charge=vb.charge \r\n" + 
				"join VerpacktKartonagePaarung vkp on vkp.vkbez=svp.VKBez\r\n" + 
				"join ProdukteVerpackt pv on pv.vkbeZ=svp.vkbez\r\n" + 
				"where ve.Datum>= '" + fromDate + "' and ve.Datum <= '" + toDate + "' \r\n" + 
				"group by svp.SKbez \r\n" + 
				"order by svp.SKBez asc";
		/*String sql = "Select svp.SKbez, sum(ve.Menge) from VerpacktBestand vb join SorteVerpacktPaarung svp on svp.vkbez=vb.vkbez join VerpackungsErfassung ve on ve.vkbez=vb.vkbez and ve.charge=vb.charge where ve.Datum>= '" + fromDate + "' and ve.Datum <= '" + toDate + "' group by svp.SKbez order by svp.SKBez asc";*/
		Statement statement = null;
		ResultSet resultSet = null;
		Map<String, Double> verpacktMengen = new LinkedHashMap<String, Double>();
		Connection connection = authenticationConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while(resultSet.next()) {
				String vkbez = resultSet.getString(1);
				double menge = resultSet.getDouble(2);
				verpacktMengen.put(vkbez, menge);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return verpacktMengen;
	}
	
	private Map<String, Double> getVerkaeufeSKBez(String fromDate, String toDate) throws SQLException {
		Statement statement = null;
		ResultSet resultSet = null;
		Map<String, Double> lieferscheinMengen = new LinkedHashMap<String, Double>();
		String sql = "Select svp.SKBez, sum(pos.Menge * kart.Menge) from Lieferscheine ls join LS_Positionen pos on pos.LS_No = ls.LS_No join VerpacktEndproduktPaarung vep on vep.pkbez=pos.pkbez join EndproduktKartonagePaarung ekp on ekp.pkbez= vep.pkbez join Kartonagen kart on kart.verpID = ekp.verpID join SorteVerpacktPaarung svp on svp.vkbez=vep.vkbez where ls.datum>='" + fromDate + "' and ls.datum<='" + toDate + "' group by svp.skbez order by svp.skbez asc";
		Connection connection = authenticationConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while(resultSet.next()) {
				String skbez = resultSet.getString(1);
				double menge = resultSet.getDouble(2);
				lieferscheinMengen.put(skbez, menge);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lieferscheinMengen;
	}
	
	private String getDatumLetzteZaehlung(JSONObject configurations) throws ParseException {
		Statement statement = null;
		ResultSet resultSet = null;
		String letzteZaehlung = null;
		try {	
			//String toDate = configurations.getJSONObject("data").getString("toDate");
			String toDate = configurations.getJSONObject("data").getString("to");
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
			Calendar calendar = new GregorianCalendar(Locale.GERMAN);
			calendar.setTime(dateFormat.parse(toDate));
			calendar.add(Calendar.MONTH, -1);
			String fromDate = dateFormat.format(calendar.getTime()); 
			String sql = " Select ab.Datum from SorteVerpacktPaarung svp join GezaehlterBestand ab on ab.VKBez=svp.VKBez where ab.Datum = (Select max(Datum) from GezaehlterBestand where Datum>='" + fromDate + "' and Datum <= '" + toDate + "') Group BY svp.SKBez order by svp.SKBez asc";
			Connection connection = authenticationConnection.checkConnectivity();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				letzteZaehlung = resultSet.getString(1);
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return letzteZaehlung;
	}
		
	private Map<String, Double> getAktuelleZaehlung(String datumLetzteZaehlung) throws ParseException{
		Map<String, Double> inventoryMap = new LinkedHashMap<String, Double>();
		Statement statement = null;
		ResultSet resultSet = null;
	try {	
		String sql = "Select svp.SKBez, sum(ab.menge/pv.prodfaktor) from SorteVerpacktPaarung svp join GezaehlterBestand ab on ab.VKBez=svp.VKBez join VerpacktKartonagePaarung vkp on vkp.vkbez=svp.vkbez join produkteVerpackt pv on pv.vkbez=svp.vkbez where ab.Datum = '" + datumLetzteZaehlung + "' Group BY svp.SKBez order by svp.SKBez asc";
		Connection connection = authenticationConnection.checkConnectivity();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
				while (resultSet.next()) {
					String sKBez =resultSet.getString(1);
					double menge = resultSet.getDouble(2);
					inventoryMap.put(sKBez, menge);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return inventoryMap;
	}
	
	public Map<String, Double> getMengenUnverpackt(JSONObject configurations) throws ParseException, SQLException{
		String toDate = configurations.getJSONObject("data").getString("to");
		//String toDate = configurations.getJSONObject("data").getString("toDate");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setTime(dateFormat.parse(toDate));
		int kw = calendar.get(Calendar.WEEK_OF_YEAR);
		calendar.set(Calendar.WEEK_OF_YEAR, kw);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		String fromDate = dateFormat.format(calendar.getTime());
		Map<String, Double> unverpacktMap = getMengeUnverpacktDB(fromDate, toDate);
		Map<String, Double> directSalesMap = getDirectSalesProduction(fromDate, toDate);
		for(String skbez : unverpacktMap.keySet()) {
			if (directSalesMap.containsKey(skbez)) {
				unverpacktMap.put(skbez, unverpacktMap.get(skbez)- directSalesMap.get(skbez));
			}
		}
		return unverpacktMap;
	}
		private Map<String, Double> getMengeUnverpacktDB(String fromDate, String toDate) {
			Map<String, Double> unverpacktMap = new LinkedHashMap<String, Double>();
			Statement statement = null;
			ResultSet resultSet = null;
			try {	
				String sql = "select pp.skbez, sum(pp.menge*ks.mengePW) from Kaesesorten ks left join Produktionsbestand pb  on ks.skbeZ=pb.skbez join Produktionsplan pp on pp.charge=pb.charge and pp.skbez=pb.skbez \r\n" + 
						"where pb.VerarbeitungDatum>='" + fromDate + "' and pb.VerarbeitungDatum <= '" + toDate + "' group by pp.skbez order by pp.skbez asc";
				Connection connection = authenticationConnection.checkConnectivity();
					statement = connection.createStatement();
				resultSet = statement.executeQuery(sql);
					while (resultSet.next()) {
						String sKBez =resultSet.getString(1);
						double menge = resultSet.getDouble(2);
						unverpacktMap.put(sKBez, menge);
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			return unverpacktMap;
		}
	

	public Map<String, Datastruct> getData(String pKBez, String sql, String fromDateString, String toDateString)
			throws UniqueConstraintException, SQLException {
		Statement statement = null;
		ResultSet resultSet = null;
		double amountPerPackage = getAmountPerPackage(pKBez);
		// Map<String, Datastruct> dailyValues =
		// getMapOfDates(LocalDate.of(LocalDate.now().getYear() - 3, 1, 1),
		// LocalDate.now()/*, nA*/);
		LocalDate fromDate = LocalDate.parse(fromDateString);
		LocalDate toDate = LocalDate.parse(toDateString);
		Map<String, Datastruct> dailyValues = getMapOfDates(fromDate, toDate);

		boolean containsPKBez = false;

		Connection connection = authenticationConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				containsPKBez = true;
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date datum = dateFormat.parse(resultSet.getString(1));
				String strDatum = dateFormat.format(datum);
				double menge = resultSet.getDouble(2);
				//System.out.println("Datum: " + strDatum + ", Menge einzeln: " + menge + ",AmountPP: " + amountPerPackage);
				menge = menge * amountPerPackage;
				//System.out.println("Menge PP: " + menge);
				String lSNo = resultSet.getString(3);
				int posNo = resultSet.getInt(4);
				if (dailyValues.get(strDatum) != null) {
					double mengeAlt = dailyValues.get(strDatum).getMenge();
					// 0.001 indicates NA if not filled (Workaround for double field)
					if (mengeAlt == 0.001) {
						mengeAlt = 0;
					}
					dailyValues.get(strDatum).setMenge(menge + mengeAlt);
				}
			}

		} catch (SQLException | ParseException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					statement.close();
					resultSet.close();
					connection.close();
					connection = null;
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		if (containsPKBez) {
			return dailyValues;
		} else {
			return null;
		}
	}

	public void update() throws UniqueConstraintException, SQLException {
		Statement statement = null;
		ResultSet resultSet = null;
		Map<String, String> update = new LinkedHashMap<String, String>();
		String sql = "Select LS_No, Datum from Lieferscheine order by Datum asc;";
		Connection connection = authenticationConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String strDatum = resultSet.getString(2);
				String lSNoAlt = resultSet.getString(1);
				String[] strArr = lSNoAlt.split("L");
				String lSNo = strDatum.substring(0, 4) + "L" + strArr[1];
				update.put(lSNo, "L" + strArr[1]);
			}
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		} finally {
			if (connection != null) {
				try {
					statement.close();
					resultSet.close();
					connection.close();
					connection = null;
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		sql = "Update LS_Positionen SET LS_No = ? where LS_No = ?";
		connection = authenticationConnection.checkConnectivity();
		for (String lSNo : update.keySet()) {
			try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
				pstmt.setString(1, lSNo);
				pstmt.setString(2, update.get(lSNo));
				pstmt.executeUpdate();
			} catch (SQLException e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
	}

	public static Map<String, Datastruct> getMapOfDates(LocalDate startDate, LocalDate endDate/* , boolean nA */) {
		Map<String, Datastruct> dailyValues = new LinkedHashMap<String, Datastruct>();
		while (!startDate.isAfter(endDate)) {
			Datastruct datastruct = null;
			/*
			 * if (nA) { datastruct = new Datastruct(startDate.toString(), 0.001, false); }
			 * else {
			 */
			datastruct = new Datastruct(startDate.toString(), 0, false);
			// }
			dailyValues.put(startDate.toString(), datastruct);
			startDate = startDate.plusDays(1);

		}
		return dailyValues;
	}

	public List<String> getAllKNo() throws UniqueConstraintException, SQLException {
		List<String> kNoList = new ArrayList<String>();
		Statement statement = null;
		ResultSet resultSet = null;

		Connection connection = authenticationConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(" select distinct K_No from Lieferscheine order by K_No asc");
			while (resultSet.next()) {
				String kNo = resultSet.getString(1);
				kNoList.add(kNo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					statement.close();
					resultSet.close();
					connection.close();
					connection = null;
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return kNoList;
	}

	public ArrayList<String> getAllPKBez() throws SQLException {
		ArrayList<String> pkBezList = new ArrayList<String>();
		pkBezList = getPKBezList();
		return pkBezList;
	}

	private ArrayList<String> getPKBezList() throws SQLException {
		ArrayList<String> pkBezList = new ArrayList<String>();
		Statement statement = null;
		ResultSet resultSet = null;
		Connection connection = authenticationConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT PKBez from Endprodukte order by PKBez asc");
			while (resultSet.next()) {
				pkBezList.add(resultSet.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					statement.close();
					resultSet.close();
					connection.close();
					connection = null;
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return pkBezList;
	}
	
	 public static String dataToCSVString(List<Datastruct> dataSet){ 
		 StringBuilder csvString = new StringBuilder(); 
		 csvString.append("Datum");
		 csvString.append(","); 
		 csvString.append("Menge"); 
		 csvString.append("\n"); 
		 for(Datastruct entry : dataSet) { 
			 String datum = entry.getDatum(); 
			 double menge = entry.getMenge(); csvString.append(datum); 
			 csvString.append(",");
		 } 
		 csvString.append("\n"); 
	 //System.out.println(csvString); 
		 return csvString.toString(); 
	}
	 
	
	public Map<String, Map<String, Datastruct>> getProductDataDailyPast(JSONObject configurations) throws JSONException, ParseException, SQLException{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		//Date datum = dateFormat.parse(configurations.getJSONObject("data").getString("toDate"));
		Date datum = dateFormat.parse(configurations.getJSONObject("data").getString("to"));
		calendar.setTime(datum); 
		calendar.add(Calendar.YEAR, -1);
		int kw = calendar.get(Calendar.WEEK_OF_YEAR);
		calendar.set(Calendar.WEEK_OF_YEAR, kw);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		datum = calendar.getTime();
		String fromDate = dateFormat.format(datum);
		calendar.add(Calendar.DAY_OF_MONTH, + 13);
		datum = calendar.getTime();
		String toDate = dateFormat.format(datum);
		System.out.println("FROM: " + fromDate);
		System.out.println("TO : " + toDate);
		ArrayList<String> pkBezList = getAllPKBez();
		Map<String, Map<String, Datastruct>> dailyDataPast = new LinkedHashMap<String, Map<String, Datastruct>>();
		for (String pKBez : pkBezList) {
			try {
				dailyDataPast.put(pKBez, getDataDaily(pKBez, fromDate, toDate));

			} catch (UniqueConstraintException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return dailyDataPast;
	}
	
	public Map<String, Map<String, Double>> getCurrentSalesAmountsDaily(JSONObject configurations) throws JSONException, ParseException, SQLException {
		Map<String, Map<String, Double>> salesAmountsSorteAllDays = new LinkedHashMap<String, Map<String, Double>>();
		//int consideredPeriods = configurations.getJSONObject("parameters").getInt("considerPeriods");
		//String fromDate = configurations.getJSONObject("data").getString("fromDate");	
		//String toDate = configurations.getJSONObject("data").getString("toDate");		
		String fromDate = configurations.getJSONObject("data").getString("from");	
		String toDate = configurations.getJSONObject("data").getString("to");		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setTime(dateFormat.parse(toDate));
		calendar.add(Calendar.DAY_OF_MONTH, + 1);
		toDate = dateFormat.format(calendar.getTime());
		calendar.setTime(dateFormat.parse(fromDate)); 
		while(!fromDate.equals(toDate)) {
			salesAmountsSorteAllDays.put(fromDate, calculateSalesAmounts(fromDate, fromDate));
			calendar.add(Calendar.DAY_OF_MONTH, + 1);
			fromDate = dateFormat.format(calendar.getTime());
		}
		return salesAmountsSorteAllDays;
	}
	
	public Map<String, Map<String, Double>> getCurrentSalesAmountsWeekly(JSONObject configurations) throws JSONException, ParseException, SQLException {
		Map<String, Map<String, Double>> salesAmountsSorteAllWeeks = new LinkedHashMap<String, Map<String, Double>>();
		//int consideredPeriods = configurations.getJSONObject("parameters").getInt("considerPeriods");
		//String fromDate = configurations.getJSONObject("data").getString("fromDate");	
		//String toDate = configurations.getJSONObject("data").getString("toDate");		
		String fromDate = configurations.getJSONObject("data").getString("from");	
		String toDate = configurations.getJSONObject("data").getString("to");	
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setTime(dateFormat.parse(toDate));
		dateFormat = new SimpleDateFormat("yy"); 
		String year = dateFormat.format(calendar.getTime());
		dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		int kwTo = calendar.get(Calendar.WEEK_OF_YEAR);
		calendar.setTime(dateFormat.parse(fromDate)); 
		calendar.add(Calendar.DAY_OF_MONTH, - 7);
		int kwFrom = calendar.get(Calendar.WEEK_OF_YEAR);
		int weekCounter = 0;
		//int kw = kwTo-kwFrom;
		//for(int weekCounter = 0; weekCounter < kw; weekCounter++) {
		calendar.set(Calendar.WEEK_OF_YEAR, kwTo); 
		while(kwFrom!=kwTo) {	
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			fromDate = dateFormat.format(calendar.getTime());
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			toDate = dateFormat.format(calendar.getTime());
			salesAmountsSorteAllWeeks.put(kwTo + "-" + year,  calculateSalesAmounts(fromDate, toDate));
			//salesAmountsSorteAllWeeks.put("demandWeekM"+weekCounter,  calculateSalesAmounts(fromDate, toDate));
			calendar.add(Calendar.DAY_OF_MONTH, -7);
			kwTo = calendar.get(Calendar.WEEK_OF_YEAR);
			weekCounter =+ 1;
			

		}
		return salesAmountsSorteAllWeeks;
	}
		
		/*Map<String, Map<String, Double>> salesAmountsSorteAllWeeks = new LinkedHashMap<String, Map<String, Double>>();
		int consideredPeriods = configurations.getJSONObject("parameters").getInt("considerPeriods");
		String toDate = configurations.getJSONObject("data").getString("toDate");		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		Date calcDate = dateFormat.parse(toDate);
		calendar.setTime(calcDate); 
		int kw = calendar.get(Calendar.WEEK_OF_YEAR);
		for(int weekCounter = 0; weekCounter < consideredPeriods; weekCounter++) {
			calendar.set(Calendar.WEEK_OF_YEAR, kw); 
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			calcDate = calendar.getTime();
			String fromDate = dateFormat.format(calcDate);
			calendar.add(Calendar.DAY_OF_MONTH, + 6);
			calcDate = calendar.getTime();
			toDate = dateFormat.format(calcDate);
			salesAmountsSorteAllWeeks.put("demandWeekM"+weekCounter,  calculateSalesAmounts(fromDate, toDate));
			kw=kw-1;
		}
		return salesAmountsSorteAllWeeks;
	}*/
	
	public Map<String, Map<String, Double>> getPastSalesAmountsDaily(JSONObject configurations) throws JSONException, ParseException, SQLException {
		Map<String, Map<String, Double>> salesAmountsSorteAllDays = new LinkedHashMap<String, Map<String, Double>>();
		//int consideredPeriods = configurations.getJSONObject("parameters").getInt("considerPeriods");
		//String fromDate = configurations.getJSONObject("data").getString("fromDate");	
		//String toDate = configurations.getJSONObject("data").getString("toDate");		
		String fromDate = configurations.getJSONObject("data").getString("from");	
		String toDate = configurations.getJSONObject("data").getString("to");			
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setTime(dateFormat.parse(toDate));
		calendar.add(Calendar.YEAR, -1);
		calendar.add(Calendar.DAY_OF_MONTH, + 1);
		toDate = dateFormat.format(calendar.getTime());
		calendar.setTime(dateFormat.parse(fromDate)); 
		calendar.add(Calendar.YEAR, -1);
		fromDate = dateFormat.format(calendar.getTime());
		//int kw = calendar.get(Calendar.WEEK_OF_YEAR);
		//calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		//calcDate = calendar.getTime();
		//String dateStr = dateFormat.format(calcDate);
		while(!fromDate.equals(toDate)) {
			salesAmountsSorteAllDays.put(fromDate, calculateSalesAmounts(fromDate, fromDate));
			calendar.add(Calendar.DAY_OF_MONTH, + 1);
			fromDate = dateFormat.format(calendar.getTime());
		}
		return salesAmountsSorteAllDays;
	}
	

	
	public Map<String, Map<String, Double>> getPastSalesAmountsWeekly(JSONObject configurations) throws JSONException, ParseException, SQLException {
		Map<String, Map<String, Double>> salesAmountsSorteAllWeeks = new LinkedHashMap<String, Map<String, Double>>();
		//int consideredPeriods = configurations.getJSONObject("parameters").getInt("considerPeriods");
		//String fromDate = configurations.getJSONObject("data").getString("fromDate");	
		//String toDate = configurations.getJSONObject("data").getString("toDate");		
		String fromDate = configurations.getJSONObject("data").getString("from");	
		String toDate = configurations.getJSONObject("data").getString("to");	
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setTime(dateFormat.parse(toDate)); 
		calendar.add(Calendar.YEAR, -1);
		int kwTo = calendar.get(Calendar.WEEK_OF_YEAR);
		dateFormat = new SimpleDateFormat("yy"); 
		String year = dateFormat.format(calendar.getTime());
		dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		calendar.setTime(dateFormat.parse(fromDate)); 
		calendar.add(Calendar.YEAR, -1);
		calendar.add(Calendar.DAY_OF_MONTH, - 7);
		int kwFrom = calendar.get(Calendar.WEEK_OF_YEAR);
		
		int weekCounter = 0;
		//int kw = kwTo-kwFrom;
		//for(int weekCounter = 0; weekCounter < kw; weekCounter++) {
		calendar.set(Calendar.WEEK_OF_YEAR, kwTo); 
		while(kwFrom!=kwTo) {	
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			fromDate = dateFormat.format(calendar.getTime());
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			toDate = dateFormat.format(calendar.getTime());
			//salesAmountsSorteAllWeeks.put("demandWeekP"+weekCounter,  calculateSalesAmounts(fromDate, toDate));
			salesAmountsSorteAllWeeks.put(kwTo + "-" + year,  calculateSalesAmounts(fromDate, toDate));
			calendar.add(Calendar.DAY_OF_MONTH, -7);
			kwTo = calendar.get(Calendar.WEEK_OF_YEAR);
			weekCounter =+ 1;
			

		}
		return salesAmountsSorteAllWeeks;
	}
		

	private Map<String, Double>  calculateSalesAmounts(String fromDate, String toDate) throws SQLException {
		Map<String, Double> salesAmountsSorteSingleWeek = getSalesAmounts(fromDate, toDate);
		Map<String, Double> campaignAmountsSorteSingleWeek = getCampaignsAmountsWeekly(fromDate, toDate);
		Map<String, Double> directSalesSorteSingleWeek = getDirectSalesWeekly(fromDate, toDate);
		Map<String, Double> deliveryShortageSingleWeek = getDeliveryShortage(fromDate, toDate);
		for(String skbez : salesAmountsSorteSingleWeek.keySet()) {
			if(campaignAmountsSorteSingleWeek.containsKey(skbez)) {
				salesAmountsSorteSingleWeek.put(skbez, salesAmountsSorteSingleWeek.get(skbez) - campaignAmountsSorteSingleWeek.get(skbez));
			}
			if(deliveryShortageSingleWeek.containsKey(skbez)) {
				salesAmountsSorteSingleWeek.put(skbez, salesAmountsSorteSingleWeek.get(skbez) + deliveryShortageSingleWeek.get(skbez));
			}
			if(directSalesSorteSingleWeek.containsKey(skbez)) {
				salesAmountsSorteSingleWeek.put(skbez, salesAmountsSorteSingleWeek.get(skbez) - directSalesSorteSingleWeek.get(skbez));
			}
		}
		return salesAmountsSorteSingleWeek;
	}
	
	private Map<String, Double> getSalesAmounts(String fromDate, String toDate) throws SQLException{
		Statement statement = null;
		ResultSet resultSet = null;
		Map<String, Double> salesamounts = new  LinkedHashMap<String, Double>();
		String sql= "select svp.skbez, sum(pos.menge * kart.menge/pv.ProdFaktor) As Menge From Lieferscheine ls \r\n" + 
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
		Connection connection = authenticationConnection.checkConnectivity();
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
	
	private Map<String, Double> getCampaignsAmountsWeekly(String fromDate, String toDate) throws SQLException{
		Statement statement = null;
		ResultSet resultSet = null;
		Map<String, Double> salesamounts = new  LinkedHashMap<String, Double>();
		String sql = "\r\n" + 
				"select svp.skbez, sum(akt.menge/pv.ProdFaktor) From Aktionen akt \r\n" + 
				"join VerpacktEndproduktPaarung vep on vep.pkbez=akt.pkbez \r\n" + 
				"join  SorteVerpacktPaarung svp on svp.vkbez=vep.vkbez \r\n" + 
				"join EndproduktKartonagePaarung ekp on ekp.pkbez = vep.pkbez \r\n" + 
				"join Kartonagen kart on kart.verpID = ekp.verpID \r\n" + 
				"join ProdukteVerpackt pv on pv.vkbez = vep.vkbez \r\n" + 
				"where akt.LieferDatum>='" + fromDate + "' and akt.LieferDatum <= '" + toDate + "' \r\n" + 
				"group by svp.skbez\r\n" + 
				"order by svp.skbez asc;";
		Connection connection = authenticationConnection.checkConnectivity();
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
	
	private Map<String, Double> getDirectSalesWeekly(String fromDate, String toDate) throws SQLException{
		Statement statement = null;
		ResultSet resultSet = null;
		Map<String, Double> salesamounts = new  LinkedHashMap<String, Double>();
		String sql = "\r\n" + 
				"select dv.skbez, sum(dv.menge) From DirektVerkaeufe dv \r\n" + 
				"where dv.VerkDatum>='" + fromDate + "' and dv.VerkDatum <= '" + toDate + "' \r\n" + 
				"group by dv.skbez\r\n" + 
				"order by dv.skbez asc";
		Connection connection = authenticationConnection.checkConnectivity();
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
	
	private Map<String, Double> getDirectSalesProduction(String fromDate, String toDate) throws SQLException{
		Statement statement = null;
		ResultSet resultSet = null;
		Map<String, Double> productionAmounts = new  LinkedHashMap<String, Double>();
		String sql = "select dv.skbez, sum(dv.Menge) from DirektVerkaeufe dv where dv.ProdDatum>='" + fromDate + "' and dv.ProdDatum <= '" + toDate + "'  Group BY dv.sKBez order by dv.sKBez asc";
		Connection connection = authenticationConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
				while (resultSet.next()) {
					String skbez =resultSet.getString(1);
					double mengeStueck = resultSet.getDouble(2);
					productionAmounts.put(skbez, mengeStueck);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	/*for(String skbez: inventoryMap.keySet()) {
		System.out.println("SKBEZ: " + skbez + ": INV: " + inventoryMap.get(skbez));
	}*/
		return productionAmounts;
	}
	
	private Map<String, Double> getDeliveryShortage(String fromDate, String toDate) throws SQLException{
		Statement statement = null;
		ResultSet resultSet = null;
		Map<String, Double> shortage = new  LinkedHashMap<String, Double>();
		String sql = "\r\n" + 
				"select svp.skbez, sum(fm.Fehlmenge) from Fehlmengen fm "
				+ "join VerpacktEndproduktPaarung vep on vep.pkbez=fm.pkbez join SorteVerpacktPaarung svp on svp.vkbez=vep.vkbez "
				+ "where fm.Datum>='" + fromDate + "' and fm.Datum<='" + toDate + "' "
				+ "group by svp.skbez "
				+ "order by svp.skbez";
		Connection connection = authenticationConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String skbez = resultSet.getString(1);
				shortage.put(skbez, resultSet.getDouble(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return shortage;
	}
	
	public Map<String, Map<String, Datastruct>> getProductDataDaily(JSONObject configurations) throws JSONException, SQLException {
		//String fromDate = configurations.getJSONObject("data").getString("fromDate");	
		//String toDate = configurations.getJSONObject("data").getString("toDate");		
		String fromDate = configurations.getJSONObject("data").getString("from");	
		String toDate = configurations.getJSONObject("data").getString("to");	
		ArrayList<String> pkBezList = getAllPKBez();
		Map<String, Map<String, Datastruct>> dailyData = new LinkedHashMap<String, Map<String, Datastruct>>();
		for (String pKBez : pkBezList) {
			try {
				dailyData.put(pKBez, getDataDaily(pKBez, fromDate, toDate));

			} catch (UniqueConstraintException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return dailyData;
	}

}