package io.resys.hdes.compiler.spi.java.fl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import org.immutables.value.Value;

import com.squareup.javapoet.AnnotationSpec;
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
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.HdesExecutable;

public class FlApiSpec {

  @Value.Immutable
  interface FlHeaderSpec {
    MethodSpec getMethod();
    List<TypeSpec> getNested();
  }
  
  public static Builder builder(Namings namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }

  public static class Builder {
    private final Namings namings;
    private FlowBody body;

    private Builder(Namings namings) {
      super();
      this.namings = namings;
    }

    public Builder body(FlowBody body) {
      this.body = body;
      return this;
    }

    /**
     * @return entity interfaces from headers
     */
    private List<TypeSpec> headers() {
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
    
    public TypeSpec build() {
      Assertions.notNull(body, () -> "body must be defined!");
      final ClassName interfaceName = namings.fl().api(body);
      final TypeName superinterface = namings.fl().executable(body);
      final AnnotationSpec annotation = AnnotationSpec.builder(javax.annotation.processing.Generated.class)
          .addMember("value", "$S", FlApiSpec.class.getCanonicalName()).build();

      final List<TypeSpec> headers = headers();

      return TypeSpec.interfaceBuilder(interfaceName).addModifiers(Modifier.PUBLIC).addAnnotation(annotation)
          .addSuperinterface(superinterface).addTypes(headers).build();

    }
  }
}