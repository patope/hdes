package io.resys.hdes.compiler.spi.java.fl;

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

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.naming.Namings;

public class FlApiSpec {

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
    
    public TypeSpec build() {
      Assertions.notNull(body, () -> "body must be defined!");
      
      final ClassName interfaceName = namings.fl().api(body);
      final TypeName superinterface = namings.fl().executable(body);
      final AnnotationSpec annotation = AnnotationSpec.builder(javax.annotation.processing.Generated.class)
          .addMember("value", "$S", FlApiSpec.class.getCanonicalName()).build();

      return TypeSpec.interfaceBuilder(interfaceName).addModifiers(Modifier.PUBLIC).addAnnotation(annotation)
        .addSuperinterface(superinterface)
        .addTypes(FlHeadersSpec.builder(namings).body(body).build())
        .addTypes(FlStateSpec.builder(namings).body(body).build())
        .build();
    }
  }
}
