package dBConnections;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import serviceImplementation.Milk;


public class MilkDAO extends DAO {

	public MilkDAO(String sourcePath, String pw) throws ClassNotFoundException, SQLException {
		super(sourcePath, pw);
	}


	
	public Map<String, Milk> getMilkData(JSONObject configurations) {
		Map<String, Milk> milkMap = new LinkedHashMap<String, Milk>();
		Statement statement = null;
		ResultSet resultSet = null;
	try {	
		//String strDate = configurations.getJSONObject("data").getString("toDate");
		String strDate = configurations.getJSONObject("data").getString("to");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setTime(dateFormat.parse(strDate)); 
		calendar.add(Calendar.DAY_OF_MONTH, +7);
		int kw = calendar.get(Calendar.WEEK_OF_YEAR);
		calendar.set(Calendar.WEEK_OF_YEAR, kw); 
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		String fromDate = dateFormat.format(calendar.getTime());
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		String toDate = dateFormat.format(calendar.getTime()); 
		String sql = " Select mb.MKBez, m.Bezeichnung, sum(mb.Milchmenge), mb.Land from MilchSorten m join Milchbestand mb on m.mkbez=mb.mkbez where mb.Datum>='" + fromDate + "' and mb.Datum <= '" + toDate + "' Group by m.mkbez, mb.land order by mb.MKBez asc";
		statement = getConnection().createStatement();
			resultSet = statement.executeQuery(sql);
				while (resultSet.next()) {
					String mKBez =resultSet.getString(1);
					String bezeichnung =resultSet.getString(2);
					double literVerfügbar =resultSet.getDouble(3);
					String land =resultSet.getString(4);
					milkMap.put(mKBez+land, new Milk(mKBez, bezeichnung, literVerfügbar/*, 0*/, land));
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	/*for(String skbez: inventoryMap.keySet()) {
		System.out.println("SKBEZ: " + skbez + ": INV: " + inventoryMap.get(skbez));
	}*/
		return milkMap;
	}
}
