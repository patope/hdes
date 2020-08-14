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

import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.compiler.spi.naming.Namings.SwitchNaming;
import io.resys.hdes.executor.api.HdesExecutable.HdesExecution;
import io.resys.hdes.executor.api.HdesExecutable.Switch;
import io.resys.hdes.executor.api.SwitchMeta;

public class JavaSwitchNaming implements SwitchNaming {
  private final JavaNaming parent;

  public JavaSwitchNaming(JavaNaming parent) {
    super();
    this.parent = parent;
  }
  @Override
  public String pkg(FlowBody body) {
    return parent.fl().pkg(body);
  }
  @Override
  public ClassName gate(FlowBody node, FlowTaskNode pointer) {
    ClassName api = api(node, pointer);
    return ClassName.get(api.packageName() + "." + api.simpleName(), JavaSpecUtil.capitalize(pointer.getId()) + "Gate");
  }
  @Override
  public ClassName api(FlowBody node, FlowTaskNode pointer) {
    return ClassName.get(pkg(node), node.getId().getValue() + pointer.getId());
  }
  @Override
  public ClassName inputValue(FlowBody node, FlowTaskNode pointer) {
    ClassName api = api(node, pointer);
    return ClassName.get(api.canonicalName(), api.simpleName() + "In");
  }
  @Override
  public ClassName outputValue(FlowBody node, FlowTaskNode pointer) {
    ClassName api = api(node, pointer);
    return ClassName.get(api.canonicalName(), api.simpleName() + "Out");
  }
  @Override
  public ClassName impl(FlowBody node, FlowTaskNode pointer) {
    ClassName api = api(node, pointer);
    return ClassName.get(api.packageName(), api.simpleName() + "Gen");
  }
  @Override
  public ParameterizedTypeName execution(FlowBody node, FlowTaskNode pointer) {
    ClassName output = outputValue(node, pointer);
    ClassName input = inputValue(node, pointer);
    return ParameterizedTypeName.get(ClassName.get(HdesExecution.class), input, ClassName.get(SwitchMeta.class), output);
  }
  @Override
  public ParameterizedTypeName executable(FlowBody node, FlowTaskNode pointer) {
    TypeName returnType = outputValue(node, pointer);
    return ParameterizedTypeName.get(ClassName.get(Switch.class), inputValue(node, pointer), returnType);
  }
}
