package Codify.parsing.repository;

import Codify.parsing.domain.Submission;
import Codify.parsing.dto.SubmissionInfoDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    /**
     * 제출 ID로 제출 정보 조회
     */
    Optional<Submission> findBySubmissionId(Long submissionId);
    
    /**
     * 파일명으로 제출 정보 조회
     */
    Optional<Submission> findByFileName(String fileName);
    

    //submissionId리스트로 s3Key 조희
    @Query("SELECT s.submissionId, s.s3Key, s.assignmentId, s.studentId " +
            "FROM Submission s WHERE s.submissionId IN :submissionIds " +
            "ORDER BY s.submissionId")
    List<SubmissionInfoDto> findAllBySubmissionIdIn(List<Long> submissionIds);
}

