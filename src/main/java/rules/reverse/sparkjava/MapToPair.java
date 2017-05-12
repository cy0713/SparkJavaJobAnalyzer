package main.java.rules.reverse.sparkjava;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;
import main.java.utils.Utils;

public class MapToPair implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		//FIXME: Do this with regex
		String result = graphNode.getLambdaSignature().substring(
				graphNode.getLambdaSignature().indexOf("new "),
				graphNode.getLambdaSignature().lastIndexOf(">")+2);		
		int index = 0;
		String variableName = graphNode.getLambdaSignature().substring(
				graphNode.getLambdaSignature().indexOf("("),
				graphNode.getLambdaSignature().lastIndexOf("->"));
		variableName = variableName.replace(" ", "");
		String paramsSequence = result.substring(0, result.length()-2).replace("new Tuple2<", "");
		for (String p: Utils.getParametersFromSignature(paramsSequence)){
			if (p.equals("java.lang.String") || p.equals("String")) 
				result += variableName+".split(\"=\")[" + index +"],";
			else result += p + ".valueOf(" + variableName + ".split(\"=\")[" + index +"]), ";
			index++;
		}		
		graphNode.setCodeReplacement("mapToPair(" + variableName+ " -> " + 
					result.substring(0, result.length()-2) + "))");
	}
}
