package main.java.compiler;

import java.io.File;

import net.openhft.compiler.CachedCompiler;
import net.openhft.compiler.CompilerUtils;

public class JobCompiler {
	
	 public Object compileFromString(String className, String javaCode) {
		try {
			javaCode = javaCode.replace(className, className+"_");
			Class aClass = CompilerUtils.CACHED_COMPILER.loadFromJava("test.resources.java8streams_jobs."+className+"_", javaCode);
			Object obj = aClass.newInstance();
			return obj;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	 }
}
