package de.jcing.utillities.task;

import java.util.Iterator;

import de.jcing.utillities.log.Log;

public class Task {

	Log log = new Log(Task.class).mute(false);

	public static final int NUM_CORES = Runtime.getRuntime().availableProcessors() / 2;
	public static int RESERVED_CORES = 0;

	private static final long START_MILLIS = System.currentTimeMillis();

	protected String name;
	
	protected final Context context;
	protected Context[] subContexts;

	protected Runnable[] preExecute;
	protected Runnable[] postExecute;

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
		this.context = new Context();
		preExecute = new Runnable[0];
		postExecute = new Runnable[0];
		context.loop(runnables);
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
	 * Adds this <b>Task</b> to given Topic
	 */
	public Task inTopic(Topic t) {
		t.addTask(this);
		inTopic = true;
		return this;
	}

	/***
	 * 
	 * @param preExecute runnables to be executed <b>once</b> before starting.
	 */
	public Task preExecute(Runnable... preExecute) {
		this.preExecute = preExecute;
		return this;
	}

	/***
	 * 
	 * @param postExecute runnables to be executed <b>once</b> after finishing.
	 */
	public Task postExecute(Runnable... postExecute) {
		this.postExecute = postExecute;
		return this;
	}
	
	
	public Task preLoop(Runnable...runnables) {
		context.preLoop(runnables);
		return this;
	}
	
	public Task postLoop(Runnable...runnables) {
		context.postLoop(runnables);
		return this;
	}

	/***
	 * delays the execution after <b>start()</b> is called.
	 * @param delay by milliseconds
	 * 
	 */
	public Task delay(long delay) {
		this.delay = delay;
		return this;
	}

	/***
	 * When Multi Execution is <b>enabled</b> This task can be executed multiple
	 * times in parallel. This spawns one or more threads every time when
	 * <b>start()</b> is called!</br>
	 * </br>
	 * 
	 * When Multi Execution is <b>disabled</b> This task can be executed only once
	 * at a time! when <b>start()</b> is called it will return, if this task is
	 * <i>already running</i> and start normally <i>otherwise</i>!
	 */
	public Task multiExecution(boolean multiExec) {
		this.multiExec = multiExec;
		return this;
	}

	/***
	 * Distributes the runnables of this Task into <b>NUM_CORES</b> -
	 * <b>RESERVED_CORES</b> threads.
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
		subContexts = new Context[threads];
		subContexts[0] = context;
		int[] lengths = new int[threads];
		int over = context.loopSize() % threads;
		int runCount = context.loopSize() / threads;

		for (int i = 1; i < subContexts.length; i++) {
			subContexts[i] = new Context();
			lengths[i] = runCount + (Integer.max(over--, 0));
		}
		
		Iterator<Runnable> it = context.getLoopIterator();
		for (int i = 0; i < threads; i++) {
			for (int t = 0; t < lengths[i]; t++) {
				subContexts[i].run(it.next());
			}
		}
		return this;
	}

	public Task start() {
		if (!running || multiExec) {
			running = true;
			if (!inTopic) {
				Topic.addGlobal(this);
			}
			log.debug("starting...");
			if (spread) {
				runParallel();
			} else {
				runSerial();
			}
		}
		return this;
	}

	private void delayAndPretasks() {
		if (delay > 0) {
			log.debug("delaying " + delay + "ms!");
			sleep(delay);
		}
		if (preExecute.length > 0)
			log.debug("executing pretask(s)...");
		for (Runnable r : preExecute) {
			r.run();
		}
	}
	
	private void postExecAndFinish() {
		if (preExecute.length > 0)
			log.debug("executing posttask(s)...");
		for (Runnable r : postExecute) {
			r.run();
		}
		log.debug("finished!");
	}

	private void runSerial() {
		new Thread(new Runnable() {
			public void run() {
				delayAndPretasks();

				long lastSec = System.currentTimeMillis();
				long lastTick;
				int ticks = 0;
				double difft = 0;
				
				if (repeating)
					log.debug("starting loop...");
				else
					log.debug("executing task(s)...");

				do {
					lastTick = System.currentTimeMillis();

					context.exec();
					
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
				postExecAndFinish();
			}
		}).start();
	}

	private void runParallel() {
		new Thread(new Runnable() {
			public void run() {

				delayAndPretasks();
				final boolean fin[] = new boolean[threads];

				log.debug("starting spreaded " + (repeating ? "loop!" : "task!"));
				for (int i = 0; i < threads; i++) {
					final int index = i;
					new Thread(new Runnable() {

						@Override
						public void run() {
							long lastSec = System.currentTimeMillis();
							long lastTick;
							double difft = 0;
							int ticks = 0;
							do {
								lastTick = System.currentTimeMillis();

								subContexts[index].exec();
								
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
						}
					}).start();
				}
				while (!finished) {
					boolean finish = true;
					for (boolean b : fin)
						finish = b && finish;
					finished = finish;
					sleep(100);
				}
				postExecAndFinish();
			}
		}).start();
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

	public Context getContext() throws RuntimeException {
		if(spread)
			throw new RuntimeException("Spreaded task has no unique context!");
		else return context;
	}
	
	public void enableLogging(boolean enabled) {
		log.mute(enabled);
	}

	// Utility functions

	public static int millis() {
		return (int) (System.currentTimeMillis() - START_MILLIS);
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
