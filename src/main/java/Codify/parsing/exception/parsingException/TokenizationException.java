package Codify.parsing.exception.parsingException;

import Codify.parsing.exception.ErrorCode;
import Codify.parsing.exception.baseException.BaseException;

public class TokenizationException extends BaseException {
    private final int line;
    private final String customMessage;

    public TokenizationException(String message, int line) {
        super(ErrorCode.TOKENIZATION_ERROR);
        this.customMessage = "토큰화 실패: " + message;
        this.line = line;
    }

    @Override
    public String getMessage() {
        return customMessage + String.format(" line: %d ", line);
    }
}
