package main.java.rules.modification.javastreams;

import java.util.List;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;
import main.java.utils.Utils;

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

	//TODO: At the moment we can work with simple types, Lists and SimpleEntry
	protected String instantiationSignature(String lastParameter) {
		//This serves for simple times, like Integer or Long
		if (!lastParameter.contains(",") && !lastParameter.contains("<"))
			return "new " + lastParameter + "(s)";
		//At the moment, only consider simple type parameters like Integer, String or Long
		if (lastParameter.startsWith("java.util.AbstractMap.SimpleEntry")){
			List<String> params = Utils.getParametersFromSignature(
					lastParameter.replace("java.util.AbstractMap.SimpleEntry<", "").replace(">", ""));
			String result = "new java.util.AbstractMap.SimpleEntry<" + params.get(0) +"," + params.get(1)+ ">(";
			int index = 0;
			for (String p: params){
				if (p.equals("java.lang.String")) result += "s.split(\"=\")[" + index +"],";
				else result += p + ".valueOf(s.split(\"=\")[" + index +"]), ";
				index++;
			}
			System.err.println(result);
			return result.substring(0, result.length()-2) + ")";
		}
		System.err.println("PROBLEM DOING CONVERSION IN: " + this.getClass().getName());
		return "";
	}
}