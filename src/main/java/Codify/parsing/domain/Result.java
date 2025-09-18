package Codify.parsing.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@RequiredArgsConstructor
@Document(collection = "result")
public class Result {

    @Id
    private String id;

    private final Integer assignmentId;
    private final Integer submissionId;
    private final Integer studentId;
    private final Object ast;
}