package serviceImplementation;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.NonNegativeConstraint;
import org.apache.commons.math3.optim.linear.PivotSelectionRule;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.json.JSONObject;

import dBConnections.GatewayDAO;
import dBConnections.GatewayServiceDBConnection;
import jdk.dynalink.linker.support.SimpleLinkRequest;
import outputHandler.CustomFileWriter;

public class ServiceCombiner {
	
	private static JSONObject prepareWeightCalculationValues(JSONObject forecastingResults, JSONObject actualDemands, String forecastDate, String username) {
		JSONObject newStructureTargetVariable = new JSONObject();;
		JSONObject newStructureForecastPeriod = null;
		JSONObject newStructureProcedure = null;
		JSONObject independentVariables = null;
		for(String procedureName : forecastingResults.keySet()) {
			JSONObject procedure = forecastingResults.getJSONObject(procedureName);
			for(String targetVariableName : procedure.keySet()) {
				JSONObject targetVariable = procedure.getJSONObject(targetVariableName);
				boolean firstPeriod = true;
				for(String forecastPeriod : targetVariable.keySet()) {
					if(firstPeriod) {
						double forecastResult = targetVariable.getDouble(forecastPeriod);
						if(!newStructureTargetVariable.has(targetVariableName)) {
							newStructureForecastPeriod = new JSONObject();
							newStructureTargetVariable.put(targetVariableName, newStructureForecastPeriod);
						} else {
							newStructureForecastPeriod = newStructureTargetVariable.getJSONObject(targetVariableName);
						}
						if(!newStructureForecastPeriod.has(forecastPeriod)) {
							newStructureProcedure = new JSONObject();
							newStructureForecastPeriod.put(forecastPeriod, newStructureProcedure);
						} else {
							newStructureProcedure = newStructureForecastPeriod.getJSONObject(forecastPeriod);
						}
						if(!newStructureProcedure.has("independentVariables")) {
							independentVariables = new JSONObject();
							newStructureProcedure.put("independentVariables", independentVariables);
						} else {
							independentVariables = newStructureProcedure.getJSONObject("independentVariables");
						}
						
						
						if(actualDemands.getJSONObject(forecastPeriod).has(targetVariableName)) {
							independentVariables.put(procedureName, forecastResult);
							if(!newStructureProcedure.has("dependentVariable")) {
								double actualDemandTargetVariable = actualDemands.getJSONObject(forecastPeriod).getDouble(targetVariableName);
								newStructureProcedure.put("dependentVariable", actualDemandTargetVariable);
							}
						}else {
							newStructureProcedure.put("dependentVariable", 0); //System.out.println("No actual Demand for Value: " + targetVariableName);
						}
						firstPeriod=false;
					}
				}
			}
		}
		return(newStructureTargetVariable);
	}
	
	public static String storeWeightCalculationValues(JSONObject forecastingResults, JSONObject actualDemands, String forecastDate, String username) throws ClassNotFoundException {
		GatewayServiceDBConnection.getInstance("GatewayDB");
		GatewayDAO gatewayDAO = new GatewayDAO();
		JSONObject weightCalculationValues = prepareWeightCalculationValues(forecastingResults, actualDemands, forecastDate, username);
		List<String> serviceNames = new ArrayList<String>();
		for(String procedureName : forecastingResults.keySet()) {
			serviceNames.add(procedureName);
		}
		gatewayDAO.writeWeightCalculationValuesToDB(username, forecastDate, serviceNames.toString(), weightCalculationValues);
		return serviceNames.toString();
	}
	
	public static JSONObject calculateWeights(String serviceNames, String forecastDate, String username) throws ClassNotFoundException, SQLException, ParseException {
		JSONObject weights = new JSONObject();
		GatewayServiceDBConnection.getInstance("GatewayDB");
		GatewayDAO gatewayDAO = new GatewayDAO();
		JSONObject weightCalculationValues = gatewayDAO.getWeightCalculationValues(forecastDate, serviceNames, username);
		weightCalculationValues = prepareAverageWeightCalculation(weightCalculationValues);
		weights = calculateAverageWeights(weightCalculationValues, forecastDate, serviceNames, username);
		
		return weights;
	}
	
	private static JSONObject prepareAverageWeightCalculation(JSONObject weightCalculationValues) {
		JSONObject newWeightsStructure = new JSONObject();
		JSONObject newTaragetVariableStructure = null;
		for(String dateString : weightCalculationValues.keySet()) {
			JSONObject targetVariables = weightCalculationValues.getJSONObject(dateString);
			for(String targetVariableName : targetVariables.keySet()) {
				JSONObject procedureValues = targetVariables.getJSONObject(targetVariableName).getJSONObject("1");
				
				if(!newWeightsStructure.has(targetVariableName)) {
					newTaragetVariableStructure = new JSONObject();	
					newWeightsStructure.put(targetVariableName, newTaragetVariableStructure);
				}else {
					newTaragetVariableStructure = newWeightsStructure.getJSONObject(targetVariableName);
				}
				newTaragetVariableStructure.put(dateString, procedureValues);		
			}
		}
		return newWeightsStructure;
	}
	
	private static JSONObject calculateAverageWeights(JSONObject weightCalculationValues, String forecastDate, String serviceNames, String username) {
		JSONObject optimizedWeights = new JSONObject();
		for(String targetVariableName : weightCalculationValues.keySet()) {
			
			JSONObject optimizedWeightsTargetVariable = new JSONObject();
			optimizedWeights.put(targetVariableName, optimizedWeightsTargetVariable);
		    //JSONObject optimizedAverageWeights = new JSONObject();
		    JSONObject optimizedSingleWeights = new JSONObject();
		    optimizedWeightsTargetVariable.put("singleWeights", optimizedSingleWeights);
		    //optimizedWeightsTargetVariable.put("averagedWeights", optimizedAverageWeights);
			

			JSONObject targetVariables = weightCalculationValues.getJSONObject(targetVariableName);	
			
			//int numberOfForecastingEntries = targetVariables.length();
			int entryCounter = 0;
			//double[][] independentVariableArray = new double[numberOfForecastingEntries][];
			//double[] dependentVariableArray = new double[numberOfForecastingEntries];
			for(String dateString : targetVariables.keySet()) {
				if(!(targetVariables.getJSONObject(dateString).getJSONObject("independentVariables").length()<=0)) {
				ArrayList<OptimizationData> objectives = new ArrayList<OptimizationData>();
				ArrayList<LinearConstraint> constraints = new ArrayList<>();
				
				
				JSONObject forecastEntry = targetVariables.getJSONObject(dateString);
				double dependentVariable = forecastEntry.getDouble("dependentVariable");
				JSONObject independentVariables = forecastEntry.getJSONObject("independentVariables");
				int numberOfIndependentVariables = independentVariables.length();
				double[] resultArraySingleForecastPeriod = new double[numberOfIndependentVariables + 1];
				
				
				//double[] resultArraySingleForecastPeriod = new double[numberOfIndependentVariables + 1];
				int procedureCounter = 0;
				ArrayList<String> procedureNames = new ArrayList<String>();
				boolean allGreater = true;
				for(String procedureName : independentVariables.keySet()) {		
					procedureNames.add(procedureName);
					double result = independentVariables.getDouble(procedureName);
					if(result<dependentVariable) {
						allGreater = false;
					}
					//resultArraySingleForecastPeriod[procedureCounter] = result;
					resultArraySingleForecastPeriod[procedureCounter] = result;
					procedureCounter = procedureCounter + 1;
				}
				
				if(allGreater) {
					resultArraySingleForecastPeriod[procedureCounter] = -1;
				}else {
					resultArraySingleForecastPeriod[procedureCounter] = 1;
				}
				
				//resultArraySingleForecastPeriod[procedureCounter] = dependentVariable;
				
				
				//Objective: C1*X1+C2*X2+Error=Y => Y-C1*X1+C2*X2=Error => Bedingungen: P1 = ResultProcedure1 P2 = Result Procedure 2, Goal: Minimize Error => Minimize
				//=> -P1*X1 + P2*X2 - Y = 0
				//Constraint 1: SUm(coeff)=1;  X1+X2 = 1 => C1 und C2 1 setzen => C1*1 + C2*1 = 1
				//Constraint 2: 0<=C1/C2<=1
				//Constraint 3:  => P1*X1 + P2*X2 = Y Example :constraints.add(new LinearConstraint(new double[] {80,100}, Relationship.EQ, dependentVariable));
				//Y not considered (constant variable)
				//Prepare Constraint for Coefficients adding up to 1
				double[] dummies = new double[numberOfIndependentVariables+1];
				Arrays.fill(dummies, 1);	
				dummies[numberOfIndependentVariables]=0;
				constraints.add(new LinearConstraint(dummies, Relationship.EQ, 1));
				
				//Prepare Constraint ensuring positive Error Term
				/*dummies = new double[numberOfIndependentVariables+1];
				Arrays.fill(dummies, 0);	
				dummies[numberOfIndependentVariables]=1;
				constraints.add(new LinearConstraint(dummies, Relationship.GEQ, 0));*/
				
				//Prepare Constraint ensuring positive Coefficient Terms
				for(int i = 0; i<numberOfIndependentVariables;i++) {
					dummies = new double[numberOfIndependentVariables+1];
					dummies[i]=1;
					constraints.add(new LinearConstraint(dummies, Relationship.LEQ, 1));
					constraints.add(new LinearConstraint(dummies, Relationship.GEQ, 0));
				}	
				
				//Condition ensuring that targetVariable is met
				constraints.add(new LinearConstraint(resultArraySingleForecastPeriod, Relationship.EQ, dependentVariable));
				
				//not needed wrong thoughts
				/*
				//Optimization Problem
				dummies = new double[numberOfIndependentVariables+1];
				Arrays.fill(dummies, -1);	
				dummies[numberOfIndependentVariables]=0;
				LinearObjectiveFunction OptimizeWeights = new LinearObjectiveFunction(dummies, dependentVariable);	
				*/
				
				dummies = new double[numberOfIndependentVariables+1];
				Arrays.fill(dummies, 0);	
				dummies[numberOfIndependentVariables]=1;
				LinearObjectiveFunction MinimizeError = new LinearObjectiveFunction(dummies,-dependentVariable);
				
				//Add Conditions to OptimizationData
				//objectives.add(OptimizeWeights);
				objectives.add(MinimizeError);
				objectives.add(new LinearConstraintSet(constraints));
				
				objectives.add(GoalType.MINIMIZE);
				objectives.add(new NonNegativeConstraint(true));
				objectives.add(PivotSelectionRule.BLAND);
			    SimplexSolver solver = new SimplexSolver();    
			    OptimizationData[] optimizations = new OptimizationData[objectives.size()];
				for(int i = 0; i<objectives.size();i++) {
					optimizations[i]=objectives.get(i);
					
				}
			    PointValuePair solution = solver.optimize(optimizations);
			    
			    JSONObject proceduresWeights = new JSONObject();
			    for(int i = 0; i<(solution.getPoint().length-1);i++) {
			    	System.out.println("X"+i+": " + solution.getPoint()[i]);
			    	proceduresWeights.put(procedureNames.get(i), solution.getPoint()[i]);
			    }
			    
			    System.out.println(solution.getValue());
			    //double[] weights = solution.getPoint();
			    double error = solution.getValue();

			    
			    JSONObject optimizedPeriodWeights = new JSONObject();
			    
			  //if(!optimizedWeightsTargetVariable.has(dateString)) {
				//optimizedWeightsTargetVariable.put(dateString, optimizedPeriodWeights);
			//}else {
			//	optimizedPeriodWeights = optimizedWeightsTargetVariable.getJSONObject(dateString);
			//}
			    optimizedSingleWeights.put(dateString, optimizedPeriodWeights);
			    optimizedPeriodWeights.put("weights", proceduresWeights);
			    optimizedPeriodWeights.put("error", error);
			   
			   
			    
			    if(optimizedWeightsTargetVariable.has("averagedWeights")) {
		    		JSONObject optimizedAverageWeights = optimizedWeightsTargetVariable.getJSONObject("averagedWeights"); //.getJSONObject("weights");
		    		 for(int i = 0; i<( optimizedAverageWeights.length());i++) {
			    		String procedureName = procedureNames.get(i);
			    		double oldValue = optimizedAverageWeights.getDouble(procedureName);
			    		double newValue = proceduresWeights.getDouble(procedureName);
			    		optimizedAverageWeights.put(procedureName, (oldValue + newValue)/2);
		    		 }
		    	}else {
		    		optimizedWeightsTargetVariable.put("averagedWeights", proceduresWeights);
		    	}
				entryCounter = entryCounter + 1;
			}
			/*OLSMultipleLinearRegression multipleRegression = new OLSMultipleLinearRegression();
			multipleRegression.setNoIntercept(true);
			multipleRegression.newSampleData(dependentVariableArray,independentVariableArray);
			double[] weights = multipleRegression.estimateRegressionParameters();
			*/
			} 

		}
		return optimizedWeights;
	}
	
	public static void test() {
		
		ArrayList<LinearConstraint> constraints = new ArrayList<>();
		ArrayList<OptimizationData> objectives = new ArrayList<OptimizationData>();
		//constraints.add(new LinearConstraint(new double[] {1,0,0}, Relationship.LEQ, 1));
		constraints.add(new LinearConstraint(new double[]{1.0, 0.0, 0.0}, Relationship.LEQ, 1));
		//constraints.add(new LinearConstraint(new double[] {0,1,0}, Relationship.LEQ, 1));
		constraints.add(new LinearConstraint(new double[]{0.0, 1.0, 0.0}, Relationship.LEQ, 1));
		//constraints.add(new LinearConstraint(new double[] {1,0,0}, Relationship.GEQ, 0));
		constraints.add(new LinearConstraint(new double[]{1.0, 0.0, 0.0}, Relationship.GEQ, 0));
		//constraints.add(new LinearConstraint(new double[] {0,1,0}, Relationship.GEQ, 0));
		constraints.add(new LinearConstraint(new double[]{0.0, 1.0, 0.0}, Relationship.GEQ, 0));
		//constraints.add(new LinearConstraint(new double[] {1,1,0}, Relationship.EQ, 1));
		constraints.add(new LinearConstraint(new double[]{1.0, 1.0, 0.0}, Relationship.EQ, 1));
		//constraints.add(new LinearConstraint(new double[] {0,0,1}, Relationship.GEQ, 0));
		//constraints.add(new LinearConstraint(new double[] {80,85,1}, Relationship.EQ, 90));
		constraints.add(new LinearConstraint(new double[]{73.9959, 63.4914, -1}, Relationship.EQ, 32.1));
		//ArrayList<OptimizationData> objectives = new ArrayList<OptimizationData>();
		//LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {-1, -1, 0}, 90);
		//LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1, 1, 0}, 32.1);	
		//LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[] {0, 0, 1},0);*/
		LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[] {0, 0, 1},-32.1);
		
		
		//objectives.add(f);
		objectives.add(f2);
		objectives.add(new LinearConstraintSet(constraints));
		objectives.add(GoalType.MINIMIZE);
		objectives.add(new NonNegativeConstraint(true));
		objectives.add(PivotSelectionRule.BLAND);
	    SimplexSolver solver = new SimplexSolver();    
	    OptimizationData[] optimizations = new OptimizationData[objectives.size()];
		for(int i = 0; i<objectives.size();i++) {
			optimizations[i]=objectives.get(i);
		}
	    PointValuePair solution1 = solver.optimize(optimizations);
	    for(int i = 0; i<solution1.getPoint().length;i++) {
	    	System.out.println("X"+i+": " + solution1.getPoint()[i]);
	    }
	    System.out.println(solution1.getValue());
		/*constraints = new ArrayList<>();
		constraints.add(new LinearConstraint(new double[] {1,0}, Relationship.LEQ, 1));
		constraints.add(new LinearConstraint(new double[] {0,1}, Relationship.LEQ, 1));
		constraints.add(new LinearConstraint(new double[] {1,0}, Relationship.GEQ, 0));
		constraints.add(new LinearConstraint(new double[] {0,1}, Relationship.GEQ, 0));
		constraints.add(new LinearConstraint(new double[] {1,1}, Relationship.EQ, 1));
		constraints.add(new LinearConstraint(new double[] {96,72}, Relationship.EQ, 90));
		objectives = new ArrayList<OptimizationData>();
		f = new LinearObjectiveFunction(new double[] {-1, -1}, 90);	
		objectives.add(f);
		objectives.add(new LinearConstraintSet(constraints));
		objectives.add(GoalType.MINIMIZE);
		objectives.add(new NonNegativeConstraint(true));
		objectives.add(PivotSelectionRule.BLAND);
	    solver = new SimplexSolver();    
	    optimizations = new OptimizationData[objectives.size()];
		for(int i = 0; i<objectives.size();i++) {
			optimizations[i]=objectives.get(i);
		}
	    PointValuePair solution2 = solver.optimize(optimizations);
	    for(int i = 0; i<solution2.getPoint().length;i++) {
	    	System.out.println("X"+i+": " + solution2.getPoint()[i]);
	    }
	    System.out.println(solution2.getValue());
	    System.out.println((solution1.getPoint()[0] + solution2.getPoint()[0])/2);
	    System.out.println((solution1.getPoint()[1] + solution2.getPoint()[1])/2);*/
	    
	}

	public static String writeWeightsToDB(JSONObject weights, String serviceNames, String forecastDate, String username) throws ClassNotFoundException {
		GatewayServiceDBConnection.getInstance("GatewayDB");
		GatewayDAO gatewayDAO = new GatewayDAO();
		gatewayDAO.writeWeightsToDB(weights, serviceNames, forecastDate, username);
		return serviceNames;
	}
		
	public static JSONObject getAveragedWeights(String toDate, String serviceNames, String username) throws ClassNotFoundException, SQLException, ParseException {
		GatewayServiceDBConnection.getInstance("GatewayDB");
		GatewayDAO gatewayDAO = new GatewayDAO();
		JSONObject averagedWeights = gatewayDAO.getAveragedWeights(toDate, serviceNames, username);
		return averagedWeights;
	}
	/*private static double[] calculateWeights(JSONObject forecastPeriodResults, double actualDemandTargetVariable) {
		int numberOfForecastingPeriods = forecastPeriodResults.length();
		double[][] independentVariableArray = new double[numberOfForecastingPeriods][];
		double[] dependentVariableArray = new double[numberOfForecastingPeriods];
		int periodCounter = 0;
		for(String forecastPeriod : forecastPeriodResults.keySet()) {
			JSONObject procedureResults = forecastPeriodResults.getJSONObject(forecastPeriod);
			int numberOfProcedures = procedureResults.length();
			double[] resultArraySingleForecastPeriod = new double[numberOfProcedures];
			int procedureCounter = 0;
			for(String procedureName : procedureResults.keySet()) {		
				double result = procedureResults.getDouble(procedureName);
				resultArraySingleForecastPeriod[procedureCounter] = result;
				procedureCounter = procedureCounter + 1;
			}
			
			//double actualDemandForecastPeriod = actualDemands.getJSONObject(forecastPeriod).getDouble(targetVariableName);
			dependentVariableArray[periodCounter] = actualDemandTargetVariable;
			independentVariableArray[periodCounter] = resultArraySingleForecastPeriod;
			periodCounter = periodCounter + 1;
		}

		OLSMultipleLinearRegression multipleRegression = new OLSMultipleLinearRegression();
		multipleRegression.newSampleData(dependentVariableArray,independentVariableArray);
		double[] weights = multipleRegression.estimateRegressionParameters();
		return weights;
	}*/
	
	private static JSONObject combineResults(JSONObject forecastingResults, JSONObject weights) {
		JSONObject combinedResult = new JSONObject();
		for(String procedureName : forecastingResults.keySet()) {
			JSONObject procedure = forecastingResults.getJSONObject(procedureName);
			for(String targetVariableName : procedure.keySet()) {
				if(!combinedResult.has(targetVariableName)) {
					combinedResult.put(targetVariableName, new JSONObject());
				}
				JSONObject targetVariable = procedure.getJSONObject(targetVariableName);	
				for(String forecastPeriod : targetVariable.keySet()){
						double weight = weights.getJSONObject(targetVariableName).getDouble(procedureName);
						if(combinedResult.getJSONObject(targetVariableName).has(forecastPeriod)) {
							double oldResult = combinedResult.getJSONObject(targetVariableName).getDouble(forecastPeriod);
							double newResult = targetVariable.getDouble(forecastPeriod) * weight;
							combinedResult.getJSONObject(targetVariableName).put(forecastPeriod,  (oldResult + newResult));
						}else {
							double newResult = targetVariable.getDouble(forecastPeriod) * weight;
							combinedResult.getJSONObject(targetVariableName).put(forecastPeriod,  (newResult));
						}
					}
			}
		}
		return combinedResult;
	}	
	
	public static JSONObject calculateCombinedResultDynamicWeights(JSONObject combinedAnalysisResult, JSONObject weights) {
		JSONObject combinedResult = new JSONObject();
		for(String procedureName : combinedAnalysisResult.keySet()) {
			JSONObject procedure = combinedAnalysisResult.getJSONObject(procedureName);
			
			for(String targetVariableName : procedure.keySet()) {
				double weight = 0;
				if(weights.getJSONObject(targetVariableName).has("averagedWeights")){
					weight = weights.getJSONObject(targetVariableName).getJSONObject("averagedWeights").getDouble(procedureName);
				}
				
				if(!combinedResult.has(targetVariableName)) {
					combinedResult.put(targetVariableName, new JSONObject());
				}
				JSONObject targetVariable = procedure.getJSONObject(targetVariableName);
				for(String forecastPeriod : targetVariable.keySet()){
					if(combinedResult.getJSONObject(targetVariableName).has(forecastPeriod)) {
						double oldResult = combinedResult.getJSONObject(targetVariableName).getDouble(forecastPeriod);
						double newResult = targetVariable.getDouble(forecastPeriod) * weight;
						combinedResult.getJSONObject(targetVariableName).put(forecastPeriod,  (oldResult + newResult));
					}else {
						double newResult = targetVariable.getDouble(forecastPeriod) * weight;
						combinedResult.getJSONObject(targetVariableName).put(forecastPeriod,  (newResult));
					}
				}
			}
		}
		return combinedResult;
		
	}
	
	public static JSONObject calculateCombinedResultStaticWeights(JSONObject combinedAnalysisResult, JSONObject weights) {
		JSONObject combinedResult = new JSONObject();
		for(String procedureName : combinedAnalysisResult.keySet()) {
			JSONObject procedure = combinedAnalysisResult.getJSONObject(procedureName);
			for(String targetVariableName : procedure.keySet()) {
				if(!combinedResult.has(targetVariableName)) {
					combinedResult.put(targetVariableName, new JSONObject());
				}
				JSONObject targetVariable = procedure.getJSONObject(targetVariableName);
				for(String forecastPeriod : targetVariable.keySet()){
					double weight = weights.getJSONObject(targetVariableName).getDouble(procedureName);
					if(combinedResult.getJSONObject(targetVariableName).has(forecastPeriod)) {
						double oldResult = combinedResult.getJSONObject(targetVariableName).getDouble(forecastPeriod);
						double newResult = targetVariable.getDouble(forecastPeriod) * weight;
						combinedResult.getJSONObject(targetVariableName).put(forecastPeriod,  (oldResult + newResult));
					}else {
						double newResult = targetVariable.getDouble(forecastPeriod) * weight;
						combinedResult.getJSONObject(targetVariableName).put(forecastPeriod,  (newResult));
					}
				}
			}
		}
		return combinedResult;
		
	}
	
	public static JSONObject calculateCombinedResultEqualWeights(JSONObject combinedAnalysisResult) throws ClassNotFoundException, SQLException, ParseException {
		JSONObject combinedResult = new JSONObject();
		for(String procedureName : combinedAnalysisResult.keySet()) {
			JSONObject procedure = combinedAnalysisResult.getJSONObject(procedureName);
			for(String targetVariableName : procedure.keySet()) {
				if(!combinedResult.has(targetVariableName)) {
					combinedResult.put(targetVariableName, new JSONObject());
				}
				JSONObject targetVariable = procedure.getJSONObject(targetVariableName);
				for(String forecastPeriod : targetVariable.keySet()){
					if(combinedResult.getJSONObject(targetVariableName).has(forecastPeriod)) {
						double oldResult = combinedResult.getJSONObject(targetVariableName).getDouble(forecastPeriod);
						double newResult = targetVariable.getDouble(forecastPeriod);
						combinedResult.getJSONObject(targetVariableName).put(forecastPeriod,  (oldResult + newResult)/2);
					}else {
						double newResult = targetVariable.getDouble(forecastPeriod);
						combinedResult.getJSONObject(targetVariableName).put(forecastPeriod,  (newResult));
					}
				}
			}
		}
		return combinedResult;
		
	}
	
	
}
