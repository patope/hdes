package io.resys.hdes.compiler.spi.java.dt;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicy;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyMatrix;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.MatrixRow;
import io.resys.hdes.ast.api.nodes.ImmutableScalarTypeDefNode;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.DecisionTableMeta.DecisionTableStaticValue;
import io.resys.hdes.executor.api.HdesExecutable;

public class DtApiSpec {

  public static Builder builder(Namings namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }

  public static class Builder {
    private final Namings namings;
    private DecisionTableBody body;

    private Builder(Namings namings) {
      super();
      this.namings = namings;
    }

    public Builder body(DecisionTableBody body) {
      this.body = body;
      return this;
    }

    /**
     * @return entity interface for representing defined outputs as static data
     */
    private TypeSpec statics() {
      final HitPolicy hitPolicy = body.getHitPolicy();
      final TypeSpec.Builder builder = JavaSpecUtil.immutableSpec(namings.dt().staticValue(body));
      final TypeName output;
      
      if (hitPolicy instanceof HitPolicyMatrix) {
        HitPolicyMatrix matrix = (HitPolicyMatrix) hitPolicy;

        Class<?> type = JavaSpecUtil.type(matrix.getToType());
        output = ParameterizedTypeName.get(List.class, type);

        for (MatrixRow row : matrix.getRows()) {
          String headerName = row.getTypeName().getValue();
          builder.addMethod(MethodSpec.methodBuilder(JavaSpecUtil.methodName(headerName))
              .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).returns(output).build());
        }

      } else if (hitPolicy instanceof HitPolicyAll) {
        @SuppressWarnings("unused")
        HitPolicyAll all = (HitPolicyAll) hitPolicy;
        output = namings.dt().outputValueMono(body);
        
      } else {
        @SuppressWarnings("unused")
        HitPolicyFirst first = (HitPolicyFirst) hitPolicy;
        output = namings.dt().outputValueMono(body);
      }


      return builder
          .addSuperinterface(ParameterizedTypeName.get(ClassName.get(DecisionTableStaticValue.class), output))
          .build();

    }

    /**
     * @return entity interfaces from headers
     */
    private List<TypeSpec> headers() {
      final boolean isMatrix = body.getHitPolicy() instanceof HitPolicyMatrix;
      final boolean isAll = body.getHitPolicy() instanceof HitPolicyAll;

      TypeSpec.Builder inputBuilder = JavaSpecUtil.immutableSpec(namings.dt().inputValue(body))
          .addSuperinterface(HdesExecutable.InputValue.class);

      TypeSpec.Builder outputBuilder = JavaSpecUtil.immutableSpec(namings.dt().outputValueFlux(body))
          .addSuperinterface(HdesExecutable.OutputValue.class);

      // normal input/outputs
      for (TypeDefNode header : body.getHeaders().getValues()) {
        MethodSpec method = header(header);
        if (header.getDirection() == DirectionType.IN) {
          inputBuilder.addMethod(method);
          continue;
        }

        if (isMatrix) {
          if (header.getRequired()) {
            throw new HdesCompilerException(HdesCompilerException.builder().dtHeaderOutputMatrixCantBeRequired(header));
          }
          final boolean isFormulaEmpty = ((ScalarTypeDefNode) header).getFormula().isEmpty();
          if (isFormulaEmpty) {
            throw new HdesCompilerException(
                HdesCompilerException.builder().dtHeaderOutputMatrixHasToHaveFormula(header));
          }
        }
        outputBuilder.addMethod(method);

      }

      // type from matrix hit policy
      if (isMatrix) {
        HitPolicyMatrix matrix = (HitPolicyMatrix) body.getHitPolicy();
        for (MatrixRow row : matrix.getRows()) {
          String headerName = row.getTypeName().getValue();
          ScalarTypeDefNode scalar = (ScalarTypeDefNode) body.getHeaders().getValues().stream()
              .filter(t -> t.getName().equals(headerName)).findFirst().get();
          MethodSpec method = header(
              ImmutableScalarTypeDefNode.builder().from(scalar).token(row.getToken()).type(matrix.getToType()).build());
          outputBuilder.addMethod(method);
        }

        // wrapper for list type
      } else if (isAll) {

        TypeSpec.Builder collectionType = JavaSpecUtil.immutableSpec(namings.dt().outputValueMono(body))
            .addSuperinterface(HdesExecutable.OutputValue.class)
            .addMethod(MethodSpec.methodBuilder(JavaSpecUtil.methodName("values"))
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), namings.dt().outputValueFlux(body)))
                .build());

        return Arrays.asList(inputBuilder.build(), outputBuilder.build(), collectionType.build());
      }

      return Arrays.asList(inputBuilder.build(), outputBuilder.build());
    }

    /**
     * @return entity field get method
     */
    private MethodSpec header(TypeDefNode node) {
      ScalarTypeDefNode scalar = (ScalarTypeDefNode) node;
      return MethodSpec.methodBuilder(JavaSpecUtil.methodName(node.getName()))
          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
          .returns(
              scalar.getRequired() ? JavaSpecUtil.typeName(scalar.getType()) : JavaSpecUtil.optional(scalar.getType()))
          .build();
    }

    public TypeSpec build() {
      Assertions.notNull(body, () -> "body must be defined!");
      final ClassName interfaceName = namings.dt().api(body);
      final TypeName superinterface = namings.dt().executable(body);
      final AnnotationSpec annotation = AnnotationSpec.builder(javax.annotation.processing.Generated.class)
          .addMember("value", "$S", DtApiSpec.class.getCanonicalName()).build();

      final List<TypeSpec> headers = headers();
      final TypeSpec statics = statics();

      return TypeSpec.interfaceBuilder(interfaceName).addModifiers(Modifier.PUBLIC).addAnnotation(annotation)
          .addSuperinterface(superinterface).addTypes(headers).addType(statics).build();

    }
  }
}
