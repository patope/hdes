package io.resys.hdes.compiler.spi.java.fl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.HdesExecutable;

public class FlSwitchApiSpec {
  
  public static Builder builder(Namings namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }

  public static class Builder {
    private final Namings namings;
    private FlowBody body;

    private Builder(Namings namings) {
      super();
      this.namings = namings;
    }

    public Builder body(FlowBody body) {
      this.body = body;
      return this;
    }

    private TypeSpec gate(WhenThenPointer node, FlowTaskNode task) {
      ClassName gateTypeName = namings.sw().gate(body, task);
      TypeSpec.Builder gateEnum = TypeSpec.enumBuilder(gateTypeName);
      for (WhenThen c : node.getValues()) {
        gateEnum.addEnumConstant(getName(c.getThen()));
      }
      return gateEnum.addModifiers(Modifier.PUBLIC, Modifier.STATIC).build();  
    }
    
    private String getName(FlowTaskPointer pointer) {
      if(pointer instanceof ThenPointer) {
        return ((ThenPointer) pointer).getName();
      } else if(pointer instanceof EndPointer) {
        return ((EndPointer) pointer).getName();
      }
      return "Nested";
    }
    
    
    private List<TypeSpec> switches(Optional<FlowTaskNode> start) {
      if (start.isEmpty()) {
        return Collections.emptyList();
      }

      FlowTaskPointer pointer = start.get().getNext();
      if (pointer instanceof ThenPointer) {
        ThenPointer then = (ThenPointer) pointer;
        return switches(then.getTask());
        
      } else if (pointer instanceof WhenThenPointer) {

        final List<TypeSpec> result = new ArrayList<>();
        final WhenThenPointer whenThen = (WhenThenPointer) pointer;
        result.add(whenThen(start.get(), whenThen));
        
        for (WhenThen c : whenThen.getValues()) {
          FlowTaskPointer nextPointer = c.getThen();
          if(nextPointer instanceof ThenPointer) {
            ThenPointer next = (ThenPointer) nextPointer;
            result.addAll(switches(next.getTask()));  
          }
        }
        return result;
      }
      
      return Collections.emptyList();
    }
    
    private TypeSpec whenThen(FlowTaskNode task, WhenThenPointer whenThen) {      
      final AnnotationSpec annotationSpec = AnnotationSpec.builder(javax.annotation.processing.Generated.class).addMember("value", "$S", FlSwitchApiSpec.class.getCanonicalName()).build();
      
      final TypeSpec input = JavaSpecUtil
        .immutableSpec(namings.sw().inputValue(body, task))
        .addSuperinterface(HdesExecutable.InputValue.class)
        .build();
      
      final TypeSpec output = JavaSpecUtil
        .immutableSpec(namings.sw().outputValue(body, task))
        .addSuperinterface(HdesExecutable.OutputValue.class)
        .addMethod(MethodSpec.methodBuilder(JavaSpecUtil.methodName("gate"))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(namings.sw().gate(body, task)).build())
        .build();
      
      final TypeSpec gate = gate(whenThen, task);
      
      return TypeSpec.interfaceBuilder(namings.sw().api(body, task))
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(annotationSpec)
        .addSuperinterface(namings.sw().executable(body, task))
        .addType(gate).addType(input).addType(output)
        .build();
    }
    
    public List<TypeSpec> build() {
      Assertions.notNull(namings, () -> "namings must be defined!");
      Assertions.notNull(body, () -> "body must be defined!");
      return switches(body.getTask());
    }
  }
}
