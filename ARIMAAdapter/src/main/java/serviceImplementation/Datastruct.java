package serviceImplementation;

public class Datastruct {
	String datum;
	String weekDayDummies;
	String monthDummies;
	String easterDummy;
	Double menge;
	boolean aktion;
	
	public Datastruct() {
		setMenge(0.0);
	}
	
	public Datastruct(String datum, double menge, boolean aktion){
		setAktion(aktion);
		setDatum(datum);
		setMenge(menge);
	}

	public String getDatum() {
		return datum;
	}

	public void setDatum(String datum) {
		this.datum = datum;
	}

	public Boolean getAktion() {
		return aktion;
	}

	public void setAktion(Boolean aktion) {
		this.aktion = aktion;
	}

	public Double getMenge() {
		return menge;
	}

	public void setMenge(Double menge) {
		this.menge = menge;
	}
	
	public String getWeekDayDummies() {
		return weekDayDummies;
	}
	public void setWeekDayDummies(String weekDayDummies) {
		this.weekDayDummies = weekDayDummies;
	}
	
	public String getMonthDummies() {
		return monthDummies;
	}
	
	public void setMonthDummies(String monthDummies) {
		this.monthDummies = monthDummies;
	}
	
	public String getEasterDummy() {
		return easterDummy;
	}
	
	public void setEasterDummy(String easterDummy) {
		this.easterDummy = easterDummy;
	}
	
	public static String writeAttributeNamesAsCSVString() {		
		StringBuilder csvStringBuilder = new StringBuilder();
		csvStringBuilder.append("Menge");
		//weekdayDummies
		csvStringBuilder.append(",");
		csvStringBuilder.append("Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday");
		//MonthDummies
		csvStringBuilder.append(",");
		csvStringBuilder.append("January,February,March,April,May,June,July,August,September,October,November,December");
		//EasterDummy
		csvStringBuilder.append(",");
		csvStringBuilder.append("Easter");
		csvStringBuilder.append("\n");
		
		return csvStringBuilder.toString();
	}
	
	public String writeAttributeValuesAsCSVString() {		
		StringBuilder csvStringBuilder = new StringBuilder();

		csvStringBuilder.append(menge);
		csvStringBuilder.append(",");
		csvStringBuilder.append(weekDayDummies);
		csvStringBuilder.append(",");
		csvStringBuilder.append(monthDummies);
		csvStringBuilder.append(",");
		csvStringBuilder.append(easterDummy);
		csvStringBuilder.append("\n");
		
		return csvStringBuilder.toString();
	}
}
