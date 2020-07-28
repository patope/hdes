package io.resys.hdes.compiler.spi.java.visitors.en;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarType;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.visitors.en.EnJavaSpec.EnListConvertionSpec;
import io.resys.hdes.compiler.spi.java.visitors.en.EnJavaSpec.EnScalarCodeSpec;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;

public class ListTypeConverter {
  private final List<EnScalarCodeSpec> values = new ArrayList<>();
  private final Set<ScalarType> types = new HashSet<>();
  private AstNode src;
  
  public ListTypeConverter src(AstNode src) {
    this.src = src;
    return this;
  }
  public ListTypeConverter value(EnScalarCodeSpec value) {
    this.values.add(value);
    types.add(value.getType());
    return this;
  }
  
  public ListTypeConverter value(CodeBlock value1, ScalarType type) {
    this.values.add(ImmutableEnScalarCodeSpec.builder().value(value1).type(type).build());
    types.add(type);
    return this;
  }
  
  public EnListConvertionSpec build() {
    Assertions.notNull(src, () -> "src side cant be null!");
    Assertions.isTrue(types.size() > 0, () -> "there has to be at least one value!");
    
    if(types.size() == 1) {
      return ImmutableEnListConvertionSpec.builder()
          .addAllValue(this.values.stream().map(t -> t.getValue()).collect(Collectors.toList()))
          .type(types.iterator().next())
          .build();
    }

    // Decimal conversion
    if(types.size() == 2 && types.contains(ScalarType.DECIMAL) && types.contains(ScalarType.INTEGER) ) {
      
      List<CodeBlock> result = new ArrayList<>();
      for(EnScalarCodeSpec spec : this.values) {
        if(spec.getType() == ScalarType.INTEGER) {
          result.add(CodeBlock.builder().add("new $T(", BigDecimal.class).add(spec.getValue()).add(")").build());
        } else {
          result.add(spec.getValue());
        }
      }
      
      return ImmutableEnListConvertionSpec.builder().value(result).type(ScalarType.DECIMAL).build();
      
    } else if(types.size() == 2 && types.contains(ScalarType.STRING) && 
        (types.contains(ScalarType.DATE_TIME) || types.contains(ScalarType.DATE) || types.contains(ScalarType.TIME))) {
     
      ScalarType type = types.stream().filter(t -> t != ScalarType.STRING).findFirst().get();
      Class<?> temporalType = JavaSpecUtil.type(type);
      
      List<CodeBlock> result = new ArrayList<>();
      for(EnScalarCodeSpec spec : this.values) {
        if(spec.getType() == ScalarType.STRING) {
          result.add(CodeBlock.builder().add("$T.parse($L)", temporalType, spec.getValue()).build());
        } else {
          result.add(spec.getValue());
        }
      }
      
      return ImmutableEnListConvertionSpec.builder().value(result).type(type).build();
    }
    
    
    throw new HdesCompilerException(HdesCompilerException.builder().incompatibleConversion(src, types.toArray(new ScalarType[] {})));
  }
}

