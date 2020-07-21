package io.resys.hdes.compiler.spi.java.visitors.en;

import java.util.List;

import org.immutables.value.Value;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarType;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeName;



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
