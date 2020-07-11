package io.resys.hdes.compiler.spi.java.visitors.en;

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
    private EnCodeSpec left;
    private EnCodeSpec right;
    
    public TypeConverter src(AstNode src) {
      this.src = src;
      return this;
    }
    
    public TypeConverter value1(EnCodeSpec left) {
      this.left = left;
      return this;
    }
    
    public TypeConverter value2(EnCodeSpec right) {
      this.right = right;
      return this;      
    }
    
    public EnConvertionSpec build() {
      Assertions.notNull(src, () -> "src side cant be null!");
      Assertions.notNull(left, () -> "left side cant be null!");
      Assertions.notNull(right, () -> "left side cant be null!");
      
      // Everything matches both are same
      if(left.getType() == right.getType()) {
        return ImmutableEnConvertionSpec.builder()
            .type(left.getType())
            .left(left.getValue())
            .right(right.getValue())
            .build();
      }
      
      // numerical conversion, integer to big decimal
      if(isDecimalConvert(left.getType(), right.getType())) {
        CodeBlock left;
        CodeBlock right;
        
        if(this.left.getType() == ScalarType.INTEGER) {
          left = CodeBlock.builder().add("new $T(", BigDecimal.class).add(this.left.getValue()).add(")").build();
          right = this.right.getValue();
        } else {
          left = this.left.getValue();
          right = CodeBlock.builder().add("new $T(", BigDecimal.class).add(this.right.getValue()).add(")").build();
        }

        return ImmutableEnConvertionSpec.builder()
            .type(ScalarType.DECIMAL)
            .left(left)
            .right(right)
            .build();
      }
      
      // string to date time, date, time
      if(isTemporalConvert(left.getType(), right.getType())) {
        CodeBlock left;
        CodeBlock right;
        
        if(this.left.getType() == ScalarType.STRING) {
          left = CodeBlock.builder().add("$T.parse(", JavaSpecUtil.type(this.right.getType())).add(this.left.getValue()).add(")").build();
          right = this.right.getValue();
        } else {
          left = this.left.getValue();
          right = CodeBlock.builder().add("$T.parse(", JavaSpecUtil.type(this.left.getType())).add(this.right.getValue()).add(")").build();
        }

        return ImmutableEnConvertionSpec.builder()
            .type(ScalarType.DECIMAL)
            .left(left)
            .right(right)
            .build();
      }
     
      throw new HdesCompilerException(HdesCompilerException.builder().incompatibleConversion(src, left.getType(), right.getType()));
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
