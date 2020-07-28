package io.resys.hdes.compiler.api;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarType;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.TaskRef;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.ManualTaskBody;

/*-
 * #%L
 * hdes-compiler
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public class HdesCompilerException extends RuntimeException {

  private static final long serialVersionUID = -7831610317362075176L;

  public HdesCompilerException() {
    super();
  }

  public HdesCompilerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public HdesCompilerException(String message, Throwable cause) {
    super(message, cause);
  }

  public HdesCompilerException(String message) {
    super(message);
  }

  public HdesCompilerException(Throwable cause) {
    super(cause);
  }
  
  
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    public String unknownAst(AstNode ast) {
      return new StringBuilder()
          .append("Unknown AST: ").append(ast.getClass())
          .append("  - ").append(ast.getClass()).append(System.lineSeparator())
          .append("  supported types are: ").append(System.lineSeparator())
          .append("    - ").append(DecisionTableBody.class).append(System.lineSeparator())
          .append("    - ").append(FlowBody.class).append(System.lineSeparator())
          .append("    - ").append(ManualTaskBody.class).append(System.lineSeparator())
          .toString();
    }
    public String unknownDTExpressionNode(AstNode ast) {
      return new StringBuilder()
          .append("Unknown DT expression AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String unknownEnExpressionNode(AstNode ast) {
      return new StringBuilder()
          .append("Unknown EN expression AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String unknownDTExpressionOperation(AstNode ast) {
      return new StringBuilder()
          .append("Unknown DT expression operation AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String unknownDTInputRule(AstNode ast) {
      return new StringBuilder()
          .append("Unknown DT input rule AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String unknownFlInputRule(AstNode ast) {
      return new StringBuilder()
          .append("Unknown FLOW input rule AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String unknownEnInputRule(AstNode ast) {
      return new StringBuilder()
          .append("Unknown EPRESSION input rule AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String unknownFlTaskRef(TaskRef ast) {
      return new StringBuilder()
          .append("Unknown FLOW task reference AST: ").append(ast.getValue()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String unknownFlTaskPointer(FlowTaskPointer ast) {
      return new StringBuilder()
          .append("Unknown FLOW task pointer AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String wildcardUnknownFlTaskWhenThen(FlowTaskPointer ast) {
      return new StringBuilder()
          .append("Unknown FLOW when/then(wildcard '?' can be only present as the last element) AST: ").append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String unknownExpressionNode(AstNode ast) {
      return new StringBuilder()
          .append("Unknown EXPRESSION AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String unknownExpressionOperation(AstNode ast) {
      return new StringBuilder()
          .append("Unknown EXPRESSION operation AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String unknownExpressionParameter(AstNode ast) {
      return new StringBuilder()
          .append("Unknown EXPRESSION parameter AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String unknownLiteral(AstNode ast) {
      return new StringBuilder()
          .append("Unknown LITERAL expression AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String incompatibleType(AstNode ast, ScalarType expected, ScalarType was) {
      return new StringBuilder()
          .append("Incompatible type used in expression!").append(System.lineSeparator())
          .append("Expected type: ").append(expected).append(" but was: ").append(was).append("!").append(System.lineSeparator())
          .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    
    public String incompatibleType(AstNode ast, ScalarType[] expected, ScalarType was) {
      return new StringBuilder()
          .append("Incompatible type used in expression!").append(System.lineSeparator())
          .append("Expected type one of: ").append(expected).append(" but was: ").append(was).append("!").append(System.lineSeparator())
          .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }

    public String incompatibleReturnType(AstNode ast, ScalarType was1, ScalarType was2) {
      return new StringBuilder()
          .append("Incompatible type used in expression!").append(System.lineSeparator())
          .append("Expected same type for both expressions but was: ").append(was1).append(was2).append("!").append(System.lineSeparator())
          .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    
    public String incompatibleConversion(AstNode ast, ScalarType ...was) {
      return new StringBuilder()
          .append("Incompatible type used in expression!").append(System.lineSeparator())
          .append("Operation is incompatible between these types: ").append(was).append("!").append(System.lineSeparator())
          .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    
    public String incompatibleScalarType(AstNode ast, TypeDefNode was) {
      return new StringBuilder()
          .append("Incompatible type used in expression!").append(System.lineSeparator())
          .append("Expected type on of: ").append(ScalarType.values()).append(" but was: ").append(was).append("!").append(System.lineSeparator())
          .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String betweenOperationNotSupportedForType(AstNode ast, ScalarType was) {
      return new StringBuilder()
          .append("Incompatible type used in BETWEEN expression!").append(System.lineSeparator())
          .append("Expected type on of: ").append(new ScalarType[] {ScalarType.DATE_TIME, ScalarType.DATE, ScalarType.TIME, ScalarType.INTEGER, ScalarType.DECIMAL})
          .append(" but was: ").append(was).append("!").append(System.lineSeparator())
          .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    
    public String incompatibleTypesInAdditiveOperation(AstNode ast, ScalarType ... was) {
      return new StringBuilder()
          .append("Incompatible type used in ADDITIVE expression!").append(System.lineSeparator())
          .append("Expected types must be of types: ")
            .append(ScalarType.INTEGER).append(" or ")
            .append(ScalarType.DECIMAL)
            .append(" but where: ").append(was).append("!").append(System.lineSeparator())
          .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    
    public String incompatibleTypesInPlusUnaryOperation(AstNode ast, ScalarType was) {
      return new StringBuilder()
          .append("Incompatible type used in PLUS UNARY expression!").append(System.lineSeparator())
          .append("Expected types must be of types: ")
            .append(ScalarType.INTEGER).append(" or ")
            .append(ScalarType.DECIMAL)
            .append(" but was: ").append(was).append("!").append(System.lineSeparator())
          .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    
    public String incompatibleTypesInNegateUnaryOperation(AstNode ast, ScalarType was) {
      return new StringBuilder()
          .append("Incompatible type used in MINUS UNARY expression!").append(System.lineSeparator())
          .append("Expected types must be of types: ")
            .append(ScalarType.INTEGER).append(" or ")
            .append(ScalarType.DECIMAL)
            .append(" but was: ").append(was).append("!").append(System.lineSeparator())
          .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String incompatibleTypesInEqualityOperation(EqualityOperation ast) {
      return new StringBuilder()
          .append("Incompatible type used in EQUALITY expression!").append(System.lineSeparator())
          .append("Equality operation: ").append(ast.getType()).append(" can't be performed!").append(System.lineSeparator())
          .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    
    public String unknownGlobalFunctionCall(AstNode ast, String was, String ... known) {
      return new StringBuilder()
          .append("Unknown function in expression!").append(System.lineSeparator())
          .append("Known functions: ").append(known).append(", but was: ").append(was).append(" !").append(System.lineSeparator())
          .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    
    
    public String unknownFunctionCall(AstNode ast, String was) {
      return new StringBuilder()
          .append("Unknown function in expression!").append(System.lineSeparator())
          .append("Function: ").append(was).append(" !").append(System.lineSeparator())
          .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    
    public String dtHeaderOutputMatrixCantBeRequired(TypeDefNode header) {
      return new StringBuilder()
          .append("Decision table with hit policy matrix can't have REQUIRED output!").append(System.lineSeparator())
          .append("Header name: ").append(header.getName()).append(" !").append(System.lineSeparator())
          .append(" AST: ").append(header.getClass()).append(System.lineSeparator())
          .append("  - ").append(header).append("!")
          .toString();
    }
    
    public String dtHeaderOutputMatrixHasToHaveFormula(TypeDefNode header) {
      return new StringBuilder()
          .append("Decision table with hit policy matrix can't have output without FORMULA!").append(System.lineSeparator())
          .append("Header name: ").append(header.getName()).append(" !").append(System.lineSeparator())
          .append(" AST: ").append(header.getClass()).append(System.lineSeparator())
          .append("  - ").append(header).append("!")
          .toString();
    }
  }
}
