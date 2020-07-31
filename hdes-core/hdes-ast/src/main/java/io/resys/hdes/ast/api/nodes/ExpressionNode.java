package io.resys.hdes.ast.api.nodes;

/*-
 * #%L
 * hdes-ast
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

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

public interface ExpressionNode extends AstNode {
  
  enum AdditiveType { ADD, SUBSTRACT }
  enum MultiplicativeType { DIVIDE, MULTIPLY }
  enum EqualityType { 
    NOTEQUAL("!="), EQUAL("="), 
    LESS("<"), LESS_THEN("<="), 
    GREATER(">"), GREATER_THEN(">=");
    
    private final String value;
    
    EqualityType(String value) {
      this.value = value;
    }
    public String getValue() {
      return value;
    }
  }
  

  @Value.Immutable
  interface ExpressionBody extends ExpressionNode {
    AstNode getValue();
  }
  
  @Value.Immutable
  interface LambdaExpression extends ExpressionNode {
    List<TypeInvocation> getParams();
    AstNode getBody();
  }

  /*
   * Unary operation
   */
  interface Unary extends ExpressionNode {
    AstNode getValue();
  }
  @Value.Immutable
  interface NotUnary extends Unary { }

  @Value.Immutable
  interface NegateUnary extends Unary { }
  
  @Value.Immutable
  interface PositiveUnary extends Unary { }
  
  @Value.Immutable
  interface PreIncrementUnary extends Unary { }
  
  @Value.Immutable
  interface PreDecrementUnary extends Unary { }

  @Value.Immutable
  interface PostIncrementUnary extends Unary { }
  
  @Value.Immutable
  interface PostDecrementUnary extends Unary { }
  
  /*
   * Ref nodes
   */
  @Value.Immutable
  interface MethodInvocation extends Invociation, ExpressionNode {
    Optional<TypeInvocation> getType();
    String getName();
    List<AstNode> getValues();
  }

  /*
   * Conditions and expressions
   */
  @Value.Immutable
  interface EqualityOperation extends ExpressionNode {
    EqualityType getType();
    AstNode getLeft();
    AstNode getRight();
  }

  // operation ? val1 : val2 
  @Value.Immutable
  interface ConditionalExpression extends ExpressionNode {
    EqualityOperation getOperation();
    AstNode getLeft();
    AstNode getRight();
  }

  @Value.Immutable
  interface BetweenExpression extends ExpressionNode {
    AstNode getValue();
    AstNode getLeft();
    AstNode getRight();
  }

  @Value.Immutable
  interface AndExpression extends ExpressionNode {
    AstNode getLeft();
    AstNode getRight();
  }

  @Value.Immutable
  interface OrExpression extends ExpressionNode {
    AstNode getLeft();
    AstNode getRight();
  }

  @Value.Immutable
  interface AdditiveExpression extends ExpressionNode {
    AdditiveType getType();
    AstNode getLeft();
    AstNode getRight();
  }

  @Value.Immutable
  interface MultiplicativeExpression extends ExpressionNode {
    MultiplicativeType getType();
    AstNode getLeft();
    AstNode getRight();
  }
}
