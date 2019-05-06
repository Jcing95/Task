package de.jcing.utillities.task;

import java.util.Iterator;
import java.util.LinkedList;

public class Context {

	private LinkedList<Runnable> toRun;
	private LinkedList<Runnable> toLoop;
	private LinkedList<Runnable> preLoop;
	private LinkedList<Runnable> postLoop;
	

	private final Object BLOCKER = new Object();

	protected Context() {
		toRun = new LinkedList<Runnable>();
		toLoop = new LinkedList<Runnable>();
		preLoop = new LinkedList<Runnable>();
		postLoop = new LinkedList<Runnable>();
	}

	public void run(Runnable... run) {
		synchronized (BLOCKER) {
			for (Runnable r : run)
				toRun.add(r);
		}
	}

	public void loop(Runnable... run) {
		synchronized (BLOCKER) {
			for (Runnable r : run)
				toLoop.add(r);
		}
	}
	
	public void preLoop(Runnable...run) {
		synchronized (BLOCKER) {
			for (Runnable r : run)
				preLoop.add(r);
		}
	}
	
	public void postLoop(Runnable...run) {
		synchronized (BLOCKER) {
			for (Runnable r : run)
				postLoop.add(r);
		}
	}
	
	protected void exec() {
		synchronized (BLOCKER) {
			while (!toRun.isEmpty()) {
				toRun.removeFirst().run();
			}
			
			for (Runnable r : preLoop) {
				r.run();
			}
			for (Runnable r : toLoop) {
				r.run();
			}
			for (Runnable r : postLoop) {
				r.run();
			}
		}
	}

	public int loopSize() {
		return toLoop.size();
	}

	public int runSize() {
		return toRun.size();
	}

	public Iterator<Runnable> getLoopIterator() {
		return toLoop.iterator();
	}

	public Iterator<Runnable> getToRunIterator() {
		return toRun.iterator();
	}

}
