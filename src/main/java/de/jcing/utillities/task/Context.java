package de.jcing.utillities.task;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class Context {

	private CopyOnWriteArrayList<Runnable> toRun;
	private CopyOnWriteArrayList<Runnable> toLoop;
	private CopyOnWriteArrayList<Runnable> preLoop;
	private CopyOnWriteArrayList<Runnable> postLoop;

	protected Context() {
		toRun = new CopyOnWriteArrayList<Runnable>();
		toLoop = new CopyOnWriteArrayList<Runnable>();
		preLoop = new CopyOnWriteArrayList<Runnable>();
		postLoop = new CopyOnWriteArrayList<Runnable>();
	}

	public void run(Runnable... run) {
		for (Runnable r : run)
			toRun.add(r);

	}

	public void loop(Runnable... run) {
		for (Runnable r : run)
			toLoop.add(r);

	}

	public void preLoop(Runnable... run) {
		for (Runnable r : run)
			preLoop.add(r);

	}

	public void postLoop(Runnable... run) {
		for (Runnable r : run)
			postLoop.add(r);

	}

	protected void exec() {
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
