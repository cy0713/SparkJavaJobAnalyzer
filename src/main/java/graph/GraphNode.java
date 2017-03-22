package main.java.graph;

public class GraphNode {
	
	//RDD operations can be only transformations or actions
	private boolean transformation = true;
	
	private String toExecute = "";
	
	private String functionType = "";
	
	private GraphNode nextNode;
	
	private GraphNode previousNode;
	
	
	/*Access methods*/

	public boolean isTransformation() {
		return transformation;
	}

	public void setTransformation(boolean transformation) {
		this.transformation = transformation;
	}

	public GraphNode getNextNode() {
		return nextNode;
	}

	public void setNextNode(GraphNode nextNode) {
		this.nextNode = nextNode;
	}

	public String getToExecute() {
		return toExecute;
	}

	public void setToExecute(String toExecute) {
		this.toExecute = toExecute;
	}
	
	public String getFunctionName() {
		return toExecute.substring(0, toExecute.indexOf("("));
	}

	public String getFunctionType() {
		return functionType;
	}

	public void setFunctionType(String functionType) {
		this.functionType = functionType;
	}

	public GraphNode getPreviousNode() {
		return previousNode;
	}

	public void setPreviousNode(GraphNode previousNode) {
		this.previousNode = previousNode;
	}

	@Override
	public String toString() {
		return "GraphNode [transformation=" + transformation + ", toExecute=" + toExecute + ", functionType="
				+ functionType + ", nextNode=" + nextNode + "]";
	}	
	
	
}