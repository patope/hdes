package io.resys.hdes.compiler.spi.naming;

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

import java.util.Optional;

import io.resys.hdes.ast.api.AstEnvir;
import io.resys.hdes.ast.api.nodes.AstNode.Body;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.CompilerContext;

public class JavaCompilerContext implements CompilerContext {
  
  private final String root;
  private final JavaFlNaming flNaming;
  private final JavaDtNaming dtNaming;
  private final SwitchNaming swNaming;

  private final AstEnvir envir;
  private final String fl;
  private final String dt;
  
  public JavaCompilerContext(AstEnvir envir, String root, 
      String fl, String dt) {
    super();
    this.envir = envir;
    this.root = root;
    this.fl = root + "." + fl;
    this.dt = root + "." + dt;
    this.flNaming = new JavaFlNaming(this, envir, this.fl);
    this.dtNaming = new JavaDtNaming(this, this.dt);
    this.swNaming = new JavaSwitchNaming(this);
  }
  
  public String pkg(Body body) {

    if(body instanceof FlowBody) {
      FlowBody node = (FlowBody) body;
      return this.fl + "." + node.getId().getValue().toLowerCase();      
    } else if(body instanceof DecisionTableBody) {
      return this.dt;
    }
    throw new IllegalArgumentException("Formula naming not implemented for: " + body + "!");
  }
  
  @Override
  public FlNaming fl() {
    return flNaming;
  }

  @Override
  public JavaDtNaming dt() {
    return dtNaming;
  }
  
  @Override
  public AstEnvir ast() {
    return envir;
  }

  @Override
  public SwitchNaming sw() {
    return swNaming;
  }

  public static Config config() {
    return new Config();
  }

  public static class Config {
    private AstEnvir envir;
    private String root;
    private String flows;
    private String decisionTables;

    public Config ast(AstEnvir envir) {
      this.envir = envir;
      return this;
    }
    
    public Config root(String root) {
      this.root = root;
      return this;
    }

    public Config flows(String flows) {
      this.flows = flows;
      return this;
    }

    public Config decisionTables(String decisionTables) {
      this.decisionTables = decisionTables;
      return this;
    }

    public JavaCompilerContext build() {
      Assertions.notNull(envir, () -> "ast can't be null!");
      
      return new JavaCompilerContext(
          envir,
          Optional.ofNullable(root).orElse("io.resys.hdes.compiler"),
          Optional.ofNullable(flows).orElse("fl"),
          Optional.ofNullable(decisionTables).orElse("dt"));
    }
  }
}
