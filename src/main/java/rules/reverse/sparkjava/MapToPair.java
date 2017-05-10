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
				graphNode.getLambdaSignature().indexOf(">(")+1);
		
		int index = 0;
		for (String p: Utils.getParametersFromSignature(result.replace("new Tuple2<", ""))){
			if (p.equals("java.lang.String") || p.equals("String")) result += "s.split(\"=\")[" + index +"],";
			else result += p + ".valueOf(s.split(\"=\")[" + index +"]), ";
			index++;
		}
		System.err.println(result);		
		graphNode.setCodeReplacement("mapToPair(word -> " + result.substring(0, result.length()-2) + ")");
	}
}
