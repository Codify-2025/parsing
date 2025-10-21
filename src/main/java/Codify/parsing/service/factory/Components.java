package Codify.parsing.service.factory;

import Codify.parsing.config.cpp.ParsingTable;
import Codify.parsing.service.parsing.Parser;
import Codify.parsing.service.token.Tokenizer;

public record Components(
        Tokenizer tokenizer,
        Parser parser,
        ParsingTable parsingTable
) {}