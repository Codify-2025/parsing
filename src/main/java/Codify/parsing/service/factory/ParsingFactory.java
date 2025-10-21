package Codify.parsing.service.factory;

import Codify.parsing.config.cpp.CppParsingTable;
import Codify.parsing.service.parsing.CppParsing;
import Codify.parsing.service.token.CppTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParsingFactory {
    private final CppTokenizer cppTokenizer;
    private final CppParsing cppParsing;
    private final CppParsingTable cppParsingTable;

    public Components createComponent(String language) {
        return switch (language.toLowerCase()) {
            case "cpp" -> new
                    Components(cppTokenizer, cppParsing,
                    cppParsingTable);
            default -> throw new
                    IllegalArgumentException("Unsupported: " +
                    language);
        };
    }
}


