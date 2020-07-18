package io.resys.hdes.compiler.spi.java.visitors.dt;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.Headers;
import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarType;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.ExpressionValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HeaderRefValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicy;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyMatrix;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.InOperation;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.LiteralValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.MatrixRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.NegateLiteralValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.Rule;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.UndefinedValue;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AndOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.BetweenExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NotUnaryOperation;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.visitors.JavaSpecUtil;
import io.resys.hdes.compiler.spi.java.visitors.dt.DtJavaSpec.DtCodeSpec;
import io.resys.hdes.compiler.spi.java.visitors.dt.DtJavaSpec.DtCodeSpecPair;
import io.resys.hdes.compiler.spi.java.visitors.dt.DtJavaSpec.DtCodeValueSpec;
import io.resys.hdes.compiler.spi.java.visitors.dt.DtJavaSpec.DtFormulaSpec;
import io.resys.hdes.compiler.spi.java.visitors.dt.DtJavaSpec.DtMethodsSpec;
import io.resys.hdes.compiler.spi.java.visitors.en.EnInterfaceVisitor;
import io.resys.hdes.compiler.spi.java.visitors.en.EnJavaSpec;
import io.resys.hdes.compiler.spi.java.visitors.en.EnJavaSpec.EnConvertionSpec;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.DecisionTableMeta;
import io.resys.hdes.executor.api.DecisionTableMeta.DecisionTableMetaEntry;
import io.resys.hdes.executor.api.HdesExecutable.ExecutionStatus;
import io.resys.hdes.executor.api.HdesExecutable.MetaToken;
import io.resys.hdes.executor.api.HdesExecutable.SourceType;
import io.resys.hdes.executor.api.HdesWhen;
import io.resys.hdes.executor.api.ImmutableDecisionTableMeta;
import io.resys.hdes.executor.api.ImmutableDecisionTableMetaEntry;
import io.resys.hdes.executor.api.ImmutableExecution;
import io.resys.hdes.executor.api.ImmutableMetaStamp;
import io.resys.hdes.executor.api.ImmutableMetaToken;
import io.resys.hdes.executor.spi.exceptions.DecisionTableHitPolicyFirstException;

public class DtImplementationVisitor extends DtTemplateVisitor<DtJavaSpec, TypeSpec> {
  private final static String HEADER_REF = "//header ref to be replaces";
  private final static String APPLY_INPUT_FORMULA = "applyInputFormula";
  private final static String APPLY_OUTPUT_FORMULA = "applyOutputFormula";
  
  private final Namings naming;
  private DtTypeNameResolver typeNames;
  private DecisionTableBody body;

  public DtImplementationVisitor(Namings naming) {
    super();
    this.naming = naming;
  }

  @Override
  public TypeSpec visitDecisionTableBody(DecisionTableBody node) {
    this.body = node;
    this.typeNames = new DtTypeNameResolver(node);
    
    return TypeSpec.classBuilder(naming.dt().impl(node))
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterface(naming.dt().api(node))
        .addJavadoc(node.getDescription().orElse(""))
        
        .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unused").build())
        .addAnnotation(AnnotationSpec.builder(javax.annotation.processing.Generated.class).addMember("value", "$S", DtImplementationVisitor.class.getCanonicalName()).build())
        
        .addField(FieldSpec.builder(HdesWhen.class, "when", Modifier.PRIVATE, Modifier.FINAL).build())
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(HdesWhen.class, "when").build())
            .addStatement("this.when = when")
            .build())
        .addMethod(MethodSpec.methodBuilder("getSourceType")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .returns(SourceType.class)
            .addStatement("return $T.DT", SourceType.class)
            .build())
       
        .addMethods(visitHitPolicy(node.getHitPolicy()).getValue())
        
        .build();
  }
  
  @Override
  public DtFormulaSpec visitFormula(Headers node) {
    CodeBlock.Builder input = null;
    CodeBlock.Builder output = null;
    
    ClassName inputTypeName = naming.dt().inputValue(body);
    
    for(TypeDefNode typeDef : node.getValues()) {
      if(!(typeDef instanceof ScalarTypeDefNode)) {
        continue;
      }
      ScalarTypeDefNode scalarDef = (ScalarTypeDefNode) typeDef;
      if(scalarDef.getFormula().isEmpty()) {
        continue;
      }
      
      // Map inputs -> Immutable.Builder().val1(input.getCal1())...
      CodeBlock.Builder inputValue = CodeBlock.builder()
          .add("$T.builder()", JavaSpecUtil.immutable(naming.fr().inputValue(body, scalarDef)));
      new EnInterfaceVisitor(this.typeNames).visitExpressionBody(scalarDef.getFormula().get())
      .forEach(t -> inputValue
          .add("\r\n")
          .add("    .$L($L)", 
              t.getName(), 
              "src." + JavaSpecUtil.methodCall(t.getName()))
      );
      
      // add inputs -> apply(builder.build())
      ClassName impl = naming.fr().impl(body, scalarDef);
      CodeBlock codeBlock = CodeBlock.builder()
          .add("$T $L = new $T()", JavaSpecUtil.type(scalarDef.getType()), scalarDef.getName(), impl)
          .add("\r\n")
          .addStatement("  .apply($L).getValue().$L", inputValue.add("\r\n").add("  .build()").build(), JavaSpecUtil.methodCall(scalarDef.getName()))
          
          .addStatement("src = $T.builder().from(src).$L($L).build()", JavaSpecUtil.immutable(inputTypeName), scalarDef.getName(), scalarDef.getName())
          
          .build();
      
      if(typeDef.getDirection() == DirectionType.IN) {
        if(input == null) {
          input = CodeBlock.builder()
              .addStatement("$T src = input", naming.dt().inputValue(body));
        }
        
        input.add(codeBlock);
        
      } else {
        if(output == null) {
          output = CodeBlock.builder();
        }
        output.add(codeBlock);
        
      } 
    }
    
    List<MethodSpec> inputs = new ArrayList<>();
    if(input != null) {
      inputs.add(MethodSpec
          .methodBuilder(APPLY_INPUT_FORMULA)
          .addModifiers(Modifier.PUBLIC)
          .addCode(input.addStatement("return src").build())
          .addParameter(inputTypeName, "input")
          .returns(inputTypeName)
          .build());
    }
    
    List<MethodSpec> outputs = new ArrayList<>();
    if(output != null) {
      ClassName type = naming.dt().outputValueMono(body);
      
      inputs.add(MethodSpec
          .methodBuilder(APPLY_OUTPUT_FORMULA)
          .addModifiers(Modifier.PUBLIC)
          .addCode(output.build())
          .addParameter(type, "input")
          .returns(type)
          .build());
    }
    
    return ImmutableDtFormulaSpec.builder()
        .addAllInputs(inputs)
        .addAllOutputs(outputs)
        .build();
  }

  @Override
  public DtMethodsSpec visitHitPolicyMatrix(HitPolicyMatrix node) {
    ClassName outputName = naming.dt().outputValueMono(body);
    ClassName immutableOutputName = JavaSpecUtil.immutable(outputName);
    
    CodeBlock.Builder statements = CodeBlock.builder()
        .addStatement("long start = System.currentTimeMillis()")
        .addStatement("int id = 0")
        .addStatement("$T.Builder result = $T.builder()", immutableOutputName, immutableOutputName)
        .addStatement("$T<Integer, $T> metaValues = new $T<>()", Map.class, DecisionTableMetaEntry.class, HashMap.class);    
    
    // Create formula on input
    DtFormulaSpec formula = visitFormula(body.getHeaders());
    if(!formula.getInputs().isEmpty()) {
      statements.addStatement("input = $L(input)", APPLY_INPUT_FORMULA);
    }
    
    // DT body
    for (MatrixRow matrixRow : node.getRows()) {
      DtCodeValueSpec spec = visitMatrixRow(matrixRow);
      statements.add(spec.getValue()).add("\r\n");
    }
    
    // Formula on output
    if(!formula.getOutputs().isEmpty()) {
      statements.addStatement("result = $L(result)", APPLY_OUTPUT_FORMULA);
    }
    
    // Output
    statements
      .addStatement("long end = System.currentTimeMillis()")
      .add("$T meta = $T.builder()", DecisionTableMeta.class, ImmutableDecisionTableMeta.class)
      .add("\r\n  ").add(".id($S).status($T.COMPLETED) ", body.getId().getValue(), ExecutionStatus.class)
      .add("\r\n  ").add(".start(start).end(end).time(end - start)")
      .add("\r\n  ").addStatement(".values(metaValues).build()", body.getId().getValue(), ExecutionStatus.class);

    statements
      .add("\r\n")
      .addStatement("$T.Builder<$T, $T> builder = $T.builder()", ImmutableExecution.class, DecisionTableMeta.class, outputName, ImmutableExecution.class)
      .addStatement("return builder.meta(meta).value(result.build()).build()")
      .build();
        
    return ImmutableDtMethodsSpec.builder()
        .addAllValue(formula.getInputs())
        .addAllValue(formula.getOutputs())
        .addValue(
          MethodSpec.methodBuilder("apply")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(ParameterSpec.builder(naming.dt().inputValue(body), "input").build())
              .returns(naming.dt().execution(body))
              .addCode(statements.build())
              .build()
        ).build();
  }
  
  @Override
  public DtMethodsSpec visitHitPolicyAll(HitPolicyAll node) {
    
    CodeBlock.Builder statements = CodeBlock.builder()
        .addStatement("$T<$T> result = new $T<>()", List.class, naming.dt().outputValueFlux(body), ArrayList.class)
        .addStatement("$T<Integer, $T> metaValues = new $T<>()", Map.class, DecisionTableMetaEntry.class, HashMap.class)
        .addStatement("int id = 0")
        .addStatement("long start = System.currentTimeMillis()");
    
    DtFormulaSpec formula = visitFormula(body.getHeaders());
    if(!formula.getInputs().isEmpty()) {
      statements.addStatement("input = $L(input)", APPLY_INPUT_FORMULA);
    }
    
    int rowIndex = 0;
    for (RuleRow row : node.getRows()) {
      DtCodeSpecPair pair = visitRuleRow(row);

      statements.add("\r\n");
      
      // control start
      if (!pair.getKey().isEmpty()) {
        statements.beginControlFlow("if($L)", pair.getKey()).add("\r\n");
      }
      
      // generate token
      CodeBlock token = CodeBlock.builder().add("$T.builder()", ImmutableMetaToken.class)
          .add("\r\n    .value($S)", row.getText().replaceAll("\\r|\\n", " ").replaceAll("\\s{2,}", " "))
          .add("\r\n    .start($T.builder().line($L).column($L).build())", ImmutableMetaStamp.class, row.getToken().getStartLine(), row.getToken().getStartCol())
          .add("\r\n    .end($T.builder().line($L).column($L).build())", ImmutableMetaStamp.class, row.getToken().getEndLine(), row.getToken().getEndCol())
          .add("\r\n    .build()")
          .build();
      
      statements
      .add("metaValues.put(id, $T.builder()", ImmutableDecisionTableMetaEntry.class)
      .add("\r\n  .id(id++)")
      .add("\r\n  .index($L)", rowIndex++)
      .add("\r\n  .token($L)", token)
      .addStatement(".build())")
      .addStatement("result.add($L)", pair.getValue());
      
      // Control end
      if (!pair.getKey().isEmpty()) {
        statements.endControlFlow();
      } 
    }
    
    ClassName outputName = naming.dt().outputValueMono(body);

    if(!formula.getOutputs().isEmpty()) {
      statements.addStatement("result = $L(result)", APPLY_OUTPUT_FORMULA);
    }
    
    statements
    .addStatement("long end = System.currentTimeMillis()")
    .add("$T meta = $T.builder()", DecisionTableMeta.class, ImmutableDecisionTableMeta.class)
    .add("\r\n  ").add(".id($S).status($T.COMPLETED) ", body.getId().getValue(), ExecutionStatus.class)
    .add("\r\n  ").add(".start(start).end(end).time(end - start)")
    .add("\r\n  ").addStatement(".values(metaValues).build()", body.getId().getValue(), ExecutionStatus.class);

    statements
      .add("\r\n")
      .addStatement("$T.Builder<$T, $T> builder = $T.builder()", ImmutableExecution.class, DecisionTableMeta.class, outputName, ImmutableExecution.class)
      .addStatement("return builder.meta(meta).value($L).build()", 
          CodeBlock.builder().add("$T.builder().values(result).build()", JavaSpecUtil.immutable(outputName)).build())
      .build();

    return ImmutableDtMethodsSpec.builder()
        .addAllValue(formula.getInputs())
        .addAllValue(formula.getOutputs())
        .addValue(
          MethodSpec.methodBuilder("apply")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(naming.dt().inputValue(body), "input").build())
            .returns(naming.dt().execution(body))
            .addCode(statements.build())
            .build()
        ).build();
  }
  
  @Override
  public DtMethodsSpec visitHitPolicyFirst(HitPolicyFirst node) {
    CodeBlock.Builder statements = CodeBlock.builder()
        .addStatement("long start = System.currentTimeMillis()");
    
    DtFormulaSpec formula = visitFormula(body.getHeaders());
    if(!formula.getInputs().isEmpty()) {
      statements.addStatement("input = $L(input)", APPLY_INPUT_FORMULA);
    }
    
    int rowIndex = 0;
    boolean conditionEmpty = false;
    for (RuleRow row : node.getRows()) {
      DtCodeSpecPair pair = visitRuleRow(row);

      statements.add("\r\n");
      
      // control start
      if (!pair.getKey().isEmpty()) {
        statements.beginControlFlow("if($L)", pair.getKey()).add("\r\n");
      } else {
        conditionEmpty = true;
      }
      
      // generate token
      statements.add("$T token = $T.builder()", MetaToken.class, ImmutableMetaToken.class)
      .add("\r\n  .value($S)", row.getText().replaceAll("\\r|\\n", " ").replaceAll("\\s{2,}", " "))
      .add("\r\n  .start($T.builder().line($L).column($L).build())", ImmutableMetaStamp.class, row.getToken().getStartLine(), row.getToken().getStartCol())
      .add("\r\n  .end($T.builder().line($L).column($L).build())", ImmutableMetaStamp.class, row.getToken().getEndLine(), row.getToken().getEndCol())
      .addStatement(".build()")
      
      .add("$T meta = $T.builder()", DecisionTableMetaEntry.class, ImmutableDecisionTableMetaEntry.class)
      .add("\r\n  .id(0)")
      .add("\r\n  .index($L)", rowIndex++)
      .add("\r\n  .token(token)")
      .addStatement(".build()")
      
      .addStatement("return createResult(start, meta, $L)", pair.getValue());
      
      // Control end
      if (!pair.getKey().isEmpty()) {
        statements.endControlFlow();
      } 
    }
    
    if(!formula.getOutputs().isEmpty()) {
      statements.addStatement("result = $L(result)", APPLY_OUTPUT_FORMULA);
    }
    
    
    // first hit policy must always return something
    if (!conditionEmpty) {
      statements.addStatement("throw new $T($S)", DecisionTableHitPolicyFirstException.class, 
        "No rules where match for DT: '" + body.getId().getValue() + "' with hit policy 'FIRST'!");
    }
    
    ClassName outputName = naming.dt().outputValueMono(body);
    ParameterizedTypeName returnType = naming.dt().execution(body);

    return ImmutableDtMethodsSpec.builder()
        .addAllValue(formula.getInputs())
        .addAllValue(formula.getOutputs())
        .addValue(
          MethodSpec.methodBuilder("createResult")
            .addModifiers(Modifier.PROTECTED)
            .addParameter(ParameterSpec.builder(long.class, "start").build())
            .addParameter(ParameterSpec.builder(DecisionTableMetaEntry.class, "metaEntry").build())
            .addParameter(ParameterSpec.builder(outputName, "result").build())
            .returns(returnType)
            .addCode(CodeBlock.builder()
                .addStatement("long end = System.currentTimeMillis()")
                .addStatement("$T<Integer, $T> metaValues = new $T<>()", Map.class, DecisionTableMetaEntry.class, HashMap.class)
                .addStatement("metaValues.put(0, metaEntry)")
                
                .add("$T meta = $T.builder()", DecisionTableMeta.class, ImmutableDecisionTableMeta.class)
                .add("\r\n  ").add(".id($S).status($T.COMPLETED) ", body.getId().getValue(), ExecutionStatus.class)
                .add("\r\n  ").add(".start(start).end(end).time(end - start)")
                .add("\r\n  ").addStatement(".values(metaValues).build()", body.getId().getValue(), ExecutionStatus.class)
                
                .addStatement("$T.Builder<$T, $T> builder = $T.builder()", ImmutableExecution.class, DecisionTableMeta.class, outputName, ImmutableExecution.class)
                .addStatement("return builder.meta(meta).value(result).build()")
                .build())
            .build(),
          
          MethodSpec.methodBuilder("apply")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(ParameterSpec.builder(naming.dt().inputValue(body), "input").build())
              .returns(returnType)
              .addCode(statements.build())
              .build()
            
        ).build();
  }
  

  @Override
  public DtCodeValueSpec visitMatrixRow(MatrixRow node) {
    HitPolicyMatrix matrix = (HitPolicyMatrix) body.getHitPolicy();
    
    CodeBlock.Builder result = CodeBlock.builder()
        .add("// row $L \r\n", node.getTypeName().getValue());

    int index = 0;
    for (Literal literal : node.getValues()) {
      
      ScalarTypeDefNode header = (ScalarTypeDefNode) body.getHeaders().getValues().stream()
          .filter(t -> t.getName().equals(node.getTypeName().getValue())).findFirst().get();
      Rule rule = matrix.getRules().get(index);
      
      DtCodeSpec valueToSet = visitLiteral(literal);
      DtCodeSpec expression = visitMatrixRule(rule, header);
      var prefix = index > 0 ? "else " : "";
      result.beginControlFlow(prefix + "if($L)", expression.getValue());
      
      // generate token
      CodeBlock token = CodeBlock.builder().add("$T.builder()", ImmutableMetaToken.class)
          .add("\r\n    .value($S)", "not available")
          .add("\r\n    .start($T.builder().line($L).column($L).build())", ImmutableMetaStamp.class, literal.getToken().getStartLine(), literal.getToken().getStartCol())
          .add("\r\n    .end($T.builder().line($L).column($L).build())", ImmutableMetaStamp.class, literal.getToken().getEndLine(), literal.getToken().getEndCol())
          .add("\r\n    .build()")
          .build();
      
      result
      .add("metaValues.put(id, $T.builder()", ImmutableDecisionTableMetaEntry.class)
      .add("\r\n  .id(id++)")
      .add("\r\n  .index($L)", index++)
      .add("\r\n  .token($L)", token).addStatement(".build())")
      .addStatement("result.$L($L)", header.getName(), valueToSet.getValue())
      .endControlFlow();
      
    }
    return ImmutableDtCodeValueSpec.builder()
        .value(result.build())
        .build();
  }
  

  @Override
  public DtCodeSpecPair visitRuleRow(RuleRow node) {
    CodeBlock.Builder key = CodeBlock.builder();
    CodeBlock.Builder value = CodeBlock.builder().add("Immutable$T.builder()", naming.dt().outputValueFlux(body));
    boolean and = false;
    for (Rule rule : node.getRules()) {
      CodeBlock ruleCode = visitRule(rule).getValue();
      if (ruleCode.isEmpty()) {
        continue;
      }
      ScalarTypeDefNode header = (ScalarTypeDefNode) body.getHeaders().getValues().get(rule.getHeader());
      if (header.getDirection() == DirectionType.IN) {
        if (and) {
          key.add("\r\n  && ");
        }
        key.add(ruleCode);
        and = true;
      } else {
        value.add(ruleCode);
      }
    }
    return ImmutableDtCodeSpecPair.builder()
        .key(key.build()).value(value.add(".build()").build())
        .build();
  }

  @Override
  public DtCodeSpec visitRule(Rule node) {
    RuleValue value = node.getValue();
    if (value instanceof UndefinedValue) {
      return ImmutableDtCodeSpec.builder().type(ScalarType.BOOLEAN).value(CodeBlock.builder().build()).build();
    }
    ScalarTypeDefNode header = (ScalarTypeDefNode) body.getHeaders().getValues().get(node.getHeader());
    if (header.getDirection() == DirectionType.IN) {
      return visitMatrixRule(node, header);
    } else {
      DtCodeSpec codeSpec = visitLiteral(((LiteralValue) value).getValue());
      
      return ImmutableDtCodeSpec.builder()
          .value(CodeBlock.builder().add(".$L($L)", header.getName(), codeSpec.getValue()).build())
          .type(codeSpec.getType())
          .build();
    }
  }

  @Override
  public DtCodeSpec visitExpressionValue(ExpressionValue node) {
    DtCodeSpec child = visitExpressionRuleValue(node.getExpression());
    return ImmutableDtCodeSpec.builder()
        .value(CodeBlock.builder().add(child.getValue()).build())
        .type(child.getType())
        .build();
  }

  @Override
  public DtCodeSpec visitEqualityOperation(EqualityOperation node) {
    DtCodeSpec value1 = visitExpressionRuleValue(node.getLeft());
    DtCodeSpec value2 = visitExpressionRuleValue(node.getRight());
    
    EnConvertionSpec spec = EnJavaSpec.converter().src(node)
        .value1(value1.getValue(), value1.getType())
        .value2(value2.getValue(), value2.getType())
        .build();
    CodeBlock left = spec.getValue1();
    CodeBlock right = spec.getValue2();
    
    String operation;
    switch (node.getType()) {
    case EQUAL:
      operation = "$L.eq($L, $L)";
      break;
    case NOTEQUAL:
      operation = "$L.neq($L, $L)";
      break;
    case GREATER:
      operation = "$L.gt($L, $L)";
      break;
    case GREATER_THEN:
      operation = "$L.gte($L, $L)";
      break;
    case LESS:
      operation = "$L.lt($L, $L)";
      break;
    case LESS_THEN:
      operation = "$L.lte($L, $L)";
      break;
    default:
      throw new HdesCompilerException(HdesCompilerException.builder().unknownDTExpressionOperation(node));
    }
    return ImmutableDtCodeSpec.builder()
        .type(ScalarType.BOOLEAN)
        .value(CodeBlock.builder().add(operation, "when", left, right).build()).build();
  }

  @Override
  public DtCodeSpec visitInOperation(InOperation node) {
    StringBuilder values = new StringBuilder();
    for (Literal literal : node.getValues()) {
      if (values.length() > 0) {
        values.append(", ");
      }
      values.append(visitLiteral(literal).getValue().toString());
    }
    return ImmutableDtCodeSpec.builder()
        .value(CodeBlock.builder().add("when.asList($L).contains($L)", values.toString(), HEADER_REF).build())
        .type(ScalarType.BOOLEAN)
        .build();
  }

  @Override
  public DtCodeSpec visitNotOperation(NotUnaryOperation node) {
    CodeBlock child = visitExpressionRuleValue(node.getValue()).getValue();
    return ImmutableDtCodeSpec.builder()
        .value(CodeBlock.builder().add("!").add(child).build())
        .type(ScalarType.BOOLEAN)
        .build();
  }

  @Override
  public DtCodeSpec visitHeaderRefValue(HeaderRefValue node) {
    ScalarTypeDefNode header = (ScalarTypeDefNode) body.getHeaders().getValues().get(node.getIndex());
    return ImmutableDtCodeSpec.builder()
        .value(CodeBlock.builder().add(HEADER_REF).build())
        .type(header.getType())
        .build();
  }

  @Override
  public DtCodeSpec visitBetweenExpression(BetweenExpression node) {
    CodeBlock value = visitExpressionRuleValue(node.getValue()).getValue();
    CodeBlock left = visitExpressionRuleValue(node.getLeft()).getValue();
    CodeBlock right = visitExpressionRuleValue(node.getRight()).getValue();
    return ImmutableDtCodeSpec.builder()
        .value(CodeBlock.builder().add("when.between($L, $L, $L)", value, left, right).build())
        .type(ScalarType.BOOLEAN)
        .build();
  }

  @Override
  public DtCodeSpec visitAndOperation(AndOperation node) {
    CodeBlock left = visitExpressionRuleValue(node.getLeft()).getValue();
    CodeBlock right = visitExpressionRuleValue(node.getRight()).getValue();
    return ImmutableDtCodeSpec.builder()
        .value(CodeBlock.builder().add(left).add("\r\n  && ").add(right).build())
        .type(ScalarType.BOOLEAN)
        .build();
  }

  @Override
  public DtCodeSpec visitLiteral(Literal node) {
    CodeBlock.Builder code = CodeBlock.builder();
    if (node.getType() == ScalarType.DECIMAL) {
      code.add("new $T(\"$L\")", BigDecimal.class, node.getValue());
    } else if (node.getType() == ScalarType.DATE) {
      code.add("$T.parse($L)", LocalDate.class, node.getValue());
    } else if (node.getType() == ScalarType.DATE_TIME) {
      code.add("$T.parse($L)", LocalDateTime.class, node.getValue());
    } else if (node.getType() == ScalarType.TIME) {
      code.add("$T.parse($L)", LocalTime.class, node.getValue());
    } else if (node.getType() == ScalarType.STRING) {
      code.add("$S", node.getValue());
    } else {
      code.add(node.getValue());
    }
    return ImmutableDtCodeSpec.builder().value(code.build()).type(node.getType()).build();
  }
  
  @Override
  public DtCodeSpec visitNegateLiteralValue(NegateLiteralValue negate) {
    Literal node = negate.getValue();
    CodeBlock.Builder code = CodeBlock.builder();
    if (node.getType() == ScalarType.DECIMAL) {
      code.add("new $T(\"-$L\")", BigDecimal.class, node.getValue());
    } else if (node.getType() == ScalarType.INTEGER) {
      code.add("-$L", node.getValue());
    } else {
      throw new HdesCompilerException(HdesCompilerException.builder().unknownDTExpressionOperation(node));
    }
    return ImmutableDtCodeSpec.builder().value(code.build()).type(node.getType()).build();
  }
  
  private DtCodeSpec visitMatrixRule(Rule node, ScalarTypeDefNode header) {
    RuleValue value = node.getValue();
    String getMethod = JavaSpecUtil.methodName(header.getName());
    
    // optional type
    if(!header.getRequired()) {
      getMethod = getMethod + "()" + ".get";
    }
    
    if (value instanceof LiteralValue) {
      
      Literal literal = ((LiteralValue) value).getValue();
      CodeBlock literalCode = visitLiteral(literal).getValue();
      CodeBlock.Builder exp = CodeBlock.builder();
      
      if (literal.getType() == ScalarType.DECIMAL) {
        exp.add("input.$L().compareTo($L) == 0", getMethod, literalCode);
      } else if (literal.getType() == ScalarType.DATE) {
        exp.add("input.$L().compareTo($L) == 0", getMethod, literalCode);
      } else if (literal.getType() == ScalarType.DATE_TIME) {
        exp.add("input.$L().compareTo($L) == 0", getMethod, literalCode);
      } else if (literal.getType() == ScalarType.TIME) {
        exp.add("input.$L().compareTo($L) == 0", getMethod, literalCode);
      } else if (literal.getType() == ScalarType.STRING) {
        exp.add("input.$L().equals($L)", getMethod, literalCode);
      } else {
        exp.add("input.$L() == $L", getMethod, literalCode);
      }
      return ImmutableDtCodeSpec.builder().type(literal.getType()).value(exp.build()).build();
      
    } else if (value instanceof ExpressionValue) {
      
      DtCodeSpec result = visitExpressionValue(((ExpressionValue) value));
      String inputName = CodeBlock.builder().add("input.$L()", getMethod).build().toString();
      
      return ImmutableDtCodeSpec.builder()
          .value(CodeBlock.builder().add(result.getValue().toString().replaceAll(HEADER_REF, inputName)).build())
          .type(result.getType())
          .build();
      
    } else if(value instanceof UndefinedValue) {
      return ImmutableDtCodeSpec.builder()
          .value(CodeBlock.builder().add("true").build())
          .type(ScalarType.BOOLEAN)
          .build();
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownDTInputRule(node));
  }
 
  private DtCodeSpec visitExpressionRuleValue(AstNode node) {
    if (node instanceof Literal) {
      return visitLiteral((Literal) node);
    } else if (node instanceof HeaderRefValue) {
      return visitHeaderRefValue((HeaderRefValue) node);
    } else if (node instanceof InOperation) {
      return visitInOperation((InOperation) node);
    } else if (node instanceof NotUnaryOperation) {
      return visitNotOperation((NotUnaryOperation) node);
    } else if (node instanceof EqualityOperation) {
      return visitEqualityOperation((EqualityOperation) node);
    } else if (node instanceof AndOperation) {
      return visitAndOperation((AndOperation) node);
    } else if (node instanceof BetweenExpression) {
      return visitBetweenExpression((BetweenExpression) node);
    } else if(node instanceof NegateLiteralValue) {
      return visitNegateLiteralValue((NegateLiteralValue) node);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownDTExpressionNode(node));
  }
  
  private DtMethodsSpec visitHitPolicy(HitPolicy node) {
    if (node instanceof HitPolicyAll) {
      return visitHitPolicyAll((HitPolicyAll) node);
    } else if (node instanceof HitPolicyMatrix) {
      return visitHitPolicyMatrix((HitPolicyMatrix) node);
    }
    return visitHitPolicyFirst((HitPolicyFirst) node);
  }
}
