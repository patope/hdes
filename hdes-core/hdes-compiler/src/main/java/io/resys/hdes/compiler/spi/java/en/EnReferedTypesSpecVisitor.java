package io.resys.hdes.compiler.spi.java.en;

import java.util.List;
import java.util.stream.Collectors;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeName;
import io.resys.hdes.ast.api.nodes.AstNode.TypeNameScope;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.ExpressionAstNodeVisitor;
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
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.en.EnReferedTypesSpec.EnReferedScope;
import io.resys.hdes.compiler.spi.java.en.EnReferedTypesSpec.EnReferedType;
import io.resys.hdes.compiler.spi.java.en.EnReferedTypesSpec.EnReferedTypeResolver;
import io.resys.hdes.compiler.spi.java.en.EnReferedTypesSpec.EnReferedTypes;

public class EnReferedTypesSpecVisitor implements ExpressionAstNodeVisitor<EnReferedTypes, EnReferedTypes> {
  
  private final EnReferedTypeResolver resolver;
  
  public EnReferedTypesSpecVisitor(EnReferedTypeResolver resolver) {
    super();
    this.resolver = resolver;
  }

  @Override
  public EnReferedTypes visitExpressionBody(ExpressionBody node) {
    ImmutableEnReferedTypes.Builder result = ImmutableEnReferedTypes.builder();
    
    for(EnReferedType ref : visit(node.getValue()).getValues()) {
      result.addScopes(ref.getScope());
      result.addValues(ref);
    }
    
    return result.build();
  }
  
  @Override
  public EnReferedTypes visitTypeName(TypeName node) {
    final TypeDefNode typeDef = resolver.accept(node);
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
  public EnReferedTypes visitMethodRefNode(MethodRefNode node) {
    final TypeDefNode typeDef = resolver.accept(node);
    ImmutableEnReferedTypes.Builder builder = ImmutableEnReferedTypes.builder().addValues(
        ImmutableEnReferedType.builder()
        .typeName(node.getType())
        .methodRef(node)
        .node(typeDef)
        .scope(EnReferedScope.METHOD)
        .build());
      
    node.getValues().stream()
    .map(v -> visit(v).getValues())
    .forEach(v -> builder.addAllValues(v));
    return builder.build();
  }

  @Override
  public EnReferedTypes visitLambdaExpression(LambdaExpression node) {
    List<String> lambdaParams = node.getParams().stream()
        .map(t -> t.getValue())
        .collect(Collectors.toList());
    
    List<AstNode> values = visit(node.getBody()).getValues().stream()
      .filter(t -> t instanceof TypeName)
      .map(t -> (TypeName) t)
      .filter(t -> !lambdaParams.contains(t.getValue()))
      .collect(Collectors.toList());

    return null;
  }
  
  @Override
  public EnReferedTypes visitLiteral(Literal node) {
    return ImmutableEnReferedTypes.builder().build();
  }

  @Override
  public EnReferedTypes visitNotUnaryOperation(NotUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnReferedTypes visitNegateUnaryOperation(NegateUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnReferedTypes visitPositiveUnaryOperation(PositiveUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnReferedTypes visitPreIncrementUnaryOperation(PreIncrementUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnReferedTypes visitPreDecrementUnaryOperation(PreDecrementUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnReferedTypes visitPostIncrementUnaryOperation(PostIncrementUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnReferedTypes visitPostDecrementUnaryOperation(PostDecrementUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnReferedTypes visitEqualityOperation(EqualityOperation node) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitAndOperation(AndOperation node) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitOrOperation(OrOperation node) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitConditionalExpression(ConditionalExpression node) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitBetweenExpression(BetweenExpression node) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .addAllValues(visit(node.getValue()).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitAdditiveOperation(AdditiveOperation node) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnReferedTypes visitMultiplicativeOperation(MultiplicativeOperation node) {
    return ImmutableEnReferedTypes.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  private EnReferedTypes visit(AstNode node) {
    if (node instanceof TypeName) {
      return visitTypeName((TypeName) node);
    } else if (node instanceof Literal) {
      return visitLiteral((Literal) node);
    } else if (node instanceof NotUnaryOperation) {
      return visitNotUnaryOperation((NotUnaryOperation) node);
    } else if (node instanceof NegateUnaryOperation) {
      return visitNegateUnaryOperation((NegateUnaryOperation) node);
    } else if (node instanceof PositiveUnaryOperation) {
      return visitPositiveUnaryOperation((PositiveUnaryOperation) node);
    } else if (node instanceof PreIncrementUnaryOperation) {
      return visitPreIncrementUnaryOperation((PreIncrementUnaryOperation) node);
    } else if (node instanceof PreDecrementUnaryOperation) {
      return visitPreDecrementUnaryOperation((PreDecrementUnaryOperation) node);
    } else if (node instanceof PostIncrementUnaryOperation) {
      return visitPostIncrementUnaryOperation((PostIncrementUnaryOperation) node);
    } else if (node instanceof PostDecrementUnaryOperation) {
      return visitPostDecrementUnaryOperation((PostDecrementUnaryOperation) node);
    } else if (node instanceof MethodRefNode) {
      return visitMethodRefNode((MethodRefNode) node);
    } else if (node instanceof EqualityOperation) {
      return visitEqualityOperation((EqualityOperation) node);
    } else if (node instanceof AndOperation) {
      return visitAndOperation((AndOperation) node);
    } else if (node instanceof OrOperation) {
      return visitOrOperation((OrOperation) node);
    } else if (node instanceof ConditionalExpression) {
      return visitConditionalExpression((ConditionalExpression) node);
    } else if (node instanceof BetweenExpression) {
      return visitBetweenExpression((BetweenExpression) node);
    } else if (node instanceof AdditiveOperation) {
      return visitAdditiveOperation((AdditiveOperation) node);
    } else if (node instanceof MultiplicativeOperation) {
      return visitMultiplicativeOperation((MultiplicativeOperation) node);
    } else if(node instanceof LambdaExpression) {
      return visitLambdaExpression((LambdaExpression) node);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownDTExpressionNode(node));
  }
}
