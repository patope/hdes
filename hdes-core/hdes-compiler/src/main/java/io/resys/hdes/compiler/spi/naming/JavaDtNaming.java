package io.resys.hdes.compiler.spi.naming;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.compiler.spi.naming.Namings.DtNaming;
import io.resys.hdes.executor.api.DecisionTableMeta;
import io.resys.hdes.executor.api.HdesExecutable.DecisionTable;
import io.resys.hdes.executor.api.HdesExecutable.Execution;

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
  public TypeName executable(DecisionTableBody node) {
    TypeName returnType = outputValueMono(node);
    return ParameterizedTypeName.get(ClassName.get(DecisionTable.class), inputValue(node), returnType);
  }

  @Override
  public ClassName impl(DecisionTableBody node) {
    return ClassName.get(pkg, node.getId().getValue() + "Gen");
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
    ClassName outputName = outputValueMono(body);
    ParameterizedTypeName returnType = ParameterizedTypeName
        .get(ClassName.get(Execution.class), ClassName.get(DecisionTableMeta.class), outputName);
    return returnType;
  } 
}

