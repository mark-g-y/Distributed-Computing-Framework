package com.distributedcomputing;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

public class DistributedWorkManager {

    private static DistributedWorkManager distributedWorkManager;

    private TaskPipeline taskPipeline = TaskPipeline.getInstance();

    private boolean hasMoreJobs = true;
    private ArrayList<WorkerCommunicator> workerCommunicators = new ArrayList<WorkerCommunicator>();
    private LinkedList<JSONObject> jobs = new LinkedList<JSONObject>();
    private LinkedList<Integer> readyWorkerIds = new LinkedList<Integer>();
    private WorkerConfig[] workerConfigs = Configuration.getInstance().getWorkerConfigs();
    private int taskIdIncrement = 0;

    private ReentrantLock jobsLock = new ReentrantLock();
    private ReentrantLock readyWorkerIdsLock  = new ReentrantLock();
    private ReentrantLock resultsLock = new ReentrantLock();

    public DistributedWorkManager() {
        for (int i = 0; i < workerConfigs.length; i++) {
            final int workerId = i;
            workerCommunicators.add(new WorkerCommunicator(i, workerConfigs[i].address, workerConfigs[i].port, new WorkerCommunicator.WorkerCommunicatorCallback() {
                @Override
                public void onWorkerConnected(int numProcessors) {
                    readyWorkerIdsLock.lock();
                    for (int m = 0; m < numProcessors; m++) {
                        readyWorkerIds.add(workerId);
                    }
                    readyWorkerIdsLock.unlock();
                }

                @Override
                public void onWorkerError(ArrayList<TaskData> unfinishedTaskData) {
                    // we've avoided timing issue here because the worker's task hashmap won't be cleared until this method finishes
                    // hence, the job runner won't stop until then
                    for (TaskData t : unfinishedTaskData) {
                        jobsLock.lock();
                        jobs.add(t.getData());
                        jobsLock.unlock();
                    }
                }

                @Override
                public void onWorkerFinished(JSONObject result) {
                    readyWorkerIdsLock.lock();
                    readyWorkerIds.add(workerId);
                    readyWorkerIdsLock.unlock();
                    resultsLock.lock();
                    // no need to worry about JSON exception because task processing class restricts results to JSON
                    taskPipeline.onResult(result.getJSONObject("result"));
                    resultsLock.unlock();
                }
            }));
        }
    }

    public void add(JSONObject data) {
        jobsLock.lock();
        jobs.add(data);
        jobsLock.unlock();
    }

    public void doneAddingJobs() {
        hasMoreJobs = false;
    }

    public void run() {
        Thread t = new Thread() {
            @Override
            public void run() {
                while (hasMoreJobs || !jobs.isEmpty()) {
                    readyWorkerIdsLock.lock();
                    if (!readyWorkerIds.isEmpty() && !jobs.isEmpty()) {
                        Integer id = readyWorkerIds.removeFirst();
                        readyWorkerIdsLock.unlock();
                        jobsLock.lock();
                        TaskData taskData = new TaskData(taskIdIncrement, jobs.removeFirst());
                        workerCommunicators.get(id).sendTaskDataToWorker(taskData, id);
                        taskIdIncrement++;
                        jobsLock.unlock();
                    } else {
                        readyWorkerIdsLock.unlock();
                    }
                }
                while (hasRunningTasks()) {
                    // wait until all running tasks have completed
                }
                for (WorkerCommunicator workerCommunicator : workerCommunicators) {
                    workerCommunicator.shutdown();
                }
                taskPipeline.onAllResultsFinished();
            }
        };
        t.start();
    }

    private boolean hasRunningTasks() {
        for (WorkerCommunicator workerCommunicator : workerCommunicators) {
            if (workerCommunicator.hasRunningTasks()) {
                return true;
            }
        }
        return false;
    }

    public static DistributedWorkManager getInstance() {
        if (distributedWorkManager == null) {
            distributedWorkManager = new DistributedWorkManager();
        }
        return distributedWorkManager;
    }

}