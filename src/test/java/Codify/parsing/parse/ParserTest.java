package Codify.parsing.parse;

import Codify.parsing.config.cpp.CppParsingTable;
import Codify.parsing.exception.parsingException.SyntaxException;
import Codify.parsing.service.parsing.CppParsing;
import Codify.parsing.service.token.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParserTest {
    @Mock
    private CppParsingTable cppParsingTable;

    @InjectMocks  // 실제 Parsing 객체 생성
    private CppParsing cppParsing;

    private List<Token> validTokens;

    @BeforeEach
    void setUp() {
        validTokens = List.of(
                new Token("TYPE", "int", 1, 0),
                new Token("IDENT", "main", 1, 4)
        );
    }

    @Test
    @DisplayName("파싱 테이블에 없는 액션일 때 SyntaxException 발생")
    void parse_UnknownAction_ShouldThrowSyntaxException() {
        // Given
        when(cppParsingTable.getAction(anyString()))
                .thenReturn("unknown_action");

        // When & Then
        SyntaxException exception = assertThrows(
                SyntaxException.class,
                () -> cppParsing.parse(validTokens, cppParsingTable)
        );

        assertThat(exception.getMessage()).contains("예상치 못한 토큰");
    }


}
