package Codify.parsing.service.parsing;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class ASTNode {
    private String type;
    private String value;
    private final int line;
    private List<ASTNode> children = new ArrayList<>(); //AST Node의 자식 노드를 list에 저장

    //constructor -> 같은 package(parsing)에서만 생성 가능(package-private 생성자)
    ASTNode(String type, String value, int line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    //원본 리스트를 읽기 전용으로 포장해서 반환
    public List<ASTNode> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    //package-private method
    //같은 package(parsing)에서만 사용 가능
    void updateType(String newType) {
        this.type = newType;
    }

    //package-private method
    void updateValue(String newValue) {
        this.value = newValue;
    }

    //package-private method
    //children Node를 쌓는 method
    void addChild(ASTNode child) {
        children.add(child);
    }

}
