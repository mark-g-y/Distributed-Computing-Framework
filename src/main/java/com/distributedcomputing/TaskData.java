
package com.distributedcomputing;

import org.json.JSONObject;

public class TaskData {

	private int id;
	private JSONObject data;

	public TaskData(int id, JSONObject data) {
		this.id = id;
		this.data = data;
	}

	public int getId() {
		return id;
	}

	public JSONObject getData() {
		return data;
	}

	public JSONObject toJson() {
		JSONObject task = new JSONObject();
		task.put("id", id);
		task.put("data", data);
		return task;
	}
}