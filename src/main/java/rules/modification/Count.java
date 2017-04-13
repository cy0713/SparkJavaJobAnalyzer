package main.java.rules.modification;

import main.java.graph.GraphNode;

public class Count extends ActionModificationRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		graphNode.setCodeReplacement("count()");
	}

}
