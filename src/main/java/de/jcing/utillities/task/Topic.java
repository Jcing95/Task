package de.jcing.utillities.task;

import java.util.HashSet;

public class Topic {
	
	private static HashSet<Topic> topics = new HashSet<>();
	
	private static Topic global = new Topic("global");
	
	private HashSet<Task> tasks;
	
	private String name;
	
	private double salt;
	
	public Topic(String name) {
		tasks = new HashSet<>();
		this.name = name;
		salt = Math.random();
		topics.add(this);
	}
	
	@Override
	public int hashCode() {
		return (name+salt).hashCode();
	}
	
	public void addTask(Task t) {
		tasks.add(t);
	}
	
	public void pause(boolean pause) {
		for(Task t: tasks) {
			t.pause(pause);
		}
	}
	
	public void start() {
		for(Task t : tasks) {
			t.start();
		}
	}
	
	public void stop() {
		for(Task t: tasks) {
			t.stop();
		}
	}
	
	public static void addGlobal(Task task) {
		global.addTask(task);
	}
	
	public static void stopAll() {
		for(Topic t : topics) {
			t.stop();
		}
	}
	
}
