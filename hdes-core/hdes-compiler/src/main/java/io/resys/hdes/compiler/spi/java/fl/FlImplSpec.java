package io.resys.hdes.compiler.spi.java.fl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.TaskRef;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.java.dt.DtImplSpec;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.compiler.spi.naming.Namings.TaskRefNaming;
import io.resys.hdes.executor.api.FlowMeta;
import io.resys.hdes.executor.api.FlowMeta.FlowTaskMetaFlux;
import io.resys.hdes.executor.api.FlowMeta.FlowTaskMetaMono;
import io.resys.hdes.executor.api.HdesExecutable.ExecutionStatus;
import io.resys.hdes.executor.api.HdesExecutable.SourceType;
import io.resys.hdes.executor.api.HdesWhen;
import io.resys.hdes.executor.api.ImmutableExecution;
import io.resys.hdes.executor.api.ImmutableFlowMeta;

public class FlImplSpec {

  public static Builder builder(Namings namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }

  public static class Builder {
    private final List<AnnotationSpec> annotations = Arrays.asList(
        AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unused").build(),
        AnnotationSpec.builder(javax.annotation.processing.Generated.class).addMember("value", "$S", DtImplSpec.class.getCanonicalName()).build());
    private final MethodSpec sourceType = MethodSpec.methodBuilder("getSourceType")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(SourceType.class)
        .addStatement("return $T.$L", SourceType.class, SourceType.FL)
        .build();
    private final MethodSpec constructor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(HdesWhen.class, "when").build())
        .addStatement("this.when = when")
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
    
    private List<MethodSpec> state(Optional<FlowTaskNode> start) {
      if (start.isEmpty()) {
        return Collections.emptyList();
      }

      List<MethodSpec> result = new ArrayList<>();      
      if(!start.get().getRef().isEmpty()) {
        TaskRef ref = start.get().getRef().get();
        TaskRefNaming refName = namings.fl().ref(body, ref);
        Assertions.notNull(namings.ast().getByAstId(ref.getValue()), () -> "Reference can't be null!");

        Class<?> taskSuperinterface = start.get().getLoop().isPresent() ? FlowTaskMetaFlux.class : FlowTaskMetaMono.class;
        final MethodSpec methodSpec = MethodSpec.methodBuilder(JavaSpecUtil.methodName(start.get().getId()))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(ParameterizedTypeName.get(
                ClassName.get(taskSuperinterface), refName.getMeta(), refName.getOutputValue())).build();
        result.add(methodSpec);  
      }

      FlowTaskPointer pointer = start.get().getNext();
      if (pointer instanceof ThenPointer) {
        ThenPointer then = (ThenPointer) pointer;
        result.addAll(state(then.getTask()));
      } else if (pointer instanceof WhenThenPointer) {
        WhenThenPointer whenThen = (WhenThenPointer) pointer;

        ClassName type = namings.sw().api(body, start.get());
        result.add(MethodSpec.methodBuilder(JavaSpecUtil.methodName(type.simpleName()))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).returns(type).build());
        
        for (WhenThen c : whenThen.getValues()) {
          FlowTaskPointer nextPointer = c.getThen();
          if(nextPointer instanceof ThenPointer) {
            ThenPointer next = (ThenPointer) nextPointer;
            result.addAll(state(next.getTask()));  
          }
        }
      }
      
      return result;
    }

    
    public TypeSpec build() {
      Assertions.notNull(body, () -> "body must be defined!");
      
      final ClassName outputType = namings.fl().outputValue(body);
      final ClassName immutableOutputType = JavaSpecUtil.immutable(outputType);
      final ClassName immutableStateType = JavaSpecUtil.immutable(namings.fl().state(body));
      
      
      final CodeBlock.Builder execution = CodeBlock.builder()
          .addStatement("long start = System.currentTimeMillis()")
          .addStatement("$T.Builder result = $T.builder()", immutableOutputType, immutableOutputType)
          .addStatement("$T state = $T.builder().build()", immutableStateType, immutableStateType)
          .add("\r\n");
  
      
      
      execution.add("\r\n")
        .addStatement("long end = System.currentTimeMillis()")
        .add("$T metaWrapper = $T.builder()", FlowMeta.class, ImmutableFlowMeta.class)
        .add("\r\n  ").add(".id($S).status($T.COMPLETED) ", body.getId().getValue(), ExecutionStatus.class)
        .add("\r\n  ").add(".start(start).end(end).time(end - start)")
        .add("\r\n  ").addStatement(".state(state).build()", body.getId().getValue(), ExecutionStatus.class)
        
        .addStatement("$T.Builder<$T, $T> resultWrapper = $T.builder()", ImmutableExecution.class, FlowMeta.class, outputType, ImmutableExecution.class)
        .addStatement("return resultWrapper.meta(metaWrapper).value(result.build()).build()");
      
      return TypeSpec.classBuilder(namings.fl().impl(body))
          .addModifiers(Modifier.PUBLIC)
          .addSuperinterface(namings.fl().api(body))
          .addJavadoc(body.getDescription().orElse(""))
          .addAnnotations(annotations)
          .addField(FieldSpec.builder(HdesWhen.class, "when", Modifier.PRIVATE, Modifier.FINAL).build())
          .addMethod(constructor)
          .addMethod(sourceType)
          .addMethod(MethodSpec.methodBuilder("apply")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(ParameterSpec.builder(namings.fl().inputValue(body), "input").build())
              .returns(namings.fl().execution(body))
              .addCode(execution.build())
              .build())
          .build();
    }
  }
}
