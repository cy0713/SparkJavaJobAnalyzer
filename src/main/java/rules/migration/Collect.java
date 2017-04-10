package main.java.rules.migration;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class Collect implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		// TODO Auto-generated method stub
		graphNode.setToPushdown(graphNode.getLambdaSignature());
	}

}
