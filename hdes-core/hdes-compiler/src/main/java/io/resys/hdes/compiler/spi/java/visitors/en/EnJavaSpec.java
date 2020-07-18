package io.resys.hdes.compiler.spi.java.visitors.en;

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
import java.util.List;
import java.util.function.Function;

import org.immutables.value.Value;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarType;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeName;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.visitors.JavaSpecUtil;

public interface EnJavaSpec {

  @FunctionalInterface
  interface TypeNameResolver {
    TypeDefNode accept(TypeName name);
  }
  
  @Value.Immutable
  interface EnRefSpec extends EnJavaSpec {
    List<AstNode> getValues();
  }
  
  @Value.Immutable
  interface EnCodeSpec extends EnJavaSpec {
    CodeBlock getValue();
    ScalarType getType();
  }
  
  
  @Value.Immutable
  interface EnConvertionSpec extends EnJavaSpec {
    CodeBlock getValue1();
    CodeBlock getValue2();
    ScalarType getType();
  }
  
  
  public static TypeConverter converter() {
    return new TypeConverter();
  }
  
  public static class TypeConverter {
    private final static Function<ScalarType, Boolean> stringType = (t) -> t == ScalarType.STRING;
    private final static Function<ScalarType, Boolean> temporalType = (t) -> t == ScalarType.DATE || t == ScalarType.DATE_TIME || t == ScalarType.TIME;
    
    private AstNode src;
    private EnCodeSpec value1;
    private EnCodeSpec value2;
    
    public TypeConverter src(AstNode src) {
      this.src = src;
      return this;
    }
    
    public TypeConverter value1(EnCodeSpec value1) {
      this.value1 = value1;
      return this;
    }
    
    public TypeConverter value1(CodeBlock value1, ScalarType type) {
      this.value1 = ImmutableEnCodeSpec.builder().value(value1).type(type).build();;
      return this;
    }
    
    public TypeConverter value2(EnCodeSpec value2) {
      this.value2 = value2;
      return this;      
    }
    
    public TypeConverter value2(CodeBlock value2, ScalarType type) {
      this.value2 = ImmutableEnCodeSpec.builder().value(value2).type(type).build();
      return this;      
    }
    
    public EnConvertionSpec build() {
      Assertions.notNull(src, () -> "src side cant be null!");
      Assertions.notNull(value1, () -> "left side cant be null!");
      Assertions.notNull(value2, () -> "left side cant be null!");
      
      // Everything matches both are same
      if(value1.getType() == value2.getType()) {
        return ImmutableEnConvertionSpec.builder()
            .type(value1.getType())
            .value1(value1.getValue())
            .value2(value2.getValue())
            .build();
      }
      
      // numerical conversion, integer to big decimal
      if(isDecimalConvert(value1.getType(), value2.getType())) {
        CodeBlock value1;
        CodeBlock value2;
        
        if(this.value1.getType() == ScalarType.INTEGER) {
          value1 = CodeBlock.builder().add("new $T(", BigDecimal.class).add(this.value1.getValue()).add(")").build();
          value2 = this.value2.getValue();
        } else {
          value1 = this.value1.getValue();
          value2 = CodeBlock.builder().add("new $T(", BigDecimal.class).add(this.value2.getValue()).add(")").build();
        }

        return ImmutableEnConvertionSpec.builder()
            .type(ScalarType.DECIMAL)
            .value1(value1)
            .value2(value2)
            .build();
      }
      
      // string to date time, date, time
      if(isTemporalConvert(value1.getType(), value2.getType())) {
        CodeBlock value1;
        CodeBlock value2;
        ScalarType type;
        if(this.value1.getType() == ScalarType.STRING) {
          Class<?> temporalType = JavaSpecUtil.type(this.value2.getType());
          value1 = CodeBlock.builder().add("$T.parse($L)", temporalType, this.value1.getValue()).build();
          value2 = this.value2.getValue();
          type = this.value2.getType();
        } else {
          Class<?> temporalType = JavaSpecUtil.type(this.value1.getType());
          value1 = this.value1.getValue();
          value2 = CodeBlock.builder().add("$T.parse($L)", temporalType, this.value2.getValue()).build();
          type = this.value1.getType();
        }

        return ImmutableEnConvertionSpec.builder()
            .type(type)
            .value1(value1)
            .value2(value2)
            .build();
      }
     
      throw new HdesCompilerException(HdesCompilerException.builder().incompatibleConversion(src, value1.getType(), value2.getType()));
    }
    
    
    
    private static boolean isDecimalConvert(ScalarType type1, ScalarType type2) {
      Function<ScalarType, Boolean> intOrDecimal = (t) -> t == ScalarType.INTEGER || t == ScalarType.DECIMAL;
      return intOrDecimal.apply(type1) && intOrDecimal.apply(type2);
    }

    private static boolean isTemporalConvert(ScalarType type1, ScalarType type2) {
      return stringType.apply(type1) && temporalType.apply(type2) ||
          stringType.apply(type2) && temporalType.apply(type1);
    }
  }
}
