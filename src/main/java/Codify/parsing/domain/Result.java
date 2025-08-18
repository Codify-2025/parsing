package Codify.parsing.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@Document(collection = "result")
public class Result {

    @Id
    private String id;

    private Integer submissionId;
    private Integer studentId;
    private Object ast;
}