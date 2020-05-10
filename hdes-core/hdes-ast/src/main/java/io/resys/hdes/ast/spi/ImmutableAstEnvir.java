package io.resys.hdes.ast.spi;

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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import io.resys.hdes.ast.HdesLexer;
import io.resys.hdes.ast.HdesParser;
import io.resys.hdes.ast.api.AstEnvir;
import io.resys.hdes.ast.api.AstNodeException;
import io.resys.hdes.ast.api.nodes.AstNode.BodyNode;
import io.resys.hdes.ast.spi.errors.AntlrErrorListener;
import io.resys.hdes.ast.spi.visitors.ast.HdesParserAstNodeVisitor;
import io.resys.hdes.ast.spi.visitors.ast.util.Nodes.TokenIdGenerator;

public class ImmutableAstEnvir implements AstEnvir {
  
  private final Map<String, BodyNode> nodes;

  public ImmutableAstEnvir(Map<String, BodyNode> nodes) {
    super();
    this.nodes = nodes;
  }
  @Override
  public Collection<BodyNode> getValues() {
    return nodes.values();
  } 

  @Override
  public BodyNode get(String id) {
    if(!nodes.containsKey(id)) {
      throw new AstNodeException("No node by id: " + id + "!");
    }
    return nodes.get(id);
  }
  
  public static Builder builder() {
    return new GenericBuilder();
  }
  
  public static class GenericBuilder implements Builder {

    private final AntlrErrorListener errorListener = new AntlrErrorListener();
    private final Map<String, BodyNode> nodes = new HashMap<>();
    
    @Override
    public Builder from(AstEnvir envir) {
      // TODO Auto-generated method stub
      return this;
    }

    @Override
    public AstEnvir build() {
      if(!errorListener.getErrors().isEmpty()) {
        throw new AstNodeException(errorListener.getErrors());
      }
      
      AstEnvir result = new ImmutableAstEnvir(nodes);
      
      // TODO :: Post processing > 
      // * validations(data types, refs)
      // * data type conversions
      
      
      return result;
    }

    @Override
    public SourceBuilder<Builder> add() {
      Builder result = this;
      return new GenericSourceBuilder() {
        @Override
        protected Builder parent(BodyNode node) {
          nodes.put(node.getId(), node);
          return result;
        }
        @Override
        protected ANTLRErrorListener errorListener() {
          return errorListener;
        }
      };
    } 
  }
  
  public static abstract class GenericSourceBuilder implements SourceBuilder<Builder> {
    protected abstract ANTLRErrorListener errorListener();
    protected abstract Builder parent(BodyNode node);
    
    @Override
    public SourceBuilder<Builder> externalId(String externalId) {
      // TODO Auto-generated method stub
      return this;
    }

    @Override
    public Builder src(String src) {
      HdesLexer lexer = new HdesLexer(CharStreams.fromString(src));
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      HdesParser parser = new HdesParser(tokens);
      parser.addErrorListener(errorListener());
      ParseTree tree = parser.hdesBody();
      return parent((BodyNode) tree.accept(new HdesParserAstNodeVisitor(new TokenIdGenerator())));
    }
  }
}