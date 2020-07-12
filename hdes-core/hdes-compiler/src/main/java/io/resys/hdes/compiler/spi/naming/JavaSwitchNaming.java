package io.resys.hdes.compiler.spi.naming;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import io.resys.hdes.ast.api.nodes.AstNode.ObjectTypeDefNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.compiler.spi.naming.Namings.SwitchNaming;
import io.resys.hdes.executor.api.HdesExecutable.Switch;

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
    return ClassName.get(api.packageName() + "." + api.simpleName(), "Gate");
  }
  @Override
  public ClassName api(FlowBody node, FlowTaskNode pointer) {
    return ClassName.get(pkg(node), node.getId().getValue() + pointer.getId() + "Switch");
  }
  @Override
  public ParameterizedTypeName executable(FlowBody node, FlowTaskNode pointer) {
    TypeName returnType = outputValue(node, pointer);
    return ParameterizedTypeName.get(ClassName.get(Switch.class), inputValue(node, pointer), returnType);
  }
  @Override
  public ClassName inputValue(FlowBody node, FlowTaskNode pointer) {
    ClassName api = api(node, pointer);
    return ClassName.get(api.canonicalName(), pointer.getId() + "In");
  }
  @Override
  public ClassName outputValue(FlowBody node, FlowTaskNode pointer) {
    ClassName api = api(node, pointer);
    return ClassName.get(api.canonicalName(), pointer.getId() + "Out");
  }
  @Override
  public ClassName inputValue(FlowBody node, FlowTaskNode pointer, ObjectTypeDefNode object) {
    ClassName api = api(node, pointer);
    return ClassName.get(api.canonicalName(), node.getId() + object.getName() + "In");
  }
}
