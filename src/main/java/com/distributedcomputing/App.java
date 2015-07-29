package com.distributedcomputing;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class App {

    private static int MIN_ARGS = 3;

    private static String getProperUsageInstructions() {
        return "Proper usage: compute(.bat) jarFilePath taskPipelineClassName role<manager, worker> port<if worker>";
    }

    private static TaskPipeline loadTaskPipelineFromClassFile(String jarFile, String className) {
        TaskPipeline taskPipeline = null;
        try {
            File file = new File(jarFile);
            URL url = file.toURI().toURL();  
            URL[] urls = new URL[]{url};
            ClassLoader cl = new URLClassLoader(urls);

            Class clazz = cl.loadClass(className);
            Constructor ctor = clazz.getDeclaredConstructor();
            taskPipeline = (TaskPipeline)ctor.newInstance();
            TaskPipeline.setTaskPipeline(taskPipeline);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return taskPipeline;
    }

    public static void main(String[] args) {

        if (args.length < MIN_ARGS) {
            throw new RuntimeException("Invalid number of arguments. " + getProperUsageInstructions());
        }

        TaskPipeline taskPipeline = loadTaskPipelineFromClassFile(args[0], args[1]);

        String role = args[2];

        if ("manager".equals(role)) {
            DistributedWorkManager distributedWorkManager = DistributedWorkManager.getInstance();
            distributedWorkManager.run();
            taskPipeline.setDistributedWorkManager(distributedWorkManager);
            taskPipeline.inputTaskData();
        } else if ("worker".equals(role)) {
            if (args.length < MIN_ARGS + 1) {
                throw new RuntimeException("Did not specify worker port. " + getProperUsageInstructions());
            }
            int port = Integer.parseInt(args[3]);
            System.out.println("Starting up - " + role);
            Worker worker = new Worker(port);
            worker.run();
        } else {
            throw new RuntimeException("Must specify role - manager or worker. " + getProperUsageInstructions());
        }

    }
}
