package io.resys.hdes.compiler.spi.java.visitors.fl;

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
