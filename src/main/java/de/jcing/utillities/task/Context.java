package de.jcing.utillities.task;

import java.util.Iterator;
import java.util.ArrayList;

public class Context {

	private ArrayList<Runnable> toRun;
	private ArrayList<Runnable> toLoop;
	private ArrayList<Runnable> preLoop;
	private ArrayList<Runnable> postLoop;
	

	private final Object BLOCKER = new Object();

	protected Context() {
		toRun = new ArrayList<Runnable>();
		toLoop = new ArrayList<Runnable>();
		preLoop = new ArrayList<Runnable>();
		postLoop = new ArrayList<Runnable>();
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
				toRun.remove(0).run();
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
