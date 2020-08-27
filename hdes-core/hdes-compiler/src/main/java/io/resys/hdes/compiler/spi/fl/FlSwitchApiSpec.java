package io.resys.hdes.compiler.spi.fl;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.compiler.spi.CompilerContext;
import io.resys.hdes.compiler.spi.invocation.InvocationGetMethod;
import io.resys.hdes.compiler.spi.invocation.InvocationGetMethodFl;
import io.resys.hdes.executor.api.HdesExecutable;

public class FlSwitchApiSpec {
  
  public static Builder builder(CompilerContext namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }
  
  public static String getGateName(FlowTaskPointer pointer) {
    if(pointer instanceof ThenPointer) {
      return ((ThenPointer) pointer).getName();
    } else if(pointer instanceof EndPointer) {
      return ((EndPointer) pointer).getName();
    }
    return "Nested";
  }

  public static class Builder {
    final AnnotationSpec annotationSpec = AnnotationSpec.builder(javax.annotation.processing.Generated.class).addMember("value", "$S", FlSwitchApiSpec.class.getCanonicalName()).build();
    private final CompilerContext namings;
    private FlowBody body;

    private Builder(CompilerContext namings) {
      super();
      this.namings = namings;
    }

    public Builder body(FlowBody body) {
      this.body = body;
      return this;
    }

    private TypeSpec gate(WhenThenPointer node, FlowTaskNode task) {
      ClassName gateTypeName = namings.sw().gate(body, task);
      TypeSpec.Builder gateEnum = TypeSpec.enumBuilder(gateTypeName);
      for (WhenThen c : node.getValues()) {
        gateEnum.addEnumConstant(getGateName(c.getThen()));
      }
      return gateEnum.addModifiers(Modifier.PUBLIC, Modifier.STATIC).build();  
    }
    
    
    private List<TypeSpec> switches(Optional<FlowTaskNode> start) {
      if (start.isEmpty()) {
        return Collections.emptyList();
      }

      FlowTaskPointer pointer = start.get().getNext();
      if (pointer instanceof ThenPointer) {
        ThenPointer then = (ThenPointer) pointer;
        return switches(then.getTask());
        
      } else if (pointer instanceof WhenThenPointer) {

        final List<TypeSpec> result = new ArrayList<>();
        final WhenThenPointer whenThen = (WhenThenPointer) pointer;
        result.add(whenThen(start.get(), whenThen));
        
        for (WhenThen c : whenThen.getValues()) {
          FlowTaskPointer nextPointer = c.getThen();
          if(nextPointer instanceof ThenPointer) {
            ThenPointer next = (ThenPointer) nextPointer;
            result.addAll(switches(next.getTask()));  
          }
        }
        return result;
      }
      
      return Collections.emptyList();
    }
    
    private TypeSpec whenThen(FlowTaskNode task, WhenThenPointer whenThen) {      
      final TypeSpec input = JavaSpecUtil
        .immutableSpec(namings.sw().inputValue(body, task))
        .addSuperinterface(HdesExecutable.InputValue.class)
        .addMethod(MethodSpec.methodBuilder(JavaSpecUtil.methodName(InvocationGetMethodFl.ACCESS_STATE_VALUE))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(namings.fl().stateValue(body)).build())
        .addMethod(MethodSpec.methodBuilder(JavaSpecUtil.methodName(InvocationGetMethod.ACCESS_INPUT_VALUE))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(namings.fl().inputValue(body)).build())
        .build();
      
      final TypeSpec output = JavaSpecUtil
        .immutableSpec(namings.sw().outputValue(body, task))
        .addSuperinterface(HdesExecutable.OutputValue.class)
        .addMethod(MethodSpec.methodBuilder(JavaSpecUtil.methodName("gate"))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(namings.sw().gate(body, task)).build())
        .build();
      
      final TypeSpec gate = gate(whenThen, task);
      
      return TypeSpec.interfaceBuilder(namings.sw().api(body, task))
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(annotationSpec)
        .addSuperinterface(namings.sw().executable(body, task))
        .addType(gate).addType(input).addType(output)
        .build();
    }
    
    public List<TypeSpec> build() {
      Assertions.notNull(namings, () -> "namings must be defined!");
      Assertions.notNull(body, () -> "body must be defined!");
      return switches(body.getTask());
    }
  }
}
