package main.java.rules.migration.javastreams;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class ForEach implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		graphNode.setToPushdown("");
	}

}
