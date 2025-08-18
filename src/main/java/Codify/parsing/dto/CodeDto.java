package Codify.parsing.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class CodeDto {
    private final String code;
    private final Integer submissionId;
    private final Integer studentId;
}
