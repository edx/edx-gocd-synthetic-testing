from dataclasses import dataclass
from typing import Dict
from DatadogClient import DatadogClient

import os

class TaskExecutor:
    def execute(self, task_config, task_execution_context):
        raise NotImplementedError

@dataclass
class SyntheticTestRequest:
    name: str
    test_id: str

class SyntheticTestingExecutor(TaskExecutor):
    PUBLIC_TEST_ID = "sad-hqu-h33"

    def execute(self, task_config, task_execution_context):
        try:
            # Extract API and APP keys from environment variables
            environment_variables = task_execution_context.environment().as_dict()
            api_key = environment_variables.get("DATADOG_API_KEY")
            app_key = environment_variables.get("DATADOG_APP_KEY")

            # Instantiate the Datadog client
            dd_client = DatadogClient(api_key, app_key)

            # Prepare and trigger the synthetic test request
            singleton_request = SyntheticTestRequest("Hello, world test", self.PUBLIC_TEST_ID)
            testing_requests = [singleton_request]
            test_run_id = dd_client.trigger_synthetic_tests(testing_requests)

            # Fetch and print testing results
            testing_results = dd_client.get_testing_results(test_run_id, testing_requests)
            print("Testing results:", testing_results)

        except Exception as e:
            print("An error occurred:", str(e))
            exit()

        return ExecutionResult.success("Datadog synthetic test ran")

@dataclass
class ExecutionResult:
    @staticmethod
    def success(message: str):
        return {"status": "success", "message": message}

@dataclass
class TaskExecutionContext:
    environment_variables: Dict[str, str]

    def environment(self):
        # Simulates an environment getter that returns environment variables as a dictionary
        return self

    def as_dict(self):
        return self.environment_variables


def main():
    # Load environment variables
    environment_variables = {
        "DATADOG_API_KEY": os.getenv("DATADOG_API_KEY"),
        "DATADOG_APP_KEY": os.getenv("DATADOG_APP_KEY")
    }

    # Set up task execution context
    task_execution_context = TaskExecutionContext(environment_variables=environment_variables)

    # Instantiate and run the synthetic testing executor
    executor = SyntheticTestingExecutor()
    result = executor.execute(task_config=None, task_execution_context=task_execution_context)

    # Print execution result
    print(result)


if __name__ == "__main__":
    main()
