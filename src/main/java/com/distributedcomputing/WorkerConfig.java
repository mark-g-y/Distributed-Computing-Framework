package com.distributedcomputing;

public class WorkerConfig {

    public static final String DEFAULT_ADDRESS = "localhost";
    public static final int DEFAULT_PORT = 1234;

    public final String address;
    public int port;

    public WorkerConfig(String address, int port) {
        this.address = address;
        this.port = port;
    }
}