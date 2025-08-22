package Codify.parsing.config;

import Codify.parsing.config.cpp.CppParsingTable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ParsingTableConfig {
    @Bean
    public CppParsingTable cppParsingTable() {
        return new CppParsingTable();
    }
}
