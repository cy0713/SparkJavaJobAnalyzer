package main.java.rules.migration;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class Map implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		//TODO: Only return if the function to be executed is self-contained
		//i.e., that the function to apply in the map is not an external one
		graphNode.setToPushdown(graphNode.getLambdaSignature());
	}

}
