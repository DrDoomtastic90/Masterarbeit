package dBConnections;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import errorHandler.UniqueConstraintException;
import serviceImplementation.Datastruct;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class PreparationDAO {
	DBConnection dBConnection = null;

	public PreparationDAO(String passPhrase) throws ClassNotFoundException {
		dBConnection = BantelDBConnection.getInstance(passPhrase);
	}


	public PreparationDAO() {
		dBConnection = BantelDBConnection.getInstance();
	}

	private boolean checkAktion(String lSNo, int posNo) throws UniqueConstraintException, SQLException {
		Statement statement = null;
		ResultSet resultSet = null;
		boolean isAktion = false;
		Connection connection = dBConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT count(*) from Aktionen where LS_No = '" + lSNo
					+ "' and Pos_No = '" + Integer.toString(posNo) + "'");
			while (resultSet.next()) {
				int occurence = resultSet.getInt(1);
				if (occurence < 0 || occurence > 1) {
					System.out.println("ERROR");
				} else if (occurence == 1) {
					isAktion = true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					statement.close();
					resultSet.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return isAktion;
	}

	public Map<String, String> getSortePKBezMapping() throws SQLException {
		Statement statement = null;
		ResultSet resultSet = null;
		Map<String, String> SortePKBezMap = new LinkedHashMap<String, String>();
		Connection connection = dBConnection.checkConnectivity();
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
		Connection connection = dBConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(
					"SELECT (kart.Menge/pv.ProdFaktor) as Menge "
					+ "from Kartonagen kart "
					+ "join EndproduktKartonagePaarung ekp on ekp.verpID=kart.verpID "
					+ "join Endprodukte ep on ep.pkbez= ekp.pkbez "
					+ "join VerpacktEndproduktPaarung vep on vep.pkbez=ep.pkbez "
					+ "join ProdukteVerpackt pv on pv.vkbez=vep.vkbez "
					+ "where ep.PKBez='" + pKBez + "'");
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
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return amountPerPackage;
	}

	
	public Map<String, Double> getCustomerDataDaily(String pKBez, String kNo, String fromDate, String toDate/*, boolean nA*/) throws UniqueConstraintException, SQLException {
		String sql = "SELECT ls.Datum, pos.Menge, pos.LS_No, pos.Pos_No from LS_Positionen pos join Lieferscheine ls on pos.LS_No = ls.LS_NO where pos.PKBez = '"
				+ pKBez + "' and ls.K_No = '" + kNo + "' order by ls.Datum asc";
		return getData(pKBez, sql, fromDate, toDate/*, nA*/);
	}

	public Map<String, Double> getDataDaily(String pKBez, String fromDate, String toDate/*, boolean nA*/) throws UniqueConstraintException, SQLException {
		String sql = "SELECT ls.Datum, pos.Menge, pos.LS_No, pos.Pos_No from LS_Positionen pos join Lieferscheine ls on pos.LS_No = ls.LS_NO where pos.PKBez = '"
				+ pKBez + "' and ls.Datum>='" + fromDate + "' and ls.Datum <= '" + toDate + "' order by ls.Datum asc";
		return getData(pKBez, sql, fromDate, toDate/*, nA*/);
	}

	public Map<String, Double> getData(String pKBez, String sql, String fromDateString, String toDateString) throws UniqueConstraintException, SQLException {
		Statement statement = null;
		ResultSet resultSet = null;
		double amountPerPackage = getAmountPerPackage(pKBez);
		boolean isAktion = false;
		//Map<String, Datastruct> dailyValues = getMapOfDates(LocalDate.of(LocalDate.now().getYear() - 3, 1, 1),
		//		LocalDate.now()/*, nA*/);
		LocalDate fromDate = LocalDate.parse(fromDateString);
		LocalDate toDate = LocalDate.parse(toDateString);
		Map<String, Double> dailyValues = getMapOfDatesWithinTimespan(fromDate, toDate);
				
		boolean containsPKBez = false;
		Connection connection = dBConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				containsPKBez = true;
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date datum = dateFormat.parse(resultSet.getString(1));
				String strDatum = dateFormat.format(datum);
				double menge = resultSet.getDouble(2) * amountPerPackage;
				String lSNo = resultSet.getString(3);
				int posNo = resultSet.getInt(4);
				//isAktion = checkAktion(lSNo, posNo);
				if (dailyValues.get(strDatum) != null) {
					double mengeAlt = dailyValues.get(strDatum);
					dailyValues.put(strDatum, (menge + mengeAlt));
				}
			}

		} catch (SQLException | ParseException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					statement.close();
					resultSet.close();
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
		Connection connection = dBConnection.checkConnectivity();
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
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		sql = "Update LS_Positionen SET LS_No = ? where LS_No = ?";
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

	public static Map<String, Double> getMapOfDatesWithinTimespan(LocalDate startDate, LocalDate endDate/*, boolean nA*/) {
		Map<String, Double> mapOfDates = new LinkedHashMap<String, Double>();
		while (!startDate.isAfter(endDate)) {
			mapOfDates.put(startDate.toString(), 0.);
			startDate = startDate.plusDays(1);

		}
		return mapOfDates;
	}

	public List<String> getAllKNo() throws UniqueConstraintException, SQLException {
		List<String> kNoList = new ArrayList<String>();
		Statement statement = null;
		ResultSet resultSet = null;
		Connection connection = dBConnection.checkConnectivity();
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
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return kNoList;
	}
	
	public ArrayList<String> getAllPKBez() throws SQLException{
		ArrayList<String> pkBezList = new ArrayList<String>();
		pkBezList = getPKBezList();
		return pkBezList;
	}
	private ArrayList<String> getPKBezList() throws SQLException{
		ArrayList<String> pkBezList = new ArrayList<String>();
		Statement statement = null;
		ResultSet resultSet = null;
		Connection connection = dBConnection.checkConnectivity();
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT PKBez from Endprodukte order by PKBez asc");
			while(resultSet.next()) {
				pkBezList.add(resultSet.getString(1));
            }
		} 
		catch (SQLException e) {			
			e.printStackTrace();
		}finally {
			if(connection!=null) {
				try {
					statement.close();
					resultSet.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}				
			}
		}
		return pkBezList;
	}
	public static String dataToCSVString(List<Datastruct> dataSet, boolean dayDummies, boolean campaigns){
		StringBuilder csvString = new StringBuilder();	
		csvString.append("Datum");
		csvString.append(",");
		csvString.append("Menge");
		if(dayDummies) {
			csvString.append(",Montag, Dienstag, Mittwoch, Donnerstag, Freitag, Samstag, Sonntag");
		}
		if(campaigns) {
			csvString.append(",Aktion");
		}
		csvString.append("\n");
		for (Datastruct entry : dataSet) {
		    String datum = entry.getDatum();
		    double menge = entry.getMenge();
		    csvString.append(datum);
		    csvString.append(",");
		    csvString.append(menge);
		    if(dayDummies) {
		    	csvString.append(",");
		    	//http://www.java2s.com/Tutorials/Java/Data_Type_How_to/Date/Get_day_of_week_int_value_and_String_value.html
		    	LocalDate localDate = LocalDate.parse(datum);
		    	int weekday = localDate.getDayOfWeek().getValue();
		    	boolean first = true;
		    	for(int i = 1; i<8;i++) {
		    		if(first) {
		    			first = false;
		    		}
		    		else {
		    			csvString.append(",");
		    		}
		    		if(i == weekday) {
		    			csvString.append("1");
		    		}
		    		else {
		    			csvString.append("0");
		    		}
		    	}
		    }
		    if(campaigns) {
		    	csvString.append(",");
		    	csvString.append(entry.getAktion()? 1:0);
		   	}
		    csvString.append("\n");
		}
		//System.out.println(csvString);
		return  csvString.toString();
	}
	
	public Map<String,Map<String,Double>> getDataDailyPerWeek(String KNo, JSONObject configurations) throws JSONException, SQLException, ParseException {
		String fromDate = configurations.getJSONObject("data").getString("from");
		String toDate = configurations.getJSONObject("data").getString("to");
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
		cal.setTime(dateformat.parse(fromDate));
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		fromDate = dateformat.format(cal.getTime());
		ArrayList<String> pkBezList = getAllPKBez();
		Map<String,Map<String,Double>> dailyData = new LinkedHashMap<String, Map<String,Double>>();
		for(String pKBez: pkBezList) {
			try {
				if(KNo.length()>0) {
					dailyData.put(pKBez, getCustomerDataDaily(pKBez, KNo, fromDate, toDate/*, nA*/));
				} else {
					dailyData.put(pKBez, getDataDaily(pKBez, fromDate, toDate/*, nA*/));
				}
				
			} catch (UniqueConstraintException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return dailyData;
	}

	
	
}