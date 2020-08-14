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
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.Mapping;
import io.resys.hdes.ast.api.nodes.FlowNode.MappingExpression;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.dt.DtImplSpec;
import io.resys.hdes.compiler.spi.java.en.ExpressionSpec;
import io.resys.hdes.compiler.spi.java.en.ExpressionVisitor.EnScalarCodeSpec;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.compiler.spi.naming.Namings.TaskRefNaming;
import io.resys.hdes.executor.api.FlowExecutionException;
import io.resys.hdes.executor.api.FlowMetaValue;
import io.resys.hdes.executor.api.HdesExecutable.ExecutionStatus;
import io.resys.hdes.executor.api.HdesExecutable.SourceType;
import io.resys.hdes.executor.api.HdesWhen;
import io.resys.hdes.executor.api.ImmutableFlowMetaValue;
import io.resys.hdes.executor.api.ImmutableFlowTaskMetaFlux;
import io.resys.hdes.executor.api.ImmutableFlowTaskMetaMono;
import io.resys.hdes.executor.api.ImmutableHdesExecution;
import io.resys.hdes.executor.api.SwitchMeta;

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
    
    private static String getExecuteTaskMethodName(FlowTaskNode node) {
      return getExecuteTaskMethodName(node.getId());
    }

    private static String getExecuteTaskMethodName(String node) {
      return "execute" + JavaSpecUtil.capitalize(node);
    }
    
    private CodeBlock mapping(FlowTaskNode task) {
      TaskRefNaming ref = namings.fl().ref(body, task.getRef().get());
      
      CodeBlock.Builder execution = CodeBlock.builder()
          .add("$T.builder()", JavaSpecUtil.immutable(ref.getInputValue()));
      
      for(Mapping mapping : task.getRef().get().getMapping()) {
        MappingExpression expression = (MappingExpression) mapping.getRight();
        EnScalarCodeSpec spec = ExpressionSpec.builder().parent(body).envir(namings.ast()).flm(expression);
        
        execution.add(".$L($L)", mapping.getLeft(), spec.getValue());
      }
      
      return execution.add("\r\n").add(".build()").build(); 
    }
    
    /*
     * Reference statement
     */
    private Optional<CodeBlock> ref(FlowTaskNode task) {
      if(task.getRef().isEmpty()) {
        return Optional.empty();  
      }
      
      TaskRefNaming ref = namings.fl().ref(body, task.getRef().get());
      CodeBlock.Builder delegate = CodeBlock.builder()
          .add("$T delegate = new $T(when)", ref.getExecution(), ref.getImpl())
          .add("\r\n  ").addStatement(".apply($L)", mapping(task));
      
      ClassName stateType = namings.fl().stateValue(body);
      ClassName delegateType = ClassName.get(task.getLoop().isPresent() ? ImmutableFlowTaskMetaFlux.class : ImmutableFlowTaskMetaMono.class);      
      return Optional.of(CodeBlock.builder()
          .add(delegate.build())
          .add("\r\n")
          .addStatement("$T.Builder<$T, $T, $T> task = $T.builder()", delegateType, ref.getInputValue(), ref.getMeta(), ref.getOutputValue(), delegateType)
          .add("\r\n")
          .add("after = $T.builder().from(before)", JavaSpecUtil.immutable(stateType))
          .addStatement(".$L(task.id($S).delegate(delegate).build()).build()", JavaSpecUtil.decapitalize(task.getId()), task.getId())
          .build());
    }

    /*
     * Switch statement
     */
    private CodeBlock sw(FlowTaskNode task) {
      final FlowTaskPointer pointer = task.getNext();
      final WhenThenPointer whenThen = (WhenThenPointer) pointer;
      final ClassName stateType = namings.fl().stateValue(body);
      
      CodeBlock delegateInput = CodeBlock.builder()
          .add("$T.builder()", JavaSpecUtil.immutable(namings.sw().inputValue(body, task)))
          .add(".inputValue(input)")
          .add(".stateValue(before)")
          .add(".build()").build();
      
      CodeBlock.Builder execution = CodeBlock.builder()
          .add("$T delegate = new $T()", namings.sw().execution(body, task), namings.sw().impl(body, task))
          .addStatement(".apply($L)", delegateInput)
          .add("\r\n")
          
          .addStatement("$T.Builder<$T, $T, $T> task = $T.builder()", ImmutableFlowTaskMetaMono.class, namings.sw().inputValue(body, task), SwitchMeta.class, namings.sw().outputValue(body, task), ImmutableFlowTaskMetaMono.class)
          
          .add("after = $T.builder().from(before)", JavaSpecUtil.immutable(stateType))
          .addStatement(".$L(task.id($S).delegate(delegate).build()).build()", JavaSpecUtil.decapitalize(task.getId()), task.getId())
          
          .add("\r\n")
          .addStatement("$T gate = after.$L.getDelegate().getOutputValue().getGate()", 
              namings.sw().gate(body, task), 
              JavaSpecUtil.methodCall(task.getId()));
      
      execution.beginControlFlow("switch(gate)");
      for (WhenThen c : whenThen.getValues()) {
        FlowTaskPointer nextPointer = c.getThen();
       
        if(nextPointer instanceof ThenPointer) {
          String nextTaskId = ((ThenPointer) nextPointer).getTask().get().getId();
          String nextMethodName = getExecuteTaskMethodName(nextTaskId);
          execution.addStatement("case $L: return $L(input, after, meta)", nextTaskId, nextMethodName);
        } else if(nextPointer instanceof EndPointer) {
          EndPointer endPointer = (EndPointer) nextPointer;
          execution
            .add("case $L:", endPointer.getName())
            .add("\r\n").add(end(task, endPointer));
        } else {
          throw new HdesCompilerException(HdesCompilerException.builder().unknownSwitchThen(task));
        }
      }
      
      String msg = new StringBuilder().append("Switch statement gate is not covered in flow: '")
          .append(body.getId().getValue()).append("', switch task: '")
          .append(task.getId()).append("'").toString();
      return execution
          .addStatement("default: throw new $T(after, $S)", FlowExecutionException.class, msg)
          .endControlFlow()
          .build();
    }
    
    /**
     * End statement
     */
    private CodeBlock end(FlowTaskNode task, EndPointer pointer) {
      final ClassName outputType = namings.fl().outputValue(body);
      final ClassName inputType = namings.fl().inputValue(body);
      final ClassName immutableOutputType = JavaSpecUtil.immutable(outputType);
      
      final CodeBlock metaValue = CodeBlock.builder()
          .add("$T.builder()", ImmutableFlowMetaValue.class)
          .add("\r\n").add(".from(meta).state(after).end(end).time(end - start)")
          .add("\r\n").add(".status($T.$L).build()", ExecutionStatus.class, ExecutionStatus.COMPLETED)
          .build();
      
      return CodeBlock.builder().add("\r\n")
        .addStatement("$T.Builder result = $T.builder()", immutableOutputType, immutableOutputType)
        .addStatement("long start = meta.getStart()")
        .addStatement("long end = System.currentTimeMillis()")      
        .addStatement("return execution(input, result.build(), $L)", metaValue)
        .build();
    }
    
    /**
     * Next task statement
     */
    private List<MethodSpec> task(Optional<FlowTaskNode> start) {
      if (start.isEmpty()) {
        return Collections.emptyList();
      }

      CodeBlock.Builder execution = CodeBlock.builder()
          .addStatement("$T after = before", namings.fl().stateValue(body));
      ref(start.get()).ifPresent(c -> execution.add(c));
      
      List<MethodSpec> result = new ArrayList<>();
      FlowTaskPointer pointer = start.get().getNext();
      if (pointer instanceof ThenPointer) {
        ThenPointer then = (ThenPointer) pointer;
        result.addAll(task(then.getTask()));
        
        String nextTaskId = then.getTask().get().getId();
        String nextMethodName = getExecuteTaskMethodName(nextTaskId);
        execution.addStatement("return $L(input, after, meta)", nextMethodName);
     
      } else if (pointer instanceof WhenThenPointer) {        
        WhenThenPointer whenThen = (WhenThenPointer) pointer;
        execution.add(sw(start.get()));
        for (WhenThen c : whenThen.getValues()) {
          FlowTaskPointer nextPointer = c.getThen();
          if(nextPointer instanceof ThenPointer) {
            ThenPointer next = (ThenPointer) nextPointer;
            result.addAll(task(next.getTask()));  
          } 
        }
      
      } else if(pointer instanceof EndPointer) {
        EndPointer endPointer = (EndPointer) pointer;
        execution.add(end(start.get(), endPointer));
      } else {
        throw new HdesCompilerException(HdesCompilerException.builder().unknownSwitchThen(start.get()));
      }
      
      result.add(MethodSpec.methodBuilder(getExecuteTaskMethodName(start.get()))
          .addModifiers(Modifier.PUBLIC)
          .addParameter(ParameterSpec.builder(namings.fl().inputValue(body), "input").build())
          .addParameter(ParameterSpec.builder(namings.fl().stateValue(body), "before").build())
          .addParameter(ParameterSpec.builder(FlowMetaValue.class, "meta").build())
          .returns(namings.fl().execution(body))
          .addCode(execution.build())
          .build());
        
      return result;
    }

    
    
    public TypeSpec build() {
      Assertions.notNull(body, () -> "body must be defined!");
      
      final ClassName outputType = namings.fl().outputValue(body);
      final ClassName immutableOutputType = JavaSpecUtil.immutable(outputType);
      final ClassName immutableStateType = JavaSpecUtil.immutable(namings.fl().stateValue(body));
      final CodeBlock.Builder execution = CodeBlock.builder()
          .addStatement("$T state = $T.builder().build()", immutableStateType, immutableStateType);
      
      execution.add("$T meta = $T.builder()", FlowMetaValue.class, ImmutableFlowMetaValue.class)
        .addStatement(".id($S).status($T.$L).start(System.currentTimeMillis()).state(state).build()", body.getId().getValue(), ExecutionStatus.class, ExecutionStatus.RUNNING);
  
      if(body.getTask().isPresent()) {
        String nextTaskId = body.getTask().get().getId();
        String nextMethodName = getExecuteTaskMethodName(nextTaskId);
        execution.addStatement("return $L(input, state, meta)", nextMethodName);
        
      } else {
        execution
          .addStatement("long end = System.currentTimeMillis()")
          .add("$T meta = $T.builder()", FlowMetaValue.class, ImmutableFlowMetaValue.class)
          .add("\r\n  ").add(".id($S).status($T.COMPLETED) ", body.getId().getValue(), ExecutionStatus.class)
          .add("\r\n  ").add(".start(start).end(end).time(end - start)")
          .add("\r\n  ").addStatement(".state(after).build()", body.getId().getValue(), ExecutionStatus.class)
          
          .addStatement("$T.Builder result = $T.builder()", immutableOutputType, immutableOutputType)
          .addStatement("$T.Builder<$T, $T> resultWrapper = $T.builder()", ImmutableHdesExecution.class, FlowMetaValue.class, outputType, ImmutableHdesExecution.class)
          .addStatement("return resultWrapper.meta(meta).value(result.build()).build()");
      }
      
      
      return TypeSpec.classBuilder(namings.fl().impl(body))
          .addModifiers(Modifier.PUBLIC)
          .addSuperinterface(namings.fl().api(body))
          .superclass(namings.fl().template(body))
          .addJavadoc(body.getDescription().orElse(""))
          .addAnnotations(annotations)
          .addField(FieldSpec.builder(HdesWhen.class, "when", Modifier.PRIVATE, Modifier.FINAL).build())
          .addMethods(task(body.getTask()))
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
