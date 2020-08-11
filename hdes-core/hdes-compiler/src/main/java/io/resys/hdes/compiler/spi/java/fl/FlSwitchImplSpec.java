package io.resys.hdes.compiler.spi.java.fl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.java.en.ExpressionSpec;
import io.resys.hdes.compiler.spi.java.en.ExpressionVisitor.EnScalarCodeSpec;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.HdesExecutable.SourceType;

public class FlSwitchImplSpec {
  
  public static Builder builder(Namings namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }

  public static class Builder {
    private final AnnotationSpec annotationSpec = AnnotationSpec.builder(javax.annotation.processing.Generated.class).addMember("value", "$S", FlSwitchImplSpec.class.getCanonicalName()).build();
    private final MethodSpec sourceType = MethodSpec.methodBuilder("getSourceType")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(SourceType.class)
        .addStatement("return $T.$L", SourceType.class, SourceType.SW)
        .build();
    
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
    
    private TypeSpec whenThen(FlowTaskNode task, WhenThenPointer pointer) {
      CodeBlock.Builder execution = CodeBlock.builder().addStatement("return null");
      
      for(WhenThen whenThen : pointer.getValues()) {
        
        if(whenThen.getWhen().isPresent()) {
          EnScalarCodeSpec scalar = ExpressionSpec.builder().parent(body).envir(namings.ast()).build(whenThen.getWhen().get());
          
          System.out.println(scalar);
          
          // if not boolean then null check or Optional.isPresent ?
          
        }
      }
      return TypeSpec.classBuilder(namings.sw().impl(body, task))
          .addModifiers(Modifier.PUBLIC)
          .addSuperinterface(namings.sw().api(body, task))
          .addJavadoc(body.getDescription().orElse(""))
          .addAnnotation(annotationSpec)
          .addMethod(sourceType)
          .addMethod(MethodSpec.methodBuilder("apply")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(ParameterSpec.builder(namings.sw().inputValue(body, task), "input").build())
              .returns(namings.sw().execution(body, task))
              .addCode(execution.build())
              .build())
          .build();
    }
    
    public List<TypeSpec> build() {
      Assertions.notNull(namings, () -> "namings must be defined!");
      Assertions.notNull(body, () -> "body must be defined!");
      return switches(body.getTask());
    }
  }
}
