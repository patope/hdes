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

import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.compiler.spi.naming.Namings.DtNaming;
import io.resys.hdes.executor.api.DecisionTableMeta;
import io.resys.hdes.executor.api.HdesExecutable.DecisionTable;
import io.resys.hdes.executor.api.HdesExecutable.HdesExecution;
import io.resys.hdes.executor.spi.HdesExecutableTemplate;

public class JavaDtNaming implements DtNaming {
  private final JavaNaming parent;
  private final String pkg;

  public JavaDtNaming(JavaNaming parent, String pkg) {
    super();
    this.parent = parent;
    this.pkg = pkg;
  }

  @Override
  public String pkg(DecisionTableBody node) {
    return pkg;
  }

  @Override
  public ClassName impl(DecisionTableBody node) {
    return ClassName.get(pkg, node.getId().getValue() + "Gen");
  }

  @Override
  public ParameterizedTypeName template(DecisionTableBody body) {
    ClassName output = outputValueMono(body);
    ClassName input = inputValue(body);
    return ParameterizedTypeName.get(ClassName.get(HdesExecutableTemplate.class), input, ClassName.get(DecisionTableMeta.class), output);
  }
  
  @Override
  public ClassName staticValue(DecisionTableBody node) {
    ClassName api = api(node);
    return ClassName.get(api.canonicalName(), node.getId().getValue() + "Static");
  }
  
  @Override
  public ClassName inputValue(DecisionTableBody node) {
    return inputValue(node.getId().getValue());
  }

  @Override
  public ClassName outputValueMono(DecisionTableBody node) {
    return outputValue(node.getId().getValue());
  }

  @Override
  public ClassName api(DecisionTableBody node) {
    return api(node.getId().getValue());
  }
  
  private ClassName api(String node) {
    return ClassName.get(pkg, node);
  }
  private ClassName inputValue(String node) {
    ClassName api = api(node);
    return ClassName.get(api.canonicalName(), node + "In");
  }
  private ClassName outputValue(String node) {
    ClassName api = api(node);
    return ClassName.get(api.canonicalName(), node + "Out");
  }

  @Override
  public ClassName outputValueFlux(DecisionTableBody node) {
    if (node.getHitPolicy() instanceof HitPolicyAll) {
      ClassName api = api(node);
      return ClassName.get(api.canonicalName(), node.getId().getValue() + "OutputEntry");        
    }
    return outputValueMono(node);
  }

  @Override
  public ParameterizedTypeName execution(DecisionTableBody body) {
    ClassName output = outputValueMono(body);
    ClassName input = inputValue(body);
    return ParameterizedTypeName.get(ClassName.get(HdesExecution.class), input, ClassName.get(DecisionTableMeta.class), output);
  }
  @Override
  public TypeName executable(DecisionTableBody node) {
    TypeName output = outputValueMono(node);
    return ParameterizedTypeName.get(ClassName.get(DecisionTable.class), inputValue(node), output);
  }
}

