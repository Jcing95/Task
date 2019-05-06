package de.jcing.utillities.task;

import java.util.LinkedList;

@Deprecated
public class TaskFactory {
	
	LinkedList<Runnable> runnables;
	Task t;
	boolean spread;
	int spreadedThreads = -1;
	
	public TaskFactory() {
		t = new Task();
		runnables = new LinkedList<Runnable>();
	}
	
	/***
	 * Build the task with all added <i>Runnables</i> and start immediately!
	 */
	public Task start() {
		get().start();
		return t;
	}
	
	/***
	 * Build the task with all added <i>Runnables</i>!
	 */
	public Task get() {
		t.runnables = new Runnable[runnables.size()];
		runnables.toArray(t.runnables);
		if(spread)
			if(spreadedThreads > -1)
				t.spread(spreadedThreads);
			else {
				t.spread();
			}
		return t;
	}
	
	/***
	 * Add a runnable to the <b>Task</b>
	 */
	public TaskFactory feed(Runnable r) {
		runnables.add(r);
		return this;
	}
	
	public TaskFactory name(String name) {
		t.name(name);
		return this;
	}
	
	/***
	 * When Multi Execution is <b>enabled</b> This task can be executed multiple times in parallel!
	 * This spawns one or more threads every time when <b>start()</b> is called!</br></br>
	 * 
	 * When Multi Execution is <b>disabled</b> This task can be executed only once at a time!
	 * when <b>start()</b> is called it will return, if this task is <i>already running</i> and start normally <i>otherwise</i>!
	 */
	public TaskFactory multiExecution(boolean multiExec) {
		t.multiExecution(multiExec);
		return this;
	}

	/***
	 * loops this <b>Task</b> until <i>stop()</i> is called.
	 * 
	 * @param delay time to wait between executions
	 */
	public TaskFactory repeat(long delay) {
		t.repeat(delay);
		return this;
	}

	/***
	 *  Adds this <b>Task</b> to given Topic 
	 */
	public TaskFactory inTopic(Topic t) {
		this.t.inTopic(t);
		return this;
	}

	/***
	 * 
	 * @param postExecute runnables to be executed before starting.
	 */
	public TaskFactory preExecute(Runnable... preExecute) {
		t.preExecute(preExecute);
		return this;
	}
	
	/***
	 * 
	 * @param postExecute runnables to be executed after finishing.
	 */
	public TaskFactory postExecute(Runnable... postExecute) {
		t.postExecute(postExecute);
		return this;
	}

	public TaskFactory delay(long delay) {
		t.delay(delay);
		return this;
	}

	/***
	 * Distributes the runnables of this Task into <b>NUM_CORES</b> - <b>RESERVED_CORES</b> threads.
	 * 
	 */
	public TaskFactory spread() {
		spread = true;
		return this;
	}

	/***
	 * Distributes the runnables of this Task into <i>n</i> threads.
	 * 
	 * @param threads how many threads should be used
	 * 
	 */
	public TaskFactory spread(int threads) {
		spread = true;
		spreadedThreads = threads;
		return this;
	}
	
}
