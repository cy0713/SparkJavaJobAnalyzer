package main.java.graph;

import java.util.Iterator;

public class FlowControlGraph implements Iterable<GraphNode>{
	
	private String rdd = "";
	
	private GraphNode root;
	
	private GraphNode lastNode;
	
	public FlowControlGraph(String rdd) {
		this.rdd = rdd;
	}

	/**
	 * Add a new node to the flow control graph.
	 * 
	 * @param operation
	 * @param transformation
	 */
	public void appendOperationToRDD(String operation, boolean transformation){
		GraphNode toAdd = new GraphNode();
		toAdd.setTransformation(transformation);
		toAdd.setToExecute(operation);
		if (root==null) {
			toAdd.setPreviousNode(toAdd);
			toAdd.setNextNode(toAdd);
		}else{
			lastNode.setNextNode(toAdd);
			toAdd.setPreviousNode(lastNode);
			lastNode = toAdd;
		}
	}
	
	/**
	 * Add a new node to the flow control graph.
	 * 
	 * @param operation
	 * @param transformation
	 */
	public void appendOperationToRDD(String operation, String functionType, boolean transformation){
		GraphNode toAdd = new GraphNode();
		toAdd.setTransformation(transformation);
		toAdd.setToExecute(operation);
		toAdd.setFunctionType(functionType);
		if (root==null) {
			root = lastNode = toAdd;
		}else{
			lastNode.setNextNode(toAdd);
			toAdd.setPreviousNode(lastNode);
			lastNode = toAdd;
		}
	}
	
	@Override
	public Iterator<GraphNode> iterator() {
		return new FlowControlGraphIterator(this);
	} 
	
	public String toString(){
		StringBuilder output = new StringBuilder("Graph for RDD: " + this.rdd + "\n{\n ");
		boolean finishLoop = root == null;
		GraphNode pointer = root;
		while (!finishLoop){
			output.append(pointer.getToExecute() + " -> type of lambda: " + pointer.getFunctionType()
					+ ". Is transformation? " + pointer.isTransformation());
			output.append("\n ^ \n");
			pointer = pointer.getNextNode();
			finishLoop = pointer == null;
		}
		output.append("\n}");
		return output.toString();		
	}
	
	/*Access methods*/
	
	public String getRdd() {
		return rdd;
	}

	public void setRdd(String rdd) {
		this.rdd = rdd;
	}

	public GraphNode getRoot() {
		return root;
	}

	public void setRoot(GraphNode root) {
		this.root = root;
	}
}

class FlowControlGraphIterator implements Iterator<GraphNode>{
	
	private GraphNode pointer;

	public FlowControlGraphIterator(FlowControlGraph flowControlGraph) {
		this.pointer = flowControlGraph.getRoot();
	}

	@Override
	public boolean hasNext() {
		return pointer!=null;
	}

	@Override
	public GraphNode next() {
		GraphNode current = pointer;
		pointer = pointer.getNextNode();
		return current;
	}
	
}
