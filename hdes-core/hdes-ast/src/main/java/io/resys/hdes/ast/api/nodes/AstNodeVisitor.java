package io.resys.hdes.ast.api.nodes;

import java.util.List;

import io.resys.hdes.ast.api.nodes.AstNode.Headers;
import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeName;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.ExpressionValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HeaderRefValue;
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
import io.resys.hdes.ast.api.nodes.ExpressionNode.AdditiveOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AndOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.BetweenExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ConditionalExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodRefNode;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MultiplicativeOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NegateUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NotUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.OrOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PositiveUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PostDecrementUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PostIncrementUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PreDecrementUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PreIncrementUnaryOperation;
import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowLoop;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
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
  @FunctionalInterface
  interface TypeNameRef {
    String get(TypeName node);
  }
  
  // basic
  T visitTypeName(TypeName node);
  T visitLiteral(Literal node);

  /* conversions
  T visitDateConversion(DateConversion node);
  T visitDateTimeConversion(DateTimeConversion node);
  T visitTimeConversion(TimeConversion node);
  T visitDecimalConversion(DecimalConversion node);
  */
  
  interface TypeDefVisitor<T, R> extends AstNodeVisitor<T, R> {
    T visitObjectDef(ObjectTypeDefNode node);
    T visitScalarDef(ScalarTypeDefNode node);
  }
  
  // expression
  interface ExpressionAstNodeVisitor<T, R> extends AstNodeVisitor<T, R> { 
    R visitExpressionBody(ExpressionBody node);
    T visitNotUnaryOperation(NotUnaryOperation node);
    T visitNegateUnaryOperation(NegateUnaryOperation node);
    T visitPositiveUnaryOperation(PositiveUnaryOperation node);
    T visitPreIncrementUnaryOperation(PreIncrementUnaryOperation node);
    T visitPreDecrementUnaryOperation(PreDecrementUnaryOperation node);
    T visitPostIncrementUnaryOperation(PostIncrementUnaryOperation node);
    T visitPostDecrementUnaryOperation(PostDecrementUnaryOperation node);
    T visitMethodRefNode(MethodRefNode node);
    T visitEqualityOperation(EqualityOperation node);
    T visitAndOperation(AndOperation node);
    T visitOrOperation(OrOperation node);
    T visitConditionalExpression(ConditionalExpression node);
    T visitBetweenExpression(BetweenExpression node);
    T visitAdditiveOperation(AdditiveOperation node);
    T visitMultiplicativeOperation(MultiplicativeOperation node);
    T visitLambdaExpression(LambdaExpression node);
  }
  
  // dt
  interface DtAstNodeVisitor<T, R> extends TypeDefVisitor<T, R> {
    R visitDecisionTableBody(DecisionTableBody node);
    T visitHeaders(Headers node);
    T visitHeader(TypeDefNode node);
    T visitHitPolicyAll(HitPolicyAll node);
    T visitHitPolicyMatrix(HitPolicyMatrix node);
    T visitHitPolicyFirst(HitPolicyFirst node);
    T visitRuleRow(RuleRow node);
    T visitRule(Rule node);
    T visitMatrixRow(MatrixRow node);
    T visitFormula(Headers node);
    
    T visitUndefinedValue(UndefinedValue node);
    T visitLiteralValue(LiteralValue node);
    T visitNegateLiteralValue(NegateLiteralValue node);
    
    T visitExpressionValue(ExpressionValue node);
    T visitEqualityOperation(EqualityOperation node);
    T visitBetweenExpression(BetweenExpression node);
    T visitAndOperation(AndOperation node);
    T visitOrOperation(OrOperation node);
    T visitInOperation(InOperation node);
    T visitNotOperation(NotUnaryOperation node);
    T visitHeaderRefValue(HeaderRefValue node);
  }
  
  // flow
  interface FlowAstNodeVisitor<T, R> extends TypeDefVisitor<T, R> {
    R visitBody(FlowBody node);
    T visitInputs(List<TypeDefNode> node);
    T visitOutputs(List<TypeDefNode> node);
    T visitTask(FlowTaskNode node);
    
    T visitTaskPointer(FlowTaskNode parent, FlowTaskPointer node);
    T visitWhenThenPointer(FlowTaskNode parent, WhenThenPointer node);
    T visitThenPointer(FlowTaskNode parent, ThenPointer node);
    T visitEndPointer(FlowTaskNode parent, EndPointer node);
    
    T visitLoop(FlowLoop node);
    T visitWhenThen(WhenThen node);
    T visitWhen(ExpressionBody node);
    T visitMapping(FlowTaskNode node);
    T visitMappingValue(MappingValue node);
    T visitTaskRef(FlowTaskNode node);
  }
  
  // mt
  interface MtAstNodeVisitor<T, R> extends TypeDefVisitor<T, R> {
    R visitManualTaskBody(ManualTaskBody node);
    T visitManualTaskInputs(List<TypeDefNode> node);
    T visitManualTaskDropdowns(ManualTaskDropdowns node);
    T visitManualTaskStatements(ManualTaskActions node);
    T visitManualTaskForm(ManualTaskForm node);
    T visitDropdown(Dropdown node);
    T visitStatement(ManualTaskAction node);
    T visitWhenStatement(WhenAction node);
    T visitThenStatement(ThenAction node);
    T visitGroup(Group node);
    T visitGroups(Groups node);
    T visitFields(Fields node);
    T visitFormField(FormField node);
    T visitDropdownField(DropdownField node);
    T visitLiteralField(LiteralField node);
  }
}
