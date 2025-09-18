package Codify.parsing.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Submission")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long submissionId; // 과제 제출 ID
    private Long assignmentId; // 과제 ID
    private String fileName; // 파일 이름
    private Long week; // 주차
    private LocalDateTime submissionDate; // 제출 날짜
    private Long studentId; // 학번
    private String studentName; // 제출한 학생의 이름
    private String s3Key; // S3 경로
}