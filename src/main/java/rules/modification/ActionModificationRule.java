package main.java.rules.modification;

import java.util.List;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class ActionModificationRule implements LambdaRule{
	
	@Override
	public void applyRule(GraphNode graphNode) {
		//We need a map for the last type prior to the collector/action
		List<String> nodeParams = graphNode.getPreviousNode().getTypeParametersAsList();
		if (!nodeParams.get(nodeParams.size()-1).equals("java.lang.String")){
			String lastParameter = nodeParams.get(nodeParams.size()-1);
			graphNode.setCodeReplacement("map(s -> " + instantiationSignature(lastParameter.trim()) + ")."
					+ graphNode.getLambdaSignature());
		} else graphNode.setCodeReplacement(graphNode.getLambdaSignature());
	}

	//TODO: This should be more generic!
	protected String instantiationSignature(String lastParameter) {
		if (!lastParameter.contains(",") && !lastParameter.contains("<"))
			return "new " + lastParameter + "(s)";
		if (lastParameter.equals("java.util.AbstractMap.SimpleEntry<java.lang.String, java.lang.Long>"))
			return "new java.util.AbstractMap.SimpleEntry<java.lang.String, java.lang.Long>"
					+ "(s.split(\"=\")[0], Long.valueOf(s.split(\"=\")[1]))";
		System.err.println("PROBLEM DOING CONVERSION TYPE MAP!!!");
		return "";
	}
}