package io.resys.hdes.compiler.spi.java.fl;

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

import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.TaskRef;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.compiler.spi.naming.Namings.TaskRefNaming;
import io.resys.hdes.executor.api.FlowMetaValue;
import io.resys.hdes.executor.api.FlowMetaValue.FlowTaskMetaFlux;
import io.resys.hdes.executor.api.FlowMetaValue.FlowTaskMetaMono;
import io.resys.hdes.executor.api.SwitchMeta;

public class FlStateSpec {

  public static Builder builder(Namings namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }

  public static class Builder {
    private final AnnotationSpec nullable = AnnotationSpec.builder(Nullable.class).build();
    private final Namings namings;
    private FlowBody body;

    private Builder(Namings namings) {
      super();
      this.namings = namings;
    }

    public Builder body(FlowBody body) {
      this.body = body;
      return this;
    }

    private List<MethodSpec> state(Optional<FlowTaskNode> start) {
      if (start.isEmpty()) {
        return Collections.emptyList();
      }

      List<MethodSpec> result = new ArrayList<>();      
      if(!start.get().getRef().isEmpty()) {
        TaskRef ref = start.get().getRef().get();
        TaskRefNaming refName = namings.fl().ref(body, ref);
        Assertions.notNull(namings.ast().getByAstId(ref.getValue()), () -> "Reference can't be null!");
        ClassName taskSuperinterface = ClassName.get(start.get().getLoop().isPresent() ? FlowTaskMetaFlux.class : FlowTaskMetaMono.class);

        final MethodSpec methodSpec = MethodSpec.methodBuilder(JavaSpecUtil.methodName(start.get().getId()))
            .addAnnotation(nullable)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(ParameterizedTypeName.get(taskSuperinterface, refName.getInputValue(), refName.getMeta(), refName.getOutputValue())).build();
        result.add(methodSpec);  
      }

      FlowTaskPointer pointer = start.get().getNext();
      if (pointer instanceof ThenPointer) {
        ThenPointer then = (ThenPointer) pointer;
        result.addAll(state(then.getTask()));
      } else if (pointer instanceof WhenThenPointer) {
        WhenThenPointer whenThen = (WhenThenPointer) pointer;

        result.add(MethodSpec.methodBuilder(JavaSpecUtil.methodName(start.get().getId()))
            .addAnnotation(nullable)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(ParameterizedTypeName.get(
                ClassName.get(FlowTaskMetaMono.class),
                namings.sw().inputValue(body, start.get()),
                ClassName.get(SwitchMeta.class), 
                namings.sw().outputValue(body, start.get()))).build());
        
        for (WhenThen c : whenThen.getValues()) {
          FlowTaskPointer nextPointer = c.getThen();
          if(nextPointer instanceof ThenPointer) {
            ThenPointer next = (ThenPointer) nextPointer;
            result.addAll(state(next.getTask()));  
          }
        }
      }
      
      return result;
    }
    
    public TypeSpec build() {
      Assertions.notNull(body, () -> "body must be defined!");
      
      final TypeSpec state = JavaSpecUtil.immutableSpec(namings.fl().stateValue(body))
          .addSuperinterface(FlowMetaValue.FlowState.class).addMethods(state(body.getTask())).build();

      return state;
    }
  }
}
