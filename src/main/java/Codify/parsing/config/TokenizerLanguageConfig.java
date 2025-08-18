package Codify.parsing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class TokenizerLanguageConfig {


    @Bean
    public CppKeyWordSets cppKeyWordSets() {

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

        return new CppKeyWordSets(TYPE,CONTROL,IO,ACCESS,CXX,EXCEPTION,OTHERS,HEADER);
    }

}
