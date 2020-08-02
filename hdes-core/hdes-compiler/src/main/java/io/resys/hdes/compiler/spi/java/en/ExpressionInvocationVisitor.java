package io.resys.hdes.compiler.spi.java.en;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarType;
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
import io.resys.hdes.ast.api.nodes.ExpressionNode.MultiplicativeType;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NegateUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NotUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.OrExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PositiveUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PostDecrementUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PostIncrementUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PreDecrementUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PreIncrementUnary;
import io.resys.hdes.ast.api.nodes.ImmutableAstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.ImmutableScalarDef;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.en.ExpressionInvocationSpec.InvocationResolver;
import io.resys.hdes.compiler.spi.java.en.ExpressionInvocationSpec.InvocationSpecParam;
import io.resys.hdes.compiler.spi.java.en.ExpressionInvocationSpec.InvocationSpecParams;
import io.resys.hdes.compiler.spi.java.en.ExpressionInvocationSpec.UsageSource;

public class ExpressionInvocationVisitor implements ExpressionAstNodeVisitor<InvocationSpecParams, InvocationSpecParams> {
  
  private final InvocationResolver resolver;
  
  public ExpressionInvocationVisitor(InvocationResolver resolver) {
    super();
    this.resolver = resolver;
  }

  @Override
  public InvocationSpecParams visitBody(ExpressionBody node, AstNodeVisitorContext ctx) {
    return visit(node.getValue(), ctx);
  }
  
  @Override
  public InvocationSpecParams visitLiteral(Literal node, AstNodeVisitorContext ctx) {
    return ImmutableInvocationSpecParams.builder()
        .returnType(ImmutableScalarDef.builder()
            .array(false).required(true)
            .token(node.getToken())
            .name("")
            .direction(DirectionType.IN)
            .type(node.getType())
            .build())
        .build();
  }
  
  @Override
  public InvocationSpecParams visitAdditive(AdditiveExpression node, AstNodeVisitorContext ctx) {
    InvocationSpecParams left = visit(node.getLeft(), ctx);
    InvocationSpecParams right = visit(node.getRight(), ctx);
    
    ScalarType returnScalarType = null;
    if(left.getReturnType() instanceof ScalarDef && right.getReturnType() instanceof ScalarDef) {
      Set<ScalarType> scalars = new HashSet<>();
      scalars.add(((ScalarDef) left.getReturnType()).getType());
      scalars.add(((ScalarDef) right.getReturnType()).getType());
      
      if(scalars.size() == 1 && (scalars.contains(ScalarType.INTEGER) || scalars.contains(ScalarType.DECIMAL))) {
        returnScalarType = scalars.iterator().next(); 
      } else if(scalars.contains(ScalarType.INTEGER) && scalars.contains(ScalarType.DECIMAL)) {
        returnScalarType = ScalarType.DECIMAL;
      } else {
        throw new HdesCompilerException(HdesCompilerException.builder().incompatibleConditionalReturnType(node, node.getLeft(), node.getRight()));
      }
      
    } else {
      throw new HdesCompilerException(HdesCompilerException.builder().incompatibleConditionalReturnType(node, node.getLeft(), node.getRight()));
    }
    
    return ImmutableInvocationSpecParams.builder()
        .addAllValues(left.getValues())
        .addAllValues(right.getValues())
        .addAllUsageSources(left.getUsageSources())
        .addAllUsageSources(right.getUsageSources())
        .returnType(ImmutableScalarDef.builder()
            .array(false).required(true)
            .token(node.getToken())
            .name("")
            .direction(DirectionType.IN)
            .type(returnScalarType)
            .build())
        .build();
  }

  @Override
  public InvocationSpecParams visitMultiplicative(MultiplicativeExpression node, AstNodeVisitorContext ctx) {
    InvocationSpecParams left = visit(node.getLeft(), ctx);
    InvocationSpecParams right = visit(node.getRight(), ctx);
    
    ScalarType returnScalarType = null;
    if(left.getReturnType() instanceof ScalarDef && right.getReturnType() instanceof ScalarDef) {
      Set<ScalarType> scalars = new HashSet<>();
      scalars.add(((ScalarDef) left.getReturnType()).getType());
      scalars.add(((ScalarDef) right.getReturnType()).getType());
      
      if(scalars.size() == 1 && scalars.contains(ScalarType.INTEGER) && scalars.contains(ScalarType.DECIMAL)) {
        returnScalarType = node.getType() == MultiplicativeType.DIVIDE ? ScalarType.DECIMAL : scalars.iterator().next(); 
      } else if(scalars.contains(ScalarType.INTEGER) && scalars.contains(ScalarType.DECIMAL)) {
        returnScalarType = ScalarType.DECIMAL;
      } else {
        throw new HdesCompilerException(HdesCompilerException.builder().incompatibleConditionalReturnType(node, node.getLeft(), node.getRight()));
      }
      
    } else {
      throw new HdesCompilerException(HdesCompilerException.builder().incompatibleConditionalReturnType(node, node.getLeft(), node.getRight()));
    }
    
    return ImmutableInvocationSpecParams.builder()
        .addAllValues(left.getValues())
        .addAllValues(right.getValues())
        .addAllUsageSources(left.getUsageSources())
        .addAllUsageSources(right.getUsageSources())
        .returnType(ImmutableScalarDef.builder()
            .array(false).required(true)
            .token(node.getToken())
            .name("")
            .direction(DirectionType.IN)
            .type(returnScalarType)
            .build())
        .build();
  }
  
  @Override
  public InvocationSpecParams visitConditional(ConditionalExpression node, AstNodeVisitorContext ctx) {
    InvocationSpecParams left = visit(node.getLeft(), ctx);
    InvocationSpecParams right = visit(node.getRight(), ctx);
    
    TypeDef returnTypeDef = null;
    if(left.getReturnType() instanceof ScalarDef && right.getReturnType() instanceof ScalarDef) {
      Set<ScalarType> scalars = new HashSet<>();
      scalars.add(((ScalarDef) left.getReturnType()).getType());
      scalars.add(((ScalarDef) right.getReturnType()).getType());
      
      ScalarType returnScalarType = null;
      if(scalars.size() == 1) {
        returnScalarType = scalars.iterator().next(); 
      } else if(scalars.contains(ScalarType.INTEGER) && scalars.contains(ScalarType.DECIMAL)) {
        returnScalarType = ScalarType.DECIMAL;
      } else {
        throw new HdesCompilerException(HdesCompilerException.builder().incompatibleConditionalReturnType(node, node.getLeft(), node.getRight()));
      }
      returnTypeDef = ImmutableScalarDef.builder()
        .array(false).required(true)
        .token(node.getToken())
        .name("")
        .direction(DirectionType.IN)
        .type(returnScalarType)
        .build();
    } else if(left.getReturnType().equals(right.getReturnType())) {
      returnTypeDef = left.getReturnType();
    }
    
    if(returnTypeDef == null) {
      throw new HdesCompilerException(HdesCompilerException.builder().incompatibleConditionalReturnType(node, node.getLeft(), node.getRight()));
    }
    
    return ImmutableInvocationSpecParams.builder()
        .addAllValues(left.getValues())
        .addAllValues(right.getValues())
        .addAllUsageSources(left.getUsageSources())
        .addAllUsageSources(right.getUsageSources())
        .returnType(returnTypeDef)
        .build();
  }
  
  @Override
  public InvocationSpecParams visitTypeInvocation(TypeInvocation node, AstNodeVisitorContext ctx) {
    final TypeDef typeDef = resolver.accept(node, ctx);
    final UsageSource scope;
    
    if(node.getScope() == TypeNameScope.STATIC) {
      scope = UsageSource.STATIC;
    } else if(node.getScope() == TypeNameScope.INSTANCE) {
      scope = UsageSource.INSTANCE;
    } else if(typeDef.getDirection() == DirectionType.IN) {
      scope = UsageSource.IN;
    } else if(typeDef.getDirection() == DirectionType.OUT) {
      scope = UsageSource.OUT;
    } else {
      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(node));
    }
    
    return ImmutableInvocationSpecParams.builder()
        .returnType(typeDef)
        .addValues(ImmutableInvocationSpecParam.builder().typeName(node).node(typeDef).usageSource(scope).build())
        .addUsageSources(scope)
        .build();
  }
  
  @Override
  public InvocationSpecParams visitMethod(MethodInvocation node, AstNodeVisitorContext ctx) {
    final TypeDef typeDef = resolver.accept(node, ctx);
    List<UsageSource> usage = new ArrayList<>();
    usage.add(UsageSource.INSTANCE);
    List<InvocationSpecParam> values = new ArrayList<>();
    
    if(node.getType().isPresent()) {
      InvocationSpecParams children = visit(node.getType().get(), ctx);
      values.addAll(children.getValues());
      usage.addAll(children.getUsageSources());
    }
    
    for(AstNode child : node.getValues()) {
      InvocationSpecParams children = visit(child, ctx);
      values.addAll(children.getValues());
      usage.addAll(children.getUsageSources());
    }
    
    return ImmutableInvocationSpecParams.builder()
        .returnType(typeDef)
        .addValues(ImmutableInvocationSpecParam.builder().methodRef(node).node(typeDef).usageSource(UsageSource.INSTANCE).build())
        .addAllValues(values)
        .addAllUsageSources(usage)
        .build();
  }

  @Override
  public InvocationSpecParams visitLambda(LambdaExpression node, AstNodeVisitorContext ctx) {
    
    return visit(node.getBody(), ctx);
  }

  @Override
  public InvocationSpecParams visitNot(NotUnary node, AstNodeVisitorContext ctx) {
    return visit(node.getValue(), ctx);
  }

  @Override
  public InvocationSpecParams visitNegate(NegateUnary node, AstNodeVisitorContext ctx) {
    return visit(node.getValue(), ctx);
  }

  @Override
  public InvocationSpecParams visitPositive(PositiveUnary node, AstNodeVisitorContext ctx) {
    return visit(node.getValue(), ctx);
  }

  @Override
  public InvocationSpecParams visitPreIncrement(PreIncrementUnary node, AstNodeVisitorContext ctx) {
    return visit(node.getValue(), ctx);
  }

  @Override
  public InvocationSpecParams visitPreDecrement(PreDecrementUnary node, AstNodeVisitorContext ctx) {
    return visit(node.getValue(), ctx);
  }

  @Override
  public InvocationSpecParams visitPostIncrement(PostIncrementUnary node, AstNodeVisitorContext ctx) {
    return visit(node.getValue(), ctx);
  }

  @Override
  public InvocationSpecParams visitPostDecrement(PostDecrementUnary node, AstNodeVisitorContext ctx) {
    return visit(node.getValue(), ctx);
  }

  @Override
  public InvocationSpecParams visitEquality(EqualityOperation node, AstNodeVisitorContext ctx) {
    InvocationSpecParams left = visit(node.getLeft(), ctx);
    InvocationSpecParams right = visit(node.getRight(), ctx);
    
    return ImmutableInvocationSpecParams.builder()
        .addAllValues(left.getValues())
        .addAllValues(right.getValues())
        .addAllUsageSources(left.getUsageSources())
        .addAllUsageSources(right.getUsageSources())
        .returnType(ImmutableScalarDef.builder()
            .array(false).required(true)
            .token(node.getToken())
            .name("")
            .direction(DirectionType.IN)
            .type(ScalarType.BOOLEAN)
            .build())
        .build();
  }

  @Override
  public InvocationSpecParams visitAnd(AndExpression node, AstNodeVisitorContext ctx) {
    InvocationSpecParams left = visit(node.getLeft(), ctx);
    InvocationSpecParams right = visit(node.getRight(), ctx);
    
    
    return ImmutableInvocationSpecParams.builder()
        .addAllValues(left.getValues())
        .addAllValues(right.getValues())
        .addAllUsageSources(left.getUsageSources())
        .addAllUsageSources(right.getUsageSources())
        .returnType(ImmutableScalarDef.builder()
            .array(false).required(true)
            .token(node.getToken())
            .name("")
            .direction(DirectionType.IN)
            .type(ScalarType.BOOLEAN)
            .build())
        .build();
  }

  @Override
  public InvocationSpecParams visitOr(OrExpression node, AstNodeVisitorContext ctx) {
    InvocationSpecParams left = visit(node.getLeft(), ctx);
    InvocationSpecParams right = visit(node.getRight(), ctx);
    
    return ImmutableInvocationSpecParams.builder()
        .addAllValues(left.getValues())
        .addAllValues(right.getValues())
        .addAllUsageSources(left.getUsageSources())
        .addAllUsageSources(right.getUsageSources())
        .returnType(ImmutableScalarDef.builder()
            .array(false).required(true)
            .token(node.getToken())
            .name("")
            .direction(DirectionType.IN)
            .type(ScalarType.BOOLEAN)
            .build())
        .build();
  }

  @Override
  public InvocationSpecParams visitBetween(BetweenExpression node, AstNodeVisitorContext ctx) {
    InvocationSpecParams left = visit(node.getLeft(), ctx);
    InvocationSpecParams right = visit(node.getRight(), ctx);
    InvocationSpecParams value = visit(node.getValue(), ctx);
    
    return ImmutableInvocationSpecParams.builder()
        .addAllValues(left.getValues())
        .addAllValues(right.getValues())
        .addAllValues(value.getValues())
        .addAllUsageSources(left.getUsageSources())
        .addAllUsageSources(right.getUsageSources())
        .addAllUsageSources(value.getUsageSources())
        .returnType(ImmutableScalarDef.builder()
            .array(false).required(true)
            .token(node.getToken())
            .name("")
            .direction(DirectionType.IN)
            .type(ScalarType.BOOLEAN)
            .build())
        .build();
  }

  public InvocationSpecParams visit(AstNode node, AstNodeVisitorContext parent) {
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
    } else if(node instanceof ExpressionBody) {
      return visitBody((ExpressionBody) node, ctx);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownDTExpressionNode(node));
  }
}
