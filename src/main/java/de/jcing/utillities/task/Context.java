package de.jcing.utillities.task;

import java.util.LinkedList;

public class Context {
	
	Task task;
	LinkedList<Runnable> toRun;
	private final Object BLOCKER = new Object();
	
	protected Context(Task t) {
		this.task = t;
		toRun = new LinkedList<Runnable>();
	}
		
	public void run(Runnable run) {
		synchronized (BLOCKER) {			
			toRun.push(run);
		}
	}
	
	protected void exec() {
		synchronized (BLOCKER) {
			while(!toRun.isEmpty()) {
				toRun.removeFirst().run();
			}
		}
	}
	
	
}
