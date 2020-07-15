package io.resys.hdes.compiler.spi.java.visitors.dt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import org.immutables.value.Value.Immutable;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.AstNode.ArrayTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.Headers;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.ImmutableScalarTypeDefNode;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.visitors.JavaSpecUtil;
import io.resys.hdes.compiler.spi.java.visitors.dt.DtJavaSpec.DtHeaderSpec;
import io.resys.hdes.compiler.spi.java.visitors.dt.DtJavaSpec.DtTypesSpec;
import io.resys.hdes.compiler.spi.java.visitors.en.EnImplementationVisitor;
import io.resys.hdes.compiler.spi.java.visitors.en.EnInterfaceVisitor;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.FormulaMeta;
import io.resys.hdes.executor.api.HdesExecutable;
import io.resys.hdes.executor.api.HdesExecutable.ExecutionStatus;
import io.resys.hdes.executor.api.HdesExecutable.SourceType;
import io.resys.hdes.executor.api.ImmutableExecution;
import io.resys.hdes.executor.api.ImmutableFormulaMeta;

public class DtFormulaVisitor extends DtTemplateVisitor<DtJavaSpec, List<TypeSpec>> {

  private final Namings naming;
  private DtTypeNameResolver typeNames;
  private DecisionTableBody body;

  public DtFormulaVisitor(Namings naming) {
    super();
    this.naming = naming;
  }
  
  @Override
  public List<TypeSpec> visitDecisionTableBody(DecisionTableBody node) {
    this.body = node;
    this.typeNames = new DtTypeNameResolver(node);
    return Collections.unmodifiableList(visitHeaders(node.getHeaders()).getValues());
  }
  
  @Override
  public DtTypesSpec visitHeaders(Headers node) {    
    List<TypeSpec> values = new ArrayList<>();
    for(TypeDefNode header : node.getValues()) {
      DtTypesSpec spec = visitHeader(header);
      if(spec.getValues().isEmpty()) {
        continue;
      }
      values.addAll(spec.getValues());
    }
    
    return ImmutableDtTypesSpec.builder().addAllValues(values).build();
  }

  @Override
  public DtTypesSpec visitHeader(TypeDefNode node) {
    if(!(node instanceof ScalarTypeDefNode)) {
      return ImmutableDtTypesSpec.builder().build();
    }
    ScalarTypeDefNode typeDef = (ScalarTypeDefNode) node;
    if(typeDef.getFormula().isEmpty()) {
      return ImmutableDtTypesSpec.builder().build();
    }
    
    ScalarTypeDefNode outputReturnType = ImmutableScalarTypeDefNode.builder().from(typeDef).required(Boolean.TRUE).build();
    
    List<TypeDefNode> types = new EnInterfaceVisitor(this.typeNames).visitExpressionBody(typeDef.getFormula().get());
    ClassName outputType = naming.fr().outputValue(body, typeDef);
    
    TypeSpec api = TypeSpec.interfaceBuilder(naming.fr().api(body, typeDef))
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(javax.annotation.processing.Generated.class)
            .addMember("value", "$S", DtFormulaVisitor.class.getCanonicalName())
            .build())
        .addSuperinterface(naming.fr().executable(body, typeDef))
        
        // input
        .addTypes(visitInputs(types, typeDef).getValues())
        
        // output
        .addType(JavaSpecUtil.immutableSpec(outputType)
            .addSuperinterface(HdesExecutable.OutputValue.class)
            .addMethod(visitTypeDef(outputReturnType, outputReturnType).getValue())
            .build())
        .build();
    
    
    CodeBlock.Builder statements = CodeBlock.builder()
        .addStatement("long start = System.currentTimeMillis()")
        .addStatement("var result = $L", new EnImplementationVisitor(typeNames).visitExpressionBody(typeDef.getFormula().get()).getValue())
        .addStatement("long end = System.currentTimeMillis()")
        .add("\r\n")
        .add("$T meta = $T.builder()", FormulaMeta.class, ImmutableFormulaMeta.class)
        .add("\r\n  ").add(".id($S).status($T.COMPLETED) ", body.getId().getValue(), ExecutionStatus.class)
        .add("\r\n  ").add(".start(start).end(end).time(end - start)")
        .add("\r\n  ").addStatement(".build()")
        .add("\r\n")
        .addStatement("$T.Builder<$T, $T> builder = $T.builder()", ImmutableExecution.class, FormulaMeta.class, outputType, ImmutableExecution.class)
        .addStatement("return builder.meta(meta).value($L).build()",
            CodeBlock.builder()
            .add("$T.builder().$L(result).build()", JavaSpecUtil.immutable(outputType), typeDef.getName())
            .build()    
        );
    
    
    
    ParameterizedTypeName returnType = naming.fr().execution(body, typeDef);
    
    TypeSpec impl = TypeSpec.classBuilder(naming.fr().impl(body, typeDef))
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(javax.annotation.processing.Generated.class)
            .addMember("value", "$S", DtFormulaVisitor.class.getCanonicalName())
            .build())
        .addSuperinterface(naming.fr().api(body, typeDef))
        .addMethod(MethodSpec.methodBuilder("getSourceType")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(SourceType.class)
            .addStatement("return $T.FR", SourceType.class)
            .build())
        
        .addMethod(MethodSpec.methodBuilder("apply")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(naming.fr().inputValue(body, typeDef), "input").build())
            .returns(returnType)
            .addCode(statements.build())
            .build())
        .build();

    return ImmutableDtTypesSpec.builder().addValues(api, impl).build();
  }
  
  private DtTypesSpec visitInputs(List<TypeDefNode> node, ScalarTypeDefNode parent) {
    TypeSpec.Builder inputBuilder = JavaSpecUtil
        .immutableSpec(naming.fr().inputValue(body, parent))
        .addSuperinterface(HdesExecutable.InputValue.class);
    
    List<TypeSpec> nested = new ArrayList<>();
    for (TypeDefNode input : node) {
      DtHeaderSpec spec = visitTypeDef(input, parent);
      nested.addAll(spec.getChildren());
      inputBuilder.addMethod(spec.getValue());
    }
    return ImmutableDtTypesSpec.builder()
        .addValues(inputBuilder.build())
        .addAllValues(nested)
        .build();
  }
  

  private DtHeaderSpec visitTypeDef(TypeDefNode node, ScalarTypeDefNode parent) {
    if (node instanceof ScalarTypeDefNode) {
      return visitScalarDef((ScalarTypeDefNode) node);
    } else if (node instanceof ArrayTypeDefNode) {
      return visitArrayDef((ArrayTypeDefNode) node, parent);
    } else if (node instanceof ObjectTypeDefNode) {
      return visitObjectDef((ObjectTypeDefNode) node, parent);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownFlInputRule(node));
  }

  @Override
  public DtHeaderSpec visitScalarDef(ScalarTypeDefNode node) {
    Class<?> returnType = JavaSpecUtil.type(node.getType());
    MethodSpec method = MethodSpec.methodBuilder(JavaSpecUtil.methodName(node.getName()))
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .returns( node.getRequired() ? ClassName.get(returnType) : ParameterizedTypeName.get(Optional.class, returnType))
        .build();
    return ImmutableDtHeaderSpec.builder().value(method).build();
  }

  private DtHeaderSpec visitArrayDef(ArrayTypeDefNode node, ScalarTypeDefNode parent) {
    DtHeaderSpec childSpec = visitTypeDef(node.getValue(), parent);
    com.squareup.javapoet.TypeName arrayType;
    if (node.getValue().getRequired()) {
      arrayType = childSpec.getValue().returnType;
    } else {
      arrayType = ((ParameterizedTypeName) childSpec.getValue().returnType).typeArguments.get(0);
    }
    return ImmutableDtHeaderSpec.builder()
        .value(childSpec.getValue().toBuilder()
            .returns(ParameterizedTypeName.get(ClassName.get(List.class), arrayType))
            .build())
        .children(childSpec.getChildren())
        .build();
  }

  private DtHeaderSpec visitObjectDef(ObjectTypeDefNode node, ScalarTypeDefNode parent) {
    ClassName typeName = naming.fr().inputValue(body, parent);
    TypeSpec.Builder objectBuilder = TypeSpec
        .interfaceBuilder(typeName)
        .addSuperinterface(node.getDirection() == DirectionType.IN ? HdesExecutable.InputValue.class : HdesExecutable.OutputValue.class)
        .addAnnotation(Immutable.class)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    List<TypeSpec> nested = new ArrayList<>();
    for (TypeDefNode input : node.getValues()) {
      DtHeaderSpec spec = visitTypeDef(input, parent);
      nested.addAll(spec.getChildren());
      objectBuilder.addMethod(spec.getValue());
    }
    TypeSpec objectType = objectBuilder.build();
    nested.add(objectType);
    return ImmutableDtHeaderSpec.builder()
        .children(nested)
        .value(
            MethodSpec.methodBuilder(JavaSpecUtil.methodName(node.getName()))
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(node.getRequired() ? typeName : ParameterizedTypeName.get(ClassName.get(Optional.class), typeName))
                .build())
        .build();
  }
}
