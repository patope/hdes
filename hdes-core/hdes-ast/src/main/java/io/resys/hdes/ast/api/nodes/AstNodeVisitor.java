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

import io.resys.hdes.ast.api.nodes.AstNode.Headers;
import io.resys.hdes.ast.api.nodes.AstNode.Invocation;
import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.ExpressionValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HeaderIndex;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyMatrix;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.InOperation;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.LiteralValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.MatrixRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.NegateLiteralValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.Rule;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.UndefinedValue;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AdditiveExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AndExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.BetweenExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ConditionalExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodInvocation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MultiplicativeExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NegateUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NotUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.OrExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PositiveUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PostDecrementUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PostIncrementUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PreDecrementUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PreIncrementUnary;
import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.LoopPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.MappingValue;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.Dropdown;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.DropdownField;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.Fields;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.FormField;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.Group;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.Groups;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.LiteralField;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.ManualTaskAction;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.ManualTaskActions;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.ManualTaskBody;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.ManualTaskDropdowns;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.ManualTaskForm;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.ThenAction;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.WhenAction;

public interface AstNodeVisitor<T, R> {
  // basic
  T visitInvocation(Invocation node, AstNodeVisitorContext ctx);
  T visitLiteral(Literal node, AstNodeVisitorContext ctx);

  interface TypeDefVisitor<T, R> extends AstNodeVisitor<T, R> {
    T visitObjectDef(ObjectDef node, AstNodeVisitorContext ctx);
    T visitScalarDef(ScalarDef node, AstNodeVisitorContext ctx);
  }
  
  @Value.Immutable
  public interface AstNodeVisitorContext {
    Optional<AstNodeVisitorContext> getParent();
    AstNode getValue();
  }
  
  // expression
  interface ExpressionAstNodeVisitor<T, R> extends AstNodeVisitor<T, R> { 
    R visitBody(ExpressionBody node, AstNodeVisitorContext ctx);
    T visitNot(NotUnary node, AstNodeVisitorContext ctx);
    T visitNegate(NegateUnary node, AstNodeVisitorContext ctx);
    T visitPositive(PositiveUnary node, AstNodeVisitorContext ctx);
    T visitPreIncrement(PreIncrementUnary node, AstNodeVisitorContext ctx);
    T visitPreDecrement(PreDecrementUnary node, AstNodeVisitorContext ctx);
    T visitPostIncrement(PostIncrementUnary node, AstNodeVisitorContext ctx);
    T visitPostDecrement(PostDecrementUnary node, AstNodeVisitorContext ctx);
    T visitMethod(MethodInvocation node, AstNodeVisitorContext ctx);
    T visitEquality(EqualityOperation node, AstNodeVisitorContext ctx);
    T visitAnd(AndExpression node, AstNodeVisitorContext ctx);
    T visitOr(OrExpression node, AstNodeVisitorContext ctx);
    T visitConditional(ConditionalExpression node, AstNodeVisitorContext ctx);
    T visitBetween(BetweenExpression node, AstNodeVisitorContext ctx);
    T visitAdditive(AdditiveExpression node, AstNodeVisitorContext ctx);
    T visitMultiplicative(MultiplicativeExpression node, AstNodeVisitorContext ctx);
    T visitLambda(LambdaExpression node, AstNodeVisitorContext ctx);
  }
  
  // dt
  interface DtAstNodeVisitor<T, R> extends TypeDefVisitor<T, R> {
    R visitBody(DecisionTableBody node, AstNodeVisitorContext ctx);
    T visitHeaders(Headers node, AstNodeVisitorContext ctx);
    T visitHeader(TypeDef node, AstNodeVisitorContext ctx);
    T visitHitPolicyAll(HitPolicyAll node, AstNodeVisitorContext ctx);
    T visitHitPolicyMatrix(HitPolicyMatrix node, AstNodeVisitorContext ctx);
    T visitHitPolicyFirst(HitPolicyFirst node, AstNodeVisitorContext ctx);
    T visitRuleRow(RuleRow node, AstNodeVisitorContext ctx);
    T visitRule(Rule node, AstNodeVisitorContext ctx);
    T visitMatrixRow(MatrixRow node, AstNodeVisitorContext ctx);
    T visitFormula(Headers node, AstNodeVisitorContext ctx);
    
    T visitUndefinedValue(UndefinedValue node, AstNodeVisitorContext ctx);
    T visitLiteralValue(LiteralValue node, AstNodeVisitorContext ctx);
    T visitNegateLiteralValue(NegateLiteralValue node, AstNodeVisitorContext ctx);
    
    T visitExpressionValue(ExpressionValue node, AstNodeVisitorContext ctx);
    T visitEquality(EqualityOperation node, AstNodeVisitorContext ctx);
    T visitBetween(BetweenExpression node, AstNodeVisitorContext ctx);
    T visitAnd(AndExpression node, AstNodeVisitorContext ctx);
    T visitOr(OrExpression node, AstNodeVisitorContext ctx);
    T visitIn(InOperation node, AstNodeVisitorContext ctx);
    T visitNot(NotUnary node, AstNodeVisitorContext ctx);
    T visitHeaderIndex(HeaderIndex node, AstNodeVisitorContext ctx);
  }
  
  // flow
  interface FlowAstNodeVisitor<T, R> extends TypeDefVisitor<T, R> {
    R visitBody(FlowBody node, AstNodeVisitorContext ctx);
    T visitInputs(List<TypeDef> node, AstNodeVisitorContext ctx);
    T visitOutputs(List<TypeDef> node, AstNodeVisitorContext ctx);
    T visitTask(FlowTaskNode node, AstNodeVisitorContext ctx);
    
    T visitTaskPointer(FlowTaskNode parent, FlowTaskPointer node, AstNodeVisitorContext ctx);
    T visitWhenThenPointer(FlowTaskNode parent, WhenThenPointer node, AstNodeVisitorContext ctx);
    T visitThenPointer(FlowTaskNode parent, ThenPointer node, AstNodeVisitorContext ctx);
    T visitEndPointer(FlowTaskNode parent, EndPointer node, AstNodeVisitorContext ctx);
    
    T visitLoop(LoopPointer node, AstNodeVisitorContext ctx);
    T visitWhenThen(WhenThen node, AstNodeVisitorContext ctx);
    T visitWhen(ExpressionBody node, AstNodeVisitorContext ctx);
    T visitMapping(FlowTaskNode node, AstNodeVisitorContext ctx);
    T visitMappingValue(MappingValue node, AstNodeVisitorContext ctx);
    T visitTaskRef(FlowTaskNode node, AstNodeVisitorContext ctx);
  }
  
  // mt
  interface MtAstNodeVisitor<T, R> extends TypeDefVisitor<T, R> {
    R visitManualTaskBody(ManualTaskBody node, AstNodeVisitorContext ctx);
    T visitManualTaskInputs(List<TypeDef> node, AstNodeVisitorContext ctx);
    T visitManualTaskDropdowns(ManualTaskDropdowns node, AstNodeVisitorContext ctx);
    T visitManualTaskStatements(ManualTaskActions node, AstNodeVisitorContext ctx);
    T visitManualTaskForm(ManualTaskForm node, AstNodeVisitorContext ctx);
    T visitDropdown(Dropdown node, AstNodeVisitorContext ctx);
    T visitStatement(ManualTaskAction node, AstNodeVisitorContext ctx);
    T visitWhenStatement(WhenAction node, AstNodeVisitorContext ctx);
    T visitThenStatement(ThenAction node, AstNodeVisitorContext ctx);
    T visitGroup(Group node, AstNodeVisitorContext ctx);
    T visitGroups(Groups node, AstNodeVisitorContext ctx);
    T visitFields(Fields node, AstNodeVisitorContext ctx);
    T visitFormField(FormField node, AstNodeVisitorContext ctx);
    T visitDropdownField(DropdownField node, AstNodeVisitorContext ctx);
    T visitLiteralField(LiteralField node, AstNodeVisitorContext ctx);
  }
}
