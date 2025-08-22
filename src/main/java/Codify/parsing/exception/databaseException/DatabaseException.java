package Codify.parsing.exception.databaseException;

import Codify.parsing.exception.ErrorCode;
import Codify.parsing.exception.baseException.BaseException;

public class DatabaseException extends BaseException {
    public DatabaseException(String message) {
        super(ErrorCode.DATABASE_ERROR);
    }
}

