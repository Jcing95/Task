package de.jcing.utillities.task;

import java.util.HashSet;

public class Topic {
	
	private static HashSet<Topic> topics = new HashSet<Topic>();
	
	private static Topic defaultTopic = new Topic("global");
	
	private HashSet<Task> tasks;
	
	private String name;
	
	/**
	 * Create a new Topic with given name.
	 * A Topic is a set of Tasks and provides functionality to start stop or pause all tasks in it.
	 * It is used to organize Tasks
	 */
	public Topic(String name) {
		tasks = new HashSet<Task>();
		this.name = name;
		topics.add(this);
	}
	
	/**
	 * Add a Task to this topic
	 */
	public void addTask(Task task) {
		tasks.add(task);
	}
	
	/**
	 * pause all Tasks in this Topic
	 * 
	 */
	public void pause(boolean pause) {
		for(Task t: tasks) {
			t.pause(pause);
		}
	}
	
	
	/**
	 * start all Tasks in this Topic
	 */
	public void start() {
		for(Task t : tasks) {
			t.start();
		}
	}
	
	/**
	 * stop all Tasks in this Topic
	 */
	public void stop() {
		for(Task t: tasks) {
			t.stop();
		}
	}
	
	
	/**
	 * @return the name of this Topic
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Find a topic with given name
	 * 
	 */
	public static Topic getTopic(String name) {
		for(Topic t: topics) {
			if(t.name.equals(name))
				return t;
		}
		return null;
	}
	
	/**
	 * Add task to the default Topic
	 * @param task
	 */
	public static void addDefault(Task task) {
		defaultTopic.addTask(task);
	}
	
	/**
	 * Stop all Tasks in every Topic
	 */
	public static void stopAll() {
		for(Topic t : topics) {
			t.stop();
		}
	}
	
}
