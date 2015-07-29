
import com.distributedcomputing.TaskPipeline;

import java.util.ArrayList;

import org.json.JSONObject;

/**
 * This is an example class that explains how to override the TaskPipeline and implement a custom data processing job
 * In this simple example, we will generate a list of numbers, and multiply each number by 2. Then we will print the list of results
 * In actual usage, the processing will be more complex and time consuming. This is just an easy to understand example :)
 */
public class MultiplyByTwo extends TaskPipeline {

    private ArrayList<JSONObject> results = new ArrayList<JSONObject>();

    /**
     * Override this function to input all data for the analysis
     */
    public void inputTaskData() {

        // generate all the numbers for multiplying by 2
        for (int i = 0; i < 20; i ++) {
            JSONObject foo = new JSONObject();
            foo.put("number", i);
            
            // call this function to add data for analysis
            addData(foo);
        }
        // call this function after adding all data
        finishedDataInput();
    }

    /**
     * Override this function to specify the analysis to perform on the data
     * This code will run on each worker machine
     */
    public JSONObject executeTask(JSONObject taskData) {
        JSONObject result = new JSONObject();
        int input = taskData.getInt("number");
        
        // perform processing here
        int resulting = input * 2;
        
        result.put("input", input);
        result.put("result", resulting);

        // return the result
        return result;
    }

    /**
     * After a worker is finished with a result, this function will be called
     */
    public void onResult(JSONObject result) {
        // add the results to an array for printing out later on
        results.add(result);
    }

    /**
     * After all results are in, this function is called
     */
    public void onAllResultsFinished() {
        // print the results
        System.out.println("Results:");
        for (JSONObject r : results) {
            System.out.println(r.toString());
        }
    }

}