package Codify.parsing.service.token;

import Codify.parsing.config.CppKeyWordSets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CppTokenizer {
    private final CppKeyWordSets cppKeyWordSets;

    private static boolean isSymbol(char c) {
        return ";(){}[]+-*/=<>,.&|!".indexOf(c) != -1;
    }

    public List<Token> tokenize(String code) {

        //토큰을 저장할 list 생성
        List<Token> tokens = new ArrayList<>();

        //현재 위치
        int current = 0;

        //코드 행
        int line = 1;

        //코드 열
        int column = 0;

        //code 길이만큼 순회
        while (current < code.length()) {

            //현재 index에 해당하는 문자를 c에 저장
            char c = code.charAt(current);

            switch (c) {
                //공백과 탭은 다음 열로 이동
                case' ':
                case'\t':{
                    current++;
                    column++;
                    break;
                }
                //줄바꿈 -> 다음 줄로 이동 후 column 초기화
                case'\n':{
                    current++;
                    line++;
                    column = 0;
                    break;
                }
                //주석 또는 symbol
                case'/': {
                    //한 줄 주석
                    if (current + 1 < code.length() && code.charAt(current + 1) == '/') {
                        int startColumn = column;
                        StringBuilder sb = new StringBuilder();
                        sb.append("//");
                        current += 2;
                        column += 2;

                        while (current < code.length() && code.charAt(current) != '\n') {
                            sb.append(code.charAt(current));
                            current++;
                            column++;
                        }
                        tokens.add(new Token("COMMENT", sb.toString(), line, startColumn));
                        break;

                    }
                    //여러 줄 주석
                    if (current + 1 < code.length() && code.charAt(current + 1) == '*') {
                        int startColumn = column;
                        int startLine = line;
                        StringBuilder sb = new StringBuilder();
                        sb.append("/*");
                        current += 2;
                        column += 2;

                        while (current + 1 < code.length()) {
                            char ch = code.charAt(current);
                            char next = code.charAt(current + 1);

                            sb.append(ch);
                            current++;
                            column++;
                            if (ch == '\n') {
                                line++;
                                column = 0;
                            }

                            if (ch == '*' && next == '/') {
                                sb.append('/');
                                current++;
                                column++;
                                break;
                            }
                        }
                        tokens.add(new Token("COMMENT", sb.toString(), startLine, startColumn));
                    }
                    break;
                }
                //문자열 리터럴
                case '"': {
                    int startColumn = column;
                    current++;
                    column++;
                    StringBuilder sb = new StringBuilder();
                    while (current < code.length() && code.charAt(current) != '"') {
                        sb.append(code.charAt(current));
                        current++;
                        column++;
                    }
                    current++;
                    column++;
                    tokens.add(new Token("STRING", sb.toString(), line, startColumn));
                    break;
                }
                default:
                    //숫자
                    if (Character.isDigit(c)) {
                        int startColumn = column;
                        StringBuilder sb = new StringBuilder();
                        while (current < code.length() && Character.isDigit(code.charAt(current))) {
                            sb.append(code.charAt(current));
                            current++;
                            column++;
                        }
                        tokens.add(new Token("NUMBER", sb.toString(), line, startColumn));
                        continue;
                    }

                    //키워드, 식별자
                    if (Character.isLetter(c) || c == '_') {
                        int startColumn = column;
                        StringBuilder sb = new StringBuilder();
                        while (current < code.length()) {
                            char ch = code.charAt(current);

                            // 알파벳, 숫자, 언더스코어
                            if (Character.isLetterOrDigit(ch) || ch == '_') {
                                sb.append(ch);
                                current++;
                                column++;
                            }
                            // 네임스페이스 연산자 ::
                            else if (ch == ':' && current + 1 < code.length() && code.charAt(current + 1) == ':') {
                                sb.append("::");
                                current += 2;
                                column += 2;
                            }
                            else {
                                break; // 토큰 경계
                            }
                        }

                        String value = sb.toString();
                        String type;

                        if (cppKeyWordSets.TYPE().contains(value)) {
                            type = "TYPE";
                        } else if (cppKeyWordSets.CONTROL().contains(value)) {
                            type = "CONTROL";
                        } else if (cppKeyWordSets.IO().contains(value)) {
                            type = "IO";
                        } else if (cppKeyWordSets.ACCESS().contains(value)) {
                            type = "ACCESS";
                        } else if (cppKeyWordSets.CXX().contains(value)) {
                            type = "CXX";
                        } else if (cppKeyWordSets.EXCEPTION().contains(value)) {
                            type = "EXCEPTION";
                        } else if (cppKeyWordSets.OTHERS().contains(value)) {
                            type = "OTHERS";
                        } else if (cppKeyWordSets.HEADER().contains(value)) {
                            type = "HEADER";
                        } else {
                            type = "IDENT";
                        }
                        tokens.add(new Token(type, value, line, startColumn));
                        continue;
                    }

                    // 기호 및 연산자 처리
                    if (current + 1 < code.length()) {
                        char next = code.charAt(current + 1);
                        String twoChar = "" + code.charAt(current) + next;

                        // 두 글자 연산자 우선 처리
                        if (twoChar.equals("&&") || twoChar.equals("||") ||
                                twoChar.equals("==") || twoChar.equals("!=") ||
                                twoChar.equals("<=") || twoChar.equals(">=") ||
                                twoChar.equals("++") || twoChar.equals("--") ||
                                twoChar.equals("+=") || twoChar.equals("-=") ||
                                twoChar.equals(">>") || twoChar.equals("<<")) {
                            tokens.add(new Token("SYMBOL", twoChar, line, column));
                            current += 2;
                            column += 2;
                            continue;
                        }
                    }

                    //기호
                    if (isSymbol(c)) {
                        tokens.add(new Token("SYMBOL", Character.toString(c), line, column));
                        current++;
                        column++;
                        continue;
                    }
                    current++;
                    column++;

            }
        }
        return tokens;
    }

}
