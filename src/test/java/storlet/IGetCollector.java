package test.java.storlet;

import java.util.stream.Collector;

public interface IGetCollector {
	
	@SuppressWarnings("rawtypes")
	public Collector getCollector();

}
