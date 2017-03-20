package main.java.migration_rules;

import main.java.graph.GraphNode;

public class Map implements IPushableTransformation {

	@Override
	public String pushdown(GraphNode graphNode) {
		//TODO: Only return if the function to be executed is self-contained
		//i.e., that the function to apply in the map is not an external one
		return graphNode.getToExecute();
	}

}
