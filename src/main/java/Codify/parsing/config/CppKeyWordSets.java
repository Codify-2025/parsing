package Codify.parsing.config;

import java.util.Set;

//불변성을 보장하기 위해 record 사용
public record CppKeyWordSets(
        Set<String> TYPE,
        Set<String> CONTROL,
        Set<String> IO,
        Set<String> ACCESS,
        Set<String> CXX,
        Set<String> EXCEPTION,
        Set<String> OTHERS,
        Set<String> HEADER
) {}

