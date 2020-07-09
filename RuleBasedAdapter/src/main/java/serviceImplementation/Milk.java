package serviceImplementation;

import org.json.JSONException;
import org.json.JSONObject;

public class Milk {

	String milktype;
	String bezeichnung;
	double literVerfuegbar;
	//double literVerwendet;
	String land;
	
	public Milk() {
		this.milktype = "";
		this.bezeichnung= "";
		this.literVerfuegbar= 0;
		//this.literVerwendet= 0;
		this.land= "";
	}
	
	public Milk(String milktype, String bezeichnung, double literVerfügbar/*, double literVerwendet*/, String land) {
		this.milktype = milktype;
		this.bezeichnung= bezeichnung;
		this.literVerfuegbar= literVerfügbar;
		//this.literVerwendet= literVerwendet;
		this.land= land;
	}

	public String getmKBez() {
		return milktype;
	}

	public void setmKBez(String milktype) {
		this.milktype = milktype;
	}

	public String getBezeichnung() {
		return bezeichnung;
	}

	public void setBezeichnung(String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}

	public double getLiterVerfügbar() {
		return literVerfuegbar;
	}

	public void setLiterVerfügbar(double literVerfügbar) {
		this.literVerfuegbar = literVerfügbar;
	}

	public String getLand() {
		return land;
	}

	public void setLand(String land) {
		this.land = land;
	}
	
	
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("milktype", milktype);
		json.put("bezeichnung", bezeichnung);
		json.put("literVerfuegbar", literVerfuegbar);
		//json.put("literVerwendet", this.literVerwendet);
		json.put("land", this.land);
		/*if(sorteData != null) {
			JSONObject data = new JSONObject(sorteData);
			json.put("sorteData", data);
		}*/
		return json;
	}

}
