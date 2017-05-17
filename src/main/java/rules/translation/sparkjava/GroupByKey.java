package main.java.rules.translation.sparkjava;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class GroupByKey implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		graphNode.setCodeReplacement("");
	}
	//Makes no much sense to off-load this operation, given its complexity and that it does not
	//reduce network traffic, only reorganized data that can be better done in SPark
	/*	//The most similar call for a java8 program of this call is a collector
		StringBuilder replacement = new StringBuilder("collect");
		
		//The collector should group by key
		replacement.append("(java.util.stream.Collectors.groupingBy(");
		String tupleType = null;
		GraphNode toExplore = graphNode;
		while (toExplore!=null){
			//FIXME: Do this with regex
			if (toExplore.getCodeReplacement().contains("new SimpleEntry")){
				tupleType = toExplore.getCodeReplacement().substring(
						toExplore.getCodeReplacement().indexOf("SimpleEntry"), 
						toExplore.getCodeReplacement().lastIndexOf(">")+1);
			}
			toExplore = toExplore.getPreviousNode();
		}
		//TODO: If the type is implicit, then look for the type of the RDD
		if (tupleType == null) 
			System.err.println("ERROR: Unknown collector for translation in ReduceByKey: " 
						+ graphNode.getLambdaSignature());
		
		replacement.append(tupleType + "::getKey, ");
		
		//We have to group all values related to the same key
		String collector = "java.util.stream.Collectors.mapping(" + tupleType 
				+"::getValue, java.util.stream.Collectors.toList())))";
		
		replacement.append(collector);
		graphNode.setCodeReplacement(replacement.toString());
	}*/
}