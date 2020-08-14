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

import io.resys.hdes.ast.api.nodes.AstNode.Body;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.compiler.spi.naming.Namings.FormulaNaming;
import io.resys.hdes.executor.api.FormulaMeta;
import io.resys.hdes.executor.api.HdesExecutable.Formula;
import io.resys.hdes.executor.api.HdesExecutable.HdesExecution;
import io.resys.hdes.executor.spi.HdesExecutableTemplate;

public class JavaFormulaNaming implements FormulaNaming {
  private final JavaNaming parent;

  public JavaFormulaNaming(JavaNaming parent) {
    super();
    this.parent = parent;
  }

  @Override
  public String pkg(Body body) {
    if(body instanceof FlowBody) {
      return parent.fl().pkg((FlowBody) body);
    } else if(body instanceof DecisionTableBody) {
      return parent.dt().pkg((DecisionTableBody) body);
    }
    throw new IllegalArgumentException("Formula naming not implemented for: " + body + "!");
  }
  
  @Override
  public ParameterizedTypeName execution(Body body, ScalarDef pointer) {
    ClassName output = outputValue(body, pointer);
    ClassName input = inputValue(body, pointer);
    return ParameterizedTypeName
        .get(ClassName.get(HdesExecution.class), input, ClassName.get(FormulaMeta.class), output);
  } 
  
  @Override
  public ParameterizedTypeName template(Body body, ScalarDef pointer) {
    ClassName output = outputValue(body, pointer);
    ClassName input = inputValue(body, pointer);
    return ParameterizedTypeName.get(ClassName.get(HdesExecutableTemplate.class), input, ClassName.get(FormulaMeta.class), output);
  }
  
  @Override
  public ClassName api(Body node, ScalarDef pointer) {
    return ClassName.get(pkg(node), node.getId().getValue() + JavaSpecUtil.capitalize(pointer.getName()) + "Formula");
  }

  @Override
  public ClassName impl(Body node, ScalarDef pointer) {
    return ClassName.get(pkg(node), node.getId().getValue() + JavaSpecUtil.capitalize(pointer.getName()) + "FormulaGen");
  }
  
  @Override
  public ParameterizedTypeName executable(Body node, ScalarDef pointer) {
    TypeName returnType = outputValue(node, pointer);
    return ParameterizedTypeName.get(ClassName.get(Formula.class), inputValue(node, pointer), returnType);
  }

  @Override
  public ClassName inputValue(Body node, ScalarDef pointer) {
    ClassName api = api(node, pointer);
    return ClassName.get(api.canonicalName(), api.simpleName() + JavaSpecUtil.capitalize(pointer.getName()) + "In");
  }

  @Override
  public ClassName outputValue(Body node, ScalarDef pointer) {
    ClassName api = api(node, pointer);
    return ClassName.get(api.canonicalName(), api.simpleName() + JavaSpecUtil.capitalize(pointer.getName()) + "Out");
  }
}
