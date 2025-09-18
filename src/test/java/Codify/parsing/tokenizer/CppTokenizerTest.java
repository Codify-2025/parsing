package Codify.parsing.tokenizer;

import Codify.parsing.config.cpp.CppKeyWordSets;
import Codify.parsing.service.token.CppTokenizer;
import Codify.parsing.service.token.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CppTokenizerTest {

    private CppTokenizer tokenizer;

    @BeforeEach
        //필요한 의존성 주입
    public void setUp() {
        Set<String> TYPE = Set.of("int", "char", "float", "double", "bool", "void");
        Set<String> CONTROL = Set.of(
                "if", "else", "while", "for", "do", "switch", "case", "default",
                "break", "continue", "return", "goto");

        Set<String> IO = Set.of(
                "cin", "cout", "endl", "std::cout", "std::endl", "cin >>", "cout <<"
        );
        Set<String> ACCESS = Set.of(
                "public", "private", "protected"
        );
        Set<String> CXX = Set.of(
                "class", "struct",
                "new", "delete", "this", "namespace",
                "const", "static", "virtual"
        );
        Set<String> EXCEPTION = Set.of(
                "try", "catch", "throw"
        );
        Set<String> OTHERS = Set.of(
                "true", "false", "sizeof", "typedef", "inline", "enum"
        );
        Set<String> HEADER = Set.of(
                "include", "using", "define"
        );

        CppKeyWordSets cppKeyWordSets = new CppKeyWordSets(TYPE, CONTROL, IO, ACCESS, CXX, EXCEPTION, OTHERS, HEADER);

        this.tokenizer = new CppTokenizer(cppKeyWordSets);
    }

    @Test
    @DisplayName("토큰화 로직 테스트")
    public void tokenizerMethodTest() {
        //given
        String code = """
        int add(int a, int b) {
            int k = 0;
            k = 1 + 2;
        }
        """;
        List<Token> expectedTokens = List.of(
                new Token("TYPE","int",1,0),
                new Token("IDENT","add",1,4),
                new Token("SYMBOL","(",1,7),
                new Token("TYPE","int",1,8),
                new Token("IDENT","a",1,12),
                new Token("SYMBOL",",",1,13),
                new Token("TYPE","int",1,15),
                new Token("IDENT","b",1,19),
                new Token("SYMBOL",")",1,20),
                new Token("SYMBOL","{",1,22),
                new Token("TYPE","int",2,4),
                new Token("IDENT","k",2,8),
                new Token("SYMBOL","=",2,10),
                new Token("NUMBER","0",2,12),
                new Token("SYMBOL",";",2,13),
                new Token("IDENT","k",3,4),
                new Token("SYMBOL","=",3,6),
                new Token("NUMBER","1",3,8),
                new Token("SYMBOL","+",3,10),
                new Token("NUMBER","2",3,12),
                new Token( "SYMBOL",";",3,13),
                new Token("SYMBOL","}",4,0)
        );

        //when
        List<Token> actualTokens = tokenizer.tokenize(code);

        //Then
        assertThat(actualTokens).isEqualTo(expectedTokens);

    }

}

