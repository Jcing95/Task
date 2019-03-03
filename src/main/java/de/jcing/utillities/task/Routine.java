package de.jcing.utillities.task;

public interface Routine extends Runnable {
	
	public void prepare();
	
	@Override
	public void run();
	
	public void cleanUp();
	
}
