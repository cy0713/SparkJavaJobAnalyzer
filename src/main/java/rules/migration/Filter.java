package main.java.rules.migration;

import java.util.HashSet;
import java.util.Set;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class Filter implements LambdaRule {

	/**
	 * Implementation of filter lambda migrations.
	 * 
	 * There are important aspects to decide whether to pushdown
	 * a filter lambda to the storage or not: i) does filtering affect
	 * to other computations/results on the same RDD? 
	 * 
	 */
	
	private boolean canExecuteFilter = true;
	
	private static Set<String> incompatibleOperators = new HashSet<>();
	
	/*Place to add those operations that would retrieve an incorrect results
	 * if we execute more filter lambdas after their execution.
	 */
	static {
		/*
		 * If we execute N filters at the storage side and then a count(), the
		 * result will be the same as if these filters were executed at Spark.
		 * However, if we execute further filters at the storage side that
		 * are defined beyond the count() call, then the count() will be affected
		 * by these filters, as they are executed before at the storage side.
		 * This will lead us to an erroneous result of the count() action.
		 */
		incompatibleOperators.add("count");
	}
	
	@Override
	public void applyRule(GraphNode graphNode) {
		//If no pushdown is possible, return null
		if (!canExecuteFilter) return;

		boolean continueOperationLookup = graphNode.getNextNode()!=null;
		//Look for incompatible operations with current or next filters
		GraphNode pointer = graphNode.getNextNode();
		while (continueOperationLookup){
			//Check whether further filter lambdas can be pushed down or not
			if (incompatibleOperators.contains(pointer.getFunctionName())){
				continueOperationLookup = canExecuteFilter = false;
			} else if (pointer.getFunctionName().equals("filter") || pointer.getNextNode()==null){
				continueOperationLookup = false;
			}
			pointer = pointer.getNextNode();
		}
		graphNode.setToPushdown(graphNode.getLambdaSignature());
	}
}