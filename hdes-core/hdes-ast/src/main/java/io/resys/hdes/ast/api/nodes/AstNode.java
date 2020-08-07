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

import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;

public interface AstNode {
  Token getToken();

  enum DirectionType { IN, OUT }
  enum TypeNameScope { VAR, STATIC, INSTANCE }
  enum ScalarType {
    STRING, INTEGER, BOOLEAN, DECIMAL,
    DATE, DATE_TIME, TIME,
  }
  
  interface Invocation extends AstNode { }
  
  interface Body extends AstNode {
    BodyId getId();
    Optional<String> getDescription();
    Headers getHeaders();
  }
  
  interface TypeDef extends AstNode {
    Boolean getRequired();
    DirectionType getDirection();
    String getName();
    Boolean getArray();
  }
  
  @Value.Immutable
  interface EmptyNode extends AstNode { }
  
  @Value.Immutable
  interface ErrorNode {
    String getBodyId();
    AstNode getTarget();
    String getMessage();
  }
  
  @Value.Immutable
  interface Token {
    int getId();
    String getText();
    
    int getStartLine();
    int getStartCol();
    
    int getEndLine();
    int getEndCol();
  }

  @Value.Immutable
  interface EmptyBody extends Body { }
  
  
  @Value.Immutable
  interface BodyId extends AstNode {
    String getValue();
  }
  
  @Value.Immutable
  interface TypeInvocation extends Invocation {
    String getValue();
    TypeNameScope getScope();
  }
  
  @Value.Immutable
  interface Literal extends AstNode {
    ScalarType getType();
    String getValue();
  }
  
  @Value.Immutable
  interface Headers extends AstNode {
    List<TypeDef> getValues();
  }
  
  @Value.Immutable
  interface ObjectDef extends TypeDef {
    List<TypeDef> getValues();
  }

  @Value.Immutable
  interface ScalarDef extends TypeDef {
    Optional<String> getDebugValue();
    Optional<ExpressionBody> getFormula();
    ScalarType getType();
  }
  
  @Value.Immutable
  interface CompilationUnit extends AstNode {
    List<FlowNode> getFlows();
    List<ManualTaskNode> getManualTasks();
    List<DecisionTableNode> getDecisionTables();
    List<AstNode> getServices();
  }
}
