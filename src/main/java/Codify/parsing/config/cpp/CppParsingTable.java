package Codify.parsing.config.cpp;

import java.util.Map;

import static java.util.Map.entry;

public class CppParsingTable implements ParsingTable {
    private Map<String, String> cppParsingTable = Map.ofEntries(
            entry("TYPE", "S"),
            entry("CONTROL", "S"),
            entry("IO", "S"),
            entry("ACCESS", "S"),
            entry("CXX", "S"),
            entry("EXCEPTION", "S"),
            entry("OTHERS", "S"),
            entry("IDENT", "S"), //함수명, 변수명
            entry("NUMBER", "S"),
            entry("STRING", "S"),
            entry("BREAK", "S"),
            entry("SYMBOL:(", "S"), //소괄호 열기 -> 함수 정의 or 함수 call
            entry("SYMBOL:)", "S"), //소괄호 닫기
            entry("SYMBOL:{", "R_Block"), //대괄호 열기
            entry("SYMBOL:}", "R_Block_Close"), //대괄호 닫기
            entry("SYMBOL:;", "R_Stmt"), //세미콜론
            entry("SYMBOL:=", "S"),
            entry("SYMBOL:+", "S"),
            entry("SYMBOL:++", "S"),
            entry("SYMBOL:-", "S"),
            entry("SYMBOL:--", "S"),
            entry("SYMBOL:+=", "S"),
            entry("SYMBOL:-=", "S"),
            entry("SYMBOL:==", "S"),
            entry("SYMBOL:>", "S"),
            entry("SYMBOL:>=", "S"),
            entry("SYMBOL:<", "S"),
            entry("SYMBOL:<=", "S"),
            entry("SYMBOL:&&", "S"),
            entry("SYMBOL:>>", "S"),
            entry("SYMBOL:<<", "S"),
            entry("SYMBOL:,", "S"),
            entry("SYMBOL:[", "S"),
            entry("SYMBOL:]", "S"),
            entry("KEYWORD:if", "S"),
            entry("KEYWORD:for", "S"),
            entry("KEYWORD:switch", "S"),
            entry("KEYWORD:case", "S"),
            entry("HEADER", "R_Header"), //#include, #define, using
            entry("EOF", "ACCEPT")
    );

    @Override
    public Map<String, String> getRules() {
        return cppParsingTable;
    }
}


