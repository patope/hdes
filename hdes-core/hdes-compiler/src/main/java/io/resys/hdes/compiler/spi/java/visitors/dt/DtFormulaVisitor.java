package io.resys.hdes.compiler.spi.java.visitors.dt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.immutables.value.Value.Immutable;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.Headers;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.compiler.spi.java.visitors.JavaSpecUtil;
import io.resys.hdes.compiler.spi.java.visitors.dt.DtJavaSpec.DtTypesSpec;
import io.resys.hdes.compiler.spi.java.visitors.fl.FlSwitchVisitor;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.HdesExecutable;

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
    Function<ClassName, TypeSpec.Builder> from = (name) -> {
      ClassName jsonType = JavaSpecUtil.immutable(name);
      return TypeSpec
          .interfaceBuilder(name)
          .addAnnotation(Immutable.class)
          .addAnnotation(AnnotationSpec.builder(ClassName.get("com.fasterxml.jackson.databind.annotation", "JsonSerialize")).addMember("as", "$T.class", jsonType).build())
          .addAnnotation(AnnotationSpec.builder(ClassName.get("com.fasterxml.jackson.databind.annotation", "JsonDeserialize")).addMember("as", "$T.class", jsonType).build())
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    };
       
    
    for(TypeDefNode header : node.getValues()) {
      MethodSpec method = visitHeader(header).getValue();
      if(header.getDirection() == DirectionType.IN) {
        inputBuilder.addMethod(method);
      } else {
        outputBuilder.addMethod(method);        
      }
    }
    
    List<TypeSpec> values = new ArrayList<>();
    values.add(inputBuilder.build());
    values.add(outputBuilder.build());
    
    boolean isCollection = body.getHitPolicy() instanceof HitPolicyAll;
    if (isCollection) {
      ParameterizedTypeName returnType = ParameterizedTypeName.get(ClassName.get(Collection.class), naming.dt().outputValueFlux(body));
      TypeSpec collectionOutput = from.apply(naming.dt().outputValueMono(body))
        .addSuperinterface(HdesExecutable.OutputValue.class)
        .addMethod(MethodSpec.methodBuilder(JavaSpecUtil.methodName("values"))
          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
          .returns(returnType)
          .build()).build();
      values.add(collectionOutput);
    }
    
    return ImmutableDtTypesSpec.builder().addAllValues(values).build();
  }

  @Override
  public DtTypesSpec visitHeader(TypeDefNode node) {
    if(TypeDefNode instanceof ) {
      
    }
    TypeSpec api = TypeSpec.interfaceBuilder(naming.fr().api(body, parent))
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(javax.annotation.processing.Generated.class)
            .addMember("value", "$S", FlSwitchVisitor.class.getCanonicalName())
            .build())
        .addSuperinterface(naming.sw().executable(body, parent))
        .addTypes(visitInputs(switchTypes, parent).getValues())
        .addTypes(visitOutputs(parent).getValues())
        .build();
    
    return ImmutableDtTypesSpec.builder().addValues(api).build();
  }
}
