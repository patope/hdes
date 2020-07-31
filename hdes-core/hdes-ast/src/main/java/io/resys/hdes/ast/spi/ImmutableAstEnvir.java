package io.resys.hdes.ast.spi;

import java.util.ArrayList;

/*-
 * #%L
 * hdes-ast
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import io.resys.hdes.ast.HdesLexer;
import io.resys.hdes.ast.HdesParser;
import io.resys.hdes.ast.api.AstEnvir;
import io.resys.hdes.ast.api.AstNodeException;
import io.resys.hdes.ast.api.nodes.AstNode.Body;
import io.resys.hdes.ast.api.nodes.AstNode.ErrorNode;
import io.resys.hdes.ast.api.nodes.AstNode.Token;
import io.resys.hdes.ast.api.nodes.ImmutableBodyId;
import io.resys.hdes.ast.api.nodes.ImmutableEmptyBody;
import io.resys.hdes.ast.api.nodes.ImmutableHeaders;
import io.resys.hdes.ast.api.nodes.ImmutableToken;
import io.resys.hdes.ast.spi.errors.HdesAntlrErrorListener;
import io.resys.hdes.ast.spi.validators.BodyValidatorVisitor;
import io.resys.hdes.ast.spi.visitors.ast.HdesParserAstNodeVisitor;
import io.resys.hdes.ast.spi.visitors.ast.util.Nodes.TokenIdGenerator;

public class ImmutableAstEnvir implements AstEnvir {
  
  private final Map<String, Body> body;
  private final Map<String, String> src;
  private final Map<String, List<ErrorNode>> errors;

  public ImmutableAstEnvir(Map<String, Body> body, Map<String, String> src,  Map<String, List<ErrorNode>> errors) {
    super();
    this.body = body;
    this.src = src;
    this.errors = errors;
  }
  @Override
  public Map<String, Body> getBody() {
    return body;
  }
  @Override
  public Body getBody(String id) {
    Assertions.isTrue(body.containsKey(id), () ->
      new StringBuilder("No node by given identifier: ").append(id).append("!").append(System.lineSeparator())
      .append("Known identifiers: ").append(body.keySet()).toString()
    );
    
    return body.get(id);
  }
  @Override
  public String getSrc(String id) {
    Assertions.isTrue(src.containsKey(id), () ->
      new StringBuilder("No src by given identifier: ").append(id).append("!").append(System.lineSeparator())
      .append("Known identifiers: ").append(src.keySet()).toString()
    );
    return src.get(id);
  }
  @Override
  public List<ErrorNode> getErrors(String id) {
    Assertions.isTrue(src.containsKey(id), () ->
      new StringBuilder("No src by given identifier: ").append(id).append("!").append(System.lineSeparator())
      .append("Known identifiers: ").append(src.keySet()).toString()
    );
    return errors.get(id);
  }
  
  @Override
  public Body getByAstId(String bodyId) {
    Optional<Body> result = body.values().stream().filter(b -> b.getId().getValue().equals(bodyId)).findFirst();
    Assertions.isTrue(result.isPresent(), () -> 
      new StringBuilder("No body node by given identifier: ").append(bodyId).append("!").append(System.lineSeparator())
      .append("Known identifiers: ").append(body.values().stream().map(b -> b.getId().getValue()).collect(Collectors.toList())).toString());
    return result.get();
  }
  
  @Override
  public Map<String, List<ErrorNode>> getErrors() {
    return errors;
  }
  
  public static Builder builder() {
    return new GenericBuilder();
  }
  
  public static class GenericBuilder implements Builder {

    private final Map<String, Body> body = new HashMap<>();
    private final Map<String, String> src = new HashMap<>();
    private final Map<String, List<ErrorNode>> errors = new HashMap<>();
    private final List<String> toBeRemoved = new ArrayList<>();
    private boolean ignoreErrors;
    private AstEnvir from;
    
    @Override
    public Builder ignoreErrors() {
      this.ignoreErrors = true;
      return this;
    }
    @Override
    public Builder delete(String id) {
      this.toBeRemoved.add(id);
      return this;
    }
    @Override
    public Builder from(AstEnvir envir) {
      this.from = envir;
      return this;
    }
    @Override
    public AstEnvir build() {
      if(from != null) {
        from.getBody().keySet().stream()
        .filter(id -> !toBeRemoved.contains(id))
        .filter(id -> !src.containsKey(id))
        .forEach(id -> add().externalId(id).src(from.getSrc(id)));
      }
      
      // TODO :: Post processing > 
      // * validations(data types, refs)
      // * data type conversions
      BodyValidatorVisitor.validate(body, errors);
      
      if(!ignoreErrors) {
        List<ErrorNode> errors = new ArrayList<>();
        this.errors.values().forEach(v -> errors.addAll(v));
        if(!errors.isEmpty()) {
          throw new AstNodeException(errors);
        }
      }
      
      AstEnvir result = new ImmutableAstEnvir(
          Collections.unmodifiableMap(body), 
          Collections.unmodifiableMap(src),
          Collections.unmodifiableMap(errors));
      
      return result;
    }

    @Override
    public SourceBuilder<Builder> add() {
      Builder result = this;
      return new GenericSourceBuilder() {
        private final HdesAntlrErrorListener errorListener = new HdesAntlrErrorListener();
        private String value;
        
        @Override
        protected Builder parent(Body node) {
          String id = externalId == null ? node.getId().getValue() : externalId;
          body.put(id, node);
          src.put(id, value);
          errors.put(id, Collections.unmodifiableList(errorListener.getErrors()));
          return result;
        }
        @Override
        public Builder src(String value) {
          this.value = value;
          
          return super.src(value);
        }
        @Override
        protected HdesAntlrErrorListener errorListener() {
          return errorListener;
        }
        
      };
    } 
  }
  
  public static abstract class GenericSourceBuilder implements SourceBuilder<Builder> {
    
    protected String externalId;
    protected abstract HdesAntlrErrorListener errorListener();
    protected abstract Builder parent(Body node);
    
    @Override
    public SourceBuilder<Builder> externalId(String externalId) {
      this.externalId = externalId;
      return this;
    }
    @Override
    public Builder src(String src) {
      HdesLexer lexer = new HdesLexer(CharStreams.fromString(src));
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      HdesParser parser = new HdesParser(tokens);
      parser.addErrorListener(errorListener());
      ParseTree tree = parser.hdesBody();
      
      Body result;
      try {
        result = (Body) tree.accept(new HdesParserAstNodeVisitor(new TokenIdGenerator()));
      } catch(Exception e) {
        errorListener().add(e);
        
        Token exceptionToken = ImmutableToken.builder()
          .id(0)
          .startCol(0).startLine(0)
          .endCol(0).endLine(0)
          .text(e.getMessage() == null ? "" : e.getMessage())
          .build();
        
        result = ImmutableEmptyBody.builder()
            .id(ImmutableBodyId.builder().value(externalId == null ? UUID.randomUUID().toString() : externalId).token(exceptionToken)
                .build())
            .headers(ImmutableHeaders.builder().token(exceptionToken).build())
            .token(exceptionToken)
            .build();
      }
      return parent(result);
    }
  }
}
