package Codify.parsing.dto;

import lombok.Builder;

//데이터 전달만 담당 -> record로 선언
@Builder
public record ResultDto(Integer assignmentId, Integer submissionId, Integer studentId) {
}
