package io.resys.hdes.compiler.spi.java.visitors.fl;

import java.util.List;

import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeName;
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
  public T visitTypeName(TypeName node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitLiteral(Literal node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitObjectDef(ObjectTypeDefNode node) {
    throw new IllegalArgumentException("Not implemented");
  }
  
  @Override
  public T visitScalarDef(ScalarTypeDefNode node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitBody(FlowBody node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitTask(FlowTaskNode node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitWhenThenPointer(FlowTaskNode parent, WhenThenPointer node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitThenPointer(FlowTaskNode parent, ThenPointer node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitWhenThen(WhenThen node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitWhen(ExpressionBody node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitMapping(FlowTaskNode node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitTaskRef(FlowTaskNode node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitTaskPointer(FlowTaskNode parent, FlowTaskPointer node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitEndPointer(FlowTaskNode parent, EndPointer node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitMappingValue(MappingValue node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitLoop(FlowLoop node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitInputs(List<TypeDefNode> node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitOutputs(List<TypeDefNode> node) {
    throw new IllegalArgumentException("Not implemented");
  }
}
