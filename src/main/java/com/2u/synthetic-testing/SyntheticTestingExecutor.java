import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult;
import com.thoughtworks.go.plugin.api.task.Console;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskExecutionContext;
import com.thoughtworks.go.plugin.api.task.TaskExecutor;

public class SyntheticTestingExecutor implements TaskExecutor {

    @Override
    public ExecutionResult execute(TaskConfig taskConfig, TaskExecutionContext taskExecutionContext) {
        // TODO - set this up as configuration values
        private static final String PUBLIC_TEST_ID = "sad-hqu-h33";
        static SyntheticTestRequest singletonRequest = new SyntheticTestRequest("Hello, world test", PUBLIC_TEST_ID);

        try {
            SyntheticTestRequest[] testingRequest = new SyntheticTestRequest[]{singletonRequest};
            String testRunId = DatadogClient.triggerSyntheticTests(testingRequest);
            System.out.println("Test run ID: " + testRunId);

            String testingResults = DatadogClient.getTestingResults(testRunId, testingRequest);
            System.out.println("Testing results: " + testingResults.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ExecutionResult.success("Datadog synthetic test ran");
    }

}
