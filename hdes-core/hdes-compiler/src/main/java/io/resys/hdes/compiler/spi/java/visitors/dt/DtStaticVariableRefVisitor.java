package io.resys.hdes.compiler.spi.java.visitors.dt;

import java.util.List;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicy;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyMatrix;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.MatrixRow;
import io.resys.hdes.compiler.spi.java.visitors.dt.DtJavaSpec.DtTypesSpec;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.DecisionTableMeta.DecisionTableStaticValue;

public class DtStaticVariableRefVisitor extends DtTemplateVisitor<DtJavaSpec, TypeSpec> {

  private final Namings naming;
  private DecisionTableBody body;

  public DtStaticVariableRefVisitor(Namings naming) {
    super();
    this.naming = naming;
  }
  
  @Override
  public TypeSpec visitDecisionTableBody(DecisionTableBody node) {
    this.body = node;
    return visitHitPolicy(node.getHitPolicy()).getValues().get(0);
  }

  @Override
  public DtTypesSpec visitHitPolicyMatrix(HitPolicyMatrix node) {
    Class<?> type = JavaSpecUtil.type(node.getToType());
    ParameterizedTypeName returnType = ParameterizedTypeName.get(List.class, type);
    
    ParameterizedTypeName superinterface = ParameterizedTypeName.get(ClassName.get(DecisionTableStaticValue.class), returnType);
    TypeSpec.Builder builder = JavaSpecUtil.immutableSpec(naming.dt().staticValue(body)).addSuperinterface(superinterface);
    
    for(MatrixRow row : node.getRows()) {
      String headerName = row.getTypeName().getValue();    
      builder.addMethod(MethodSpec.methodBuilder(JavaSpecUtil.methodName(headerName))
          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
          .returns(returnType).build());
    }
    
    return ImmutableDtTypesSpec.builder().addValues(builder.build()).build();
  }
  
  @Override
  public DtTypesSpec visitHitPolicyAll(HitPolicyAll node) {
    ClassName output = naming.dt().outputValueMono(body);
    ParameterizedTypeName superinterface = ParameterizedTypeName.get(ClassName.get(DecisionTableStaticValue.class), output);
    
    return ImmutableDtTypesSpec.builder().addValues(
        JavaSpecUtil.immutableSpec(naming.dt().staticValue(body)).addSuperinterface(superinterface).build()
        ).build();
  }
  
  @Override
  public DtTypesSpec visitHitPolicyFirst(HitPolicyFirst node) {
    ClassName output = naming.dt().outputValueMono(body);
    ParameterizedTypeName superinterface = ParameterizedTypeName.get(ClassName.get(DecisionTableStaticValue.class), output);
    
    return ImmutableDtTypesSpec.builder().addValues(
        JavaSpecUtil.immutableSpec(naming.dt().staticValue(body)).addSuperinterface(superinterface).build()
        ).build();
  }
  
  private DtTypesSpec visitHitPolicy(HitPolicy node) {
    if (node instanceof HitPolicyAll) {
      return visitHitPolicyAll((HitPolicyAll) node);
    } else if (node instanceof HitPolicyMatrix) {
      return visitHitPolicyMatrix((HitPolicyMatrix) node);
    }
    return visitHitPolicyFirst((HitPolicyFirst) node);
  }
}
