package main.java.rules.modification.sparkjava;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class Filter implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		graphNode.setCodeReplacement(graphNode.getLambdaSignature());
	}

}
