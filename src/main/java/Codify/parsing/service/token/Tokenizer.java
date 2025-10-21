package Codify.parsing.service.token;

import java.util.List;

public interface Tokenizer {
    List<Token> tokenize(String code);
}
