package io.resys.hdes.compiler.spi.java.visitors.en;

import java.util.ArrayList;
import java.util.List;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeName;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.ExpressionAstNodeVisitor;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AdditiveOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AndOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.BetweenExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ConditionalExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
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
import io.resys.hdes.compiler.spi.java.visitors.en.EnJavaSpec.EnRefSpec;
import io.resys.hdes.compiler.spi.java.visitors.en.EnJavaSpec.TypeNameResolver;

public class EnInterfaceVisitor extends EnTemplateVisitor<EnRefSpec, List<TypeDefNode>> implements ExpressionAstNodeVisitor<EnRefSpec, List<TypeDefNode>> {
  
  private final TypeNameResolver resolver;
  
  public EnInterfaceVisitor(TypeNameResolver resolver) {
    super();
    this.resolver = resolver;
  }

  @Override
  public List<TypeDefNode> visitExpressionBody(ExpressionBody node) {
    List<TypeDefNode> result = new ArrayList<>();
    for(AstNode ref : visit(node.getValue()).getValues()) {
      if(ref instanceof TypeName) {
        TypeDefNode def = resolver.accept((TypeName) ref); 
        result.add(def);
      } else {
        throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(ref));
      }
    }
    return result;
  }
  
  @Override
  public EnRefSpec visitTypeName(TypeName node) {
    return ImmutableEnRefSpec.builder().addValues(node).build();
  }

  @Override
  public EnRefSpec visitMethodRefNode(MethodRefNode node) {
    List<AstNode> values = new ArrayList<>();
    node.getValues().forEach(v -> values.addAll(visit(v).getValues()));
    return ImmutableEnRefSpec.builder().addValues(node).build();
  }
  
  @Override
  public EnRefSpec visitLiteral(Literal node) {
    return ImmutableEnRefSpec.builder().build();
  }

  @Override
  public EnRefSpec visitNotUnaryOperation(NotUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitNegateUnaryOperation(NegateUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitPositiveUnaryOperation(PositiveUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitPreIncrementUnaryOperation(PreIncrementUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitPreDecrementUnaryOperation(PreDecrementUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitPostIncrementUnaryOperation(PostIncrementUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitPostDecrementUnaryOperation(PostDecrementUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitEqualityOperation(EqualityOperation node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitAndOperation(AndOperation node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitOrOperation(OrOperation node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitConditionalExpression(ConditionalExpression node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitBetweenExpression(BetweenExpression node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .addAllValues(visit(node.getValue()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitAdditiveOperation(AdditiveOperation node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitMultiplicativeOperation(MultiplicativeOperation node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  private EnRefSpec visit(AstNode node) {
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
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownDTExpressionNode(node));
  }
}
