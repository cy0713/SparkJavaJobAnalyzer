package main.java.rules.reverse.sparkjava;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class Foreach implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		String replacement = graphNode.getLambdaSignature().replace("forEach", "foreach");
		graphNode.setCodeReplacement(replacement);
	}

}
