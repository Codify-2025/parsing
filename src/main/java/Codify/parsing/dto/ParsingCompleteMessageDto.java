package Codify.parsing.dto;

public record ParsingCompleteMessageDto(Integer assignmentId, Integer submissionId, Integer studentId)
{
    @Override
    public String toString() {
        return "ParsingCompleteMessage{" +
                "assignmentId='" + assignmentId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", submissionId='" + submissionId + '\'' +
                '}';
    }

}
