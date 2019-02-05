package de.jcing.utillities.task;

public interface Routine extends Runnable {
	
	public default void prepare() {};
	
	@Override
	public void run();
	
	public default void cleanUp() {};
	
}
