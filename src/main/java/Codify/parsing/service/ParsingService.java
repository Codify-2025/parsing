package Codify.parsing.service;

import Codify.parsing.config.cpp.CppParsingTable;
import Codify.parsing.domain.Result;
import Codify.parsing.dto.CodeDto;
import Codify.parsing.dto.ResultDto;
import Codify.parsing.exception.databaseException.DatabaseException;
import Codify.parsing.repository.ResultRepository;
import Codify.parsing.service.parsing.ASTNode;
import Codify.parsing.service.parsing.Parsing;
import Codify.parsing.service.token.CppTokenizer;
import Codify.parsing.service.token.Token;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParsingService {
    private final ResultRepository resultRepository;
    private final CppTokenizer cppTokenizer;
    private final CppParsingTable cppParsingTable;
    private final Parsing parsing;

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
    }

    @Transactional
    public ResultDto parseFromFile(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource("test-cpp/" + fileName);
        String code = Files.readString(resource.getFile().toPath());
        
        CodeDto codeDto = new CodeDto(code, 1, 5,2000004);
        return parsing(codeDto);
    }



}
