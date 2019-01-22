package de.jcing.utillities.task;

public class TaskTest {

	static long startNanos;

	public static void main(String[] args) {
		
		
		TaskFactory tf = Task.getFactory();
		
		for (int i = 0; i < 100000; i++) {
			final int index = i;
			tf.feed(() -> System.out.println(index));
		}
		tf.feed(() -> System.out.print("\n"));
		
		Task t = tf.name("runn").preExecute(() -> startNanos = Task.millis()).postExecute(() -> System.out.println("took " + (Task.millis() - startNanos) + " milliseconds!")).start();
		t.spread().multiExecution(false).start();
		
	}
	
	
	
}
