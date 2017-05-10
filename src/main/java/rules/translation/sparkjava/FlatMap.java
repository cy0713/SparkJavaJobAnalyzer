package main.java.rules.translation.sparkjava;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class FlatMap implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		String replacement = graphNode.getLambdaSignature();
		if (replacement.contains(".iterator()")){
			replacement = replacement.replace(".iterator()", "");
			if (replacement.contains("Arrays.asList")) {
				replacement = replacement.replace("Arrays.asList", "Arrays.stream");
			}else replacement = "Streams.of(" + replacement +")";
		}
		graphNode.setCodeReplacement(replacement);
	}

}
