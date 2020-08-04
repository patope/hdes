package io.resys.hdes.compiler.spi.java.visitors.fl;

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

import java.util.List;

import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.FlowAstNodeVisitor;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowLoop;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.MappingValue;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;

public class FlTemplateVisitor<T, R> implements FlowAstNodeVisitor<T, R> {
  @Override
  public T visitTypeInvocation(TypeInvocation node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitLiteral(Literal node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitObjectDef(ObjectDef node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }
  
  @Override
  public T visitScalarDef(ScalarDef node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitBody(FlowBody node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitTask(FlowTaskNode node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitWhenThenPointer(FlowTaskNode parent, WhenThenPointer node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitThenPointer(FlowTaskNode parent, ThenPointer node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitWhenThen(WhenThen node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitWhen(ExpressionBody node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitMapping(FlowTaskNode node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitTaskRef(FlowTaskNode node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitTaskPointer(FlowTaskNode parent, FlowTaskPointer node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitEndPointer(FlowTaskNode parent, EndPointer node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitMappingValue(MappingValue node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitLoop(FlowLoop node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitInputs(List<TypeDef> node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitOutputs(List<TypeDef> node, AstNodeVisitorContext ctx) {
    throw new IllegalArgumentException("Not implemented");
  }
}
