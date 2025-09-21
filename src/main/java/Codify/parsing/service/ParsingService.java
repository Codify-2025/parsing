package Codify.parsing.service;

import Codify.parsing.config.cpp.CppParsingTable;
import Codify.parsing.domain.Result;
import Codify.parsing.dto.CodeDto;
import Codify.parsing.dto.MessageDto;
import Codify.parsing.dto.ResultDto;
import Codify.parsing.dto.SubmissionInfoDto;
import Codify.parsing.exception.databaseException.DatabaseException;
import Codify.parsing.repository.ResultRepository;
import Codify.parsing.repository.SubmissionRepository;
import Codify.parsing.service.parsing.ASTNode;
import Codify.parsing.service.parsing.Parsing;
import Codify.parsing.service.token.CppTokenizer;
import Codify.parsing.service.token.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParsingService {
    private final ResultRepository resultRepository;
    private final CppTokenizer cppTokenizer;
    private final CppParsingTable cppParsingTable;
    private final Parsing parsing;
    private final S3Service s3Service;
    private final SubmissionRepository submissionRepository;
    private final RabbitTemplate rabbitTemplate;


    @Transactional
    public ResultDto parsing(CodeDto codeDto) {
        try {
            int assignmentId = codeDto.assignmentId();
            int studentId = codeDto.studentId();
            int submissionId = codeDto.submissionId();

            List<Token> tokens = cppTokenizer.tokenize(codeDto.code());
            ASTNode resultNode = parsing.parse(tokens, cppParsingTable);

            Result result = new Result(assignmentId,submissionId, studentId, resultNode);
            resultRepository.save(result);


            return ResultDto.builder()
                    .assignmentId(assignmentId)
                    .studentId(studentId)
                    .submissionId(submissionId)
                    .build();
        } catch (DatabaseException e) {
            throw new DatabaseException("파싱 결과 저장 중 데이터베이스 오류가 발생했습니다");
        }
        //db에 save후 message broker에 넣기
    }

    @Transactional
    public ResultDto parseFromFile(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource("test-cpp/" + fileName);
        String code = Files.readString(resource.getFile().toPath());
        
        CodeDto codeDto = new CodeDto(code, 1, 5,2000004);
        return parsing(codeDto);
    }


    //s3에서 파일을 가져와서 -> 파싱 완료 후 -> 메시지 브로커의 similarity.queue에 저장(parsing.complete exchange가 전달)
    @Transactional
    public MessageDto parseFromS3BySubmissionId(MessageDto message) {
        log.info("parsing queue에서 메시지 pull");
        log.info("submissionIds: {}", message.getSubmissionIds());
        log.info("assignmentId: {}", message.getAssignmentId());
        log.info("push groupId: {}", message.getGroupId());
        log.info("push messageType: {}", message.getMessageType());
        log.info("totalFiles: {}", message.getTotalFiles());
        try {
            // 1. 데이터베이스에서 제출 정보 조회
            List<SubmissionInfoDto> submissions = submissionRepository.findAllBySubmissionIdIn(message.getSubmissionIds());

            if (submissions.size() != message.getSubmissionIds().size()) {
                log.warn("일부 submission을 찾을 수 없습니다. 요청: {}, 조회됨: {}",
                        message.getSubmissionIds().size(), submissions.size());
            }
            // 2. Submission을 순회하며 S3Key로 파일 내용 읽기
            for(int i=0;i<submissions.size();i++) {
                SubmissionInfoDto submission = submissions.get(i);

                String code = s3Service.readFileFromS3ByKey(submission.s3Key());

                //3. 파싱 실행
                CodeDto codeDto = new CodeDto(code,
                        submission.assignmentId().intValue(),
                        submission.submissionId().intValue(),
                        submission.studentId().intValue());

                List<Token> tokens = cppTokenizer.tokenize(codeDto.code());
                ASTNode resultNode = parsing.parse(tokens, cppParsingTable);

                //4. 파싱 결과를 mongodb에 저장
                Result result = new Result(codeDto.assignmentId(),codeDto.submissionId(), codeDto.studentId(), resultNode);
                resultRepository.save(result);
                log.info("parsing 완료 후 mongodb에 저장 : {}", codeDto.submissionId());
            }

            //파싱 완료 후 message를 RabbitMQ에 push
            MessageDto completedMessage = new MessageDto(
                "PARSING_COMPLETED",
                message.getGroupId(),
                message.getAssignmentId(),
                message.getSubmissionIds(),
                message.getTotalFiles(),
                LocalDateTime.now()
            );
            rabbitTemplate.convertAndSend("codifyExchange", "parsing.complete", completedMessage);
            log.info("similarity queue에 push완료");
            log.info("submissionIds: {}", completedMessage.getSubmissionIds());
            log.info("assignmentId: {}", completedMessage.getAssignmentId());
            log.info("groupId: {}", completedMessage.getGroupId());
            log.info("messageType: {}", completedMessage.getMessageType());
            log.info("totalFiles: {}", completedMessage.getTotalFiles());

            return completedMessage;

        } catch (Exception e) {
            throw new RuntimeException("S3 파일 파싱 중 오류 발생: submissionIds=" + message.getSubmissionIds(), e);
        }
    }



}
