package com.distributedcomputing;

import org.json.JSONObject;

public abstract class TaskPipeline {

    private static TaskPipeline taskPipeline;
    private DistributedWorkManager distributedWorkManager;

    protected void setDistributedWorkManager(DistributedWorkManager distributedWorkManager) {
        this.distributedWorkManager = distributedWorkManager;
    }

    public abstract void inputTaskData();

    protected final void addData(JSONObject data) {
        distributedWorkManager.add(data);
    }

    protected final void finishedDataInput() {
        distributedWorkManager.doneAddingJobs();
    }

    public abstract JSONObject executeTask(JSONObject taskData);

    public abstract void onResult(JSONObject result);

    public abstract void onAllResultsFinished();

    public static final void setTaskPipeline(TaskPipeline taskPipelineInstance) {
        taskPipeline = taskPipelineInstance;
    }

    public static TaskPipeline getInstance() {
        return taskPipeline;
    }

}