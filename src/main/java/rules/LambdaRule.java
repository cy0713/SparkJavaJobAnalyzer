package main.java.rules;

import main.java.graph.GraphNode;

public interface LambdaRule {
	
	/**
	 * The methods implementing this interface receive as input the
	 * {@link FlowControlGraph} of the operations executed on an RDD. 
	 * Based on that information, this class should return the list of
	 * transformations that can be safely pushed down to the storage 
	 * without changing the result compared to the original job where
	 * all the computations are executed at the Spark side. Note that
	 * classes might change the graph to achieve pushing down some
	 * computation to the storage, which requires the SparkJobAnalyzer
	 * class to reflect the changes in the code of the job afterwards. 
	 * 
	 * @param graphNode
	 * @return list of lambdas to execute at the storage side
	 */
	public void applyRule(GraphNode graphNode);

}