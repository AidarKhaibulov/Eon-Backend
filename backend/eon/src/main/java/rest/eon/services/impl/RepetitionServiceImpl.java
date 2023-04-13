package rest.eon.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rest.eon.models.Notification;
import rest.eon.models.Repetition;
import rest.eon.models.Task;
import rest.eon.repositories.RepetitionRepository;
import rest.eon.services.RepetitionService;
import rest.eon.services.TaskService;

@Service
@RequiredArgsConstructor
public class RepetitionServiceImpl implements RepetitionService {
    private final RepetitionRepository repetitionRepository;
    private final TaskService taskService;

    @Override
    public Repetition save(Repetition rep) {
        Task linkedTask=taskService.getTaskById(rep.getTaskId()).get();
        Repetition toSave=repetitionRepository.save(rep);
        linkedTask.setRepetitionId(toSave.getId());
        taskService.update(linkedTask);
        return toSave;
    }
}
