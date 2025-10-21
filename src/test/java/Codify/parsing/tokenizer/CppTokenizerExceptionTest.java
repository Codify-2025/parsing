package Codify.parsing.tokenizer;

import Codify.parsing.config.cpp.CppKeyWordSets;
import Codify.parsing.exception.parsingException.TokenizationException;
import Codify.parsing.service.factory.Components;
import Codify.parsing.service.factory.ParsingFactory;
import Codify.parsing.service.token.CppTokenizer;
import Codify.parsing.service.token.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
public class CppTokenizerExceptionTest {
    @Mock
    private CppKeyWordSets cppKeyWordSets;
    @InjectMocks
    private ParsingFactory parsingFactory;

    @Test
    @DisplayName("null code 입력 시 TokenizerException 발생")
    void tokenize_NullCode_ShouldThrowTokenizationException() {
        // Given
        String nullCode = null;
        Components components = parsingFactory.createComponent("cpp");

        // When & Then
        TokenizationException exception = assertThrows(
                TokenizationException.class,
                () -> components.tokenizer().tokenize(nullCode)
        );

        // 예외 메시지 검증
        assertThat(exception.getMessage()).contains("토큰화 실패");
    }

}
