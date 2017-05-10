package main.java.rules.modification.javastreams;

import main.java.graph.GraphNode;

public class Count extends ActionModificationRuleJavastreams {

	@Override
	public void applyRule(GraphNode graphNode) {
		graphNode.setCodeReplacement("count()");
	}

}
