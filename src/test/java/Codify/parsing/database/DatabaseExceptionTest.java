package Codify.parsing.database;

import Codify.parsing.config.cpp.CppParsingTable;
import Codify.parsing.domain.Result;
import Codify.parsing.dto.CodeDto;
import Codify.parsing.exception.databaseException.DatabaseException;
import Codify.parsing.repository.ResultRepository;
import Codify.parsing.service.ParsingService;
import Codify.parsing.service.parsing.ASTNode;
import Codify.parsing.service.parsing.Parsing;
import Codify.parsing.service.token.CppTokenizer;
import Codify.parsing.service.token.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
public class DatabaseExceptionTest {
    @Mock
    private ResultRepository resultRepository;
    @Mock
    private CppTokenizer cppTokenizer;
    @Mock
    private CppParsingTable cppParsingTable;
    @Mock
    private Parsing parsing;

    @InjectMocks
    private ParsingService parsingService;

    private CodeDto testCodeDto;
    private List<Token> mockTokens;
    private ASTNode mockASTNode;

    @BeforeEach
    void setUp() {
        testCodeDto = new CodeDto("int main() { return 0; }", 1,
                100, 2000001);
        mockTokens = mock(List.class);
        mockASTNode = mock(ASTNode.class);
    }

    @Test
    @DisplayName("데이터베이스 저장 실패 시 DatabaseException 발생")
    void parsing_DatabaseSaveFails_ShouldThrowDatabaseException() {
        // Given
        when(cppTokenizer.tokenize(testCodeDto.code())).thenReturn(
                mockTokens);
        when(parsing.parse(mockTokens,
                cppParsingTable)).thenReturn(mockASTNode);

        // 데이터베이스 저장 시 예외 발생 시뮬레이션
        when(resultRepository.save(any(Result.class)))
                .thenThrow(new DatabaseException("Database connection failed") {
                });

        // When & Then
        DatabaseException exception = assertThrows(
                DatabaseException.class,
                () -> parsingService.parsing(testCodeDto)
        );

        // 예외 메시지 검증
        assertThat(exception.getMessage()).contains("파싱 결과 저장 중 데이터베이스 오류가 발생했습니다");

        // 토큰화와 파싱은 정상 실행되었는지 확인
        verify(cppTokenizer).tokenize(testCodeDto.code());
        verify(parsing).parse(mockTokens, cppParsingTable);
        verify(resultRepository).save(any(Result.class));

    }
}
