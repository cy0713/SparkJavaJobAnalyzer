package main.java.rules.reverse.sparkjava;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class Count extends ActionModificationRuleSpark {

	@Override
	public void applyRule(GraphNode graphNode) {
		graphNode.setCodeReplacement("");
	}

}
