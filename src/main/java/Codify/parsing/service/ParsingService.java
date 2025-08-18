package Codify.parsing.service;

import Codify.parsing.dto.CodeDto;
import Codify.parsing.dto.ResultDto;
import Codify.parsing.repository.ResultRepository;
import Codify.parsing.service.token.CppTokenizer;
import Codify.parsing.service.token.Token;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParsingService {
    private final ResultRepository resultRepository;
    private final CppTokenizer cppTokenizer;

    @Transactional
    public ResultDto parsing(CodeDto codeDto) {
        int studentId = codeDto.getStudentId();
        int submissionId = codeDto.getSubmissionId();

        List<Token> tokens = cppTokenizer.tokenize(codeDto.getCode());



        return ResultDto.builder()
                .studentId(studentId)
                .submissionId(submissionId)
                .build();
    }



}
