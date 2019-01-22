package de.jcing.utillities.task;

import de.jcing.utillities.log.Log;

public class Task {

	Log log = new Log(Task.class).mute(false);

	public static final int NUM_CORES = Runtime.getRuntime().availableProcessors() / 2;
	public static int RESERVED_CORES = 0;

	protected String name;

	protected Runnable[] runnables;
	protected Runnable[] preExecute;
	protected Runnable[] postExecute;

	protected Runnable[][] spreaded;
	protected boolean spread = false;
	protected int threads;

	protected boolean repeating = false;
	protected long waitTime;
	protected int tps;

	protected boolean running = false;
	protected boolean paused = false;

	protected boolean inTopic = false;

	protected boolean finished = false;
	
	protected boolean multiExec = false;
	
	protected long delay;


	public Task(Runnable... runnables) {
		this.runnables = runnables;
		preExecute = new Runnable[0];
		postExecute = new Runnable[0];
		spreaded = new Runnable[0][0];
	}

	public Task name(String name) {
		this.name = name;
		log.appendName(name);
		return this;
	}

	/***
	 * loops this <b>Task</b> until <i>stop()</i> is called.
	 * 
	 * @param delay time to wait between executions
	 */
	public Task repeat(long delay) {
		repeating = true;
		waitTime = delay;
		return this;
	}
	
	/***
	 *  Adds this <b>Task</b> to given Topic 
	 */
	public Task inTopic(Topic t) {
		t.addTask(this);
		inTopic = true;
		return this;
	}

	/***
	 * 
	 * @param postExecute runnables to be executed before starting.
	 */
	public Task preExecute(Runnable... preExecute) {
		this.preExecute = preExecute;
		return this;
	}

	/***
	 * 
	 * @param postExecute runnables to be executed after finishing.
	 */
	public Task postExecute(Runnable... postExecute) {
		this.postExecute = postExecute;
		return this;
	}

	public Task delay(long delay) {
		this.delay = delay;
		return this;
	}
	
	
	/***
	 * When Multi Execution is <b>enabled</b> This task can be executed multiple times in parallel!
	 * This spawns one or more threads every time when <b>start()</b> is called!</br></br>
	 * 
	 * When Multi Execution is <b>disabled</b> This task can be executed only once at a time!
	 * when <b>start()</b> is called it will return, if this task is <i>already running</i> and start normally <i>otherwise</i>!
	 */
	public Task multiExecution(boolean multiExec) {
		this.multiExec = multiExec;
		return this;
	}
	
	/***
	 * Distributes the runnables of this Task into <b>NUM_CORES</b> - <b>RESERVED_CORES</b> threads.
	 * 
	 */
	public Task spread() {
		return spread(NUM_CORES - RESERVED_CORES);
	}

	/***
	 * Distributes the runnables of this Task into <i>n</i> threads.
	 * 
	 * @param threads how many threads should be used
	 * 
	 */
	public Task spread(int threads) {
		this.spread = true;
		this.threads = threads;
		int runCount = runnables.length / threads;
		int over = runnables.length % threads;
		spreaded = new Runnable[threads][];
		for (int i = 0, r = 0; i < threads; i++) {
			spreaded[i] = new Runnable[runCount + (Integer.max(over--, 0))];
			for (int t = 0; t < spreaded[i].length; t++) {
				spreaded[i][t] = runnables[r++];
			}
		}
		return this;
	}

	public Task start() {
		if(!running || multiExec) {
			running = true;
			if (!inTopic) {
				Topic.addGlobal(this);
			}
			log.debug("starting...");
			if (spread) {
				new Thread(() -> {
					if (delay > 0) {
						log.debug("delaying " + delay + "ms!");
						sleep(delay);
					}
					if (preExecute.length > 0)
						log.debug("executing pretask(s)...");
					for (Runnable r : preExecute) {
						r.run();
					}
					final boolean fin[] = new boolean[threads];
	
					log.debug("starting spreaded " + (repeating ? "loop!" : "task!"));
					for (int i = 0; i < threads; i++) {
						final int index = i;
						new Thread(() -> {
							long lastSec = System.currentTimeMillis();
							long lastTick;
							double difft = 0;
							int ticks = 0;
							do {
								lastTick = System.currentTimeMillis();
								
								for (Runnable r : spreaded[index]) 
									r.run();
								
								if (repeating) {
									if (index == 0 && System.currentTimeMillis() - lastSec >= 1000) {
										tps = ticks;
										ticks = 0;
										lastSec = System.currentTimeMillis();
									}
	
									difft += waitTime - (System.currentTimeMillis() - lastTick);
	
									if (difft > 0)
										sleep((long) (difft));
	
									difft -= (int) difft;
	
									while (paused && running) {
										ticks = 0;
										tps = 0;
										sleep((long) waitTime);
									}
								} else {
									running = false;
								}
								ticks++;
							} while (repeating && running);
							fin[index] = true;
						}).start();
					}
					while (!finished) {
						boolean finish = true;
						for (boolean b : fin)
							finish = b && finish;
						finished = finish;
						sleep(100);
					}
					if (preExecute.length > 0)
						log.debug("executing posttask(s)...");
					for (Runnable r : postExecute) {
						r.run();
					}
					log.debug("finished!");
				}).start();
			} else {
				new Thread(() -> {
					long lastSec = System.currentTimeMillis();
					long lastTick;
					int ticks = 0;
					double difft = 0;
					if (delay > 0) {
						log.debug("delaying " + delay + "ms!");
						sleep(delay);
					}
					if (preExecute.length > 0)
						log.debug("executing pretask(s)...");
					for (Runnable r : preExecute) {
						r.run();
					}
					if (repeating)
						log.debug("starting loop...");
					else
						log.debug("executing task(s)...");
					do {
						lastTick = System.currentTimeMillis();
	
						for (Runnable r : runnables)
							r.run();
	
						if (repeating) {
							if (System.currentTimeMillis() - lastSec >= 1000) {
								tps = ticks;
								ticks = 0;
								lastSec = System.currentTimeMillis();
							}
	
							difft -= (int) difft;
							difft += waitTime - (System.currentTimeMillis() - lastTick);
	
							if (difft > 0)
								sleep((long) (difft));
	
							while (paused && running) {
								ticks = 0;
								tps = 0;
								sleep((long) waitTime);
							}
						} else {
							running = false;
						}
						ticks++;
					} while (repeating && running);
					if (repeating)
						log.debug("finished loop!");
					finished = true;
					if (preExecute.length > 0)
						log.debug("executing posttask(s)...");
					for (Runnable r : postExecute) {
						r.run();
					}
					log.debug("finished!");
				}).start();
			}
		}
		return this;
	}

	public void stop() {
		running = false;
	}

	public void pause(boolean pause) {
		paused = pause;
	}

	public int getTps() {
		return tps;
	}

	public boolean isFinished() {
		return finished;
	}

	// Uttillities
	public static TaskFactory getFactory() {
		return new TaskFactory();
	}

	public static long millis() {
		return System.currentTimeMillis();
	}

	public static long nanos() {
		return System.nanoTime();
	}

	public static long perSecond(double perSecond) {
		return (long) (1000.0 / perSecond);
	}

	public static long perMinute(double perMinute) {
		return (long) (60000.0 / perMinute);
	}

	public static long perHour(double perHour) {
		return (long) (3600000.0 / perHour);
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
