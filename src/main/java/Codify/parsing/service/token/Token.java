package Codify.parsing.service.token;

//불변성을 보장하기 위해 record 사용

public record Token(String type, String value, int line, int column) {}
