package io.resys.hdes.compiler.spi.java.fl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
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
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.HdesExecutable.ExecutionStatus;
import io.resys.hdes.executor.api.HdesExecutable.SourceType;
import io.resys.hdes.executor.api.ImmutableExecution;
import io.resys.hdes.executor.api.ImmutableSwitchMeta;
import io.resys.hdes.executor.api.SwitchMeta;

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
      final ClassName outputType = namings.sw().outputValue(body, task);
      final ClassName immutableOutputName = JavaSpecUtil.immutable(outputType);
      final ClassName gate = namings.sw().gate(body, task);
      
      CodeBlock.Builder execution = CodeBlock.builder()
        .addStatement("long start = System.currentTimeMillis()")
        .addStatement("$T.Builder result = $T.builder()", immutableOutputName, immutableOutputName).add("\r\n");
      
      for(WhenThen whenThen : pointer.getValues()) {
        
        if(whenThen.getWhen().isPresent()) {
          EnScalarCodeSpec scalar = ExpressionSpec.builder().parent(body).envir(namings.ast()).build(whenThen.getWhen().get());
          
          execution
          .beginControlFlow("if($L)", scalar.getValue())
          .addStatement("result.gate($T.$L)", gate, FlSwitchApiSpec.getGateName(whenThen.getThen()))
          .endControlFlow();
          
          // if not boolean then null check or Optional.isPresent ?
        } else if(pointer.getValues().size() > 1) {
          execution
          .beginControlFlow("else")
          .addStatement("result.gate($T.$L)", gate, FlSwitchApiSpec.getGateName(whenThen.getThen()))
          .endControlFlow();
          
          break;
        }
      }
      
      execution.add("\r\n")
      .addStatement("$T output = result.build()", outputType)
      .addStatement("long end = System.currentTimeMillis()")
      .add("$T metaWrapper = $T.builder()", ImmutableSwitchMeta.class, ImmutableSwitchMeta.class)
      .add("\r\n  ").add(".id($S).status($T.COMPLETED) ", task.getId(), ExecutionStatus.class)
      .add("\r\n  ").addStatement(".start(start).end(end).time(end - start).build()")
      .add("\r\n")
      
      .addStatement("$T.Builder<$T, $T> resultWrapper = $T.builder()", ImmutableExecution.class, SwitchMeta.class, outputType, ImmutableExecution.class)
      .addStatement("return resultWrapper.meta(metaWrapper).value(output).build()")
      .build();
    
      
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
