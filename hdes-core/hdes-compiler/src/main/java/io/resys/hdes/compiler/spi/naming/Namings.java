package io.resys.hdes.compiler.spi.naming;

import org.immutables.value.Value;

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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import io.resys.hdes.ast.api.AstEnvir;
import io.resys.hdes.ast.api.nodes.AstNode.Body;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.TaskRef;

public interface Namings {
  
  AstEnvir ast();
  FlNaming fl();
  DtNaming dt();
  SwitchNaming sw();
  FormulaNaming fr();
  
  interface FormulaNaming {
    String pkg(Body body);
    ClassName api(Body node, ScalarDef pointer);
    ClassName impl(Body node, ScalarDef pointer);
    ParameterizedTypeName executable(Body node, ScalarDef pointer);
    ParameterizedTypeName execution(Body node, ScalarDef pointer);
    
    ClassName inputValue(Body node, ScalarDef pointer);
    ClassName outputValue(Body node, ScalarDef pointer);
  }
  
  interface SwitchNaming {
    String pkg(FlowBody body);
    ClassName api(FlowBody node, FlowTaskNode pointer);
    ParameterizedTypeName executable(FlowBody node, FlowTaskNode pointer);
    
    ClassName gate(FlowBody node, FlowTaskNode pointer);
    ClassName inputValue(FlowBody node, FlowTaskNode pointer);
    ClassName outputValue(FlowBody node, FlowTaskNode pointer);
  }
  
  interface DtNaming {
    String pkg(DecisionTableBody body);
    
    ClassName api(DecisionTableBody node);
    ClassName impl(DecisionTableBody node);
    TypeName executable(DecisionTableBody node);
    
    ParameterizedTypeName execution(DecisionTableBody node);
    
    ClassName staticValue(DecisionTableBody node);
    ClassName inputValue(DecisionTableBody node);
    ClassName outputValueMono(DecisionTableBody node);
    ClassName outputValueFlux(DecisionTableBody node);
    
    //ClassName inputSuperinterface(DecisionTableBody node);
    //ClassName outputSuperinterface(DecisionTableBody node);
  }
  
  interface FlNaming {
    String pkg(FlowBody body);
    
    ClassName api(FlowBody node);
    ClassName impl(FlowBody node);
    ClassName state(FlowBody body);
    
    TaskRefNaming ref(FlowBody node, TaskRef ref);
    ParameterizedTypeName execution(FlowBody body);
    TypeName executable(FlowBody node);
    
    ClassName inputValue(FlowBody node);
    ClassName inputValue(FlowBody node, ObjectDef object);
    
    ClassName outputValue(FlowBody node);
    ClassName outputValue(FlowBody node, ObjectDef object);
    
    
    /*
    ClassName ref(TaskRef ref);
    ClassName refInput(TaskRef ref);
    ClassName refOutput(TaskRef ref);
    String refMethod(TaskRef ref);
    ClassName taskState(FlowBody body, FlowTaskNode task);
    */
  }
  
  @Value.Immutable
  interface TaskRefNaming {
    ClassName getType();
    ParameterizedTypeName getReturnType();
  }
  
}
