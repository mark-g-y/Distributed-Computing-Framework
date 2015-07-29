# Distributed-Computing-Framework

This is a flexible distributed data processing framework that automatically scales a user-specified processing function to process data in parallel on multiple machines. Fault tolerance is built into the system by keeping track of the data and rerunning the analysis if a machine fails.

To implement framework:
- Look at the example/MultiplyByTwo.java tutorial -> it provides an example of how to implement this framework for processing large sets of data.
  
Example (how to compile & run the example/MultiplyByTwo.java job):
- Assume ${HOME} is the source directory of this framework
- Navigate into the example directory
- javac -cp "${HOME}/target/distributedcomputing-1.0-SNAPSHOT.jar;${HOME}/lib/json-20140107.jar" MultiplyByTwo.java
  * In your own project, replace MultiplyByTwo.java with your own Java files
- jar cf multiplybytwo.jar MultiplyByTwo.class
- Modify/create the server.config file (as shown in example/server.config) - this specifies the worker machines, their hostnames, and their ports
- ${HOME}/compute multiplybytwo.jar MultiplyByTwo worker 1234 (repeat for all workers)
  * Note: MultiplyByTwo is the name of the class that extends TaskPipeline. Replace this with your own class name
- ${HOME}/compute multiplybytwo.jar MultiplyByTwo manager
  * Note: MultiplyByTwo is the name of the class that extends TaskPipeline. Replace this with your own class name

Simply change the names and paths from the above instructions, and it should allow you to run your own jobs!
