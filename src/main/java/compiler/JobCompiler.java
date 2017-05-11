package main.java.compiler;

import net.openhft.compiler.CachedCompiler;
import net.openhft.compiler.CompilerUtils;

/**
 * Class that provides a simple compilation services for java classes
 * in String objects.
 * 
 * @author Raul Gracia
 *
 */
public class JobCompiler {
	
	private static final String COMPILED_JOB_PATH = "test.resources.test_jobs";
	
	private static CachedCompiler compiler = CompilerUtils.CACHED_COMPILER;
	
	public Object compileFromString(String className, String javaCode) {
		return compileFromString(COMPILED_JOB_PATH, className, javaCode);
	 }
	
	public Object compileFromString(String classPath, String className, String javaCode) {
		try {
			javaCode = javaCode.replace(className, className+"_");
			Class aClass = compiler.loadFromJava(classPath+"."+className+"_", javaCode);
			Object obj = aClass.newInstance();
			return obj;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	 }
}