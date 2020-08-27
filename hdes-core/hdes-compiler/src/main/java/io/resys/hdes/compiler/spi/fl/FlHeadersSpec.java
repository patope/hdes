package io.resys.hdes.compiler.spi.fl;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import org.immutables.value.Value;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.CompilerContext;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.executor.api.HdesExecutable;

public class FlHeadersSpec {

  @Value.Immutable
  interface FlHeaderSpec {
    MethodSpec getMethod();
    List<TypeSpec> getNested();
  }
  
  public static Builder builder(CompilerContext namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }

  public static class Builder {
    private final CompilerContext namings;
    private FlowBody body;

    private Builder(CompilerContext namings) {
      super();
      this.namings = namings;
    }

    public Builder body(FlowBody body) {
      this.body = body;
      return this;
    }

    
    /**
     * @return entity field get method
     */
    private FlHeaderSpec header(TypeDef node) {
      
      // scalar def
      if (node instanceof ScalarDef) {
        MethodSpec method = MethodSpec.methodBuilder(JavaSpecUtil.methodName(node.getName()))
          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
          .returns(returnType(node)).build();
        return ImmutableFlHeaderSpec.builder().method(method).build();
      }
      
      // object def
      final ObjectDef objectDef = (ObjectDef) node;
      final ClassName typeName;
      final Class<?> superinterface; 
      if(node.getDirection() == DirectionType.IN) {
        typeName = namings.fl().inputValue(body, objectDef);
        superinterface = HdesExecutable.InputValue.class;
      } else {
        typeName = namings.fl().outputValue(body, objectDef);
        superinterface = HdesExecutable.OutputValue.class;
      }
      
      final TypeSpec.Builder objectBuilder = JavaSpecUtil
          .immutableSpec(typeName)
          .addSuperinterface(superinterface);
      
      final List<TypeSpec> nested = new ArrayList<>();
      for (TypeDef input : objectDef.getValues()) {
        FlHeaderSpec spec = header(input);
        nested.addAll(spec.getNested());
        objectBuilder.addMethod(spec.getMethod());
      }
      
      final MethodSpec method = MethodSpec.methodBuilder(JavaSpecUtil
          .methodName(node.getName()))
          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
          .returns(returnType(node)).build();
      return ImmutableFlHeaderSpec.builder()
          .nested(nested).addNested(objectBuilder.build())
          .method(method).build();
    }
    
    /*
     * Return type of the header
     */
    private TypeName returnType(TypeDef node) {
      final TypeName typeName;
      if(node instanceof ScalarDef) {
        ScalarDef scalar = (ScalarDef) node;  
        typeName = ClassName.get(JavaSpecUtil.type(scalar.getType()));
      } else {
        ObjectDef objectDef = (ObjectDef) node;
        if(node.getDirection() == DirectionType.IN) {
          typeName = namings.fl().inputValue(body, objectDef);
        } else {
          typeName = namings.fl().outputValue(body, objectDef);
        }
      }
      if(node.getArray()) {
        return ParameterizedTypeName.get(ClassName.get(List.class), typeName);
      } else if(node.getRequired()) {
        return typeName;
      }
      return ParameterizedTypeName.get(ClassName.get(Optional.class), typeName);
    }
    
    public List<TypeSpec> build() {
      Assertions.notNull(body, () -> "body must be defined!");
      
      TypeSpec.Builder inputBuilder = JavaSpecUtil.immutableSpec(namings.fl().inputValue(body))
          .addSuperinterface(HdesExecutable.InputValue.class);

      TypeSpec.Builder outputBuilder = JavaSpecUtil.immutableSpec(namings.fl().outputValue(body))
          .addSuperinterface(HdesExecutable.OutputValue.class);

      
      List<TypeSpec> result = new ArrayList<>();
      for(TypeDef typeDef : body.getHeaders().getValues()) {
        FlHeaderSpec spec = header(typeDef);
        result.addAll(spec.getNested());
        TypeSpec.Builder builder = typeDef.getDirection() == DirectionType.IN ? inputBuilder : outputBuilder;
        builder.addMethod(spec.getMethod());
      }
      result.add(inputBuilder.build());
      result.add(outputBuilder.build());
      return result;
    }
  }
}
