package main.java.rules.reverse.sparkjava;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class FlatMap implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		graphNode.setCodeReplacement("");
	}

}
