package com.distributedcomputing;

import java.io.File;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Configuration {

    private static final Configuration configuration = new Configuration();

    public static final String CONFIG_FILE_NAME = "server.config";

    private WorkerConfig[] workerConfigs;

    public Configuration() {
        try {
            Scanner sc = new Scanner(new File(CONFIG_FILE_NAME));
            sc.useDelimiter("\\Z");
            JSONObject fileContent = new JSONObject(sc.next());
            sc.close();
            parseWorkerConfigs(fileContent);
        } catch (Exception e) {
            System.out.println("server.config file not found - reverting to default runtime settings");
            workerConfigs = getDefaultWorkerConfigs();
        }
    }

    private void parseWorkerConfigs(JSONObject fileContent) throws JSONException {
        if (fileContent.getJSONArray("workers") == null) {
            workerConfigs = getDefaultWorkerConfigs();
        } else {
            JSONArray workers = fileContent.getJSONArray("workers");
            workerConfigs = new WorkerConfig[workers.length()];
            for (int i = 0; i < workers.length(); i++) {
                JSONObject worker = workers.getJSONObject(i);
                if (!worker.has("address")) {
                    throw new RuntimeException("Missing address field for worker in the configuration file");
                }
                if (!worker.has("port")) {
                    throw new RuntimeException("Missing port field for worker in the configuration file");
                }
                String address = worker.getString("address");
                Integer port = worker.getInt("port");
                workerConfigs[i] = new WorkerConfig(address, port);
            }
        }
    }

    private WorkerConfig[] getDefaultWorkerConfigs() {
        return new WorkerConfig[] {new WorkerConfig(WorkerConfig.DEFAULT_ADDRESS, WorkerConfig.DEFAULT_PORT)};
    }

    public WorkerConfig[] getWorkerConfigs() {
        return workerConfigs;
    }

    public static Configuration getInstance() {
        return configuration;
    }

}