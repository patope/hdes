package io.resys.hdes.compiler.spi.java.dt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.EnReferedScope;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.EnReferedTypes;
import io.resys.hdes.compiler.spi.java.en.ExpressionVisitor;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.DecisionTableMeta;
import io.resys.hdes.executor.api.DecisionTableMeta.DecisionTableMetaEntry;
import io.resys.hdes.executor.api.HdesExecutable.ExecutionStatus;
import io.resys.hdes.executor.api.HdesExecutable.SourceType;
import io.resys.hdes.executor.api.HdesWhen;
import io.resys.hdes.executor.api.ImmutableDecisionTableMeta;
import io.resys.hdes.executor.api.ImmutableExecution;

public class DtImplSpec {
  
  public static Builder builder(Namings namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }
  
  public static class Builder {
    private final Namings namings;
    private final List<AnnotationSpec> annotations = Arrays.asList(
        AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unused").build(),
        AnnotationSpec.builder(javax.annotation.processing.Generated.class).addMember("value", "$S", DtImplSpec.class.getCanonicalName()).build());
    private final MethodSpec sourceType = MethodSpec.methodBuilder("getSourceType")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(SourceType.class)
        .addStatement("return $T.DT", SourceType.class)
        .build();
    private final MethodSpec constructor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(HdesWhen.class, "when").build())
        .addStatement("this.when = when")
        .build();
    
    private DecisionTableBody body;
    private DtParameterResolver resolver;
    
    public Builder(Namings namings) {
      super();
      this.namings = namings;
    }
    
    public Builder body(DecisionTableBody body) {
      this.body = body;
      this.resolver = new DtParameterResolver(body);
      return this;
    }

    public CodeBlock formula(ScalarDef scalarDef) {
      ClassName typeName = scalarDef.getDirection() == DirectionType.IN ? 
          namings.dt().inputValue(body) :
          namings.dt().outputValueMono(body);

      // Map inputs -> Immutable.Builder().val1(input.getCal1())...
      CodeBlock.Builder formulaInput = CodeBlock.builder()
          .add("$T.builder()",  JavaSpecUtil.immutable(namings.fr().inputValue(body, scalarDef)));
      
      EnReferedTypes referedTypes = ExpressionRefsSpec.builder(resolver).body(scalarDef.getFormula().get()).build();
      for(EnReferedScope scope : referedTypes.getScopes()) {
        switch (scope) {
        case IN:
          formulaInput.add("\r\n").add("  .$L(input)", ExpressionVisitor.ACCESS_INPUT_VALUE);
          break;
        case OUT:
          formulaInput.add("\r\n").add("  .$L(output)", ExpressionVisitor.ACCESS_OUTPUT_VALUE);
        case STATIC:
        default: throw new IllegalArgumentException("Scope: " + scope + " parameter: " + scalarDef + " not implemented!"); 
        }
      }
      
      // add inputs -> apply(builder.build())
      ClassName impl = namings.fr().impl(body, scalarDef);
      CodeBlock formulaCall = CodeBlock.builder()
          .add("$T $L = new $T()", JavaSpecUtil.type(scalarDef.getType()), scalarDef.getName(), impl)
          .add("\r\n").add("  .apply($L)", formulaInput.add("\r\n").add("    .build()").build())
          .add("\r\n").addStatement("  .getValue().$L", JavaSpecUtil.methodCall(scalarDef.getName()))
          .build();

      return CodeBlock.builder()
          .add(formulaCall)
          .addStatement("mutator = $T.builder().from(mutator).$L($L).build()", JavaSpecUtil.immutable(typeName), scalarDef.getName(), scalarDef.getName())
          .build();
    }

    
    public TypeSpec build() {
      Assertions.notNull(body, () -> "body must be defined!");
      
      final List<MethodSpec> formulas = new ArrayList<>();
      final ClassName inputType = namings.dt().inputValue(body);
      final ClassName outputType = namings.dt().outputValueMono(body);
      final ClassName immutableOutputName = JavaSpecUtil.immutable(outputType);
      
      CodeBlock.Builder execution = CodeBlock.builder()
          .addStatement("long start = System.currentTimeMillis()")
          .addStatement("int id = 0")
          .addStatement("$T<Integer, $T> meta = new $T<>()", Map.class, DecisionTableMetaEntry.class, HashMap.class)
          .addStatement("$T.Builder result = $T.builder()", immutableOutputName, immutableOutputName);
      
      
      // Create formula on input
      List<CodeBlock> inputFormulas = body.getHeaders().getValues().stream()
        .filter(v -> v.getDirection() == DirectionType.IN).map(v -> (ScalarDef) v)
        .filter(v -> v.getFormula().isPresent()).map(v -> formula(v))
        .collect(Collectors.toList());
      if(!inputFormulas.isEmpty()) {
        CodeBlock.Builder builder = CodeBlock.builder()
            .addStatement("$T mutator = input", inputType);
        
        for(CodeBlock codeBlock : inputFormulas) {
          builder.add(codeBlock).add("\r\n");
        }
        
        MethodSpec method = MethodSpec
          .methodBuilder("applyInputFormula")
          .addModifiers(Modifier.PUBLIC)
          .addCode(builder.addStatement("return mutator").build())
          .addParameter(inputType, "input")
          .returns(inputType)
          .build();
        
        formulas.add(method);
        execution.addStatement("input = $L(input)", method.name);
      }
      
      if(body.getHitPolicy() instanceof HitPolicyFirst) {
        execution.add(HitPolicyFirstSpec.builder(namings).body(body).build());
      } else if(body.getHitPolicy() instanceof HitPolicyAll) {
        execution.add(HitPolicyAllSpec.builder(namings).body(body).build());
      } else  {
        execution.add(HitPolicyMatrixSpec.builder(namings).body(body).build());
      }
      
      execution.addStatement("$T output = result.build()", outputType);
      
      // Create formula on output
      List<CodeBlock> outputFormulas = body.getHeaders().getValues().stream()
        .filter(v -> v.getDirection() == DirectionType.OUT).map(v -> (ScalarDef) v)
        .filter(v -> v.getFormula().isPresent()).map(v -> formula(v))
        .collect(Collectors.toList());
      if(!outputFormulas.isEmpty()) {
        
        CodeBlock.Builder builder = CodeBlock.builder()
            .addStatement("$T mutator = output", outputType);
        
        for(CodeBlock codeBlock : outputFormulas) {
          builder.add(codeBlock).add("\r\n");
        }
        
        MethodSpec method = MethodSpec
          .methodBuilder("applyOutputFormula")
          .addModifiers(Modifier.PUBLIC)
          .addCode(builder.addStatement("return mutator").build())
          .addParameter(inputType, "input")
          .addParameter(outputType, "output")
          .returns(outputType)
          .build();
        
        formulas.add(method);
        execution.addStatement("output = $L(input, output)", method.name);
      }
      
      
      execution.add("\r\n")
      .addStatement("long end = System.currentTimeMillis()")
      .add("$T metaWrapper = $T.builder()", DecisionTableMeta.class, ImmutableDecisionTableMeta.class)
      .add("\r\n  ").add(".id($S).status($T.COMPLETED) ", body.getId().getValue(), ExecutionStatus.class)
      .add("\r\n  ").add(".start(start).end(end).time(end - start)")
      .add("\r\n  ").addStatement(".values(meta).build()", body.getId().getValue(), ExecutionStatus.class)
      
      .addStatement("$T.Builder<$T, $T> resultWrapper = $T.builder()", ImmutableExecution.class, DecisionTableMeta.class, outputType, ImmutableExecution.class)
      .addStatement("return resultWrapper.meta(metaWrapper).value(output).build()")
      .build();
    
      return TypeSpec.classBuilder(namings.dt().impl(body))
          .addModifiers(Modifier.PUBLIC)
          .addSuperinterface(namings.dt().api(body))
          .addJavadoc(body.getDescription().orElse(""))
          .addAnnotations(annotations)
          .addField(FieldSpec.builder(HdesWhen.class, "when", Modifier.PRIVATE, Modifier.FINAL).build())
          .addMethod(constructor)
          .addMethod(sourceType)
          .addMethod(MethodSpec.methodBuilder("apply")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(ParameterSpec.builder(namings.dt().inputValue(body), "input").build())
              .returns(namings.dt().execution(body))
              .addCode(execution.build())
              .build())
          .addMethods(formulas)
          .build();
    }
  }
}
