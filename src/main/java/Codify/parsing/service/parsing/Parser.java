package Codify.parsing.service.parsing;

import Codify.parsing.config.cpp.ParsingTable;
import Codify.parsing.service.token.Token;

import java.util.List;

public interface Parser {
    ASTNode parse(List<Token> tokens, ParsingTable table);
}
