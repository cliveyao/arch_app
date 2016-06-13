package lab.s2jh.aud.service;

import lab.s2jh.aud.entity.JobRunHist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JobRunHistFacadeService {

    @Autowired
    private JobRunHistService jobRunHistService;

    /**
     * Asynchronous newly opened transaction timing of writing tasks log
     * @param entity
     */
    @Async
    public void saveWithAsyncAndNewTransition(JobRunHist entity) {
        jobRunHistService.save(entity);
    }
}
