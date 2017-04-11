package main.java.rules.migration;

import java.lang.reflect.Method;
import java.util.stream.Collectors;

import main.java.graph.GraphNode;
import main.java.rules.LambdaRule;

public class Collect implements LambdaRule {

	@Override
	public void applyRule(GraphNode graphNode) {
		//Add the class Collectors to all calls to ease compilation
		String cleanSignature = graphNode.getLambdaSignature();
		for (Method method: Collectors.class.getMethods()){
			if (cleanSignature.contains(method.getName()) &&
					!cleanSignature.contains("Collectors."+method.getName())){
				cleanSignature = cleanSignature.replace(method.getName(), 
						"Collectors."+method.getName());
			}
		}
		System.out.println("CLEAN SIGNATURE COLLECTOR: " + cleanSignature);
		graphNode.setToPushdown(cleanSignature);
	}

}
