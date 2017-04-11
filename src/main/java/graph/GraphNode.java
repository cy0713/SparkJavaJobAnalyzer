package main.java.graph;

/**
 * Each node within a graph represents an operation executed
 * in an RDD. The node contains information about the type of the
 * operation, as well as meta information to be filled by
 * migration/modification rules.
 * 
 * @author Raul Gracia
 *
 */
public class GraphNode {
	
	//RDD operations can be only transformations or actions
	//private boolean pushable = true;
	private boolean terminal = false;
	
	private String lambdaSignature;	
	private String functionType;
	
	private GraphNode nextNode;	
	private GraphNode previousNode;
	
	private String toPushdown;
	private String codeReplacement = "";	
	
	/*Access methods*/

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
		return "GraphNode [terminal=" + terminal + 
			", toExecute=" + lambdaSignature + ", functionType="
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

	public void setTerminal(boolean terminal) {
		this.terminal = terminal;
	}

	public boolean isTerminal() {
		return terminal;
	}	
}