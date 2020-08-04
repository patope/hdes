package io.resys.hdes.compiler.spi.java.dt;

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

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.executor.api.ImmutableMetaStamp;
import io.resys.hdes.executor.api.ImmutableMetaToken;

public class DtTokenSpec {

  public static CodeBlock build(AstNode node, String value) {
    return CodeBlock.builder().add("$T.builder()", ImmutableMetaToken.class)
        .add("\r\n    .value($S)", value.replaceAll("\\r|\\n", " ").replaceAll("\\s{2,}", " "))
        .add("\r\n    .start($T.builder().line($L).column($L).build())", ImmutableMetaStamp.class, node.getToken().getStartLine(), node.getToken().getStartCol())
        .add("\r\n    .end($T.builder().line($L).column($L).build())", ImmutableMetaStamp.class, node.getToken().getEndLine(), node.getToken().getEndCol())
        .add("\r\n    .build()")
        .build();
  }
}
