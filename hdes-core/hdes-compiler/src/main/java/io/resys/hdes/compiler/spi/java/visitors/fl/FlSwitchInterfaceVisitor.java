package io.resys.hdes.compiler.spi.java.visitors.fl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import io.resys.hdes.ast.api.nodes.AstNode.ScalarType;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.NamingContext;
import io.resys.hdes.compiler.spi.java.visitors.JavaSpecUtil;
import io.resys.hdes.compiler.spi.java.visitors.en.EnInterfaceVisitor;
import io.resys.hdes.compiler.spi.java.visitors.fl.FlJavaSpec.FlHeaderSpec;
import io.resys.hdes.compiler.spi.java.visitors.fl.FlJavaSpec.FlTypesSpec;
import io.resys.hdes.executor.api.HdesExecutable;

public class FlSwitchInterfaceVisitor extends FlTemplateVisitor<FlJavaSpec, List<TypeSpec>> {
  private final NamingContext naming;
  private FlTypeNameResolver typeNames;
  private FlowBody body;

  public FlSwitchInterfaceVisitor(NamingContext naming) {
    super();
    this.naming = naming;
  }

  @Override
  public List<TypeSpec> visitBody(FlowBody node) {
    this.body = node;
    this.typeNames = new FlTypeNameResolver(node, naming.ast());
    
    List<TypeSpec> values = new ArrayList<>();
    if(node.getTask().isPresent()) {
      FlTypesSpec types = visitTask(node.getTask().get());
      values.addAll(types.getValues());
    }
    return values;
  }

  @Override
  public FlTypesSpec visitTask(FlowTaskNode node) {
    return ImmutableFlTypesSpec.builder()
        .values(visitTaskPointer(node, node.getNext()).getValues())
        .build();
  }

  @Override
  public FlTypesSpec visitTaskPointer(FlowTaskNode parent, FlowTaskPointer node) {
    if (node instanceof ThenPointer) {
      ThenPointer then = (ThenPointer) node;
      return visitTask(then.getTask().get());
    } else if (node instanceof WhenThenPointer) { 
     
      List<TypeSpec> values = new ArrayList<>();
      List<TypeDefNode> types = new ArrayList<>();
      
      for (WhenThen c : ((WhenThenPointer) node).getValues()) {
        
        // Collect types used
        if(c.getWhen().isPresent()) {
          types.addAll(new EnInterfaceVisitor(this.typeNames).visitExpressionBody(c.getWhen().get()));
        }
        
        // Collect all children
        values.addAll(visitTaskPointer(parent, c.getThen()).getValues());
      }
      
      TypeSpec.Builder builder = TypeSpec.interfaceBuilder(naming.sw().api(body, parent))
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation(AnnotationSpec.builder(javax.annotation.processing.Generated.class).addMember("value", "$S", FlSwitchInterfaceVisitor.class.getCanonicalName()).build())
          .addSuperinterface(naming.sw().executable(body, parent))
          .addTypes(visitInputs(types, parent).getValues())
          .addTypes(visitOutputs(parent).getValues());
      
      values.add(builder.build());
      return ImmutableFlTypesSpec.builder().values(values).build();
    }
    return ImmutableFlTypesSpec.builder().build();
  }

  private FlTypesSpec visitInputs(List<TypeDefNode> node, FlowTaskNode parent) {
    TypeSpec.Builder inputBuilder = TypeSpec
        .interfaceBuilder(naming.sw().inputValue(body, parent))
        .addSuperinterface(HdesExecutable.InputValue.class)
        .addAnnotation(Immutable.class)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    List<TypeSpec> nested = new ArrayList<>();
    for (TypeDefNode input : node) {
      FlHeaderSpec spec = visitTypeDef(input, parent);
      nested.addAll(spec.getChildren());
      inputBuilder.addMethod(spec.getValue());
    }
    return ImmutableFlTypesSpec.builder()
        .addValues(inputBuilder.build())
        .addAllValues(nested)
        .build();
  }
  
  public FlTypesSpec visitOutputs(FlowTaskNode parent) {
    TypeSpec.Builder outputBuilder = TypeSpec
        .interfaceBuilder(naming.sw().outputValue(body, parent))
        .addSuperinterface(HdesExecutable.OutputValue.class)
        .addAnnotation(Immutable.class)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    
    Class<?> returnType = JavaSpecUtil.type(ScalarType.STRING);
    MethodSpec method = MethodSpec.methodBuilder(JavaSpecUtil.getMethodName("gate"))
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .returns(ClassName.get(returnType))
        .build();
    return ImmutableFlTypesSpec.builder()
        .addValues(outputBuilder.addMethod(method).build())
        .build();
  }

  private FlHeaderSpec visitTypeDef(TypeDefNode node, FlowTaskNode parent) {
    if (node instanceof ScalarTypeDefNode) {
      return visitScalarDef((ScalarTypeDefNode) node, parent);
    } else if (node instanceof ArrayTypeDefNode) {
      return visitArrayDef((ArrayTypeDefNode) node, parent);
    } else if (node instanceof ObjectTypeDefNode) {
      return visitObjectDef((ObjectTypeDefNode) node, parent);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownFlInputRule(node));
  }


  private FlHeaderSpec visitScalarDef(ScalarTypeDefNode node, FlowTaskNode parent) {
    Class<?> returnType = JavaSpecUtil.type(node.getType());
    MethodSpec method = MethodSpec.methodBuilder(JavaSpecUtil.getMethodName(node.getName()))
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .returns(node.getRequired() ? ClassName.get(returnType) : ParameterizedTypeName.get(Optional.class, returnType))
        .build();
    return ImmutableFlHeaderSpec.builder().value(method).build();
  }

  private FlHeaderSpec visitArrayDef(ArrayTypeDefNode node, FlowTaskNode parent) {
    FlHeaderSpec childSpec = visitTypeDef(node.getValue(), parent);
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

  private FlHeaderSpec visitObjectDef(ObjectTypeDefNode node, FlowTaskNode parent) {
    ClassName typeName = naming.sw().inputValue(body, parent, node);
    TypeSpec.Builder objectBuilder = TypeSpec
        .interfaceBuilder(typeName)
        .addSuperinterface(node.getDirection() == DirectionType.IN ? HdesExecutable.InputValue.class : HdesExecutable.OutputValue.class)
        .addAnnotation(Immutable.class)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    List<TypeSpec> nested = new ArrayList<>();
    for (TypeDefNode input : node.getValues()) {
      FlHeaderSpec spec = visitTypeDef(input, parent);
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
