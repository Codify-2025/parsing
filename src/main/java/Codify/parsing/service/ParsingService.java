package Codify.parsing.service;

import Codify.parsing.config.cpp.ParsingTable;
import Codify.parsing.domain.Result;
import Codify.parsing.dto.*;
import Codify.parsing.exception.databaseException.DatabaseException;
import Codify.parsing.repository.ResultRepository;
import Codify.parsing.service.factory.Components;
import Codify.parsing.service.factory.ParsingFactory;
import Codify.parsing.service.parsing.ASTNode;
import Codify.parsing.service.parsing.Parser;
import Codify.parsing.service.token.Token;
import Codify.parsing.service.token.Tokenizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParsingService {
    private final ResultRepository resultRepository;
    private final S3Service s3Service;
    private final RabbitTemplate rabbitTemplate;
    private final ParsingFactory factory;

    @Transactional
    public ResultDto parsing(CodeDto codeDto) {

        Components components = factory.createComponent("cpp");

        try {
            int assignmentId = codeDto.assignmentId();
            int studentId = codeDto.studentId();
            int submissionId = codeDto.submissionId();

            List<Token> tokens = components.tokenizer().tokenize(codeDto.code());
            ASTNode resultNode = components.parser().parse(tokens, components.parsingTable());

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

    //s3에서 파일을 가져와서 -> 파싱 완료 후 -> 메시지 브로커의 similarity.queue에 저장(parsing.complete exchange가 전달)
    @Transactional
    public MessageDto parseFromS3BySubmissionId(MessageDto message) {

        Components components = factory.createComponent("cpp");

        //message에 s3key도 저장하도록 변경
        log.info("parsing queue에서 메시지 pull");
        log.info("submissionIds: {}", message.getSubmissionIds());
        log.info("assignmentId: {}", message.getAssignmentId());
        log.info("push groupId: {}", message.getGroupId());
        log.info("push messageType: {}", message.getMessageType());
        log.info("total s3Key: {}", message.getS3Keys().size());
        log.info("totalFiles: {}", message.getTotalFiles());

        try {
            // 1.s3Key list를 순회하며 파일 내용 읽기
            Long assignmentId = message.getAssignmentId();

            for(int i=0;i<message.getS3Keys().size();i++) {
                Long submissionId = message.getSubmissionIds().get(i);
                Long studentId = message.getStudentIds().get(i);
                String s3Key = message.getS3Keys().get(i);

                String code = s3Service.readFileFromS3ByKey(s3Key);

                //2. 파싱 실행
                CodeDto codeDto = new CodeDto(code,
                        assignmentId.intValue(),
                        submissionId.intValue(),
                        studentId.intValue());

                List<Token> tokens = components.tokenizer().tokenize(codeDto.code());
                ASTNode resultNode = components.parser().parse(tokens, components.parsingTable());

                //3. 파싱 결과를 mongodb에 저장
                Result result = new Result(codeDto.assignmentId(),codeDto.submissionId(), codeDto.studentId(), resultNode);
                resultRepository.save(result);
            }

            //파싱 완료 후 message를 RabbitMQ에 push
            CompletedMessageDto completedMessage = new CompletedMessageDto(
                "PARSING_COMPLETED",
                message.getGroupId(),
                message.getAssignmentId(),
                message.getSubmissionIds(),
                message.getTotalFiles(),
                LocalDateTime.now()
            );
            rabbitTemplate.convertAndSend("codifyExchange", "parsing.complete", completedMessage);

            return message;

        } catch (Exception e) {
            throw new RuntimeException("S3 파일 파싱 중 오류 발생: submissionIds=" + message.getSubmissionIds(), e);
        }
    }



}
