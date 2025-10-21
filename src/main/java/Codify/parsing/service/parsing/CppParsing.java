package Codify.parsing.service.parsing;

import Codify.parsing.config.cpp.ParsingTable;
import Codify.parsing.exception.parsingException.SyntaxException;
import Codify.parsing.service.token.Token;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CppParsing implements Parser{

    private static final Set<String>
            OPERATORS = Set.of(
            "=", "+", "-", "++", "--", "*",
            "/", "||", "&&",
            "==", "!=", "<", ">", "<=", ">=",
            "+=", "-="
    );

    private static final int FOR_UPDATE_IS_NOT_UNARY = 2;
    //method

    //node를 받아 type update
    private static void convertTokenType(ASTNode node) {
        switch (node.getType()) {
            case "IDENT" -> {
                node.updateType("VariableName");
            }
            case "NUMBER" -> {
                node.updateType("Literal");
            }
            case "TYPE" -> {
                node.updateType("Type");
            }
            case "STRING" -> {
                node.updateType("StringLiteral");
            }
            case "SYMBOL" -> {
                if (isOperator(node.getValue())) {
                    node.updateType("Operator");
                }
            }
        }
    }



    //산술 연산자 우선순위 함수
    public static int precedence(String op) {
        return switch (op) {
            case "=" -> 1;
            case "||" -> 2;
            case "&&" -> 3;
            case "==", "!=", "<", ">", "<=", ">=" -> 4;
            case "+", "-", "++", "--", "-=", "+=" -> 5;
            case "*", "/" -> 6;
            default -> 0;
        };
    }

    //연산자 판별 함수
    public static boolean isOperator(String value) {
        return OPERATORS.contains(value);
    }

    public static boolean isUnaryExpr(String value) {
        return value.equals("++") || value.equals("--") || value.equals("!");
    }

    //switch 파싱 함수
    public Integer buildSwitch(List<Token> tokens, int index, ASTNode blockStmt) {

        Deque<ASTNode> tempDeque = new ArrayDeque<>();
        boolean isEnd = false;
        while (!isEnd) {
            Token token = tokens.get(index);
            index++;
            if (token.value().equals(")")) {
                isEnd = true;
            } else {
                ASTNode temp = new ASTNode(token.type(), token.value(), token.line());
                tempDeque.push(temp);
            }
        }

        ASTNode declaration = tempDeque.removeLast();
        declaration.updateType("SwitchStmt");

        //()제거
        tempDeque.removeLast();

        //()안의 토큰 파싱
        int tempSize = tempDeque.size();
        for (int i = 0; i < tempSize; i++) {
            ASTNode node = tempDeque.removeLast();
            convertTokenType(node);
            declaration.addChild(node);
        }

        ASTNode switchBlock = new ASTNode("BlockStmt", null, declaration.getLine());
        index++;

        boolean isCase = false;
        boolean breakpoint = false;


        Token firstToken = tokens.get(index);
        if (firstToken.value().equals("case")) {
            ASTNode firstNode = new ASTNode(firstToken.type(), firstToken.value(), firstToken.line());
            tempDeque.push(firstNode);
        } else {
            System.out.println("parsing error " + firstToken.value());
        }
        index++;

        while (!breakpoint) {
            Token nodeToken = tokens.get(index);
            Token nextToken = tokens.get(index + 1);
            ASTNode node = new ASTNode(nodeToken.type(), nodeToken.value(), nodeToken.line());

            if (nextToken.value().equals("case") || nextToken.value().equals("default")) {
                isCase = true;
                tempDeque.push(node);
            }
            if (nextToken.value().equals("}")) {
                breakpoint = true;
            }

            if (isCase) {
                //case + 조건 파싱
                ASTNode caseNode = tempDeque.removeLast();
                caseNode.updateType("SwitchEntry");
                ASTNode literalNode = tempDeque.removeLast();
                if (literalNode.getType().equals("STRING")) {
                    literalNode.updateType("StringLiteral");
                } else {
                    literalNode.updateType("Literal");
                }

                caseNode.addChild(literalNode);

                //case 조건이 맞으면 실행하는 토큰들 파싱
                int length = tempDeque.size();
                Deque<ASTNode> nodeTempDeque = new ArrayDeque<>();
                for (int i = 0; i < length; i++) {
                    ASTNode tempNode = tempDeque.removeLast();
                    if (tempNode.getValue().equals(";")) {
                        ParseResult parseResult = buildStmtNode(nodeTempDeque,i);
                        caseNode.addChild(parseResult.getAstNode());
                    } else {
                        nodeTempDeque.push(tempNode);
                    }
                }
                switchBlock.addChild(caseNode);
                tempDeque.clear();
                isCase = false;
            } else {
                tempDeque.push(node);
            }

            index++;
        }
        //default문 파싱 or case문 파싱(마지막에 남은거 파싱)
        if (!tempDeque.isEmpty()) {
            ASTNode node = tempDeque.removeLast();
            ASTNode lastCaseNode = new ASTNode("SwitchEntry", null, node.getLine());
            if (node.getValue().equals("case")) {
                ASTNode literalNode = tempDeque.removeLast();
                if (literalNode.getType().equals("STRING")) {
                    literalNode.updateType("StringLiteral");
                } else {
                    literalNode.updateType("Literal");
                }
                lastCaseNode.addChild(literalNode);
            } else {
                ASTNode literalNode = new ASTNode("Literal", node.getValue(), lastCaseNode.getLine());
                lastCaseNode.addChild(literalNode);
            }

            int length = tempDeque.size();
            Deque<ASTNode> nodeTempDeque = new ArrayDeque<>();
            for (int i = 0; i < length; i++) {
                ASTNode tempNode = tempDeque.removeLast();
                if (tempNode.getValue().equals(";")) {
                    ParseResult parseResult = buildStmtNode(nodeTempDeque,i);
                    lastCaseNode.addChild(parseResult.getAstNode());
                    nodeTempDeque.clear();
                } else {
                    nodeTempDeque.push(tempNode);
                }
            }

            switchBlock.addChild(lastCaseNode);
        }
        declaration.addChild(switchBlock);
        blockStmt.addChild(declaration);

        return index;

    }

    //method call 파싱 함수
    public ASTNode buildMethodCall(Deque<ASTNode> tempDeque, boolean isIO) {
        // ()를 제거
        ASTNode resultNode = new ASTNode("Arguments", null, tempDeque.getLast().getLine());
        if (!isIO) {
            tempDeque.removeFirst();
            tempDeque.removeLast();
            int length = tempDeque.size();
            for (int i = 0; i < length; i++) {
                ASTNode node = tempDeque.removeLast();
                if (!node.getValue().equals(",")) {
                    node.updateType("VariableName");
                    resultNode.addChild(node);
                }
            }
        } else {
            int length = tempDeque.size();
            for (int i = 0; i < length; i++) {
                ASTNode node = tempDeque.removeLast();

                boolean isIoSymbol = false;
                if (node.getValue().equals(">>") || node.getValue().equals("<<")) {
                    isIoSymbol = true;
                }
                if (!isIoSymbol) {
                    convertTokenType(node);
                    resultNode.addChild(node);
                }
            }
        }

        return resultNode;
    }

    //배열 파싱 함수
    public ASTNode buildArray(Deque<ASTNode> tempDeque) {
        ASTNode isType = tempDeque.removeLast();
        ASTNode parent;
        //배열 호출
        if (isType.getType().equals("IDENT")) {
            parent = new ASTNode("ArrayAccessExpr", null, isType.getLine());
            isType.updateType("VariableName");
            parent.addChild(isType);

            //[]안 토큰 파싱
            int length = tempDeque.size();
            for (int i = 0; i < length; i++) {
                ASTNode arrNode = tempDeque.removeLast();
                if (!arrNode.getType().equals("SYMBOL")) {
                    arrNode.updateType("VariableName");
                    parent.addChild(arrNode);
                }
            }

        } else { //배열 생성
            parent = new ASTNode("VariableDeclaration", null, isType.getLine());
            isType.updateType("Type");

            ASTNode declaration = new ASTNode("ArrayDeclarator", null, isType.getLine());

            ASTNode variableName = tempDeque.removeLast();
            variableName.updateType("VariableName");

            declaration.addChild(variableName);

            //[]안 토큰 파싱
            int length = tempDeque.size();
            for (int i = 0; i < length; i++) {
                ASTNode arrNode = tempDeque.removeLast();
                if (!arrNode.getType().equals("SYMBOL")) {
                    arrNode.updateType("ArraySize");
                    declaration.addChild(arrNode);
                }
            }
            parent.addChild(isType);
            parent.addChild(declaration);
        }

        return parent;
    }

    //연산자 탐색 함수
    public ASTNode buildExpressionTree(Deque<ASTNode> tempDeque) {
        Deque<ASTNode> nodeDeque = new ArrayDeque<>();
        Deque<ASTNode> opDeque = new ArrayDeque<>();

        while (!tempDeque.isEmpty()) {
            ASTNode stmtNode = tempDeque.pop(); //deque의 원소 pop
            //if-else 문으로 연산자의 우선순위에 따라 stack에 넣음
            if (isOperator(stmtNode.getValue())) { //stmtNode가 연산자라면
                while (!opDeque.isEmpty() && precedence(opDeque.peek().getValue()) >= precedence(stmtNode.getValue())) { //연산자의 우선순위 파악
                    //operatorNode pop
                    ASTNode op = opDeque.pop();
                    op.updateType("Operator");

                    ASTNode right = nodeDeque.pop();
                    ASTNode left = nodeDeque.pop();
                    ASTNode opNode;

                    if (op.getValue().equals("=")) {
                        opNode = new ASTNode("InitExpr", op.getValue(), stmtNode.getLine());
                    } else if (op.getValue().equals("!") || op.getValue().equals("++") || op.getValue().equals("--")) {
                        opNode = new ASTNode("UnaryExpr", null, stmtNode.getLine());
                    } else {
                        opNode = new ASTNode("BinaryExpr", null, stmtNode.getLine());
                    }

                    opNode.addChild(right);
                    opNode.addChild(op);
                    opNode.addChild(left);
                    nodeDeque.push(opNode);
                }
                opDeque.push(stmtNode);
            } else {
                convertTokenType(stmtNode);

                nodeDeque.push(stmtNode);
            }
        } //연산자 파악 후 노드스택에 넣는건 문제 x

        while (!opDeque.isEmpty()) {
            ASTNode op = opDeque.pop();

            op.updateType("Operator");
            ASTNode right = nodeDeque.pop();
            ASTNode left = nodeDeque.pop();
            ASTNode opNode;
            if (op.getValue().equals("=")) {
                opNode = new ASTNode("InitExpr", op.getValue(), left.getLine());
            } else if (op.getValue().equals("!") || op.getValue().equals("++") || op.getValue().equals("--")) {
                opNode = new ASTNode("UnaryExpr", null, left.getLine());
            } else {
                opNode = new ASTNode("BinaryExpr", null, left.getLine());
            }
            opNode.addChild(right);
            opNode.addChild(op);
            opNode.addChild(left);
            nodeDeque.push(opNode);
        }
        //nodeStack을 ASTNode형태로 리턴하면 끝날듯...
        return nodeDeque.pop();
    }

    //{ 만났을때 파싱하는 함수
    public ParseResult buildBlock(Deque<ASTNode> tempDeque, List<Token> tokens, int index) {
        //선언부 파싱
        ASTNode declaration = tempDeque.removeLast();

        //for 문 파싱 여부 결정
        boolean isFor = false;

        switch (declaration.getValue()) {
            case "if" -> {
                declaration.updateType("IfStmt");
                //()파싱 -> tempDeque에서 ()제거
                tempDeque.removeLast();
                tempDeque.removeFirst();
                ASTNode ifChild;
                if (tempDeque.size() > 1) {
                    ifChild = buildExpressionTree(tempDeque);
                } else {
                    //true, false에 따른 처리 또 필요할듯..
                    ASTNode tempNode = tempDeque.pop();
                    ifChild = new ASTNode("VariableName", tempNode.getValue(), tempNode.getLine());
                }
                declaration.addChild(ifChild);
            }
            case "else" -> {
                declaration.updateType("ElseStmt");
            }
            case "while" -> {
                declaration.updateType("WhileStmt");

                tempDeque.removeLast();
                tempDeque.removeFirst();
                ASTNode whileChild;
                if (tempDeque.size() > 1) {
                    whileChild = buildExpressionTree(tempDeque);
                } else {
                    ASTNode tempNode = tempDeque.pop();
                    whileChild = new ASTNode("VariableName", tempNode.getValue(), tempNode.getLine());
                }
                declaration.addChild(whileChild);
            }
            case "for" -> {
                declaration.updateType("ForStmt");

                tempDeque.removeLast();
                tempDeque.removeFirst();
                int length = tempDeque.size();
                boolean isequal = false;
                Deque<ASTNode> forTempDeque = new ArrayDeque<>();
                for (int l = 0; l < length; l++) {
                    ASTNode node = tempDeque.removeLast();

                    if (node.getValue().equals("=")) {
                        isequal = true;
                    }
                    if (node.getValue().equals(";")) {
                        ASTNode forChild;
                        if (isequal) {
                            forChild = buildStmtNode(forTempDeque, index).getAstNode();
                            isequal = false;
                        } else {
                            forChild = buildExpressionTree(forTempDeque);
                        }
                        declaration.addChild(forChild);
                        forTempDeque.clear();
                    } else {
                        forTempDeque.addFirst(node);
                    }
                }
                if (!forTempDeque.isEmpty()) {
                    if (forTempDeque.size() == FOR_UPDATE_IS_NOT_UNARY) {
                        ASTNode var = forTempDeque.removeFirst();
                        var.updateType("VariableName");
                        ASTNode op = forTempDeque.removeLast();
                        op.updateType("Operator");
                        ASTNode forChild = new ASTNode("UnaryExpr", null, var.getLine());
                        forChild.addChild(var);
                        forChild.addChild(op);
                        declaration.addChild(forChild);
                    } else {
                        ASTNode forChild = buildExpressionTree(forTempDeque);
                        declaration.addChild(forChild);
                    }
                    forTempDeque.clear();
                }
                isFor = false;
            }
            case "switch" -> {
                declaration.updateType("SwitchStmt");

                //()제거
                tempDeque.removeLast();
                tempDeque.removeFirst();

                //()안의 토큰 파싱
                ASTNode node = tempDeque.removeLast();
                convertTokenType(node);

                declaration.addChild(node);
            }
            default -> {
                ASTNode methodType = new ASTNode("Type", declaration.getValue(), declaration.getLine());
                ASTNode methodName = tempDeque.removeLast();
                methodName.updateType("FunctionName");
                ASTNode paramList = new ASTNode("ParameterList", null, methodName.getLine());

                declaration.updateType("MethodDeclaration");


                tempDeque.removeLast();
                tempDeque.removeFirst();
                if (!tempDeque.isEmpty()) {
                    int length = tempDeque.size();
                    Deque<ASTNode> methodTempDeque = new ArrayDeque<>();
                    for (int l = 0; l < length; l++) {
                        ASTNode node = tempDeque.removeLast();
                        if (node.getValue().equals(",")) {
                            ASTNode param = new ASTNode("Parameter", null, node.getLine());

                            ASTNode type = methodTempDeque.pop();
                            type.updateType("Type");
                            ASTNode variable = methodTempDeque.pop();
                            variable.updateType("VariableName");

                            param.addChild(type);
                            param.addChild(variable);

                            paramList.addChild(param);
                            methodTempDeque.clear();
                        } else {
                            methodTempDeque.addLast(node);
                        }
                    }
                    if (!methodTempDeque.isEmpty()) {
                        ASTNode param = new ASTNode("Parameter", null, paramList.getLine());

                        ASTNode type = methodTempDeque.pop();
                        type.updateType("Type");
                        ASTNode variable = methodTempDeque.pop();
                        variable.updateType("VariableName");

                        param.addChild(type);
                        param.addChild(variable);

                        paramList.addChild(param);
                    }
                    declaration.addChild(methodType);
                    declaration.addChild(methodName);
                    declaration.addChild(paramList);
                } else {
                    declaration.addChild(methodType);
                    declaration.addChild(methodName);
                }
            }
        }
        declaration.updateValue(null);
        tempDeque.clear();

        //{뒤쪽 파싱 시작
        ASTNode blockStmt = new ASTNode("BlockStmt", null, tokens.get(index).line());
        boolean breakpoint = true;
        int i = index+1;
        while (breakpoint) {
            Token token = tokens.get(i);
            String action = token.value();
            if (action.equals("switch")) {
                action = "switch";
            }
            if (action.equals("for")) {
                isFor = true;
            }
            switch (action) {
                case ";" -> {
                    if (!isFor) {
                        ParseResult result = buildStmtNode(tempDeque, index);
                        blockStmt.addChild(result.getAstNode());
                        tempDeque.clear();
                        i++;
                    } else {
                        tempDeque.push(new ASTNode(token.type(), token.value(), token.line()));
                        i++;
                    }
                }
                case "{" -> {
                    ParseResult reResult = buildBlock(tempDeque, tokens, i);
                    blockStmt.addChild(reResult.getAstNode());
                    i = reResult.getIndex()+1;
                    tempDeque.clear();
                    isFor = false;
                }
                case "}" -> {
                    breakpoint = false;
                }
                case "switch" -> {
                    int switchIndex = buildSwitch(tokens, i,blockStmt);

                    //}의 index를 출력 -> }까지 파싱한 후 }의 이전 인덱스로 갱신
                    i =switchIndex;
                    tempDeque.clear();
                    i++;
                }
                default -> {
                    tempDeque.push(new ASTNode(token.type(), token.value(), token.line()));
                    i++;
                }
            }
        }
        declaration.addChild(blockStmt);
        ParseResult buildBlockResult = new ParseResult(declaration, i);

        return buildBlockResult;
    }

    //;만났을때 파싱하는 함수
    public ParseResult buildStmtNode(Deque<ASTNode> tempDeque, int index) {
        ASTNode firstNode = tempDeque.removeLast();
        ASTNode secondNode = new ASTNode(firstNode.getType(), firstNode.getValue(), firstNode.getLine());
        if (!tempDeque.isEmpty()) {
            secondNode = tempDeque.getLast();
        }
        tempDeque.offerLast(firstNode);
        ASTNode lastNode = tempDeque.getFirst();

        boolean isMethodCall = false;
        boolean isReturn = false;
        boolean isArray = false;
        boolean isBreak = false;
        boolean isIO = false;

        ASTNode innerParent = new ASTNode("VariableName", null, firstNode.getLine());

        if (firstNode.getType().equals("IDENT") && isUnaryExpr(secondNode.getValue())) {
            innerParent.updateType("UnaryExpr");
        } else if (firstNode.getType().equals("TYPE")) {
            innerParent.updateType("VariableDeclaration");
        } else if (firstNode.getValue().equals("break")) {
            isBreak = true;
        }  else if (firstNode.getType().equals("IO")) {
            isMethodCall = true;
            isIO = true;
        } else if (firstNode.getValue().equals("return")) {
            isReturn = true;
        }

        if (lastNode.getValue().equals("]")) {
            isArray = true;
        }else if (lastNode.getValue().equals(")")) {
            isMethodCall = true;
        }

        Deque<ASTNode> leftDeque = new ArrayDeque<>();
        Deque<ASTNode> rightDeque = new ArrayDeque<>();
        boolean foundEqual = false;

        while (!tempDeque.isEmpty()) {
            ASTNode tempNode = tempDeque.removeLast();
            if (!foundEqual && tempNode.getValue().equals("=")) {
                foundEqual = true;
            } else if (!foundEqual) { // = 만나기 이전
                leftDeque.push(tempNode);
            }else
                rightDeque.push(tempNode);
        }

        if (foundEqual) {
            ASTNode rightExpr;
            ASTNode assign;
            if (rightDeque.getFirst().getValue().equals("]")) {
                isArray = true;
            }
            if (isMethodCall) {
                ASTNode fuctionName = rightDeque.removeLast();
                fuctionName.updateType("FunctionName");
                rightExpr = buildMethodCall(rightDeque,isIO);

                ASTNode methodCall = new ASTNode("MethodCallExpr", null, rightExpr.getLine());

                assign = new ASTNode("InitExpr", "=", rightExpr.getLine());
                methodCall.addChild(fuctionName);
                methodCall.addChild(rightExpr);
                assign.addChild(methodCall);
            } else if (isArray) {
                ASTNode arrayNode = buildArray(rightDeque);
                assign = new ASTNode("InitExpr", "=", arrayNode.getLine());
                assign.addChild(arrayNode);
            } else {
                rightExpr = buildExpressionTree(rightDeque);

                assign = new ASTNode("InitExpr", "=", rightExpr.getLine());
                assign.addChild(rightExpr);
            }

            if (leftDeque.getFirst().getValue().equals("]")) {
                isArray = true;
            }

            if (isArray) {
                ASTNode arrayNode = buildArray(leftDeque);
                innerParent = arrayNode;
            } else {
                int length = leftDeque.size();
                for (int l = 0; l < length; l++) {
                    ASTNode leftNode = leftDeque.removeLast();
                    convertTokenType(leftNode);
                    innerParent.addChild(leftNode);
                }
            }

            innerParent.addChild(assign);
        } else {
            if (isMethodCall) {
                innerParent.updateType("MethodCallExpr");
                ASTNode functionName = leftDeque.removeLast();
                functionName.updateType("FunctionName");

                ASTNode leftNode = buildMethodCall(leftDeque,isIO);

                innerParent.addChild(functionName);
                innerParent.addChild(leftNode);
            } else if (isReturn) {
                innerParent.updateType("ReturnStmt");
                leftDeque.removeLast();
                ASTNode returnNode = buildExpressionTree(leftDeque);
                innerParent.addChild(returnNode);
            } else if (isArray) {
                ASTNode arrayNode = buildArray(leftDeque);
                innerParent = arrayNode;
            } else if (isBreak) {
                innerParent.updateType("BreakStmt");
                innerParent.updateValue(firstNode.getValue());
            } else {
                int length = leftDeque.size();
                for (int l = 0; l < length; l++) {
                    ASTNode leftNode = leftDeque.removeLast();
                    convertTokenType(leftNode);
                    innerParent.addChild(leftNode);
                }
            }
        }
        return new ParseResult(innerParent, index+1);
    }

    @Override
    public ASTNode parse(List<Token> tokens, ParsingTable parsingTable) {
        // 입력 검증
        if (tokens == null) {
            throw new SyntaxException("토큰 리스트가 null입니다", 1 );
        }

        if (tokens.isEmpty()) {
            throw new SyntaxException("파싱할 토큰이 없습니다", 1 );
        }

        if (parsingTable == null) {
            throw new SyntaxException("파싱 테이블이 null입니다", 1);
        }


        List<ASTNode> astList = new ArrayList<>(); // AST tree 저장 stack
        Deque<ASTNode> tempDeque = new ArrayDeque<>(); // 임시 저장 스택. R을 만나면 규칙에 따라 node 들을 처리 후 AST Stack에 push
        int i = 0;

        //token의 길이 만큼 순회
        while (i < tokens.size()) {
            // i index의 token의 type을 가져와 parsingTable의 어떤 value와 key 값이 일치하는지 확인
            Token token = tokens.get(i);
            String key = token.type();
            if (token.type().equals("SYMBOL")) key += ":" + token.value();
            String action = parsingTable.getAction(key);

            switch (action) {
                case "S" -> {
                    tempDeque.push(new ASTNode(token.type(), token.value(), token.line())); //tempstack에 token push
                    i++;
                }
                case "R_Stmt" -> {
                    ASTNode firstNode = tempDeque.removeLast();
                    ASTNode secondNode = new ASTNode(firstNode.getType(), firstNode.getValue(), firstNode.getLine());
                    if (!tempDeque.isEmpty()) {
                        secondNode = tempDeque.getLast();
                    }
                    tempDeque.offerLast(firstNode);
                    if (firstNode.getValue().equals("for")) {
                        tempDeque.push(new ASTNode(token.type(), token.value(), token.line()));
                        i++;
                    } else {
                        ParseResult parseResult = buildStmtNode(tempDeque, i);
                        astList.add(parseResult.getAstNode());
                        i = parseResult.getIndex();
                        tempDeque.clear();
                    }
                }
                case "R_Block" -> {
                    ParseResult parseResult = buildBlock(tempDeque, tokens, i);
                    astList.add(parseResult.getAstNode());
                    i = parseResult.getIndex();
                    tempDeque.clear();
                    i++;
                }
                case "R_Header" -> {
                    String value = token.value();

                    Token modifiedToken = switch
                            (value) {
                        case "include" ->
                                token.updateType("IncludeDirective");
                        case "define" ->
                                token.updateType("MacroDefinition");
                        case "using" ->
                                token.updateType("UsingDirective");
                        default -> token;
                    };

                    int line = modifiedToken.line();
                    ASTNode header = new ASTNode(modifiedToken.type(), modifiedToken.value(), line);
                    int index = i+1;
                    StringBuilder sb = new StringBuilder();
                    Token nextToken = tokens.get(index);
                    while (modifiedToken.line() == nextToken.line()) {
                        sb.append(nextToken.value());
                        index++;
                        nextToken = tokens.get(index);
                    }
                    header.updateValue(sb.toString());
                    astList.add(header);
                    i = index;
                }
                case "ERROR" -> {
                    ASTNode errorNode = new ASTNode("ErrorNode", token.value(), token.line());
                    astList.add(errorNode);
                    i++;
                }
                case "ACCEPT" -> {
                    break;
                }
                default -> throw new SyntaxException("Unexpected parsing action. The value is: " + token.value() + ", " ,token.line());

            }
        }
        ASTNode root = new ASTNode("CompilationUnit", null, 0);
        for (int t = 0; t < astList.size(); t++) {
            root.addChild(astList.get(t));
        }
        return root;
    }



}
