package io.resys.hdes.compiler.spi.java.visitors.fl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/*-
 * #%L
 * hdes-compiler
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

import javax.lang.model.element.Modifier;

import org.immutables.value.Value.Immutable;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.AstNode.ArrayTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.TaskRef;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.NamingContext;
import io.resys.hdes.compiler.spi.NamingContext.TaskRefNaming;
import io.resys.hdes.compiler.spi.java.visitors.JavaSpecUtil;
import io.resys.hdes.compiler.spi.java.visitors.en.EnInterfaceVisitor;
import io.resys.hdes.compiler.spi.java.visitors.fl.FlJavaSpec.FlHeaderSpec;
import io.resys.hdes.compiler.spi.java.visitors.fl.FlJavaSpec.FlMethodSpec;
import io.resys.hdes.compiler.spi.java.visitors.fl.FlJavaSpec.FlTypesSpec;
import io.resys.hdes.executor.api.HdesExecutable;

public class FlInterfaceVisitor extends FlTemplateVisitor<FlJavaSpec, TypeSpec> {
  private final NamingContext naming;
  private FlowBody body;
  private FlTypeNameResolver typeNameResolver;

  public FlInterfaceVisitor(NamingContext naming) {
    super();
    this.naming = naming;
  }

  @Override
  public TypeSpec visitBody(FlowBody node) {
    this.body = node;
    this.typeNameResolver = new FlTypeNameResolver(node, naming.ast());
    TypeSpec.Builder stateBuilder = TypeSpec
        .interfaceBuilder(naming.fl().state(node))
        .addAnnotation(Immutable.class)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addSuperinterface(Serializable.class)
        .addMethods(node.getTask().map(t -> visitTask(t).getValue()).orElse(Collections.emptyList()));
    TypeSpec.Builder flowBuilder = TypeSpec.interfaceBuilder(naming.fl().interfaze(node))
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(javax.annotation.processing.Generated.class).addMember("value", "$S", FlInterfaceVisitor.class.getCanonicalName()).build())
        .addSuperinterface(naming.fl().superinterface(node))
        .addTypes(visitInputs(node.getHeaders().getValues().stream().filter(t -> t.getDirection() == DirectionType.IN).collect(Collectors.toList())).getValues())
        .addTypes(visitOutputs(node.getHeaders().getValues().stream().filter(t -> t.getDirection() == DirectionType.OUT).collect(Collectors.toList())).getValues())
        .addType(stateBuilder.build());
    return flowBuilder.build();
  }

  @Override
  public FlMethodSpec visitTask(FlowTaskNode node) {
    List<MethodSpec> value = new ArrayList<>();
    // figure out ref
    if (!node.getRef().isEmpty()) {
      TaskRef ref = node.getRef().get();
      TaskRefNaming type = naming.fl().ref(ref);
      value.add(MethodSpec.methodBuilder(JavaSpecUtil.getMethodName(node.getId()))
          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
          .returns(node.getLoop().isPresent() ? ParameterizedTypeName.get(ClassName.get(List.class), type.getReturnType()) : type.getReturnType())
          .build());
    }
    FlMethodSpec children = visitTaskPointer(node.getNext());
    value.addAll(children.getValue());
    return ImmutableFlMethodSpec.builder().addAllValue(value).build();
  }

  @Override
  public FlMethodSpec visitTaskPointer(FlowTaskPointer node) {
    if (node instanceof ThenPointer) {
      ThenPointer then = (ThenPointer) node;
      return visitTask(then.getTask().get());
    } else if (node instanceof WhenThenPointer) {
      List<MethodSpec> values = new ArrayList<>();
      WhenThenPointer whenThen = (WhenThenPointer) node;
      for (WhenThen c : whenThen.getValues()) {
        if (!c.getWhen().isEmpty()) {
          TypeSpec typeSpec = new EnInterfaceVisitor(typeNameResolver).visitExpressionBody(c.getWhen().get());
        }
        values.addAll(visitTaskPointer(c.getThen()).getValue());
      }
      return ImmutableFlMethodSpec.builder().value(values).build();
    }
    return ImmutableFlMethodSpec.builder().build();
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
