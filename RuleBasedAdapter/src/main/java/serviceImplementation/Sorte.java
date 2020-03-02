package serviceImplementation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class Sorte {
	private String skbez;
	private String sorteBez;
	private double demandAverage;
	private double productionAmount;
	private String milktype;
	private List<String> laender;
	private double milkDemandProduction;
	private double unitsPerTube;
	private int prioritaetProd;
	private int prioritaetZut;
	private int stueckelung;
	private double inventory;
	private double targetInventory;
	private double saisonalitaet;
	private Map<String, Datastruct> sorteData;
	
	
	public Sorte() {
		skbez = "";
		sorteBez = "";
		demandAverage = 0;
		productionAmount = 0;
		inventory = 0;
		targetInventory = 0;
		milktype = "";
		laender=new ArrayList<String>();
		milkDemandProduction = 0;
		unitsPerTube = 0;
		prioritaetProd = 0;
		prioritaetZut = 0;
		stueckelung = 0;
		saisonalitaet = 1;
		sorteData = new LinkedHashMap<String, Datastruct>();
	}

	public Sorte(String skbez, String sorteBez,  String milktype, double milkDemandProduction, int prioritaetProd, int prioritaetZut, int stueckelung, double targetInventory, double unitsPerTube, List<String> laender, double saisonalitaet) {
		this.skbez = skbez;
		this.sorteBez = sorteBez;
		this.milktype = milktype;
		this.laender = laender;
		this.milkDemandProduction = milkDemandProduction;
		this.unitsPerTube = unitsPerTube;
		this.prioritaetProd = prioritaetProd;
		this.prioritaetZut = prioritaetZut;
		this.stueckelung = stueckelung;
		this.targetInventory = targetInventory;
		this.saisonalitaet = saisonalitaet;
		sorteData = new LinkedHashMap<String, Datastruct>();
	}
	
	public Sorte(String skbez, String sorteBez, String milktype, double milkDemandProduction, int prioritaetProd, int prioritaetZut, int stueckelung, double inventory, double targetInventory, Map<String, Datastruct> sorteData, double unitsPerTube, List<String> laender, double saisonalitaet) {
		this(skbez, sorteBez, milktype, milkDemandProduction, prioritaetProd, prioritaetZut, stueckelung, targetInventory, unitsPerTube, laender, saisonalitaet);
		this.sorteData = sorteData;
		this.inventory = inventory;
	}
	
	public Map<String, Datastruct> getSorteData() {
		return sorteData;
	}

	public void setSorteData(Map<String, Datastruct> sorteData) {
		this.sorteData = sorteData;
	}
	
	public String getSkbez() {
		return skbez;
	}

	public void setSkbez(String skbez) {
		this.skbez = skbez;
	}
	public String getSorteBez() {
		return sorteBez;
	}

	public void setSorteBez(String sorteBez) {
		this.sorteBez = sorteBez;
	}

	public double getDemandAverage() {
		return demandAverage;
	}

	public String toJSONString() {
		String jsonString = "";
		return jsonString;
	}
	public void setDemandAverage(double demandAverage) {
		this.demandAverage = demandAverage;
	}

	public double getProductionAmount() {
		return productionAmount;
	}

	public void setProductionAmount(double productionAmount) {
		this.productionAmount = productionAmount;
	}

	public double getInventory() {
		return inventory;
	}

	public void setInventory(double inventory) {
		this.inventory = inventory;
	}
	

	public double getTargetInventory() {
		return targetInventory;
	}

	public void setTargetInventory(double targetInventory) {
		this.targetInventory = targetInventory;
	}

	public String getMilktype() {
		return milktype;
	}

	public void setMilktype(String milktype) {
		this.milktype = milktype;
	}

	public List<String> getLaender() {
		return laender;
	}

	public void setLaender(List<String> laender) {
		this.laender = laender;
	}
	
	public double getMilkDemandProduction() {
		return milkDemandProduction;
	}

	public void setMilkDemandProduction(double milkDemandProduction) {
		this.milkDemandProduction = milkDemandProduction;
	}
	
	public double getUnitsPerTube() {
		return unitsPerTube;
	}

	public void setUnitsPerTube(double unitsPerTube) {
		this.unitsPerTube = unitsPerTube;
	}
	public int getPrioritaetProd() {
		return prioritaetProd;
	}

	public void setPrioritaetProd(int prioritaetProd) {
		this.prioritaetProd = prioritaetProd;
	}

	public int getPrioritaetZut() {
		return prioritaetZut;
	}
	public void setPrioritaetZut(int prioritaetZut) {
		this.prioritaetZut = prioritaetZut;
	}
	
	public int getStueckelung() {
		return stueckelung;
	}
	public void setStueckelungd(int stueckelung) {
		this.stueckelung = stueckelung;
	}
	
	public double getSaisonalitaet() {
		return saisonalitaet;
	}
	
	public void setSaisonalitaet(double saisonalitaet) {
		this.saisonalitaet = saisonalitaet;
	}
	
	public void printSorte() {
		System.out.println("skbez: " + this.skbez);
		System.out.println("sorte: " + this.sorteBez);
		System.out.println("DemAV: " + this.demandAverage);
		System.out.println("prodAM: " + this.productionAmount);
		System.out.println("Inv: " + this.inventory);
		System.out.println("TargetInv: " + this.targetInventory);
		System.out.println("milkT: " + this.milktype);
		System.out.println("laender: " + this.laender.toString());
		System.out.println("milkDemProd: " + this.milkDemandProduction);
		System.out.println("unitsPerTube: " + this.unitsPerTube);
		System.out.println("prioProd: " + this.prioritaetProd);
		System.out.println("prioZut: " + this.prioritaetZut);
		System.out.println("stueckelung: " + this.stueckelung);
		if(this.sorteData == null){
			System.out.println("NULL");
		}else {
			System.out.println("DATA: " + this.sorteData);
		}
	}
	public JSONObject SorteTOJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("skbez", this.skbez);
		json.put("name", this.sorteBez);
		json.put("milktype", this.milktype);
		json.put("laender", new JSONArray(laender));
		json.put("productionAmount", this.productionAmount);
		json.put("prioritaetProd", this.prioritaetProd);
		json.put("prioritaetZut", this.prioritaetZut);
		json.put("stueckelung", this.stueckelung);
		json.put("inventory", this.inventory);
		json.put("targetInventory", this.targetInventory);
		json.put("saisonalitaet", this.saisonalitaet);
		json.put("milkDemandProduction", this.milkDemandProduction);
		json.put("unitsPerTube", this.unitsPerTube);
		/*if(sorteData != null) {
			JSONObject data = new JSONObject(sorteData);
			json.put("sorteData", data);
		}*/
		return json;
	}


}
