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
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.EnReferedScope;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.EnReferedType;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.EnReferedTypeResolver;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.EnReferedTypes;

public class ExpressionRefsVisitor implements ExpressionAstNodeVisitor<EnReferedTypes, EnReferedTypes> {
  
  private final EnReferedTypeResolver resolver;
  
  public ExpressionRefsVisitor(EnReferedTypeResolver resolver) {
    super();
    this.resolver = resolver;
  }

  @Override
  public EnReferedTypes visitBody(ExpressionBody node) {
    ImmutableEnReferedTypes.Builder result = ImmutableEnReferedTypes.builder();
    
    for(EnReferedType ref : visit(node.getValue()).getValues()) {
      result.addScopes(ref.getScope());
      result.addValues(ref);
    }
    
    return result.build();
  }
  
  @Override
  public EnReferedTypes visitTypeInvocation(TypeInvocation node) {
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
  public EnReferedTypes visitMethod(MethodInvocation node) {
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
  public EnReferedTypes visitLambda(LambdaExpression node) {
    List<String> lambdaParams = node.getParams().stream()
        .map(t -> t.getValue())
        .collect(Collectors.toList());
    
    List<AstNode> values = visit(node.getBody()).getValues().stream()
      .filter(t -> t instanceof TypeInvocation)
      .map(t -> (TypeInvocation) t)
      .filter(t -> !lambdaParams.contains(t.getValue()))
      .collect(Collectors.toList());

    return null;
  }
  
  @Override
  public EnReferedTypes visitLiteral(Literal node) {
    return ImmutableEnReferedTypes.builder().build();
  }

  @Override
  public EnReferedTypes visitNot(NotUnary node) {
    return visit(node.getValue());
  }

  @Override
  public EnReferedTypes visitNegate(NegateUnary node) {
    return visit(node.getValue());
  }

  @Override
  public EnReferedTypes visitPositive(PositiveUnary node) {
    return visit(node.getValue());
  }

  @Override
  public EnReferedTypes visitPreIncrement(PreIncrementUnary node) {
    return visit(node.getValue());
  }

  @Override
  public EnReferedTypes visitPreDecrement(PreDecrementUnary node) {
    return visit(node.getValue());
  }

  @Override
  public EnReferedTypes visitPostIncrement(PostIncrementUnary node) {
    return visit(node.getValue());
  }

  @Override
  public EnReferedTypes visitPostDecrement(PostDecrementUnary node) {
    return visit(node.getValue());
  }

  @Override
  public EnReferedTypes visitEquality(EqualityOperation node) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitAnd(AndExpression node) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitOr(OrExpression node) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitConditional(ConditionalExpression node) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitBetween(BetweenExpression node) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .addAllValues(visit(node.getValue()).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitAdditive(AdditiveExpression node) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitMultiplicative(MultiplicativeExpression node) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  private EnReferedTypes visit(AstNode node) {
    if (node instanceof TypeInvocation) {
      return visitTypeInvocation((TypeInvocation) node);
    } else if (node instanceof Literal) {
      return visitLiteral((Literal) node);
    } else if (node instanceof NotUnary) {
      return visitNot((NotUnary) node);
    } else if (node instanceof NegateUnary) {
      return visitNegate((NegateUnary) node);
    } else if (node instanceof PositiveUnary) {
      return visitPositive((PositiveUnary) node);
    } else if (node instanceof PreIncrementUnary) {
      return visitPreIncrement((PreIncrementUnary) node);
    } else if (node instanceof PreDecrementUnary) {
      return visitPreDecrement((PreDecrementUnary) node);
    } else if (node instanceof PostIncrementUnary) {
      return visitPostIncrement((PostIncrementUnary) node);
    } else if (node instanceof PostDecrementUnary) {
      return visitPostDecrement((PostDecrementUnary) node);
    } else if (node instanceof MethodInvocation) {
      return visitMethod((MethodInvocation) node);
    } else if (node instanceof EqualityOperation) {
      return visitEquality((EqualityOperation) node);
    } else if (node instanceof AndExpression) {
      return visitAnd((AndExpression) node);
    } else if (node instanceof OrExpression) {
      return visitOr((OrExpression) node);
    } else if (node instanceof ConditionalExpression) {
      return visitConditional((ConditionalExpression) node);
    } else if (node instanceof BetweenExpression) {
      return visitBetween((BetweenExpression) node);
    } else if (node instanceof AdditiveExpression) {
      return visitAdditive((AdditiveExpression) node);
    } else if (node instanceof MultiplicativeExpression) {
      return visitMultiplicative((MultiplicativeExpression) node);
    } else if(node instanceof LambdaExpression) {
      return visitLambda((LambdaExpression) node);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownDTExpressionNode(node));
  }
}
