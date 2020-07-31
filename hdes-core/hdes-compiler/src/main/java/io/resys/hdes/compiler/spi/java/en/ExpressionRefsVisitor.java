package io.resys.hdes.compiler.spi.java.en;

import java.util.List;
import java.util.stream.Collectors;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
import io.resys.hdes.ast.api.nodes.AstNode.TypeNameScope;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.ExpressionAstNodeVisitor;
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
import io.resys.hdes.ast.api.nodes.ImmutableAstNodeVisitorContext;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.EnReferedScope;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.EnReferedType;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.EnReferedTypes;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.InvocationResolver;

public class ExpressionRefsVisitor implements ExpressionAstNodeVisitor<EnReferedTypes, EnReferedTypes> {
  
  private final InvocationResolver resolver;
  
  public ExpressionRefsVisitor(InvocationResolver resolver) {
    super();
    this.resolver = resolver;
  }

  
  @Override
  public EnReferedTypes visitBody(ExpressionBody node, AstNodeVisitorContext ctx) {
    ImmutableEnReferedTypes.Builder result = ImmutableEnReferedTypes.builder();
    for(EnReferedType ref : visit(node.getValue(), ctx).getValues()) {
      result.addScopes(ref.getScope());
      result.addValues(ref);
    }
    return result.build();
  }
  
  @Override
  public EnReferedTypes visitTypeInvocation(TypeInvocation node, AstNodeVisitorContext ctx) {
    final TypeDef typeDef = resolver.accept(node);
    final EnReferedScope scope;
    
    if(node.getScope() == TypeNameScope.STATIC) {
      scope = EnReferedScope.STATIC;
    } else if(node.getScope() == TypeNameScope.INSTANCE) {
      scope = EnReferedScope.INSTANCE;
    } else if(typeDef.getDirection() == DirectionType.IN) {
      scope = EnReferedScope.IN;
    } else if(typeDef.getDirection() == DirectionType.OUT) {
      scope = EnReferedScope.OUT;
    } else {
      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(node));
    }
    
    return ImmutableEnReferedTypes.builder()
        .addValues(ImmutableEnReferedType.builder()
            .typeName(node)
            .node(typeDef)
            .scope(scope)
            .build())
        .build();
  }
  
  @Override
  public EnReferedTypes visitMethod(MethodInvocation node, AstNodeVisitorContext ctx) {
    final TypeDef typeDef = resolver.accept(node);
    ImmutableEnReferedTypes.Builder builder = ImmutableEnReferedTypes.builder().addValues(
        ImmutableEnReferedType.builder()
        .typeName(node.getType())
        .methodRef(node)
        .node(typeDef)
        .scope(EnReferedScope.METHOD)
        .build());
    
    if(ExpressionVisitor.LAMBDA_METHODS.contains(node.getName())) {
      
    }
    
    for(AstNode value : node.getValues()) {
      if(value instanceof LambdaExpression) {
        
      }
    }
      
    return builder.build();
  }

  @Override
  public EnReferedTypes visitLambda(LambdaExpression node, AstNodeVisitorContext ctx) {
    List<String> lambdaParams = node.getParams().stream()
        .map(t -> t.getValue())
        .collect(Collectors.toList());
    
    List<AstNode> values = visit(node.getBody(), ctx).getValues().stream()
      .filter(t -> t instanceof TypeInvocation)
      .map(t -> (TypeInvocation) t)
      .filter(t -> !lambdaParams.contains(t.getValue()))
      .collect(Collectors.toList());

    return null;
  }
  
  @Override
  public EnReferedTypes visitLiteral(Literal node, AstNodeVisitorContext ctx) {
    return ImmutableEnReferedTypes.builder().build();
  }

  @Override
  public EnReferedTypes visitNot(NotUnary node, AstNodeVisitorContext ctx) {
    return visit(node.getValue(), ctx);
  }

  @Override
  public EnReferedTypes visitNegate(NegateUnary node, AstNodeVisitorContext ctx) {
    return visit(node.getValue(), ctx);
  }

  @Override
  public EnReferedTypes visitPositive(PositiveUnary node, AstNodeVisitorContext ctx) {
    return visit(node.getValue(), ctx);
  }

  @Override
  public EnReferedTypes visitPreIncrement(PreIncrementUnary node, AstNodeVisitorContext ctx) {
    return visit(node.getValue(), ctx);
  }

  @Override
  public EnReferedTypes visitPreDecrement(PreDecrementUnary node, AstNodeVisitorContext ctx) {
    return visit(node.getValue(), ctx);
  }

  @Override
  public EnReferedTypes visitPostIncrement(PostIncrementUnary node, AstNodeVisitorContext ctx) {
    return visit(node.getValue(), ctx);
  }

  @Override
  public EnReferedTypes visitPostDecrement(PostDecrementUnary node, AstNodeVisitorContext ctx) {
    return visit(node.getValue(), ctx);
  }

  @Override
  public EnReferedTypes visitEquality(EqualityOperation node, AstNodeVisitorContext ctx) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft(), ctx).getValues())
        .addAllValues(visit(node.getRight(), ctx).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitAnd(AndExpression node, AstNodeVisitorContext ctx) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft(), ctx).getValues())
        .addAllValues(visit(node.getRight(), ctx).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitOr(OrExpression node, AstNodeVisitorContext ctx) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft(), ctx).getValues())
        .addAllValues(visit(node.getRight(), ctx).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitConditional(ConditionalExpression node, AstNodeVisitorContext ctx) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft(), ctx).getValues())
        .addAllValues(visit(node.getRight(), ctx).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitBetween(BetweenExpression node, AstNodeVisitorContext ctx) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft(), ctx).getValues())
        .addAllValues(visit(node.getRight(), ctx).getValues())
        .addAllValues(visit(node.getValue(), ctx).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitAdditive(AdditiveExpression node, AstNodeVisitorContext ctx) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft(), ctx).getValues())
        .addAllValues(visit(node.getRight(), ctx).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitMultiplicative(MultiplicativeExpression node, AstNodeVisitorContext ctx) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft(), ctx).getValues())
        .addAllValues(visit(node.getRight(), ctx).getValues())
        .build();
  }

  private EnReferedTypes visit(AstNode node, AstNodeVisitorContext parent) {
    AstNodeVisitorContext ctx = ImmutableAstNodeVisitorContext.builder().parent(parent).value(node).build();

    if (node instanceof TypeInvocation) {
      return visitTypeInvocation((TypeInvocation) node, ctx);
    } else if (node instanceof Literal) {
      return visitLiteral((Literal) node, ctx);
    } else if (node instanceof NotUnary) {
      return visitNot((NotUnary) node, ctx);
    } else if (node instanceof NegateUnary) {
      return visitNegate((NegateUnary) node, ctx);
    } else if (node instanceof PositiveUnary) {
      return visitPositive((PositiveUnary) node, ctx);
    } else if (node instanceof PreIncrementUnary) {
      return visitPreIncrement((PreIncrementUnary) node, ctx);
    } else if (node instanceof PreDecrementUnary) {
      return visitPreDecrement((PreDecrementUnary) node, ctx);
    } else if (node instanceof PostIncrementUnary) {
      return visitPostIncrement((PostIncrementUnary) node, ctx);
    } else if (node instanceof PostDecrementUnary) {
      return visitPostDecrement((PostDecrementUnary) node, ctx);
    } else if (node instanceof MethodInvocation) {
      return visitMethod((MethodInvocation) node, ctx);
    } else if (node instanceof EqualityOperation) {
      return visitEquality((EqualityOperation) node, ctx);
    } else if (node instanceof AndExpression) {
      return visitAnd((AndExpression) node, ctx);
    } else if (node instanceof OrExpression) {
      return visitOr((OrExpression) node, ctx);
    } else if (node instanceof ConditionalExpression) {
      return visitConditional((ConditionalExpression) node, ctx);
    } else if (node instanceof BetweenExpression) {
      return visitBetween((BetweenExpression) node, ctx);
    } else if (node instanceof AdditiveExpression) {
      return visitAdditive((AdditiveExpression) node, ctx);
    } else if (node instanceof MultiplicativeExpression) {
      return visitMultiplicative((MultiplicativeExpression) node, ctx);
    } else if(node instanceof LambdaExpression) {
      return visitLambda((LambdaExpression) node, ctx);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownDTExpressionNode(node));
  }
}
