package serviceImplementation;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

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
import org.json.JSONException;
import org.json.JSONObject;

import dBConnections.GatewayDAO;
import dBConnections.GatewayServiceDBConnection;
import jdk.dynalink.linker.support.SimpleLinkRequest;
import outputHandler.CustomFileWriter;

public class ServiceCombiner {
	static List<String> skipList = Arrays.asList("MAE", "ME", "MAEPercentage", "MEPercentage");
	/*
	public static JSONObject prepareWeightCalculationValues(JSONObject forecastingResults, JSONObject actualDemands, String forecastDate, String username) {
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
	*/
	
	public static JSONObject prepareWeightCalculationValues(JSONObject forecastingResults, JSONObject actualDemands, String forecastDate, String username) throws ParseException {
		JSONObject newStructureTargetVariable = new JSONObject();;
		JSONObject newStructureForecastPeriod = null;
		JSONObject newStructureProcedure = null;
		JSONObject independentVariables = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
		Calendar calendar = new GregorianCalendar(Locale.GERMAN);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		for(String procedureName : forecastingResults.keySet()) {
			JSONObject procedure = forecastingResults.getJSONObject(procedureName);
			for(String dateString : procedure.keySet()) {
				JSONObject dateResults = procedure.getJSONObject(dateString);
				for(String targetVariableName : dateResults.keySet()) {
					JSONObject targetVariable = dateResults.getJSONObject(targetVariableName);
						double forecastResult = targetVariable.getDouble("1");
						if(!newStructureTargetVariable.has(targetVariableName)) {
							newStructureForecastPeriod = new JSONObject();
							newStructureTargetVariable.put(targetVariableName, newStructureForecastPeriod);
						} else {
							newStructureForecastPeriod = newStructureTargetVariable.getJSONObject(targetVariableName);
						}
						if(!newStructureForecastPeriod.has(dateString)) {
							newStructureProcedure = new JSONObject();
							newStructureForecastPeriod.put(dateString, newStructureProcedure);
						} else {
							newStructureProcedure = newStructureForecastPeriod.getJSONObject(dateString);
						}
						if(!newStructureProcedure.has("independentVariables")) {
							independentVariables = new JSONObject();
							newStructureProcedure.put("independentVariables", independentVariables);
						} else {
							independentVariables = newStructureProcedure.getJSONObject("independentVariables");
						}
						

						
						calendar.setTime(dateFormat.parse(dateString));
						calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
						calendar.add(Calendar.DAY_OF_MONTH, + 1);
						Date weekBeginDate = calendar.getTime();
						String weekBeginDateString = dateFormat.format(weekBeginDate);
						if(actualDemands.getJSONObject(weekBeginDateString).has(targetVariableName)) {
							independentVariables.put(procedureName, forecastResult);
							if(!newStructureProcedure.has("dependentVariable")) {
								//switch commentline if campaign amounts are not considered
								//double actualDemandTargetVariable = actualDemands.getJSONObject(weekBeginDateString).getDouble(targetVariableName);
								double actualDemandTargetVariable = actualDemands.getJSONObject(weekBeginDateString).getJSONObject(targetVariableName).getDouble("unknownDemand");
								newStructureProcedure.put("dependentVariable", actualDemandTargetVariable);
							}
						}else {
							newStructureProcedure.put("dependentVariable", 0); //System.out.println("No actual Demand for Value: " + targetVariableName);
						}
			
					
				}
			}
		}
		return(newStructureTargetVariable);
	}
	
	public static String storeWeightCalculationValues(JSONObject forecastingResults, JSONObject actualDemands, String forecastDate, String username) throws ClassNotFoundException, ParseException {
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
	
	public static JSONObject calculateWeights(ArrayList<String> serviceNames, String forecastDate, String username, JSONObject forecastResults) throws ClassNotFoundException, SQLException, ParseException {
		JSONObject weights = new JSONObject();
		//GatewayServiceDBConnection.getInstance("GatewayDB");
		//GatewayDAO gatewayDAO = new GatewayDAO();
		//JSONObject weightCalculationValues = gatewayDAO.getWeightCalculationValues(forecastDate, serviceNames, username);
		//weightCalculationValues = prepareAverageWeightCalculation(weightCalculationValues);
		//weights = calculateAverageWeights(weightCalculationValues, forecastDate, serviceNames, username);
		JSONObject weightCalculationValues = prepareAverageWeightCalculation(forecastResults);
		
		weights = calculateAverageWeights(weightCalculationValues, forecastDate, serviceNames.toString(), username);
		//weights = calculateAverageWeightsAllPeriods(weightCalculationValues, forecastDate, serviceNames, username);
		
		
		for(String targetVariableName : weights.keySet()) {
			JSONObject targetVariableResult = weights.getJSONObject(targetVariableName);
			if(targetVariableResult.has("averagedWeights")) {
				JSONObject averagedResult = targetVariableResult.getJSONObject("averagedWeights");
				for(String procedureName : averagedResult.keySet()) {
					double averagedWeights = averagedResult.getJSONObject(procedureName).getDouble("total");
					weights.getJSONObject(targetVariableName).getJSONObject("averagedWeights").put(procedureName, averagedWeights);
				}
			}else {
				double factor = 1.0/serviceNames.size();
				JSONObject averagedResult = new JSONObject();
				for(String procedureName : serviceNames) {
					averagedResult.put(procedureName, factor);
				}
				targetVariableResult.put("averagedWeights", averagedResult);
			}
		}
		return weights;
	}
	
	
	private static JSONObject prepareAverageWeightCalculation(JSONObject weightCalculationValues) {
		JSONObject newWeightsStructure = new JSONObject();
		JSONObject newTaragetVariableStructure = null;
		for(String targetVariableName : weightCalculationValues.keySet()) {
			JSONObject targetVariableResults = weightCalculationValues.getJSONObject(targetVariableName);
			for(String dateString : targetVariableResults.keySet()) {
				JSONObject dateResults = targetVariableResults.getJSONObject(dateString);
				//JSONObject independentVariables = dateResults.getJSONObject("independentVariables");
				//for(String procedureName : independentVariables.keySet()) {
				//JSONObject procedureValues = dateResults.getJSONObject("independentVariables");
				
				if(!newWeightsStructure.has(targetVariableName)) {
					newTaragetVariableStructure = new JSONObject();	
					newWeightsStructure.put(targetVariableName, newTaragetVariableStructure);
				}else {
					newTaragetVariableStructure = newWeightsStructure.getJSONObject(targetVariableName);
				}
				newTaragetVariableStructure.put(dateString, dateResults);		
				//}
			}
		}
		CustomFileWriter.writeResultToFile("D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\Bantel\\Daten\\test\\newStructure.json", newWeightsStructure);
		return newWeightsStructure;
	}
	
	private static JSONObject prepareAverageWeightCalculationNeu(JSONObject weightCalculationValues, JSONObject demands) {
		JSONObject newWeightsStructure = new JSONObject();
		JSONObject newTaragetVariableStructure = null;
		for(String procedureName : weightCalculationValues.keySet()) {
			JSONObject procedureResult = weightCalculationValues.getJSONObject(procedureName);
			for(String dateString : procedureResult.keySet()) {
				JSONObject dateResults = procedureResult.getJSONObject(dateString);
				for(String configuration : dateResults.keySet()) {
					JSONObject configurationResult = dateResults.getJSONObject(configuration);
					for(String targetVariableName : configurationResult.keySet()) {
						JSONObject variableResult = configurationResult.getJSONObject(targetVariableName);
						double periodResult = variableResult.getDouble("1");
						
						if(!newWeightsStructure.has(targetVariableName)) {
							newWeightsStructure.put(targetVariableName, new JSONObject());
						}
						JSONObject newDateStructure = newWeightsStructure.getJSONObject(targetVariableName);
						double demand = 0;
						if(!newDateStructure.has(dateString)) {
							newDateStructure.put(dateString,  new JSONObject());
							newDateStructure.getJSONObject(dateString).put("dependentVariable", demand);
							newDateStructure.getJSONObject(dateString).put("independentVariables", new JSONObject());
						}
						JSONObject newProcedureStructure = newDateStructure.getJSONObject(dateString).getJSONObject("independentVariables");
						if(demands.getJSONObject(dateString).getJSONObject("1").has(targetVariableName)){
								demand = demands.getJSONObject(dateString).getJSONObject("1").getJSONObject(targetVariableName).getDouble("unknownDemand");
								newDateStructure.getJSONObject(dateString).put("dependentVariable", demand);
								if(!newProcedureStructure.has(procedureName)) {
									newProcedureStructure.put(procedureName, periodResult);
								}
						
						}
							
						
					}
				
				}
				//JSONObject independentVariables = dateResults.getJSONObject("independentVariables");
				//for(String procedureName : independentVariables.keySet()) {
				//JSONObject procedureValues = dateResults.getJSONObject("independentVariables");
				
			
				//}
			}
		}
		
		//CustomFileWriter.writeResultToFile("D:\\Arbeit\\Bantel\\Masterarbeit\\Implementierung\\Bantel\\Daten\\test\\newStructure.json", newWeightsStructure);
		return newWeightsStructure;
	}
	
	public static JSONObject calculateWeightsNeu(ArrayList<String> serviceNames, String forecastDate, String username, JSONObject forecastResults, JSONObject demand, JSONObject evaluationResults) throws ClassNotFoundException, SQLException, ParseException {
		JSONObject weights = new JSONObject();
		JSONObject variableStructuredResult = new JSONObject();
		JSONObject comparator = new JSONObject();
		for(String procedureName : evaluationResults.keySet()) {
			if(!procedureName.equals("compared")) {
				JSONObject procedureEvaluationResult = evaluationResults.getJSONObject(procedureName).getJSONObject("MAE");
				for(String targetVariable : procedureEvaluationResult.keySet()) {
					if(!skipList.contains(targetVariable)) {
						JSONObject targetVariableEvaluationResult = procedureEvaluationResult.getJSONObject(targetVariable);
						if(!variableStructuredResult.has(targetVariable)) {
							variableStructuredResult.put(targetVariable, new JSONObject());
						}
						if(!variableStructuredResult.getJSONObject(targetVariable).has(procedureName)){
							//JSONObject evaluationResult = new JSONObject();
							//evaluationResult.put("MAEPercentage", targetVariableEvaluationResult.getDouble("MAEPercentage"));
							
							//JSONObject evalResults = new JSONObject();
							//evalResults.put("MEPercentage",  targetVariableEvaluationResult.getDouble("MEPercentage"));
							//evalResults.put("MAEPercentage",  targetVariableEvaluationResult.getDouble("MAEPercentage"));
							//variableStructuredResult.getJSONObject(targetVariable).put(procedureName,  evalResults);
							variableStructuredResult.getJSONObject(targetVariable).put(procedureName,  targetVariableEvaluationResult.getDouble("MAEAverage"));
						}
					}
				}
			}
		}
		for(String targetVariableName : variableStructuredResult.keySet()) {
			comparator.put(targetVariableName, new JSONObject());
			JSONObject targetVariableResult = variableStructuredResult.getJSONObject(targetVariableName);
			for(String procedureName : targetVariableResult.keySet()) {
				if(!comparator.getJSONObject(targetVariableName).has("bestResult")) {
					comparator.getJSONObject(targetVariableName).put("bestResult", new JSONObject());
					comparator.getJSONObject(targetVariableName).getJSONObject("bestResult").put("procedureName", (procedureName));
					comparator.getJSONObject(targetVariableName).getJSONObject("bestResult").put("procedureValue", targetVariableResult.getDouble(procedureName));
				}else {
					double currentBestResult = comparator.getJSONObject(targetVariableName).getJSONObject("bestResult").getDouble("procedureValue");
					if(currentBestResult > targetVariableResult.getDouble(procedureName)) {
						comparator.getJSONObject(targetVariableName).getJSONObject("bestResult").put("procedureName", procedureName);
						comparator.getJSONObject(targetVariableName).getJSONObject("bestResult").put("procedureValue", targetVariableResult.getDouble(procedureName));
					}
				}
			}
		}
		JSONObject preparedValues = prepareAverageWeightCalculationNeu(forecastResults, demand);
		weights = calculateAverageWeightsBasedOnBestWeakLearner(preparedValues, comparator, forecastDate, serviceNames, username);		
		/*		
				JSONObject bestProcedure = new JSONObject();
				comparator.put(targetVariable, bestProcedure);
				
				double valueToCompare = targetVariableEvaluationResult.getDouble("MAE");
				if(!comparator.has("bestResult")) {
					comparator.put("bestResult", valueToCompare);
				}else {
					double currentBestResult = comparator.getDouble("bestResult");
					if(currentBestResult > valueToCompare) {
						comparator.put("bestResult", valueToCompare);
					}
				}
			}
		}
		*/
		for(String targetVariableName : weights.keySet()) {
			JSONObject targetVariableResult = weights.getJSONObject(targetVariableName);
			if(targetVariableResult.has("averagedWeights")) {
				JSONObject averagedResult = targetVariableResult.getJSONObject("averagedWeights");
				for(String procedureName : averagedResult.keySet()) {
					double averagedWeights = averagedResult.getJSONObject(procedureName).getDouble("total");
					weights.getJSONObject(targetVariableName).getJSONObject("averagedWeights").put(procedureName, averagedWeights);
				}
			}else {
				double factor = 1.0/serviceNames.size();
				JSONObject averagedResult = new JSONObject();
				for(String procedureName : serviceNames) {
					averagedResult.put(procedureName, factor);
				}
				targetVariableResult.put("averagedWeights", averagedResult);
			}
		}
		return weights;
	}
	
	
	private static JSONObject calculateAverageWeightsBasedOnBestWeakLearner(JSONObject weightCalculationValues, JSONObject evaluationResult, String forecastDate, ArrayList<String> serviceNames, String username) {
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
				int amountOfPeriodsConsidered = 0;
				for(String dateString : targetVariables.keySet()) {
					if(!(targetVariables.getJSONObject(dateString).getJSONObject("independentVariables").length()<=0)) {
						amountOfPeriodsConsidered+=1;
					}
				}
				//TO DO ORDER TIMELINE CHRONOLOGICALLY, otherwise decay meaningless
				double decay = 0.1;
				double factor= 1.0/amountOfPeriodsConsidered;
				for(String dateString : targetVariables.keySet()) {
					if(!(targetVariables.getJSONObject(dateString).getJSONObject("independentVariables").length()<=0)) {
					ArrayList<OptimizationData> objectives = new ArrayList<OptimizationData>();
					ArrayList<LinearConstraint> constraints = new ArrayList<>();
					
					
					JSONObject forecastEntry = targetVariables.getJSONObject(dateString);
					double dependentVariable = forecastEntry.getDouble("dependentVariable");
					JSONObject independentVariables = forecastEntry.getJSONObject("independentVariables");
					//int numberOfIndependentVariables = independentVariables.length();
					int numberOfIndependentVariables = 2;
					double[] resultArraySingleForecastPeriod = new double[numberOfIndependentVariables + 1];
					
					String bestProcedureName = evaluationResult.getJSONObject(targetVariableName).getJSONObject("bestResult").getString("procedureName");
					double bestProcedureResult = 0;
					if( independentVariables.has(bestProcedureName)) {
						bestProcedureResult = independentVariables.getDouble(bestProcedureName);
					}
					double deviation = dependentVariable - bestProcedureResult;
					
					//double[] resultArraySingleForecastPeriod = new double[numberOfIndependentVariables + 1];
					JSONObject nearestValues = new JSONObject();
					nearestValues.put("nearestSmallerValue", new JSONObject());
					nearestValues.put("nearestGreaterValue", new JSONObject());
					boolean bestProcedureIsSmaller=false;
					boolean bestProcedureIsGreater=false;
					boolean allGreater = true;
					if(independentVariables.has(bestProcedureName)) {
						if(deviation>=0) {
							nearestValues.getJSONObject("nearestSmallerValue").put("procedureName",bestProcedureName);
							nearestValues.getJSONObject("nearestSmallerValue").put("value",bestProcedureResult);
							bestProcedureIsSmaller=true;
							allGreater = false;
						}else {
							nearestValues.getJSONObject("nearestGreaterValue").put("procedureName",bestProcedureName);
							nearestValues.getJSONObject("nearestGreaterValue").put("value",bestProcedureResult);
							bestProcedureIsGreater=true;
						}
					}
					int procedureCounter = 0;
					ArrayList<String> procedureNames = new ArrayList<String>();
					
					for(String procedureName : independentVariables.keySet()) {		
						procedureNames.add(procedureName);
						double result = independentVariables.getDouble(procedureName);
						if(result<dependentVariable) {
							allGreater = false;
							if(!bestProcedureIsSmaller) {
								if(nearestValues.getJSONObject("nearestSmallerValue").has("procedureName")) {
									double deviationNewResult = dependentVariable - result;
									double deviationStoredResult = dependentVariable - nearestValues.getJSONObject("nearestSmallerValue").getDouble("value");
									if(deviationNewResult<deviationStoredResult) {
										nearestValues.getJSONObject("nearestSmallerValue").put("procedureName",procedureName);
										nearestValues.getJSONObject("nearestSmallerValue").put("value",result);
									}
								}else {
									nearestValues.getJSONObject("nearestSmallerValue").put("procedureName",procedureName);
									nearestValues.getJSONObject("nearestSmallerValue").put("value",result);
								}
							}
						}else {
							if(!bestProcedureIsGreater) {
								if(nearestValues.getJSONObject("nearestGreaterValue").has("procedureName")) {
									double deviationNewResult = result - dependentVariable;
									double deviationStoredResult = nearestValues.getJSONObject("nearestGreaterValue").getDouble("value") - dependentVariable;
									if(deviationNewResult<deviationStoredResult) {
										nearestValues.getJSONObject("nearestGreaterValue").put("procedureName",procedureName);
										nearestValues.getJSONObject("nearestGreaterValue").put("value",result);
									}
								}else {
									nearestValues.getJSONObject("nearestGreaterValue").put("procedureName",procedureName);
									nearestValues.getJSONObject("nearestGreaterValue").put("value",result);
								}
							}
						}
						//resultArraySingleForecastPeriod[procedureCounter] = result;
						//resultArraySingleForecastPeriod[procedureCounter] = result;
						//procedureCounter = procedureCounter + 1;
					}
					
					
					
					 JSONObject proceduresWeights = new JSONObject();
					    //double[] weights = solution.getPoint();
					    double solutionValue =  0;
					    double error = 0;
					if(nearestValues.getJSONObject("nearestSmallerValue").has("procedureName") && nearestValues.getJSONObject("nearestGreaterValue").has("procedureName")){
						resultArraySingleForecastPeriod[0] =  nearestValues.getJSONObject("nearestSmallerValue").getDouble("value");
						resultArraySingleForecastPeriod[1] =  nearestValues.getJSONObject("nearestGreaterValue").getDouble("value");
						if(allGreater) {
							resultArraySingleForecastPeriod[numberOfIndependentVariables] = -1;
						}else {
							resultArraySingleForecastPeriod[numberOfIndependentVariables] = 1;
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
					    
					   
					    //for(int i = 0; i<(solution.getPoint().length-1);i++) {
					    //	System.out.println("X"+i+": " + solution.getPoint()[i]);
					    //	proceduresWeights.put(procedureNames.get(i), solution.getPoint()[i]);
					    //}
					    proceduresWeights.put(nearestValues.getJSONObject("nearestSmallerValue").getString("procedureName"), solution.getPoint()[0]);
					    proceduresWeights.put(nearestValues.getJSONObject("nearestGreaterValue").getString("procedureName"), solution.getPoint()[1]);
					    
					    System.out.println(solution.getValue());
					    //double[] weights = solution.getPoint();
					    solutionValue = solution.getValue();
					    error = solutionValue + dependentVariable;
				   
					} else {
						if(nearestValues.getJSONObject("nearestSmallerValue").has("procedureName")) {
							 proceduresWeights.put(nearestValues.getJSONObject("nearestSmallerValue").getString("procedureName"), 1);
							 error = dependentVariable - nearestValues.getJSONObject("nearestSmallerValue").getDouble("value");
						}else if(nearestValues.getJSONObject("nearestGreaterValue").has("procedureName")) {
							 proceduresWeights.put(nearestValues.getJSONObject("nearestGreaterValue").getString("procedureName"), 1);
							 error = nearestValues.getJSONObject("nearestGreaterValue").getDouble("value")- dependentVariable;
						}else {
							throw new RuntimeException("No forecast results available for given period");
						}
					}
				    for(String procedureName : procedureNames) {
				    	if(!proceduresWeights.has(procedureName)) {
				    		proceduresWeights.put(procedureName, 0.0);
				    	}
				    }
				   
				    
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
			    		for(String procedureName : optimizedAverageWeights.keySet()) { 
			    		//for(int i = 0; i<( optimizedAverageWeights.length());i++) {
			    			JSONObject procedureResults = optimizedAverageWeights.getJSONObject(procedureName);
			    			double newValue = 0;
			    			if(procedureNames.contains(procedureName)) {
			    				newValue = proceduresWeights.getDouble(procedureName) * factor;
			    			}
			    			double oldValue = procedureResults.getDouble("oldValue");
			    			double currentWeight = (oldValue * (0.5-decay) + newValue *(0.5 + decay));
			    			double total = procedureResults.getDouble("total");
			    				
				    		procedureResults.put("oldValue", currentWeight);
				    		procedureResults.put("total", currentWeight + total);
				    		optimizedAverageWeights.put(procedureName, procedureResults);
			    		 }
			    	}else {
			    		JSONObject optimizedAverageWeights = new JSONObject();
			    		for(String procedureName : proceduresWeights.keySet()) {
			    			JSONObject procedureResults = new JSONObject();
				    		double value = proceduresWeights.getDouble(procedureName) * factor;
				    		procedureResults.put("oldValue", value);
				    		procedureResults.put("total", value);
				    		optimizedAverageWeights.put(procedureName, procedureResults);
			    		 }
			    		optimizedWeightsTargetVariable.put("averagedWeights", optimizedAverageWeights);
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
			int amountOfPeriodsConsidered = 0;
			for(String dateString : targetVariables.keySet()) {
				if(!(targetVariables.getJSONObject(dateString).getJSONObject("independentVariables").length()<=0)) {
					amountOfPeriodsConsidered+=1;
				}
			}
		
			double decay = 0.1;
			double factor= 1.0/amountOfPeriodsConsidered;
			for(String dateString : targetVariables.keySet()) {
				if(!(targetVariables.getJSONObject(dateString).getJSONObject("independentVariables").length()<=0)) {
				ArrayList<OptimizationData> objectives = new ArrayList<OptimizationData>();
				ArrayList<LinearConstraint> constraints = new ArrayList<>();
				
				
				JSONObject forecastEntry = targetVariables.getJSONObject(dateString);
				double dependentVariable = forecastEntry.getDouble("dependentVariable");
				JSONObject independentVariables = forecastEntry.getJSONObject("independentVariables");
				//int numberOfIndependentVariables = independentVariables.length();
				int numberOfIndependentVariables = 2;
				double[] resultArraySingleForecastPeriod = new double[numberOfIndependentVariables + 1];
				
				
				
				//double[] resultArraySingleForecastPeriod = new double[numberOfIndependentVariables + 1];
				JSONObject nearestValues = new JSONObject();
				nearestValues.put("nearestSmallerValue", new JSONObject());
				nearestValues.put("nearestGreaterValue", new JSONObject());
				int procedureCounter = 0;
				ArrayList<String> procedureNames = new ArrayList<String>();
				boolean allGreater = true;
				for(String procedureName : independentVariables.keySet()) {		
					procedureNames.add(procedureName);
					double result = independentVariables.getDouble(procedureName);
					if(result<dependentVariable) {
						allGreater = false;
						
						if(nearestValues.getJSONObject("nearestSmallerValue").has("procedureName")) {
							double deviationNewResult = dependentVariable - result;
							double deviationStoredResult = dependentVariable - nearestValues.getJSONObject("nearestSmallerValue").getDouble("value");
							if(deviationNewResult<deviationStoredResult) {
								nearestValues.getJSONObject("nearestSmallerValue").put("procedureName",procedureName);
								nearestValues.getJSONObject("nearestSmallerValue").put("value",result);
							}
						}else {
							nearestValues.getJSONObject("nearestSmallerValue").put("procedureName",procedureName);
							nearestValues.getJSONObject("nearestSmallerValue").put("value",result);
						}
					}else {
						if(nearestValues.getJSONObject("nearestGreaterValue").has("procedureName")) {
							double deviationNewResult = result - dependentVariable;
							double deviationStoredResult = nearestValues.getJSONObject("nearestGreaterValue").getDouble("value") - dependentVariable;
							if(deviationNewResult<deviationStoredResult) {
								nearestValues.getJSONObject("nearestGreaterValue").put("procedureName",procedureName);
								nearestValues.getJSONObject("nearestGreaterValue").put("value",result);
							}
						}else {
							nearestValues.getJSONObject("nearestGreaterValue").put("procedureName",procedureName);
							nearestValues.getJSONObject("nearestGreaterValue").put("value",result);
						}
					}
					//resultArraySingleForecastPeriod[procedureCounter] = result;
					//resultArraySingleForecastPeriod[procedureCounter] = result;
					//procedureCounter = procedureCounter + 1;
				}
				
				
				
				 JSONObject proceduresWeights = new JSONObject();
				    //double[] weights = solution.getPoint();
				    double solutionValue =  0;
				    double error = 0;
				if(nearestValues.getJSONObject("nearestSmallerValue").has("procedureName") && nearestValues.getJSONObject("nearestGreaterValue").has("procedureName")){
					resultArraySingleForecastPeriod[0] =  nearestValues.getJSONObject("nearestSmallerValue").getDouble("value");
					resultArraySingleForecastPeriod[1] =  nearestValues.getJSONObject("nearestGreaterValue").getDouble("value");
					if(allGreater) {
						resultArraySingleForecastPeriod[numberOfIndependentVariables] = -1;
					}else {
						resultArraySingleForecastPeriod[numberOfIndependentVariables] = 1;
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
				    
				   
				    //for(int i = 0; i<(solution.getPoint().length-1);i++) {
				    //	System.out.println("X"+i+": " + solution.getPoint()[i]);
				    //	proceduresWeights.put(procedureNames.get(i), solution.getPoint()[i]);
				    //}
				    proceduresWeights.put(nearestValues.getJSONObject("nearestSmallerValue").getString("procedureName"), solution.getPoint()[0]);
				    proceduresWeights.put(nearestValues.getJSONObject("nearestGreaterValue").getString("procedureName"), solution.getPoint()[1]);
				    
				    System.out.println(solution.getValue());
				    //double[] weights = solution.getPoint();
				    solutionValue = solution.getValue();
				    error = solutionValue + dependentVariable;
			   
				} else {
					if(nearestValues.getJSONObject("nearestSmallerValue").has("procedureName")) {
						 proceduresWeights.put(nearestValues.getJSONObject("nearestSmallerValue").getString("procedureName"), 1);
						 error = dependentVariable - nearestValues.getJSONObject("nearestSmallerValue").getDouble("value");
					}
					if(nearestValues.getJSONObject("nearestGreaterValue").has("procedureName")) {
						 proceduresWeights.put(nearestValues.getJSONObject("nearestGreaterValue").getString("procedureName"), 1);
						 error = nearestValues.getJSONObject("nearestGreaterValue").getDouble("value")- dependentVariable;
					}
				}
			    for(String procedureName : procedureNames) {
			    	if(!proceduresWeights.has(procedureName)) {
			    		proceduresWeights.put(procedureName, 0.0);
			    	}
			    }
			   
			    
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
		    		for(String procedureName : optimizedAverageWeights.keySet()) { 
		    		//for(int i = 0; i<( optimizedAverageWeights.length());i++) {
		    			JSONObject procedureResults = optimizedAverageWeights.getJSONObject(procedureName);
		    			double newValue = 0;
		    			if(procedureNames.contains(procedureName)) {
		    				newValue = proceduresWeights.getDouble(procedureName) * factor;
		    			}
		    			double oldValue = procedureResults.getDouble("oldValue");
		    			double currentWeight = (oldValue * (0.5-decay) + newValue *(0.5 + decay));
		    			double total = procedureResults.getDouble("total");
		    				
			    		procedureResults.put("oldValue", currentWeight);
			    		procedureResults.put("total", currentWeight + total);
			    		optimizedAverageWeights.put(procedureName, procedureResults);
		    		 }
		    	}else {
		    		JSONObject optimizedAverageWeights = new JSONObject();
		    		for(String procedureName : proceduresWeights.keySet()) {
		    			JSONObject procedureResults = new JSONObject();
			    		double value = proceduresWeights.getDouble(procedureName) * factor;
			    		procedureResults.put("oldValue", value);
			    		procedureResults.put("total", value);
			    		optimizedAverageWeights.put(procedureName, procedureResults);
		    		 }
		    		optimizedWeightsTargetVariable.put("averagedWeights", optimizedAverageWeights);
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
	
	//ToDo: Remove? Concurrent optimization not working??
	private static JSONObject calculateAverageWeightsAllPeriods(JSONObject weightCalculationValues, String forecastDate, ArrayList<String> serviceNames, String username) {
		JSONObject optimizedWeights = new JSONObject();
		for(String targetVariableName : weightCalculationValues.keySet()) {
			
			JSONObject optimizedWeightsTargetVariable = new JSONObject();
			optimizedWeights.put(targetVariableName, optimizedWeightsTargetVariable);
		    //JSONObject optimizedAverageWeights = new JSONObject();
		    JSONObject optimizedSingleWeights = new JSONObject();
		    optimizedWeightsTargetVariable.put("singleWeights", optimizedSingleWeights);
		    //optimizedWeightsTargetVariable.put("averagedWeights", optimizedAverageWeights);
			

			JSONObject targetVariables = weightCalculationValues.getJSONObject(targetVariableName);	
			int numberOfIndependentVariables = serviceNames.size();
			int entryCounter = 0;
			ArrayList<OptimizationData> objectives = new ArrayList<OptimizationData>();
			ArrayList<LinearConstraint> constraints = new ArrayList<>(); 
			JSONObject proceduresWeights = new JSONObject();
			
			for(String dateString : targetVariables.keySet()) {
				if(!(targetVariables.getJSONObject(dateString).getJSONObject("independentVariables").length()<=0)) {
					JSONObject forecastEntry = targetVariables.getJSONObject(dateString);
					double dependentVariable = forecastEntry.getDouble("dependentVariable");
					JSONObject independentVariables = forecastEntry.getJSONObject("independentVariables");
					
					
					double[] resultArraySingleForecastPeriod = new double[numberOfIndependentVariables + 1];
					Arrays.fill(resultArraySingleForecastPeriod, 0);
					boolean allGreater = true;
					for(String procedureName : independentVariables.keySet()) {		
						double result = independentVariables.getDouble(procedureName);
						if(result<dependentVariable) {
							allGreater = false;
						}
						resultArraySingleForecastPeriod[serviceNames.indexOf(procedureName)] = result;
					}
					if(allGreater) {
						resultArraySingleForecastPeriod[numberOfIndependentVariables] = -1;
					}else {
						resultArraySingleForecastPeriod[numberOfIndependentVariables] = 1;
					}
					
					
					//Condition ensuring that targetVariable is met
					constraints.add(new LinearConstraint(resultArraySingleForecastPeriod, Relationship.EQ, dependentVariable));
					
				
					

					
				}
				
			}
			
			double[] dummies = new double[numberOfIndependentVariables+1];
			Arrays.fill(dummies, 1);	
			dummies[numberOfIndependentVariables]=0;
			constraints.add(new LinearConstraint(dummies, Relationship.EQ, 1));
			
			
			//Prepare Constraint ensuring positive Coefficient Terms
			for(int i = 0; i<numberOfIndependentVariables;i++) {
				dummies = new double[numberOfIndependentVariables+1];
				dummies[i]=1;
				constraints.add(new LinearConstraint(dummies, Relationship.LEQ, 1));
				constraints.add(new LinearConstraint(dummies, Relationship.GEQ, 0));
			}	
			//Add Conditions to OptimizationData
			//objectives.add(OptimizeWeights);
			dummies = new double[numberOfIndependentVariables+1];
			Arrays.fill(dummies, 0);	
			dummies[numberOfIndependentVariables]=1;
			LinearObjectiveFunction MinimizeError = new LinearObjectiveFunction(dummies, 0);
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
		    
		   
		    for(int i = 0; i<(solution.getPoint().length-1);i++) {
		    	System.out.println(serviceNames.get(i)+": " + solution.getPoint()[i]);
		    	proceduresWeights.put(serviceNames.get(i), solution.getPoint()[i]);
		    }
		    
		    System.out.println(solution.getValue());
		    //double[] weights = solution.getPoint();
		    double solutionValue = solution.getValue();
		    double error = solutionValue;
		   
						    
		    JSONObject optimizedPeriodWeights = new JSONObject();
		    
		    if(!optimizedWeightsTargetVariable.has(forecastDate)) {
		    	optimizedWeightsTargetVariable.put(forecastDate, optimizedPeriodWeights);
		    }else {
		    	optimizedPeriodWeights = optimizedWeightsTargetVariable.getJSONObject(forecastDate);
		    }
		    optimizedSingleWeights.put(forecastDate, optimizedPeriodWeights);
		    optimizedPeriodWeights.put("weights", proceduresWeights);
		    optimizedPeriodWeights.put("error", error);
		   
		   
			entryCounter = entryCounter + 1;
	
			/*OLSMultipleLinearRegression multipleRegression = new OLSMultipleLinearRegression();
			multipleRegression.setNoIntercept(true);
			multipleRegression.newSampleData(dependentVariableArray,independentVariableArray);
			double[] weights = multipleRegression.estimateRegressionParameters();
			*/
			 

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
	
	public static JSONObject prepare4MultiPeriodForecasting(JSONObject forecastResult, ArrayList<String> serviceNames, String username/*JSONObject weights*/) throws JSONException, ClassNotFoundException, SQLException, ParseException {
		JSONObject combinedResult = new JSONObject();
		JSONObject preparedStructure = new JSONObject();
		String configurationString = "";
		
		JSONObject weights = new JSONObject();
		for(String procedureName : forecastResult.keySet()) {
			JSONObject procedureResults = forecastResult.getJSONObject(procedureName);
			for(String dateString : procedureResults.keySet()) {
				if(!weights.has(dateString)) {
					weights.put(dateString, ServiceCombiner.getAveragedWeights(dateString, serviceNames.toString(), username));
				}
				combinedResult.put(dateString, new JSONObject());
				JSONObject dateResult = procedureResults.getJSONObject(dateString);
				for(String configuration : dateResult.keySet()) {
					configurationString = configuration;
					combinedResult.getJSONObject(dateString).put(configurationString, new JSONObject());
					JSONObject configurationResult = dateResult.getJSONObject(configuration);
					//preparedStructure.put(procedureName, configurationResult);
					
					if(!preparedStructure.has(dateString)) {
						preparedStructure.put(dateString, new JSONObject());
					}
					JSONObject preparedDateResult = preparedStructure.getJSONObject(dateString);
					if(!preparedDateResult.has(procedureName)) {
						preparedDateResult.put(procedureName, configurationResult);
					}
				}
				
			}
		}
		for(String dateString : preparedStructure.keySet()) {
			JSONObject combinedResultSingle =  ServiceCombiner.calculateCombinedResultDynamicWeights(preparedStructure.getJSONObject(dateString), weights.getJSONObject(dateString));
			combinedResult.put(dateString, combinedResultSingle);
		}
		return combinedResult;
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
		double factor = 0;
		for(String procedureResult : combinedAnalysisResult.keySet()) {
			JSONObject procedure = combinedAnalysisResult.getJSONObject(procedureResult);
			//ToDo if weights == null => factor = 1/anzahlProcedures new result = factor * oldResult
			for(String targetVariableName : procedure.keySet()) {
				double weight = 0;
				if(weights!=null) {
					
					if(weights.getJSONObject(targetVariableName).has("averagedWeights")){
						String procedureName = procedureResult;
						if(procedureResult.contains("Result")) {
							procedureName = procedureResult.substring(0, (procedureResult.length()-6));
						}
						System.out.println(targetVariableName);
						if(weights.getJSONObject(targetVariableName).getJSONObject("averagedWeights").has(procedureName)) {
						//weight = weights.getJSONObject(targetVariableName).getJSONObject("averagedWeights").getJSONObject(procedureName).getDouble("total");
							weight = weights.getJSONObject(targetVariableName).getJSONObject("averagedWeights").getDouble(procedureName);
						}
					}
				}else {
					factor = 1/combinedAnalysisResult.length();
				}
				
				if(!combinedResult.has(targetVariableName)) {
					combinedResult.put(targetVariableName, new JSONObject());
				}
				JSONObject targetVariable = procedure.getJSONObject(targetVariableName);
				for(String forecastPeriod : targetVariable.keySet()){
					if(combinedResult.getJSONObject(targetVariableName).has(forecastPeriod)) {
						double oldResult = combinedResult.getJSONObject(targetVariableName).getDouble(forecastPeriod);
						double newResult = 0;
						if(weights!=null) {
							newResult = targetVariable.getDouble(forecastPeriod) * weight;
						}else {
							newResult = targetVariable.getDouble(forecastPeriod) * factor;
						}
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
