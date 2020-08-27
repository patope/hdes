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
import java.util.Arrays;
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

import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.LoopPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.TaskRef;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.CompilerContext;
import io.resys.hdes.compiler.spi.CompilerContext.TaskRefNaming;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.executor.api.FlowMetaValue;
import io.resys.hdes.executor.api.FlowMetaValue.FlowTaskMono;
import io.resys.hdes.executor.api.FlowMetaValue.FlowTaskMulti;
import io.resys.hdes.executor.api.SwitchMeta;

public class FlStateSpec {

  public static Builder builder(CompilerContext namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }

  public static class Builder {
    private final AnnotationSpec nullable = AnnotationSpec.builder(Nullable.class).build();
    private final CompilerContext namings;
    private final List<TypeSpec> nested = new ArrayList<>();
    private FlowBody body;

    private Builder(CompilerContext namings) {
      super();
      this.namings = namings;
    }

    public Builder body(FlowBody body) {
      this.body = body;
      return this;
    }
    
    private Optional<MethodSpec> task(FlowTaskNode start) {
      if(!start.getRef().isEmpty()) {
        TaskRef ref = start.getRef().get();
        TaskRefNaming refName = namings.fl().ref(body, ref);
        Assertions.notNull(namings.ast().getByAstId(ref.getValue()), () -> "Reference can't be null!");
        
        ClassName taskSuperinterface = ClassName.get(refName.getArray() ? FlowTaskMulti.class : FlowTaskMono.class);
        final MethodSpec methodSpec = MethodSpec.methodBuilder(JavaSpecUtil.methodName(start.getId()))
            .addAnnotation(nullable)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(ParameterizedTypeName.get(taskSuperinterface, refName.getInputValue(), refName.getMeta(), refName.getOutputValue())).build();
        return Optional.of(methodSpec);
      }
      
      FlowTaskPointer pointer = start.getNext();
      if (pointer instanceof WhenThenPointer) {
        final MethodSpec methodSpec = MethodSpec.methodBuilder(JavaSpecUtil.methodName(start.getId()))
            .addAnnotation(nullable)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(ParameterizedTypeName.get(
                ClassName.get(FlowTaskMono.class),
                namings.sw().inputValue(body, start),
                ClassName.get(SwitchMeta.class), 
                namings.sw().outputValue(body, start))).build();
        return Optional.of(methodSpec);
      }
      
      return Optional.empty();
    }
    
    private List<MethodSpec> state(Optional<FlowTaskNode> start) {
      if (start.isEmpty()) {
        return Collections.emptyList();
      }
      final FlowTaskPointer pointer = start.get().getNext();
      if(pointer instanceof LoopPointer) {
        return loop(start.get(), (LoopPointer) pointer);
      } else if (pointer instanceof ThenPointer) {
        return then(start.get(), (ThenPointer) pointer);
      } else if (pointer instanceof WhenThenPointer) {
        return whenThen(start.get(), (WhenThenPointer) pointer); 
      } else if (pointer instanceof EndPointer) {
        Optional<MethodSpec> result = task(start.get());
        return result.isEmpty() ? Collections.emptyList() : Arrays.asList(result.get());
      }
      return Collections.emptyList();
    }
    
    private List<MethodSpec> loop(FlowTaskNode task, LoopPointer loopPointer) {
      /* TODO nested type inside of loop
      {
        FlowTaskPointer nestedPointer = loopPointer.getInsidePointer();
        final List<MethodSpec> nestedMethods = new ArrayList<>();
        if (nestedPointer instanceof ThenPointer) {
          nestedMethods.addAll(then(task, (ThenPointer) nestedPointer));
        } else if (nestedPointer instanceof WhenThenPointer) {
          nestedMethods.addAll(whenThen(task, (WhenThenPointer) nestedPointer)); 
        }
        task(task).ifPresent(m -> nestedMethods.add(m));
        nested.add(JavaSpecUtil.immutableSpec(namings.fl().stateValue(body, task))
            .addSuperinterface(FlowMetaValue.FlowState.class)
            .addMethods(nestedMethods)
            .build());
      }*/
      
      
      final List<MethodSpec> result = new ArrayList<>();
      
      // After loop
      FlowTaskPointer nextPointer = loopPointer.getAfterPointer();
      if(nextPointer instanceof ThenPointer) {
        ThenPointer next = (ThenPointer) nextPointer;
        result.addAll(state(next.getTask()));
      }
      
      /* TODO loop element type
      ClassName taskSuperinterface = ClassName.get(FlowTaskFlux.class);
      final MethodSpec methodSpec = MethodSpec.methodBuilder(JavaSpecUtil.methodName(start.getId()))
          .addAnnotation(nullable)
          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
          .returns(ParameterizedTypeName.get(taskSuperinterface, refName.getInputValue(), refName.getMeta(), refName.getOutputValue())).build();
      */
      return result;
    }

    private List<MethodSpec> then(FlowTaskNode task, ThenPointer then) {
      final List<MethodSpec> result = new ArrayList<>();
      task(task).ifPresent(m -> result.add(m));
      result.addAll(state(then.getTask()));
      return result;
    }
    
    private List<MethodSpec> whenThen(FlowTaskNode task, WhenThenPointer whenThen) {
      final List<MethodSpec> result = new ArrayList<>();
      task(task).ifPresent(m -> result.add(m));
      
      for (WhenThen c : whenThen.getValues()) {
        FlowTaskPointer nextPointer = c.getThen();
        if(nextPointer instanceof ThenPointer) {
          ThenPointer next = (ThenPointer) nextPointer;
          
          
          result.addAll(state(next.getTask()));  
        }
      }
      return result;
    }
    
    public List<TypeSpec> build() {
      Assertions.notNull(body, () -> "body must be defined!");
      
      List<TypeSpec> result = new ArrayList<>(nested);
      nested.clear();
      
      result.add(JavaSpecUtil.immutableSpec(namings.fl().stateValue(body))
          .addSuperinterface(FlowMetaValue.FlowState.class)
          .addMethods(state(body.getTask()))
          .build());
      
      return result;
    }
  }
}
