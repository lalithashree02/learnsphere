package com.skill_learning_platform.skill_learning_platform.repository;

import com.skill_learning_platform.skill_learning_platform.model.CourseMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CourseMessageRepository extends JpaRepository<CourseMessage, Long> {
    @Query("select m from CourseMessage m where m.courseId = :courseId "
            + "and ((m.senderId = :studentId and m.receiverId = :mentorId) "
            + "or (m.senderId = :mentorId and m.receiverId = :studentId)) "
            + "order by m.sentAt asc")
    List<CourseMessage> findConversation(@Param("courseId") Long courseId,
            @Param("studentId") Long studentId,
            @Param("mentorId") Long mentorId);
}