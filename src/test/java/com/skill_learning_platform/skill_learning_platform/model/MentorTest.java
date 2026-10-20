package com.skill_learning_platform.skill_learning_platform.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MentorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSupportMultipleSkills() {
        Mentor mentor = new Mentor();
        mentor.setSkills(List.of("Java", "Spring Boot", "SQL"));

        assertEquals(List.of("Java", "Spring Boot", "SQL"), mentor.getSkills());
    }

    @Test
    void shouldDeserializeSkillsFromJsonArray() throws Exception {
        Mentor mentor = objectMapper.readValue(
                """
                {"skills":["Java","Spring Boot","SQL"]}
                """,
                Mentor.class);

        assertEquals(List.of("Java", "Spring Boot", "SQL"), mentor.getSkills());
    }
}
