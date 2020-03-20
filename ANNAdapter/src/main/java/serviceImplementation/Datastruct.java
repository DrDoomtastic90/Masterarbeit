package serviceImplementation;

public class Datastruct {
	String datum;
	Double menge;
	boolean aktion;

	
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
}
