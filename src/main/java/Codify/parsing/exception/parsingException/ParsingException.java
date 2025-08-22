package Codify.parsing.exception.parsingException;

import Codify.parsing.exception.ErrorCode;
import Codify.parsing.exception.baseException.BaseException;

public class ParsingException extends BaseException {

    private final int line;

    public ParsingException(String message, int line) {
        super(ErrorCode.INVALID_INPUT_VALUE);
        this.line = line;
    }
}
