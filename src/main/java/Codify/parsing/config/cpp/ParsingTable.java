package Codify.parsing.config.cpp;

import java.util.Map;

public interface ParsingTable {
    Map<String, String> getRules();

    //parsingTable을 확인하고 -> 해당 Key값이 테이블에 없다면 ERROR를 리턴하는 메서드
    //모든 언어에서 동일한 로직을 사용하기 때문에 default method로 선언
    default String getAction(String key) {
        return getRules().getOrDefault(key, "ERROR");
    }
}
