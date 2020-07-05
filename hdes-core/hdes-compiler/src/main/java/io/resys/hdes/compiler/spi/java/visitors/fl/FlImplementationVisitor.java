package io.resys.hdes.compiler.spi.java.visitors.fl;

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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.compiler.spi.NamingContext;
import io.resys.hdes.compiler.spi.java.visitors.JavaSpecUtil;
import io.resys.hdes.executor.api.DecisionTableMeta;
import io.resys.hdes.executor.api.FlowMeta;
import io.resys.hdes.executor.api.HdesExecutable.ExecutionStatus;
import io.resys.hdes.executor.api.HdesExecutable.Output;
import io.resys.hdes.executor.api.HdesWhen;
import io.resys.hdes.executor.api.ImmutableDecisionTableMeta;
import io.resys.hdes.executor.api.ImmutableOutput;

public class FlImplementationVisitor extends FlTemplateVisitor<FlJavaSpec, TypeSpec> {
  private final NamingContext naming;

  public FlImplementationVisitor(NamingContext naming) {
    super();
    this.naming = naming;
  }

  @Override
  public TypeSpec visitBody(FlowBody body) {
    ClassName outputName = naming.fl().output(body);
    ClassName immutableOutputName = JavaSpecUtil.immutable(outputName);
    ParameterizedTypeName returnType = ParameterizedTypeName
        .get(ClassName.get(Output.class), ClassName.get(FlowMeta.class), outputName);
    
    CodeBlock.Builder statements = CodeBlock.builder()
      .addStatement("long start = System.currentTimeMillis()")
      //.addStatement("$T tasks = $T.create()", FlowUtil.MutableFlowTasks.class, FlowUtil.MutableFlowTasks.class)
      .addStatement("$T.Builder result = $T.builder()", immutableOutputName, immutableOutputName)
      
      
      .addStatement("// run")
      
      
      .addStatement("long end = System.currentTimeMillis()")
      .add("$T meta = $T.builder()", DecisionTableMeta.class, ImmutableDecisionTableMeta.class)
      .add("\r\n  ").add(".id($S).status($T.COMPLETED) ", body.getId().getValue(), ExecutionStatus.class)
      .add("\r\n  ").add(".start(start).end(end).time(end - start)")
      .add("\r\n  ").addStatement(".tasks(tasks.build()).build()", body.getId().getValue(), ExecutionStatus.class)

      .add("\r\n")
      .addStatement("$T.Builder<$T, $T> builder = $T.builder()", ImmutableOutput.class, FlowMeta.class, outputName, ImmutableOutput.class)
      .addStatement("return builder.meta(meta).value(result.build()).build()")
      ;
    
    return TypeSpec.classBuilder(naming.fl().impl(body))
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterface(naming.fl().interfaze(body))
        .addAnnotation(AnnotationSpec.builder(javax.annotation.processing.Generated.class).addMember("value", "$S", FlImplementationVisitor.class.getCanonicalName()).build())
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(HdesWhen.class, "when").build())
            .addStatement("this.when = when")
            .build())
        .addField(FieldSpec.builder(HdesWhen.class, "when", Modifier.PRIVATE, Modifier.FINAL).build())
        .addMethod(MethodSpec.methodBuilder("apply")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(naming.fl().input(body), "input").build())
            .returns(returnType)
            .addCode(statements.build())
            .build())
        .build();
  }

//  @Override
//  public FlTaskVisitSpec visitTask(FlowTaskNode node) {
//    CodeBlock.Builder codeblock = CodeBlock.builder();
//    
//    // visit method
//    if (node.getRef().isPresent()) {
//      codeblock.add(visitTaskRef(node).getValue());
//    } else {
//      codeblock.addStatement("$T after = before", flowState);
//    }
//    
//    // next
//    String methodName = "visit" + node.getId();
//    List<MethodSpec> children = new ArrayList<>();
//    FlTaskVisitSpec next = visitTaskPointer(node.getNext());
//    codeblock.add(next.getValue());
//    for (MethodSpec method : next.getValues()) {
//      if (!children.stream().filter(m -> m.name.equals(method.name)).findFirst().isPresent()) {
//        children.add(method);
//      }
//    }
//    
//    return ImmutableFlTaskVisitSpec.builder()
//        .value(CodeBlock.builder().addStatement("return $L(after)", methodName).build())
//        .addValues(MethodSpec
//            .methodBuilder(methodName)
//            .addModifiers(Modifier.PRIVATE)
//            .addParameter(ParameterSpec.builder(flowState, "before").build())
//            .addCode(codeblock.build())
//            .returns(flowState).build())
//        .addAllValues(children).build();
//  }
}
