package main.java.graph;

public class GraphNode {
	
	//RDD operations can be only transformations or actions
	private boolean transformation = true;
	
	private String lambdaSignature;	
	private String functionType;
	
	private GraphNode nextNode;	
	private GraphNode previousNode;
	
	private String toPushdown;
	private String codeReplacement = "";
	
	
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

	public String getLambdaSignature() {
		return lambdaSignature;
	}

	public void setLambdaSignature(String toExecute) {
		this.lambdaSignature = toExecute;
	}
	
	public String getFunctionName() {
		return lambdaSignature.substring(0, lambdaSignature.indexOf("("));
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
		return "GraphNode [transformation=" + transformation + ", toExecute=" + lambdaSignature + ", functionType="
				+ functionType + ", nextNode=" + nextNode + "]";
	}

	public String getToPushdown() {
		return toPushdown;
	}

	public void setToPushdown(String toPushdown) {
		this.toPushdown = toPushdown;
	}

	public String getCodeReplacement() {
		return codeReplacement;
	}

	public void setCodeReplacement(String codeReplacement) {
		this.codeReplacement = codeReplacement;
	}
	
}