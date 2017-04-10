package main.java.rules.modification;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class Collect implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		// TODO Auto-generated method stub
		if (graphNode.getLambdaSignature().equals("collect(groupingBy(SimpleEntry<String, Long>::getKey, counting()))"))
			graphNode.setCodeReplacement("collect(groupingBy(SimpleEntry<String, Long>::getKey, summingLong(SimpleEntry::getValue)))");
	}

}
