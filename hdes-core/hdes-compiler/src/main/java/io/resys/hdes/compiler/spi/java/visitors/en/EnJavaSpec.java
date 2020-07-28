package io.resys.hdes.compiler.spi.java.visitors.en;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarType;



public interface EnJavaSpec {

  
  @Value.Immutable
  interface EnRefSpec extends EnJavaSpec {
    List<AstNode> getValues();
  }
  
  @Value.Immutable
  interface EnScalarCodeSpec extends EnJavaSpec {
    CodeBlock getValue();
    Optional<Boolean> getArray();
    ScalarType getType();
  }
  
  @Value.Immutable
  interface EnObjectCodeSpec extends EnJavaSpec {
    CodeBlock getValue();
    Optional<Boolean> getArray();
    ObjectTypeDefNode getType();
  }
  
  @Value.Immutable
  interface EnConvertionSpec extends EnJavaSpec {
    CodeBlock getValue1();
    CodeBlock getValue2();
    ScalarType getType();
  }
  
  @Value.Immutable
  interface EnListConvertionSpec extends EnJavaSpec {
    List<CodeBlock> getValue();
    ScalarType getType();
  }
  
  public static ListTypeConverter listConverter() {
    return new ListTypeConverter();
  }
  
  public static TypeConverter converter() {
    return new TypeConverter();
  }
  
}
