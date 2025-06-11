package test;

import manager.FileBackedTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;

import java.io.File;
import java.io.IOException;

public class FileBackedTaskManagerTest extends AbstractTaskManagerTest {

    private File tempFile;

    @Override
    protected TaskManager createTaskManager() {
        try {
            tempFile = File.createTempFile("test_tasks", ".csv");
            tempFile.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void cleanUp() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }
}
