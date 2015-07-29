package com.distributedcomputing;

import java.net.*;
import java.io.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

public class Worker {

    private TaskPipeline taskPipeline = TaskPipeline.getInstance();

    private int portNumber;
    private int numProcessors;

    public Worker(int portNumber) {
        this.portNumber = portNumber;
        numProcessors = Runtime.getRuntime().availableProcessors();
    }

    public void run() {

        try {
            final ServerSocket serverSocket = new ServerSocket(portNumber);
            final Socket clientSocket = serverSocket.accept();
            final PrintWriter serverOutput = new PrintWriter(clientSocket.getOutputStream(), true);
            final BufferedReader serverInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            ExecutorService executorService = Executors.newFixedThreadPool(numProcessors);

            serverOutput.println(numProcessors);
        
            String inputLine, outputLine;

            while ((inputLine = serverInput.readLine()) != null) {
                final String inputLineWrapper = inputLine;
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject input = new JSONObject(inputLineWrapper);
                            JSONObject processedResult = taskPipeline.executeTask(input.getJSONObject("data"));
                            JSONObject result = new JSONObject();
                            result.put("task_id", input.getInt("id"));
                            result.put("result", processedResult);
                            serverOutput.println(result.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }
                });
            }

            executorService.shutdownNow();
            
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }

}