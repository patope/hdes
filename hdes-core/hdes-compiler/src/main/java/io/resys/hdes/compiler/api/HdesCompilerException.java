package io.resys.hdes.compiler.api;

import java.util.List;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarType;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.MatrixRow;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodInvocation;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
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
    public String unknownHeader(AstNode ast) {
      return new StringBuilder()
          .append("Unknown header AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String unknownEnInputRule(AstNode ast) {
      return new StringBuilder()
          .append("Unknown EPRESSION input rule AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("  - ").append(ast).append("!")
          .toString();
    }
    public String unknownFlTaskRef(FlowBody flow, TaskRef ast) {
      return new StringBuilder()
          .append("Unknown FLOW task reference").append(System.lineSeparator())
            .append("  Flow: ")
            .append("\"").append(flow.getId().getValue()).append("\"")
            .append(" task ref: ")
            .append("\"").append(ast.getValue()).append("\"")
            .append(" does not exist!").append(System.lineSeparator())
          .append("  AST: ").append(ast.getValue()).append(System.lineSeparator())
          .append("    - ").append(ast).append("!")
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
      String value;
      if(ast instanceof TypeInvocation) {
        value = ((TypeInvocation) ast).getValue();
      } else if(ast instanceof Literal) {
        value = ((Literal) ast).getValue();
      } else {
        value = "";
      }
      return new StringBuilder()
          .append("Unknown parameter in EXPRESSION!").append(System.lineSeparator())
          .append(" parameter: ").append(value).append(System.lineSeparator())
          .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
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
    

    public String incompatibleConditionalReturnType(AstNode ast, AstNode was1, AstNode was2) {
      return new StringBuilder()
          .append("Incompatible type used in expression!").append(System.lineSeparator())
          .append("Expected same type for both expressions but was: ")
          .append(" value 1 - ").append(was1).append(System.lineSeparator())
          .append(" value 1 - ").append(was2).append(System.lineSeparator())
          .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
          .append("   - ").append(ast).append("!")
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
    
    public String incompatibleScalarType(AstNode ast, TypeDef was) {
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
    
    public String dtHeaderOutputMatrixCantBeRequired(TypeDef header) {
      return new StringBuilder()
          .append("Decision table with hit policy matrix can't have REQUIRED output!").append(System.lineSeparator())
          .append("Header name: ").append(header.getName()).append(" !").append(System.lineSeparator())
          .append(" AST: ").append(header.getClass()).append(System.lineSeparator())
          .append("  - ").append(header).append("!")
          .toString();
    }
    
    public String dtHeaderOutputMatrixHasToHaveFormula(TypeDef header) {
      return new StringBuilder()
          .append("Decision table with hit policy matrix can't have output without FORMULA!").append(System.lineSeparator())
          .append("Header name: ").append(header.getName()).append(" !").append(System.lineSeparator())
          .append(" AST: ").append(header.getClass()).append(System.lineSeparator())
          .append("  - ").append(header).append("!")
          .toString();
    }
    
    public String dtFormulaContainsIncorectScopeParameters(TypeDef header, List<String> unusables) {
      return new StringBuilder()
          .append("Decision table FORMULA contains parameters that can't be used!").append(System.lineSeparator())
          .append("Header name: ").append(header.getName()).append(" !").append(System.lineSeparator())
          .append("Unusable parameters: ").append(unusables).append(" !").append(System.lineSeparator())
          .append(" AST: ").append(header.getClass()).append(System.lineSeparator())
          .append("  - ").append(header).append("!")
          .toString();
    }
        
    public String dtFormulaContainsIncorectArrayType(TypeDef header, boolean was) {
      return new StringBuilder()
          .append("Decision table FORMULA array type is incorrect!").append(System.lineSeparator())
          .append("Header name: ").append(header.getName()).append(" !").append(System.lineSeparator())
          .append("Expecting formula to be array: ").append(header.getArray()).append(" but was: ").append(was).append("!").append(System.lineSeparator())
          .append(" AST: ").append(header.getClass()).append(System.lineSeparator())
          .append("  - ").append(header).append("!")
          .toString();
    }
    
    public String dtFormulaContainsIncorectScalarTypes(ScalarDef header, ScalarType was) {
      return new StringBuilder()
          .append("Decision table FORMULA declaration and formula evaluation types do not match!").append(System.lineSeparator())
          .append("Header name: ").append(header.getName()).append(" !").append(System.lineSeparator())
          .append("Declared type: ").append(header.getType()).append(" but was: ").append(was).append("!").append(System.lineSeparator())
          .append(" AST: ").append(header.getClass()).append(System.lineSeparator())
          .append("  - ").append(header).append("!")
          .toString();
    }
    
    public String dtMissingHeaderForMatrixRow(MatrixRow row) {
      return new StringBuilder()
          .append("Incorrect header name defined on matrix row!").append(System.lineSeparator())
          .append("Row name: ").append(row.getTypeName().getValue()).append("!").append(System.lineSeparator())
          .append(" AST: ").append(row.getClass()).append(System.lineSeparator())
          .append("  - ").append(row).append("!")
          .toString();
    }
    
    public String incorrectLambdaFormula(MethodInvocation invocation) {
      return new StringBuilder()
          .append("Incorrect lambda formula declaration!").append(System.lineSeparator())
          .append("Lambda name: ").append(invocation.getValue()).append("!").append(System.lineSeparator())
          .append(" AST: ").append(invocation.getClass()).append(System.lineSeparator())
          .append("  - ").append(invocation).append("!")
          .toString();
    }
    
    public String unknownSwitchThen(FlowTaskNode node) {
      return new StringBuilder()
          .append("Unknown then declaration used in task switch!").append(System.lineSeparator())
          .append("Task name: ").append(node.getId()).append("!").append(System.lineSeparator())
          .append(" AST: ").append(node.getClass()).append(System.lineSeparator())
          .append("  - ").append(node).append("!")
          .toString();
    }
  }
}
