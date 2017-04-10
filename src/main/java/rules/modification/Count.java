package main.java.rules.modification;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class Count implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		graphNode.setCodeReplacement("count()");
	}

}
