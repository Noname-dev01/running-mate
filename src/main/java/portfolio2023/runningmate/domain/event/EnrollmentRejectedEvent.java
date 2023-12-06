package portfolio2023.runningmate.domain.event;

import portfolio2023.runningmate.domain.Enrollment;

public class EnrollmentRejectedEvent extends EnrollmentEvent{
    public EnrollmentRejectedEvent(Enrollment enrollment) {
        super(enrollment , "모임 참가 신청을 거절했습니다.");
    }
}
