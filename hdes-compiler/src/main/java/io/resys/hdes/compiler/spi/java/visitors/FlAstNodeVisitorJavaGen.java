package io.resys.hdes.compiler.spi.java.visitors;

import java.util.ArrayList;
import java.util.List;

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

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.Mapping;
import io.resys.hdes.ast.api.nodes.FlowNode.TaskRef;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.When;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;
import io.resys.hdes.compiler.api.Flow.FlowExecutionLog;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.api.ImmutableFlowExecutionLog;
import io.resys.hdes.compiler.spi.NamingContext;
import io.resys.hdes.compiler.spi.java.JavaSpecUtil;
import io.resys.hdes.compiler.spi.java.visitors.FlJavaSpec.FlCodeSpec;
import io.resys.hdes.compiler.spi.java.visitors.FlJavaSpec.FlTaskImplSpec;
import io.resys.hdes.compiler.spi.java.visitors.FlJavaSpec.FlTaskRefSpec;

public class FlAstNodeVisitorJavaGen extends FlAstNodeVisitorTemplate<FlJavaSpec, TypeSpec> {
  
  private final NamingContext naming;
  private FlowBody body;
  private ClassName flowState;
  
  public FlAstNodeVisitorJavaGen(NamingContext naming) {
    super();
    this.naming = naming;
  }
  
  @Override
  public TypeSpec visitFlowBody(FlowBody node) {
    this.body = node;
    this.flowState = naming.fl().state(node);
    
    TypeSpec.Builder flowBuilder = TypeSpec.classBuilder(naming.fl().impl(node))
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterface(naming.fl().interfaze(node));

    FlTaskImplSpec taskImpl = node.getTask().map(n -> visitFlowTask(n)).orElseGet(() ->
      ImmutableFlTaskImplSpec.builder().value(CodeBlock.builder().add("// no tasks described ").build()).build()
    );
    
    MethodSpec applyMethod = MethodSpec.methodBuilder("apply")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(naming.fl().input(node), "input").build())
        .returns(flowState)
        .addStatement(visitInit(node))
        .addCode(CodeBlock.builder()
            .add(taskImpl.getValue())
            .build())
        .addCode(CodeBlock.builder()
            .add("\r\n").addStatement("long end = System.currentTimeMillis()")
            .build())
        .addStatement(CodeBlock.builder()
            .add("return $T.builder()", naming.immutable(flowState))
            .add("\r\n").add(".from(currentState)")
            .add("\r\n").add(".id($S)", node.getId())
            .add("\r\n").add(".parent(currentState.getLog())")
            .add("\r\n").add(".start(start)").add(".end(end)")
            .add("\r\n").add(".duration(end - start)")
            .add("\r\n").add(".build()")
            .build())
        .build();
 
    return flowBuilder
        .addMethod(applyMethod)
        .addMethods(taskImpl.getChildren())
        .build();
  }
  
  private CodeBlock visitInit(FlowBody node) {
    return CodeBlock.builder()
        .add("$T currentState = Immutable$T.builder().input(input).build()", flowState, flowState)
        .build();
  }
  
  @Override
  public FlTaskImplSpec visitFlowTask(FlowTaskNode node) {
    List<MethodSpec> children = new ArrayList<>();
    CodeBlock.Builder codeblock = CodeBlock.builder();
    
    // visit method
    if(node.getRef().isPresent()) {
      FlTaskRefSpec ref = visitTaskRef(node);
      children.add(ref.getMethod());
      codeblock.addStatement("currentState = $L(currentState)", ref.getMethod().name);
    }
    
    // next
    if(node.getNext().isPresent()) {
      FlTaskImplSpec next = visitFlowTaskPointer(node.getNext().get());
      codeblock.add(next.getValue());
      
      for(MethodSpec method : next.getChildren()) {
        if(!children.stream().filter(m -> m.name.equals(method.name)).findFirst().isPresent()) {
          children.add(method);          
        }
      }
    }
    
    return ImmutableFlTaskImplSpec.builder()
        .value(codeblock.build())
        .addAllChildren(children)
        .build();
  }
  
  @Override
  public FlTaskRefSpec visitTaskRef(FlowTaskNode parent) {
    TaskRef node = parent.getRef().get(); 
    
    MethodSpec.Builder visitBuilder = MethodSpec
        .methodBuilder("visit" + parent.getId())
        .addModifiers(Modifier.PRIVATE)
        .addParameter(ParameterSpec.builder(flowState, "currentState").build())
        .returns(flowState);
    
    CodeBlock logBlock = CodeBlock.builder().add("\r\n")
      .add("$T log = $T.builder()", FlowExecutionLog.class, ImmutableFlowExecutionLog.class)
      .add("\r\n").add(".id($S)", parent.getId())
      .add("\r\n").add(".parent(currentState.getLog())")
      .add("\r\n").add(".start(start)").add(".end(end)")
      .add("\r\n").add(".duration(end - start)")
      .add("\r\n").addStatement(".build()").build();
    
    CodeBlock returnBlock = CodeBlock.builder()
        .add("\r\n")
        .add("return $T.builder()", naming.immutable(naming.fl().state(this.body))).add("\r\n")
        .add(".from(currentState).input(input).output(output)").add("\r\n")
        .addStatement(".log(log).build()").build(); 
    
    return ImmutableFlTaskRefSpec.builder().method(
        visitBuilder
        
          .addStatement("long start = System.currentTimeMillis()")
          .addCode(visitMapping(parent).getValue())
          .addStatement("long end = System.currentTimeMillis()")
          
          .addCode(logBlock)
          .addCode(returnBlock)

          .build())
        .build();
  }
  
  @Override
  public FlCodeSpec visitMapping(FlowTaskNode node) {
    TaskRef ref = node.getRef().get();
    ClassName input = naming.fl().refInput(node.getRef().get());
    
    CodeBlock.Builder codeBlock = CodeBlock.builder()
        .add("$T input = $T.builder()", input, naming.immutable(input));
    for(Mapping mapping : ref.getMapping()) {
      String[] src = mapping.getRight().split("\\.");
      StringBuilder right = new StringBuilder();
      for(String target : src) {
        right.append(".").append(JavaSpecUtil.getMethod(target)).append("()");
      }
      codeBlock.add("\r\n").add(".$L(currentState$L)", mapping.getLeft(), right.toString());
    }
    
    codeBlock
    .addStatement(".build()")
    .add("$T output = ", naming.fl().refOutput(node.getRef().get()));
    
    switch (ref.getType()) {
    case DECISION_TABLE: codeBlock.addStatement("$L.apply(input)", naming.fl().refMethod(ref)); break;
    case FLOW_TASK: codeBlock.addStatement("$L.apply(input)", naming.fl().refMethod(ref)); break;
    case MANUAL_TASK: codeBlock.addStatement("$L().apply(input)", naming.fl().refMethod(ref)); break;
    case SERVICE_TASK: codeBlock.addStatement("$L().apply(input)", naming.fl().refMethod(ref)); break;
    default: throw new HdesCompilerException(HdesCompilerException.builder().unknownFlTaskRef(ref));
    }
    return ImmutableFlCodeSpec.builder().value(codeBlock.build()).build();
  }
  
  @Override
  public FlTaskImplSpec visitFlowTaskPointer(FlowTaskPointer node) {
    // if / else
    
    
    // next
    if(node instanceof ThenPointer) {
      ThenPointer then = (ThenPointer) node;
      if(then.getTask().isPresent()) {
        return visitFlowTask(then.getTask().get());
      }
    }
    
    return ImmutableFlTaskImplSpec.builder().build();
  }
  
  @Override
  public FlJavaSpec visitWhen(When node) {
    // TODO Auto-generated method stub
    return super.visitWhen(node);
  }
  
  @Override
  public FlJavaSpec visitThenPointer(ThenPointer node) {
    // TODO Auto-generated method stub
    return super.visitThenPointer(node);
  }
  
  
  @Override
  public FlJavaSpec visitWhenThen(WhenThen node) {
    // TODO Auto-generated method stub
    return super.visitWhenThen(node);
  }
  @Override
  public FlJavaSpec visitWhenThenPointer(WhenThenPointer node) {
    // TODO Auto-generated method stub
    return super.visitWhenThenPointer(node);
  }
}