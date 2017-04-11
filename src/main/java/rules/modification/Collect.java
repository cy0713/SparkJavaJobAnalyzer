package main.java.rules.modification;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class Collect implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		//In general, the collector should be executed both at the storage and compute sides
		graphNode.setCodeReplacement(graphNode.getLambdaSignature());
		//1) Exception: A groupBy + counting can be replaced by a groupBy + summingXXX
		//as the summingXXX will aggregate the partial counting of all storlets
		if (graphNode.getLambdaSignature().equals("collect(groupingBy(SimpleEntry<String, Long>::getKey, counting()))"))
			graphNode.setCodeReplacement("collect(groupingBy(SimpleEntry<String, Long>::getKey, summingLong(SimpleEntry::getValue)))");
		
	}
}
