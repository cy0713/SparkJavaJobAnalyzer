package main.java.rules.reverse.sparkjava;

import main.java.graph.GraphNode;

public class Count extends ActionModificationRuleSpark {

	@Override
	public void applyRule(GraphNode graphNode) {
		graphNode.setCodeReplacement("");
	}

}
