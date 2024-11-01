import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatadogClient {
    private static final String DATADOG_SYNTHETIC_TESTS_API_URL = "https://api.datadoghq.com/api/v1/synthetics/tests";

    private static final long MAX_ALLOWABLE_TIME_MS = 60 * 1000; // 1 minute

    private String apiKey;
    private String appKey;

    public DatadogClient(String apiKey, String appKey) {
        this.apiKey = apiKey;
        this.appKey = appKey;
    }

    /* ******************* Datadog API actions *****************/

    public String triggerSyntheticTests(SyntheticTestRequest[] testRequests) throws Exception {
        /*
        ** Trigger a synthetic test run consisting of one or more tests. At best this queues a request; the actual run
        ** will occur later, if at all. The request will just be dropped if testing is paused
        ** at the time of the request
        **
        ** Returns the testRunId for the requested test run
        */
        String url = DATADOG_SYNTHETIC_TESTS_API_URL + "/trigger";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Set the request method and headers
        System.out.println("API Key: " + apiKey);
        System.out.println("Application Key: " + appKey);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("DD-API-KEY", apiKey);
        con.setRequestProperty("DD-APPLICATION-KEY", appKey);

        // Send the request with a request body indicating the specific tests to be run
        String jsonRequestBody = String.format("{\"tests\":[{\"public_id\":\"%s\"}]}", testRequests[0].testId);
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] requestBody = jsonRequestBody.getBytes("utf-8");
            os.write(requestBody, 0, requestBody.length);
        }

        // Get the response
        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);
        if (responseCode != 200) {
            throw new Exception("Datadog API error");
        }
        String responseBody = DatadogClient.getResponseBody(con).toString();

        // We only care about the test run ID in the response, which we'll use later on
        // to interrogate for test results. Extract and return it
        return DatadogClient.jsonGetTestRunId(responseBody.toString());
    }

    public String getTestingResults(String testRunId, SyntheticTestRequest[] testRequests) throws Exception {
        String jsonTestRunResults = null;
        for (SyntheticTestRequest request : testRequests) {
            jsonTestRunResults = waitForTestOnTestRunResult(testRunId, request.testId);
        }

        return jsonTestRunResults; // TODO: Create and return array of results; don't just return the last one
    }

   // The datadog API doesn't return the results for all tests on a test run; rather, it returns the results on a test
   // across all test runs that have run the test. This forces us to make extra calls
   //
   // TBD: see whether there's another Datadog API call that actually does what we want
   private String waitForTestOnTestRunResult(String testRunId, String testId) throws Exception {
        /*
        ** Poll for test run completion subject to a maximum allowable time
        **
        ** Returns the json structure with all test run results for tests in this test run
        */
        String jsonTestOnTestRunResult = null;
        long startTime = System.currentTimeMillis();
        while (jsonTestOnTestRunResult == null && (System.currentTimeMillis() - startTime) < MAX_ALLOWABLE_TIME_MS) {
            TimeUnit.SECONDS.sleep(5); // Poll every 5 seconds
            jsonTestOnTestRunResult = getJsonTestOnTestRunResult(testRunId, testId);
        }

        if (jsonTestOnTestRunResult == null) {
            throw new Exception("The test run timed out.");
        }
        return jsonTestOnTestRunResult;
    }

    private String getJsonTestOnTestRunResult(String testRunId, String testId) throws Exception {
        /*
        ** This method is called in a polling loop and returns a null response if the test run
        ** has not yet completed; otherwise, it returns a json structure with all test results in it
        */

        String url = DATADOG_SYNTHETIC_TESTS_API_URL + "/" + testId + "/results";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Set the request method and headers. We don't specify which test we care about, as this
        // Datadog API returns the results for all tests run in the test run
        con.setRequestMethod("GET");
        con.setRequestProperty("DD-API-KEY", apiKey);
        con.setRequestProperty("DD-APPLICATION-KEY", appKey);

        // Read the response
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode != 200) {
            return null;
        }
        String responseBody = DatadogClient.getResponseBody(con);
        String testResult = DatadogClient.jsonGetSecondFieldValueAfterFirstField(
            responseBody,"result_id", "passed\":", testRunId);
        return testResult;
    }

    /* *************** HTTP connection utilities *****************/

    private static String getResponseBody(HttpURLConnection con) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String responseLine;
        StringBuilder responseBody = new StringBuilder();
        while ((responseLine = in.readLine()) != null) {
            responseBody.append(responseLine);
        }
        in.close();

        return responseBody.toString();
    }

    /* *************** JSON parsing **************** */

    private static String jsonGetTestRunId(String jsonResponse) throws Exception {
        // Extract test run ID from trigger test run result. Appears as result_id in response.
        // Example JSON response format:
        // { "result_id": "12345" }

        String key = "\"result_id\":\"";
        int index = jsonResponse.indexOf(key);
        if (index != -1) {
            int startIndex = index + key.length();
            int endIndex = jsonResponse.indexOf("\"", startIndex);
            return jsonResponse.substring(startIndex, endIndex);
        }
        throw new Exception("Trigger request failed");
    }

    private static String jsonGetSecondFieldValueAfterFirstField(
        String input,
        String testRunIdTag,
        String testResultTag,
        String testRunId) throws Exception {

        String regex = testRunIdTag + "\\s*([^,]+).*?" + testResultTag + "\\s*([^,]+)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        String testRunIdValue = null;
        String testResultValue = null;
        while (testRunIdValue == null && matcher.find()) {
            testRunIdValue = matcher.group(1).trim();
            testResultValue = matcher.group(2).trim();

            if (testRunIdValue == testRunId) {
                break;
            }
        }

        if (testResultValue == null) {
            throw new Exception("Failed to find test result in test run");
        }

        return testResultValue;
    }
}
