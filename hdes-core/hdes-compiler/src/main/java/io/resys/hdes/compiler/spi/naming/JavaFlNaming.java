package io.resys.hdes.compiler.spi.naming;

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
import io.resys.hdes.ast.api.nodes.AstNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.TaskRef;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.naming.Namings.FlNaming;
import io.resys.hdes.compiler.spi.naming.Namings.TaskRefNaming;
import io.resys.hdes.executor.api.FlowMeta;
import io.resys.hdes.executor.api.HdesExecutable.Execution;
import io.resys.hdes.executor.api.HdesExecutable.Flow;

public class JavaFlNaming implements FlNaming {
  private final JavaNaming parent;
  private final AstEnvir envir;
  private final String pkg;

  public JavaFlNaming(JavaNaming parent, AstEnvir envir, String pkg) {
    super();
    this.parent = parent;
    this.envir = envir;
    this.pkg = pkg;
  }

  @Override
  public String pkg(FlowBody node) {
    return pkg + "." + node.getId().getValue().toLowerCase();
  }

  @Override
  public ClassName api(FlowBody node) {
    return ClassName.get(pkg, node.getId().getValue());
  }

  @Override
  public ClassName impl(FlowBody node) {
    return ClassName.get(pkg, node.getId().getValue() + "Gen");
  }
  
  @Override
  public ClassName state(FlowBody node) {
    return ClassName.get(pkg, node.getId().getValue() + "State");
  }

  @Override
  public ClassName inputValue(FlowBody node) {
    return ClassName.get(api(node).canonicalName(), node.getId().getValue() + "In");
  }

  @Override
  public ClassName outputValue(FlowBody node) {
    return ClassName.get(api(node).canonicalName(), node.getId().getValue() + "Out");
  }

  @Override
  public TypeName executable(FlowBody node) {
    return ParameterizedTypeName.get(ClassName.get(Flow.class), inputValue(node), outputValue(node));
  }

  @Override
  public ParameterizedTypeName execution(FlowBody body) {
    ClassName outputName = outputValue(body);
    ParameterizedTypeName returnType = ParameterizedTypeName
        .get(ClassName.get(Execution.class), ClassName.get(FlowMeta.class), outputName);
    return returnType;
  } 
  
  @Override
  public ClassName inputValue(FlowBody node, ObjectDef object) {
    return ClassName.get(api(node).simpleName(), node.getId().getValue() + JavaSpecUtil.capitalize(object.getName()) + "In");
  }
  
  @Override
  public ClassName outputValue(FlowBody node, ObjectDef object) {
    return ClassName.get(api(node).simpleName(), node.getId().getValue() + JavaSpecUtil.capitalize(object.getName()) + "Out");
  }

  @Override
  public TaskRefNaming ref(TaskRef node) {
    switch (node.getType()) {
    case DECISION_TABLE: {
      String typeName = node.getValue();
      DecisionTableBody body = (DecisionTableBody) envir.getByAstId(typeName);
      return ImmutableTaskRefNaming.builder()
          .type(parent.dt().api(body))
          .returnType(parent.dt().execution(body))
          .build(); 
        
    }
    //case FLOW_TASK: return ClassName.get(parent, node.getValue());
    //case MANUAL_TASK: return ClassName.get(parent, node.getValue());
    //case SERVICE_TASK: return ClassName.get(parent, node.getValue());
    default: throw new HdesCompilerException(HdesCompilerException.builder().unknownFlTaskRef(node));
    }
  }

  /*
  @Override
  public ClassName taskState(FlowBody body, FlowTaskNode task) {
    return ClassName.get(interfaze(body).canonicalName(), body.getId().getValue() + task.getId());
  }
  @Override
  public String refMethod(TaskRef ref) {
    return JavaSpecUtil.decapitalize(ref(ref).simpleName());
  }

  @Override
  public ClassName refInput(TaskRef node) {
    switch (node.getType()) {
    case DECISION_TABLE: return parent.dt().input(node.getValue());
    //case FLOW_TASK: return ClassName.get(parent, node.getValue());
    //case MANUAL_TASK: return ClassName.get(parent, node.getValue());
    //case SERVICE_TASK: return ClassName.get(parent, node.getValue());
    default: throw new HdesCompilerException(HdesCompilerException.builder().unknownFlTaskRef(node));
    }
  }

  @Override
  public ClassName refOutput(TaskRef node) {
    switch (node.getType()) {
    case DECISION_TABLE: return parent.dt().output(node.getValue());
    //case FLOW_TASK: return ClassName.get(parent, node.getValue());
    //case MANUAL_TASK: return ClassName.get(parent, node.getValue());
    //case SERVICE_TASK: return ClassName.get(parent, node.getValue());
    default: throw new HdesCompilerException(HdesCompilerException.builder().unknownFlTaskRef(node));
    }
  }
  */
}
