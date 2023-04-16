package rest.eon;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import rest.eon.models.Task;
import rest.eon.services.impl.TaskServiceImpl;

@SpringBootTest(
)
class EonApplicationTests {
    @Autowired
    private TaskServiceImpl taskService;

    @Test
    void intersect_tasks_without_repetition_return_true() {
        Task newT = Task.builder()
                .repetitionId(null)
                .dateStart("2023-04-09T10:30:00Z")
                .dateFinish("2023-04-09T11:00:00Z")
                .build();
        Task oldT = Task.builder()
                .repetitionId(null)
                .dateStart("2023-04-09T10:50:00Z")
                .dateFinish("2023-04-09T12:00:00Z")
                .build();

        boolean result=taskService.twoTasksIntersect(newT,oldT);
        Assert.isTrue(result,"ok");


    }
    @Test
    void intersect_tasks_without_repetition_return_false() {
        Task newT = Task.builder()
                .repetitionId(null)
                .dateStart("2023-04-09T10:30:00Z")
                .dateFinish("2023-04-09T11:00:00Z")
                .build();
        Task oldT = Task.builder()
                .repetitionId(null)
                .dateStart("2023-04-09T12:00:00Z")
                .dateFinish("2023-04-09T13:00:00Z")
                .build();

        boolean result=taskService.twoTasksIntersect(newT,oldT);
        Assert.isTrue(!result,"ok");
    }
    @Test
    void intersect_task_with_repetition(){
        Task newT=taskService.getTaskById("6435108ad585040eb76b7c69").get();
        Task oldT=taskService.getTaskById("64381bf235dfac4a0ed89d8a").get();

        boolean result=taskService.twoTasksIntersect(newT,oldT);
        Assert.isTrue(result,"ok");
    }
}
