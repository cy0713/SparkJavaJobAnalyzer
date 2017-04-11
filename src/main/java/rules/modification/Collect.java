package main.java.rules.modification;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class Collect implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		// TODO Auto-generated method stub
		if (graphNode.getLambdaSignature().equals("collect(groupingBy(SimpleEntry<String, Long>::getKey, counting()))"))
			graphNode.setCodeReplacement("collect(groupingBy(SimpleEntry<String, Long>::getKey, summingLong(SimpleEntry::getValue)))");
		if (graphNode.getLambdaSignature().equals("collect(Collectors.toList())"))
			graphNode.setCodeReplacement("collect(Collectors.toList())");
		if (graphNode.getLambdaSignature().equals("collect(Collectors.maxBy(String::compareTo))"))
			graphNode.setCodeReplacement("collect(Collectors.maxBy(String::compareTo))");
		if (graphNode.getLambdaSignature().equals("collect(Collectors.minBy(String::compareTo))"))
			graphNode.setCodeReplacement("collect(Collectors.minBy(String::compareTo))");
	}
}
