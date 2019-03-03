package de.jcing.utillities.task;

public class TaskTest {
	
	static long startMillis;
	
	private static Runnable preExec = new Runnable() {
		@Override
		public void run() {
			startMillis = Task.millis();
		}
	};
	
	private static Runnable postExec = new Runnable() {
		@Override
		public void run() {
			System.out.println("took " + (Task.millis() - startMillis) + " milliseconds!");
		}
	};

	public static void main(String[] args) {
		
		
		TaskFactory tf = Task.getFactory();
		for (int i = 0; i < 100000; i++) {
			final int index = i;
			tf.feed(new Runnable() {
				@Override
				public void run() {
					System.out.println(index);
				}
			});
		}
		tf.feed(new Runnable() {
			@Override
			public void run() {
				System.out.print("\n");
			}
		});
		
		Task t = tf.name("runn").preExecute(preExec).postExecute().start();
		t.spread().multiExecution(false).start();
		
	}
	
	
	
}
