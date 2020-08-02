package io.resys.hdes.compiler.spi.naming;

import java.io.IOException;
import java.io.UncheckedIOException;

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
import java.util.Optional;

import javax.lang.model.element.Modifier;

import org.immutables.value.Value.Immutable;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.AstNode.ScalarType;
import io.resys.hdes.compiler.api.HdesCompiler.TypeName;
import io.resys.hdes.compiler.api.ImmutableTypeName;

public class JavaSpecUtil {
  
  
  public static TypeSpec.Builder immutableSpec(ClassName name) {
    ClassName immutable = immutable(name);
    return TypeSpec
      .interfaceBuilder(name)
      .addAnnotation(Immutable.class)
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addAnnotation(AnnotationSpec
          .builder(ClassName.get("com.fasterxml.jackson.databind.annotation", "JsonSerialize"))
          .addMember("as", "$T.class", immutable)
          .build())
      
      .addAnnotation(AnnotationSpec.builder(ClassName.get("com.fasterxml.jackson.databind.annotation", "JsonDeserialize")).addMember("as", "$T.class", immutable).build());    
  }
  
  public static String javaFile(TypeSpec spec, String pkg) {
    try {
      StringBuilder appendable = new StringBuilder();
      JavaFile file = JavaFile.builder(pkg, spec).build();
      file.writeTo(appendable);
      return appendable.toString();
    } catch (IOException e) {
      throw new UncheckedIOException(e.getMessage(), e);
    }
  }

  public static TypeName typeName(ClassName name) {
    return ImmutableTypeName.builder().name(name.simpleName()).pkg(name.packageName()).build();
  }
  
  public static ClassName immutable(ClassName src) {
    String pkg = src.packageName();
    String top = pkg.substring(0, pkg.lastIndexOf("."));
    return ClassName.get(top, "Immutable" + src.simpleName());
  }
  
  public static ParameterizedTypeName optional(ClassName src) {
    return ParameterizedTypeName.get(ClassName.get(Optional.class), src);
  }
  
  public static ParameterizedTypeName optional(ScalarType entry) {
    return optional(ClassName.get(type(entry)));
  }
  
  public static ClassName immutableBuilder(ClassName src) {
    ClassName type = immutable(src);
    return ClassName.get(type.packageName(), type.simpleName() + ".Builder");
  }  
  
  public static String methodCall(String name) {
    //turns given ref into "get + (N)ame + ()"
    String[] src = name.split("\\.");
    StringBuilder result = new StringBuilder();
    for(String target : src) {
      if(result.length() > 0) {
        result.append(".");
      }
      result.append("get").append(capitalize(target)).append("()");
    }
    return result.toString();
  }
  
  public static String methodVarCall(String name) {
    //turns given ref into "get + (N)ame + ()"
    String[] src = name.split("\\.");
    StringBuilder result = new StringBuilder();
    for(String target : src) {
      if(result.length() > 0) {
        result.append(".").append("get").append(capitalize(target)).append("()");;
      } else {
        result.append(target);
      }
    }
    return result.toString();
  }
  
  public static String methodName(String name) {
    //turns given ref into "get + (N)ame"
    return new StringBuilder()
        .append("get")
        .append(capitalize(name))
        .toString();
  }

  public static String capitalize(String name) {
    return new StringBuilder()
        .append(name.substring(0, 1).toUpperCase())
        .append(name.length() == 1 ? "" : name.substring(1))
        .toString();
  }
  public static String decapitalize(String name) {
    return new StringBuilder()
        .append(name.substring(0, 1).toLowerCase())
        .append(name.length() == 1 ? "" : name.substring(1))
        .toString();
  }
  
  public static ClassName typeName(ScalarType entry) {
    return ClassName.get(type(entry));
  }
  
  public static Class<?> type(ScalarType entry) {
    switch (entry) {
    case BOOLEAN:
      return boolean.class;
    case DATE:
      return LocalDate.class;
    case DATE_TIME:
      return LocalDateTime.class;
    case TIME:
      return LocalTime.class;
    case DECIMAL:
      return BigDecimal.class;
    case INTEGER:
      return Integer.class;
    case STRING:
      return String.class;
    default:
      throw new IllegalArgumentException("Unimplemented type: " + entry + "!");
    }
  }
}
