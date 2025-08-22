package Codify.parsing.service.parsing;

import lombok.Getter;

@Getter
public class ParseResult {

    private ASTNode astNode;
    private int index;

    ParseResult(ASTNode astNode, int index) {
        this.astNode = astNode;
        this.index = index;
    }
}
