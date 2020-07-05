package io.resys.hdes.compiler.spi.java.visitors.en;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import org.immutables.value.Value.Immutable;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.ArrayTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarTypeDefNode;
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
import io.resys.hdes.compiler.spi.java.visitors.JavaSpecUtil;
import io.resys.hdes.compiler.spi.java.visitors.en.EnJavaSpec.EnRefSpec;
import io.resys.hdes.compiler.spi.java.visitors.en.EnJavaSpec.TypeNameResolver;
import io.resys.hdes.compiler.spi.java.visitors.fl.FlJavaSpec.FlHeaderSpec;
import io.resys.hdes.compiler.spi.java.visitors.fl.FlJavaSpec.FlTypesSpec;
import io.resys.hdes.compiler.spi.java.visitors.fl.ImmutableFlHeaderSpec;
import io.resys.hdes.compiler.spi.java.visitors.fl.ImmutableFlTypesSpec;
import io.resys.hdes.executor.api.HdesExecutable;

public class EnInterfaceVisitor extends EnTemplateVisitor implements ExpressionAstNodeVisitor<EnRefSpec, TypeSpec> {
  
  private final TypeNameResolver resolver;
  private final ScalarTypeDefNode output;
  
  public EnInterfaceVisitor(TypeNameResolver resolver, ScalarTypeDefNode output) {
    super();
    this.resolver = resolver;
    this.output = output;
  }

  @Override
  public TypeSpec visitExpressionBody(ExpressionBody node) {
    EnRefSpec refSpec = visit(node.getValue());
    
    for(AstNode ref : refSpec.getValues()) {
      if(ref instanceof TypeName) {
        TypeDefNode def = resolver.accept((TypeName) ref); 
        
      } else {
        throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(ref));
      }
    }
    return null;
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
  

  @Override
  public FlTypesSpec visitInputs(List<TypeDefNode> node) {
    TypeSpec.Builder inputBuilder = TypeSpec
        .interfaceBuilder(naming.fl().input(body))
        .addSuperinterface(HdesExecutable.InputValue.class)
        .addAnnotation(Immutable.class)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    List<TypeSpec> nested = new ArrayList<>();
    for (TypeDefNode input : node) {
      FlHeaderSpec spec = visitTypeDef(input);
      nested.addAll(spec.getChildren());
      inputBuilder.addMethod(spec.getValue());
    }
    return ImmutableFlTypesSpec.builder()
        .addValues(inputBuilder.build())
        .addAllValues(nested)
        .build();
  }

  @Override
  public FlTypesSpec visitOutputs(List<TypeDefNode> node) {
    TypeSpec.Builder outputBuilder = TypeSpec
        .interfaceBuilder(naming.fl().output(body))
        .addSuperinterface(HdesExecutable.OutputValue.class)
        .addAnnotation(Immutable.class)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    List<TypeSpec> nested = new ArrayList<>();
    for (TypeDefNode output : node) {
      FlHeaderSpec spec = visitTypeDef(output);
      nested.addAll(spec.getChildren());
      outputBuilder.addMethod(spec.getValue());
    }
    return ImmutableFlTypesSpec.builder()
        .addValues(outputBuilder.build())
        .addAllValues(nested)
        .build();
  }

  private FlHeaderSpec visitTypeDef(TypeDefNode node) {
    if (node instanceof ScalarTypeDefNode) {
      return visitScalarDef((ScalarTypeDefNode) node);
    } else if (node instanceof ArrayTypeDefNode) {
      return visitArrayDef((ArrayTypeDefNode) node);
    } else if (node instanceof ObjectTypeDefNode) {
      return visitObjectDef((ObjectTypeDefNode) node);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownFlInputRule(node));
  }

  @Override
  public FlHeaderSpec visitScalarDef(ScalarTypeDefNode node) {
    Class<?> returnType = JavaSpecUtil.type(node.getType());
    MethodSpec method = MethodSpec.methodBuilder(JavaSpecUtil.getMethodName(node.getName()))
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .returns(node.getRequired() ? ClassName.get(returnType) : ParameterizedTypeName.get(Optional.class, returnType))
        .build();
    return ImmutableFlHeaderSpec.builder().value(method).build();
  }

  @Override
  public FlHeaderSpec visitArrayDef(ArrayTypeDefNode node) {
    FlHeaderSpec childSpec = visitTypeDef(node.getValue());
    com.squareup.javapoet.TypeName arrayType;
    if (node.getValue().getRequired()) {
      arrayType = childSpec.getValue().returnType;
    } else {
      arrayType = ((ParameterizedTypeName) childSpec.getValue().returnType).typeArguments.get(0);
    }
    return ImmutableFlHeaderSpec.builder()
        .value(childSpec.getValue().toBuilder()
            .returns(ParameterizedTypeName.get(ClassName.get(List.class), arrayType))
            .build())
        .children(childSpec.getChildren())
        .build();
  }

  @Override
  public FlHeaderSpec visitObjectDef(ObjectTypeDefNode node) {
    ClassName typeName = node.getDirection() == DirectionType.IN ? naming.fl().input(body, node) : naming.fl().output(body, node);
    TypeSpec.Builder objectBuilder = TypeSpec
        .interfaceBuilder(typeName)
        .addSuperinterface(node.getDirection() == DirectionType.IN ? HdesExecutable.InputValue.class : HdesExecutable.OutputValue.class)
        .addAnnotation(Immutable.class)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    List<TypeSpec> nested = new ArrayList<>();
    for (TypeDefNode input : node.getValues()) {
      FlHeaderSpec spec = visitTypeDef(input);
      nested.addAll(spec.getChildren());
      objectBuilder.addMethod(spec.getValue());
    }
    TypeSpec objectType = objectBuilder.build();
    nested.add(objectType);
    return ImmutableFlHeaderSpec.builder()
        .children(nested)
        .value(
            MethodSpec.methodBuilder(JavaSpecUtil.getMethodName(node.getName()))
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(node.getRequired() ? typeName : ParameterizedTypeName.get(ClassName.get(Optional.class), typeName))
                .build())
        .build();
  }
}
