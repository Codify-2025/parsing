package Codify.parsing.service;

import Codify.parsing.config.cpp.CppParsingTable;
import Codify.parsing.domain.Result;
import Codify.parsing.dto.CodeDto;
import Codify.parsing.dto.ResultDto;
import Codify.parsing.repository.ResultRepository;
import Codify.parsing.service.parsing.ASTNode;
import Codify.parsing.service.parsing.Parsing;
import Codify.parsing.service.token.CppTokenizer;
import Codify.parsing.service.token.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// ParsingService 단위 테스트 - 서비스 로직 흐름에 집중
@ExtendWith(MockitoExtension.class)
class ParsingServiceTest {

    @Mock
    private ResultRepository resultRepository;
    
    @Mock
    private CppTokenizer cppTokenizer;
    
    @Mock
    private CppParsingTable cppParsingTable;
    
    @Mock
    private Parsing parsing;

    // parsingService에 Mock 객체들 주입
    @InjectMocks
    private ParsingService parsingService;

    private CodeDto testCodeDto;
    private List<Token> mockTokens;
    private ASTNode mockASTNode;

    @BeforeEach
    //
    void setUp() {
        testCodeDto = new CodeDto("int main() { return 0; }", 1, 100, 2000001);
        //가짜 Tokens list 생성
        mockTokens = mock(List.class);
        //가짜 astNode 객체 생성
        mockASTNode = mock(ASTNode.class);
    }

    @Test
    @DisplayName("파싱 로직이 올바른 순서로 실행되는지 테스트")
    void parsing_ShouldCallTokenizerAndParserInCorrectOrder() {

        // Given - 가짜 객체들이 어떤 값을 반환할지 설정
        when(cppTokenizer.tokenize(testCodeDto.code())).thenReturn(mockTokens);
        when(parsing.parse(mockTokens, cppParsingTable)).thenReturn(mockASTNode);
        
        // When - service method호출
        parsingService.parsing(testCodeDto);
        
        // Then - 올바른 순서로 호출되었는지 확인
        InOrder inOrder = inOrder(cppTokenizer, parsing, resultRepository);
        inOrder.verify(cppTokenizer).tokenize(testCodeDto.code());
        inOrder.verify(parsing).parse(eq(mockTokens), eq(cppParsingTable));
        inOrder.verify(resultRepository).save(any(Result.class));
    }

    @Test
    @DisplayName("올바른 ResultDto를 반환하는지 테스트")
    void parsing_ShouldReturnCorrectResultDto() {
        // Given
        when(cppTokenizer.tokenize(any())).thenReturn(mockTokens);
        when(parsing.parse(any(), any())).thenReturn(mockASTNode);
        
        // When
        ResultDto result = parsingService.parsing(testCodeDto);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.assignmentId()).isEqualTo(1);
        assertThat(result.studentId()).isEqualTo(2000001);
        assertThat(result.submissionId()).isEqualTo(100);
    }
    
    @Test
    @DisplayName("Result 엔티티가 올바른 데이터로 저장되는지 테스트")
    void parsing_ShouldSaveResultWithCorrectData() {
        // Given
        when(cppTokenizer.tokenize(any())).thenReturn(mockTokens);
        when(parsing.parse(any(), any())).thenReturn(mockASTNode);
        
        // When
        parsingService.parsing(testCodeDto);
        
        // Then
        verify(resultRepository).save(any(Result.class));
    }

}