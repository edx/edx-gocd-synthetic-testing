import com.thoughtworks.go.plugin.api.task.TaskView;

public class SyntheticTestingView implements TaskView {

    @Override
    public String displayValue() {
        return "SyntheticTesting";
    }

    @Override
    public String template() {
        return( "<h1>Hello, world</h1>");
    }
}
