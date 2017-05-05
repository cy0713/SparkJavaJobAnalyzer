package main.java.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Utils {
	
	static final String LAMBDA_TYPE_AND_BODY_SEPARATOR = "|";
	//TODO: There is a problem using "," when passing lambdas as Storlet parameters, as the
	//Storlet middleware treats every "," as a separation between key/value parameter pairs
	static final String COMMA_REPLACEMENT_IN_PARAMS = "'";
	
	public static List<String> getParametersFromSignature(String parameters) {
		List<String> result = new ArrayList<>();
		int openBr = 0;
		int inipos = 0, pos = 0;
		while (pos<parameters.length()) {
			if (parameters.charAt(pos)=='<') openBr++;
			if (parameters.charAt(pos)=='>') openBr--;
			if ((parameters.charAt(pos)==',' && openBr==0) || (pos == parameters.length()-1)){
				if (pos == parameters.length()-1) pos++;
				result.add(parameters.substring(inipos, pos));
				inipos = pos+1; //avoid the comma
			}
			pos++;
		}
		return result;	
	}
	
	
	/**
	 * This method is intended to return to an external program a JSON String response with
	 * both the lambdas to send to the storage and the final version of the job to execute
	 * at the Spark cluster.
	 * 
	 * @param lambdasToMigrate
	 * @param cu
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject encodeResponse(String originalJob, String modifiedJob, 
									List<SimpleEntry<String, String>> lambdasToMigrate) {
		JSONObject obj = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		
		for (SimpleEntry<String, String> lambda: lambdasToMigrate){
			System.out.println(lambda);
			JSONObject lambdaObj = new JSONObject();
			//TODO: The comma replacement is needed as the "," character is reserved in the
			//Storlet middleware to separate key/value pairs
			String lambdaSignature = (lambda.getValue() + LAMBDA_TYPE_AND_BODY_SEPARATOR 
					+ lambda.getKey()).replace(",", COMMA_REPLACEMENT_IN_PARAMS);
			System.err.println(lambdaSignature);
			lambdaObj.put("lambda-type-and-body", lambdaSignature);
			jsonArray.add(lambdaObj); 
		}
		//Separator between lambdas and the job source code
		obj.put("original-job-code", originalJob);	
		obj.put("pushdown-job-code", modifiedJob);	
		obj.put("lambdas", jsonArray);
		return obj;
	}
	
	public static List<SimpleEntry<String, String>> getLambdasToMigrate(JSONObject json){		
		List<SimpleEntry<String, String>> lambdasToMigrate = new ArrayList<>();
		JSONArray jsonArray = (JSONArray) json.get("lambdas");
		Iterator<JSONObject> it = jsonArray.listIterator();
		while (it.hasNext()){
			String lambdaSignature = (String) it.next().get("lambda-type-and-body");
			lambdaSignature = lambdaSignature.replace(COMMA_REPLACEMENT_IN_PARAMS, ",");
			lambdasToMigrate.add(new SimpleEntry<String, String>(
					lambdaSignature.substring(lambdaSignature.indexOf(LAMBDA_TYPE_AND_BODY_SEPARATOR)+1),
					lambdaSignature.substring(0, lambdaSignature.indexOf(LAMBDA_TYPE_AND_BODY_SEPARATOR))));
		} 		
		return lambdasToMigrate;
	}
	
	public static String getModifiedJobCode(JSONObject json){
		return (String) json.get("pushdown-job-code");
	}
}
