package main.java.graph.algorithms;

import java.util.HashMap;
import java.util.Iterator;

import main.java.graph.FlowControlGraph;
import main.java.graph.GraphNode;

public class SafeLambdaMigrationFinder {
	
	/**
	 * This algorithm traverses a flow control graph representing a RDD in order to
	 * to find the maximum number of lambdas to be migrated to the storage without
	 * impacting on the job results. At the moment the algorithm just cuts the graph
	 * after the point in which an RDD has assigned other RDDs. For instance:
	 * 
	 * RDD A = [get the data from storage]
	 * A.filter(...).map(...)
	 * RDD B = A <- We finish the graph for A at this point, and do nothing on B
	 * B.filter(...)
	 * A.count()
	 * 
	 * @param identifiedStreams
	 * @return
	 */
	public HashMap<String, FlowControlGraph> computeMigrationGraph(HashMap<String, FlowControlGraph> identifiedStreams) {
		//TODO: This algorithm may be a little more complex to be more optimized, perhaps
		HashMap<String, FlowControlGraph> resultStreamGraphs = new HashMap<>();
		for (String key: identifiedStreams.keySet()){
			FlowControlGraph graph = identifiedStreams.get(key);
			//RDDs that are derived are not necessary to iterate, we will do when we process the root ones
			if (graph.isLinked()) continue;
			Iterator<GraphNode> nodeIterator = graph.iterator();
			GraphNode lastSecureNode = null;
			while (nodeIterator.hasNext()){
				lastSecureNode = nodeIterator.next();
				//Basically, if there are RDDs that are assigned from this point in the graph or there is a terminal
				//operation, just avoid migrating lambdas to the storage
				if (!lastSecureNode.getAssignedRDDs().isEmpty() || lastSecureNode.isTerminal()){
					lastSecureNode.setNextNode(null);
					break;
				}
			}
			resultStreamGraphs.put(key, graph);
		}
		return resultStreamGraphs;
	}
}
