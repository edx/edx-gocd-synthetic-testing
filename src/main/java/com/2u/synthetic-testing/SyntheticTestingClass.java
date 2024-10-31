// package com.2u.synthetic-testing;

import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.thoughtworks.go.plugin.api.task.Task;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskExecutor;
import com.thoughtworks.go.plugin.api.task.TaskView;

@Extension
public class SyntheticTestingClass implements Task {

    @Override
    public TaskConfig config() {
        TaskConfig config = new TaskConfig();

        return config;
    }

    @Override
    public TaskExecutor executor() {
        return new SyntheticTestingExecutor();
    }

    @Override
    public TaskView view() {
        return new SyntheticTestingView();
    }

    @Override
    public ValidationResult validate(TaskConfig taskConfig) {
        ValidationResult result = new ValidationResult();

        return result;
    }

}
