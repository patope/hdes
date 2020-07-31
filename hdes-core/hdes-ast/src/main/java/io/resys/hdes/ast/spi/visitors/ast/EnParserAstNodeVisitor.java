package io.resys.hdes.ast.spi.visitors.ast;

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

import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.immutables.value.Value;

import io.resys.hdes.ast.HdesParser;
import io.resys.hdes.ast.HdesParser.AdditiveExpressionContext;
import io.resys.hdes.ast.HdesParser.AndExpressionContext;
import io.resys.hdes.ast.HdesParser.ConditionalAndExpressionContext;
import io.resys.hdes.ast.HdesParser.ConditionalExpressionContext;
import io.resys.hdes.ast.HdesParser.ConditionalOrExpressionContext;
import io.resys.hdes.ast.HdesParser.EnBodyContext;
import io.resys.hdes.ast.HdesParser.EqualityExpressionContext;
import io.resys.hdes.ast.HdesParser.ExpressionContext;
import io.resys.hdes.ast.HdesParser.LambdaBodyContext;
import io.resys.hdes.ast.HdesParser.LambdaExpressionContext;
import io.resys.hdes.ast.HdesParser.LambdaParametersContext;
import io.resys.hdes.ast.HdesParser.MethodArgsContext;
import io.resys.hdes.ast.HdesParser.MethodInvocationContext;
import io.resys.hdes.ast.HdesParser.MethodNameContext;
import io.resys.hdes.ast.HdesParser.MultiplicativeExpressionContext;
import io.resys.hdes.ast.HdesParser.PostfixExpressionContext;
import io.resys.hdes.ast.HdesParser.PreDecrementExpressionContext;
import io.resys.hdes.ast.HdesParser.PreIncrementExpressionContext;
import io.resys.hdes.ast.HdesParser.PrimaryContext;
import io.resys.hdes.ast.HdesParser.RelationalExpressionContext;
import io.resys.hdes.ast.HdesParser.UnaryExpressionContext;
import io.resys.hdes.ast.HdesParser.UnaryExpressionNotPlusMinusContext;
import io.resys.hdes.ast.HdesParserBaseVisitor;
import io.resys.hdes.ast.api.AstNodeException;
import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
import io.resys.hdes.ast.api.nodes.ExpressionNode;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AdditiveType;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityType;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodInvocation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MultiplicativeType;
import io.resys.hdes.ast.api.nodes.ImmutableAdditiveExpression;
import io.resys.hdes.ast.api.nodes.ImmutableAndExpression;
import io.resys.hdes.ast.api.nodes.ImmutableBetweenExpression;
import io.resys.hdes.ast.api.nodes.ImmutableConditionalExpression;
import io.resys.hdes.ast.api.nodes.ImmutableEqualityOperation;
import io.resys.hdes.ast.api.nodes.ImmutableExpressionBody;
import io.resys.hdes.ast.api.nodes.ImmutableLambdaExpression;
import io.resys.hdes.ast.api.nodes.ImmutableMethodInvocation;
import io.resys.hdes.ast.api.nodes.ImmutableMultiplicativeExpression;
import io.resys.hdes.ast.api.nodes.ImmutableNegateUnary;
import io.resys.hdes.ast.api.nodes.ImmutableNotUnary;
import io.resys.hdes.ast.api.nodes.ImmutableOrExpression;
import io.resys.hdes.ast.api.nodes.ImmutablePositiveUnary;
import io.resys.hdes.ast.api.nodes.ImmutablePostDecrementUnary;
import io.resys.hdes.ast.api.nodes.ImmutablePostIncrementUnary;
import io.resys.hdes.ast.api.nodes.ImmutablePreDecrementUnary;
import io.resys.hdes.ast.api.nodes.ImmutablePreIncrementUnary;
import io.resys.hdes.ast.spi.visitors.ast.util.Nodes;
import io.resys.hdes.ast.spi.visitors.ast.util.Nodes.TokenIdGenerator;

public class EnParserAstNodeVisitor extends HdesParserBaseVisitor<AstNode> {

  protected final TokenIdGenerator tokenIdGenerator;

  // Internal only
  @Value.Immutable
  public interface RedundentMethodName extends ExpressionNode {
    String getValue();
  }

  @Value.Immutable
  public interface RedundentArgs extends ExpressionNode {
    List<AstNode> getValues();
  }
  
  @Value.Immutable
  public interface RedundentLambdaParams extends ExpressionNode {
    List<TypeInvocation> getValues();
  }

  @Value.Immutable
  public interface RedundentLambdaBody extends ExpressionNode {
    AstNode getValue();
  }
  
  public EnParserAstNodeVisitor(TokenIdGenerator tokenIdGenerator) {
    super();
    this.tokenIdGenerator = tokenIdGenerator;
  }

  @Override
  public MethodInvocation visitMethodInvocation(MethodInvocationContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableMethodInvocation.builder()
        .token(token(ctx))
        .name(nodes.of(RedundentMethodName.class).get().getValue())
        .type(nodes.of(TypeInvocation.class))
        .values(nodes.of(RedundentArgs.class).map(a -> a.getValues()).orElse(Collections.emptyList()))
        .build();
  }
  @Override
  public RedundentMethodName visitMethodName(MethodNameContext ctx) {
    return ImmutableRedundentMethodName.builder()
        .token(token(ctx))
        .value(ctx.getText())
        .build();
  }
  @Override
  public RedundentArgs visitMethodArgs(MethodArgsContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableRedundentArgs.builder()
        .token(token(ctx))
        .values(nodes.list(AstNode.class))
        .build();
  }

  @Override
  public LambdaExpression visitLambdaExpression(LambdaExpressionContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableLambdaExpression.builder()
        .token(token(ctx))
        .params(nodes.of(RedundentLambdaParams.class).map(e -> e.getValues()).orElse(Collections.emptyList()))
        .body(nodes.of(RedundentLambdaBody.class).get().getValue())
        .build();
  }
  
  
  @Override
  public RedundentLambdaBody visitLambdaBody(LambdaBodyContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableRedundentLambdaBody.builder()
        .token(token(ctx))
        .value(nodes.of(AstNode.class).get())
        .build();
  }
  
  @Override
  public RedundentLambdaParams visitLambdaParameters(LambdaParametersContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableRedundentLambdaParams.builder()
        .token(token(ctx))
        .values(nodes.list(TypeInvocation.class))
        .build();
  }
  
  @Override
  public AstNode visitPrimary(PrimaryContext ctx) {
    int n = ctx.getChildCount();
    for (int i = 0; i < n; i++) {
      ParseTree c = ctx.getChild(i);
      if (c instanceof TerminalNode) {
        continue;
      }
      return c.accept(this);
    }
    // TODO:: error handling
    throw new AstNodeException("unknown primary node: " + ctx.getText() + "!");
  }

  @Override
  public ExpressionBody visitEnBody(EnBodyContext ctx) {
    return ImmutableExpressionBody.builder()
        .value(first(ctx))
        .token(token(ctx))
        .build();
  }

  @Override
  public AstNode visitExpression(ExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }
    ParseTree c = ctx.getChild(0);
    AstNode childResult = c.accept(this);
    // TODO:: error handling
    throw new AstNodeException("Unknown node: '" + childResult + "', '" + c.getText() + "'");
  }

  @Override
  public AstNode visitConditionalExpression(ConditionalExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }
    // x < 20 ? 5 : 120
    // child[0] = x < 20
    // child[1] = ?
    // child[2] = 5
    // child[3] = :
    // child[4] = 120
    AstNode condition = ctx.getChild(0).accept(this);
    AstNode left = ctx.getChild(2).accept(this);
    AstNode right = ctx.getChild(4).accept(this);
    ParseTree first = ctx.getChild(1);
    if (first instanceof TerminalNode &&
        ((TerminalNode) first).getSymbol().getType() == HdesParser.BETWEEN) {
      return ImmutableBetweenExpression.builder()
          .token(token(ctx))
          .value(condition)
          .left(left)
          .right(right)
          .build();
    }
    return ImmutableConditionalExpression.builder()
        .token(token(ctx))
        .operation((EqualityOperation) condition)
        .left(left)
        .right(right)
        .build();
  }

  @Override
  public AstNode visitConditionalOrExpression(ConditionalOrExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }
    AstNode left = ctx.getChild(0).accept(this);
    AstNode right = ctx.getChild(2).accept(this);
    return ImmutableOrExpression.builder()
        .token(token(ctx))
        .left(left)
        .right(right).build();
  }

  @Override
  public AstNode visitConditionalAndExpression(ConditionalAndExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }
    ParseTree c = ctx.getChild(0);
    AstNode childResult = c.accept(this);
    
    // TODO:: error handling
    throw new AstNodeException("Unknown node: '" + childResult + "', '" + c.getText() + "'");
  }

  @Override
  public AstNode visitAndExpression(AndExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }
    AstNode left = ctx.getChild(0).accept(this);
    AstNode right = ctx.getChild(2).accept(this);
    return ImmutableAndExpression.builder()
        .token(token(ctx))
        .left(left).right(right).build();
  }

  @Override
  public AstNode visitEqualityExpression(EqualityExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }

    AstNode left = ctx.getChild(0).accept(this);
    String v = ctx.getChild(1).getText();
    AstNode right = ctx.getChild(2).accept(this);
    EqualityType type;
    
    if (v.equals(EqualityType.NOTEQUAL.getValue())) {
      type = EqualityType.NOTEQUAL;
    } else {
      type = EqualityType.EQUAL;
    }
    
    return ImmutableEqualityOperation.builder()
        .token(token(ctx))
        .type(type)
        .left(left).right(right).build();
  }

  @Override
  public AstNode visitRelationalExpression(RelationalExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }

    
    AstNode left = ctx.getChild(0).accept(this);
    String v = ctx.getChild(1).getText();
    AstNode right = ctx.getChild(2).accept(this);
    EqualityType type;
    if (v.equals(EqualityType.LESS.getValue())) {
      type = EqualityType.LESS;
    } else if (v.equals(EqualityType.LESS_THEN.getValue())) {
      type = EqualityType.LESS_THEN;
    } else if (v.equals(EqualityType.GREATER.getValue())) {
      type = EqualityType.GREATER;
    } else {
      type = EqualityType.GREATER_THEN;
    }
    return ImmutableEqualityOperation.builder()
        .token(token(ctx))
        .type(type)
        .left(left).right(right).build();
  }

  @Override
  public AstNode visitAdditiveExpression(AdditiveExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }

    AstNode left = ctx.getChild(0).accept(this);
    AstNode right = ctx.getChild(2).accept(this);
    TerminalNode sign = (TerminalNode) ctx.getChild(1);
    return ImmutableAdditiveExpression.builder()
        .token(token(ctx))
        .type(sign.getSymbol().getType() == HdesParser.ADD ? AdditiveType.ADD : AdditiveType.SUBSTRACT)
        .left(left).right(right).build();
  }

  @Override
  public AstNode visitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }

    AstNode left = ctx.getChild(0).accept(this);
    AstNode right = ctx.getChild(2).accept(this);
    TerminalNode sign = (TerminalNode) ctx.getChild(1);
    return ImmutableMultiplicativeExpression.builder()
        .token(token(ctx))
        .type(sign.getSymbol().getType() == HdesParser.MULTIPLY ? MultiplicativeType.MULTIPLY : MultiplicativeType.DIVIDE)
        .left(left).right(right).build();
  }

  @Override
  public AstNode visitUnaryExpression(UnaryExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }
    
    TerminalNode terminalNode = null;
    AstNode childResult = null;
    for (int i = 0; i < n; i++) {
      ParseTree c = ctx.getChild(i);

      if (c instanceof TerminalNode) {
        terminalNode = (TerminalNode) c;
        continue;
      }
      childResult = c.accept(this);
    }
    
    if(terminalNode.getSymbol().getType() == HdesParser.ADD) {
      return ImmutablePositiveUnary.builder()
          .token(token(ctx))
          .value(childResult).build();
    }
    return ImmutableNegateUnary.builder()
        .token(token(ctx))
        .value(childResult).build();
  }

  @Override
  public AstNode visitPreIncrementExpression(PreIncrementExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }
    
    AstNode childResult = null;
    for (int i = 0; i < n; i++) {
      ParseTree c = ctx.getChild(i);
      if (c instanceof TerminalNode) {
        continue;
      }
      childResult = c.accept(this);
    }
    
    return ImmutablePreIncrementUnary.builder()
        .token(token(ctx))
        .value(childResult).build();
  }

  @Override
  public AstNode visitPreDecrementExpression(PreDecrementExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }
    
    AstNode childResult = null;
    for (int i = 0; i < n; i++) {
      ParseTree c = ctx.getChild(i);
      if (c instanceof TerminalNode) {
        continue;
      }
      childResult = c.accept(this);
    }
    
    return ImmutablePreDecrementUnary.builder()
        .token(token(ctx))
        .value(childResult).build();
  }

  @Override
  public AstNode visitUnaryExpressionNotPlusMinus(UnaryExpressionNotPlusMinusContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }

    AstNode childResult = null;
    for (int i = 0; i < n; i++) {
      ParseTree c = ctx.getChild(i);
      if (c instanceof TerminalNode) {
        continue;
      }
      childResult = c.accept(this);
    }
    return ImmutableNotUnary.builder()
        .token(token(ctx))
        .value(childResult).build();
  }

  @Override
  public AstNode visitPostfixExpression(PostfixExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }
    
    TerminalNode terminalNode = null;
    AstNode childResult = null;
    for (int i = 0; i < n; i++) {
      ParseTree c = ctx.getChild(i);
      if (c instanceof TerminalNode) {
        terminalNode = (TerminalNode) c;
        continue;
      }
      childResult = c.accept(this);
    }
    if(terminalNode.getSymbol().getType() == HdesParser.INCREMENT) {
      return ImmutablePostIncrementUnary.builder()
        .token(token(ctx))
        .value(childResult).build();
    } 
    return ImmutablePostDecrementUnary.builder()
        .token(token(ctx))
        .value(childResult).build();
  }
 
  
  protected final AstNode first(ParserRuleContext ctx) {
    ParseTree c = ctx.getChild(0);
    return c.accept(this);
  }

  protected final Nodes nodes(ParserRuleContext node) {
    return Nodes.from(node, this);
  }

  protected final AstNode.Token token(ParserRuleContext node) {
    return Nodes.token(node, tokenIdGenerator);
  }
}
