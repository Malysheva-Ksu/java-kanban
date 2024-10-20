package ru.yandex.javacource.malysheva.schedule.manager.tests;

import org.junit.jupiter.api.Test;
import ru.yandex.javacource.malysheva.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacource.malysheva.schedule.manager.Managers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagersTest {

    @Test
    void getDefaultAndItShouldReturnTaskManager() {
        Managers manager = new Managers();
        InMemoryTaskManager taskManager = (InMemoryTaskManager) manager.getDefault();

        assertNotNull(taskManager);
    }
}