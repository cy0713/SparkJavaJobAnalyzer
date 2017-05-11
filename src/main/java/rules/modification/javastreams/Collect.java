package main.java.rules.modification.javastreams;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.java.graph.GraphNode;
import main.java.utils.Utils;

public class Collect extends ActionModificationRuleJavastreams {
	
	private Pattern groupingBy = Pattern.compile("collect\\s*\\(\\s*groupingBy\\s*");
	private Pattern groupingByLong = Pattern.compile("collect\\s*\\(\\s*java.util.stream.Collectors.groupingBy\\s*");
	
	private Pattern couting = Pattern.compile("\\w*counting\\s*\\(\\s*\\)");
	private Pattern countingLong = Pattern.compile("\\w*java.util.stream.Collectors.counting\\s*\\(\\s*\\)");

	@Override
	public void applyRule(GraphNode graphNode) {
		//Add map to adapt the proper type
		super.applyRule(graphNode);
		
		//Special job modification cases for groupBy collector
		Matcher groupingByMatcher = groupingBy.matcher(graphNode.getLambdaSignature());
		if (!groupingByMatcher.lookingAt()) 
			groupingByMatcher = groupingByLong.matcher(graphNode.getLambdaSignature());
		
		if (groupingByMatcher.lookingAt()){
			//CASE 1: A groupBy + counting can be replaced by a groupBy + summingXXX
			//as the summingXXX will aggregate the partial counting of all storlets	
			Matcher coutingMatcher = couting.matcher(graphNode.getLambdaSignature());
			boolean found = coutingMatcher.find();
			if (!found) {
				coutingMatcher = countingLong.matcher(graphNode.getLambdaSignature());
				found = coutingMatcher.find();
			}
			if (!found) return;
			String summingCollector = "java.util.stream.Collectors.summing";
			//All this stuff is to find the types of Tuples in a more or less general way
			int initIndexTupleTypes = graphNode.getLambdaSignature()
					.indexOf("SimpleEntry<") + "SimpleEntry<".length();
			int endIndexTupleTypes = initIndexTupleTypes;
			int braces = 1;
			while (braces>0){
				if (graphNode.getLambdaSignature().charAt(endIndexTupleTypes)=='<') braces++;
				if (graphNode.getLambdaSignature().charAt(endIndexTupleTypes)=='>') braces--;
				endIndexTupleTypes++;
			}
			String tupleType = graphNode.getLambdaSignature().substring(initIndexTupleTypes, endIndexTupleTypes-1);
			List<String> inputTypes = Utils.getParametersFromSignature(tupleType);
			String summingInputType = inputTypes.get(inputTypes.size()-1);
			if (summingInputType.contains("Integer")){
				summingCollector += "Int()";
			}else summingCollector += summingInputType + "(SimpleEntry::getValue)";	
			
			StringBuilder codeReplacement = new StringBuilder(graphNode.getCodeReplacement()
												.replace(graphNode.getLambdaSignature(), ""));
			codeReplacement.append(graphNode.getLambdaSignature().substring(0, coutingMatcher.start()));
			codeReplacement.append(summingCollector.replace(" ", ""));
			codeReplacement.append(graphNode.getLambdaSignature().substring(coutingMatcher.end()));
			graphNode.setCodeReplacement(codeReplacement.toString());	
		}
	}
}