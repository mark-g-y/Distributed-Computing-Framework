package com.distributedcomputing;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

public class WorkerCommunicator {

    private int id;
    private String hostName;
    private int portNumber;

    private Socket kkSocket;
    private PrintWriter outputToWorker;
    private BufferedReader inputFromWorker;

    private boolean isShuttingDown = false;
    private ReentrantLock isShuttingDownLock = new ReentrantLock();

    private ConcurrentHashMap<Integer, TaskData> runningTasks = new ConcurrentHashMap<Integer, TaskData>();
    private Thread listenToServerThread;
    private WorkerCommunicatorCallback callback;

    public WorkerCommunicator(int id, String hostName, int portNumber, WorkerCommunicatorCallback callback) {
        this.id = id;
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.callback = callback;
        try {
            kkSocket = new Socket(hostName, portNumber);
            outputToWorker = new PrintWriter(kkSocket.getOutputStream(), true);
            inputFromWorker = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName + ":" + portNumber);
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName + ":" + portNumber);
            e.printStackTrace();
            System.exit(1);
        }
        listenToServerThread = new Thread() {
            @Override
            public void run() {
                listenToServer();
            }
        };
        listenToServerThread.start();
    }

    public void listenToServer() {
        try {

            String fromServer;
            String fromUser;

            // wait for startup information to be sent from client
            fromServer = inputFromWorker.readLine();
            callback.onWorkerConnected(Integer.parseInt(fromServer));

            while ((fromServer = inputFromWorker.readLine()) != null) {
                try {
                    JSONObject result = new JSONObject(fromServer);
                    runningTasks.remove(result.getInt("task_id"));
                    callback.onWorkerFinished(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Could not locate host " + hostName + ":" + portNumber);
            workerErrorShutdown();
        } catch (IOException e) {
            isShuttingDownLock.lock();
            if (!isShuttingDown) {
                System.err.println("Couldn't get I/O for the connection to " + hostName + ":" + portNumber);
                workerErrorShutdown();
            }
            isShuttingDownLock.unlock();
        }
    }

    public void sendTaskDataToWorker(TaskData taskData, int workerId) {
        runningTasks.put(taskData.getId(), taskData);
        outputToWorker.println(taskData.toJson().toString());
    }

    public boolean hasRunningTasks() {
        return !runningTasks.isEmpty();
    }

    private void workerErrorShutdown() {
        ArrayList<TaskData> unfinishedTasks = new ArrayList<TaskData>();
        Iterator<Integer> iterator = runningTasks.keySet().iterator();
        while (iterator.hasNext()) {
            unfinishedTasks.add(runningTasks.get(iterator.next()));
        }
        callback.onWorkerError(unfinishedTasks);
        runningTasks.clear();
    }

    public void shutdown() {
        isShuttingDownLock.lock();
        isShuttingDown = true;
        isShuttingDownLock.unlock();
        try {
            kkSocket.shutdownOutput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface WorkerCommunicatorCallback {
        public void onWorkerConnected(int numProcessors);
        public void onWorkerError(ArrayList<TaskData> unfinishedTasks);
        public void onWorkerFinished(JSONObject result);
    }

}