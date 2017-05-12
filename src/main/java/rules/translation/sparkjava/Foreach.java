package main.java.rules.translation.sparkjava;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class Foreach implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		String replacement = graphNode.getLambdaSignature().replace("foreach", "forEach");
		graphNode.setCodeReplacement(replacement);
	}

}
