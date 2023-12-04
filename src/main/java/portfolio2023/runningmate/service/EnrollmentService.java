package portfolio2023.runningmate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import portfolio2023.runningmate.domain.Enrollment;
import portfolio2023.runningmate.repository.EnrollmentRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public Enrollment findEnrollmentById(Long enrollmentId){
        return enrollmentRepository.findById(enrollmentId).orElseThrow();
    }

}
