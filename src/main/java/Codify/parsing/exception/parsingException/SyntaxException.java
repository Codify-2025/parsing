package Codify.parsing.exception.parsingException;

import Codify.parsing.exception.ErrorCode;
import Codify.parsing.exception.baseException.BaseException;

public class SyntaxException extends BaseException {
    private final int line;
    private final String customMessage;

    public SyntaxException(String message, int line) {
        super(ErrorCode.SYNTAX_ERROR);
        this.customMessage = "구문 오류: " + message;
        this.line = line;
    }

    @Override
    public String getMessage() {
        return customMessage + String.format("line: %d ", line );
    }

}
