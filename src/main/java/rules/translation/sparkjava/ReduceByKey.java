package main.java.rules.translation.sparkjava;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class ReduceByKey implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		//The most similar call for a java8 program of this call is a collector
		StringBuilder replacement = new StringBuilder("collect");
		
		//The collector should group by key
		replacement.append("(java.util.stream.Collectors.groupingBy(");
		String tupleType = null;
		GraphNode toExplore = graphNode;
		while (toExplore.getPreviousNode()!=null){
			//FIXME: Do this with regex
			if (toExplore.getCodeReplacement().contains("new SimpleEntry")){
				tupleType = toExplore.getCodeReplacement().substring(
						toExplore.getCodeReplacement().indexOf("SimpleEntry"), 
						toExplore.getCodeReplacement().lastIndexOf(">")+1);
			}
			toExplore = toExplore.getPreviousNode();
		}
		if (tupleType == null) 
			System.err.println("ERROR: Unknown collector for translation in ReduceByKey: " 
						+ graphNode.getLambdaSignature());
		
		replacement.append(tupleType + "::getKey, ");
		//Infer the correct built-in collector from the specified function
		String collector = null;
		String reduceFunction = graphNode.getLambdaSignature()
										 .substring(graphNode.getLambdaSignature().indexOf("->"));
		
		//TODO: Add more collectors for other reduce functions
		if (reduceFunction.matches("->\\s*\\(?.\\s*\\+.\\s*.\\s*\\)?"))
			collector = "java.util.stream.Collectors.counting()";
		
		if (collector == null) 
			System.err.println("ERROR: Unknown collector for translation in ReduceByKey: " + reduceFunction);
		
		replacement.append(collector + "))");
		graphNode.setCodeReplacement(replacement.toString());
	}

}
