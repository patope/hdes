package io.resys.hdes.compiler.spi.naming;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import io.resys.hdes.ast.api.nodes.AstNode.BodyNode;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarTypeDefNode;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.compiler.spi.java.visitors.JavaSpecUtil;
import io.resys.hdes.compiler.spi.naming.Namings.FormulaNaming;
import io.resys.hdes.executor.api.FormulaMeta;
import io.resys.hdes.executor.api.HdesExecutable.Execution;
import io.resys.hdes.executor.api.HdesExecutable.Formula;

public class JavaFormulaNaming implements FormulaNaming {
  private final JavaNaming parent;

  public JavaFormulaNaming(JavaNaming parent) {
    super();
    this.parent = parent;
  }

  @Override
  public String pkg(BodyNode body) {
    if(body instanceof FlowBody) {
      return parent.fl().pkg((FlowBody) body);
    } else if(body instanceof DecisionTableBody) {
      return parent.dt().pkg((DecisionTableBody) body);
    }
    throw new IllegalArgumentException("Formula naming not implemented for: " + body + "!");
  }
  
  @Override
  public ParameterizedTypeName execution(BodyNode body, ScalarTypeDefNode pointer) {
    ClassName outputName = outputValue(body, pointer);
    ParameterizedTypeName returnType = ParameterizedTypeName
        .get(ClassName.get(Execution.class), ClassName.get(FormulaMeta.class), outputName);
    return returnType;
  } 
  

  @Override
  public ClassName api(BodyNode node, ScalarTypeDefNode pointer) {
    return ClassName.get(pkg(node), node.getId().getValue() + JavaSpecUtil.capitalize(pointer.getName()) + "Formula");
  }

  @Override
  public ClassName impl(BodyNode node, ScalarTypeDefNode pointer) {
    return ClassName.get(pkg(node), node.getId().getValue() + JavaSpecUtil.capitalize(pointer.getName()) + "FormulaGen");
  }
  
  @Override
  public ParameterizedTypeName executable(BodyNode node, ScalarTypeDefNode pointer) {
    TypeName returnType = outputValue(node, pointer);
    return ParameterizedTypeName.get(ClassName.get(Formula.class), inputValue(node, pointer), returnType);
  }

  @Override
  public ClassName inputValue(BodyNode node, ScalarTypeDefNode pointer) {
    ClassName api = api(node, pointer);
    return ClassName.get(api.canonicalName(), api.simpleName() + JavaSpecUtil.capitalize(pointer.getName()) + "In");
  }

  @Override
  public ClassName outputValue(BodyNode node, ScalarTypeDefNode pointer) {
    ClassName api = api(node, pointer);
    return ClassName.get(api.canonicalName(), api.simpleName() + JavaSpecUtil.capitalize(pointer.getName()) + "Out");
  }
}