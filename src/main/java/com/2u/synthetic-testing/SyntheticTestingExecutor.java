import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult;
import com.thoughtworks.go.plugin.api.task.Console;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskExecutionContext;
import com.thoughtworks.go.plugin.api.task.TaskExecutor;

import java.util.Map;

public class SyntheticTestingExecutor implements TaskExecutor {

    private static final String PUBLIC_TEST_ID = "sad-hqu-h33";

    @Override
    public ExecutionResult execute(TaskConfig taskConfig, TaskExecutionContext taskExecutionContext) {
        // TODO - set this up as configuration values

        try {
            Map<String, String> environmentVariables = taskExecutionContext.environment().asMap();
            String api_key = environmentVariables.get("DATADOG_API_KEY");
            String app_key = environmentVariables.get("DATADOG_APP_KEY");
            DatadogClient ddClient = new DatadogClient(api_key, app_key);

            SyntheticTestRequest singletonRequest = new SyntheticTestRequest("Hello, world test", PUBLIC_TEST_ID);
            SyntheticTestRequest[] testingRequests = new SyntheticTestRequest[]{singletonRequest};
            String testRunId = ddClient.triggerSyntheticTests(testingRequests);
            // System.out.println("Test run ID: " + testRunId);

            String testingResults = ddClient.getTestingResults(testRunId, testingRequests);
            // System.out.println("Testing results: " + testingResults.toString());
        }
        catch (Exception e) {
            // e.printStackTrace();
        }

        return ExecutionResult.success("Datadog synthetic test ran");
    }

}
