package main.java.rules.modification;

import java.util.AbstractMap.SimpleEntry;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class Map implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		//TODO: Only return if the function to be executed is self-contained
		//i.e., that the function to apply in the map is not an external one
		if (graphNode.getLambdaSignature().equals("map(word -> new SimpleEntry<String, Long>(word, (long) 1))"))
			graphNode.setCodeReplacement("map(word -> new SimpleEntry<String, Long>(word.split(\"=\")[0], Long.valueOf(word.split(\"=\")[1])))");
		if (graphNode.getLambdaSignature().equals("map(l -> new Integer(l.length()))"))
			graphNode.setCodeReplacement("map(l -> new Integer(l))");
			
			
	}

}
