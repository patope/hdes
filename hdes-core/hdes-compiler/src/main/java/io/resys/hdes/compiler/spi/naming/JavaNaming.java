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
import io.resys.hdes.ast.spi.Assertions;

public class JavaNaming implements Namings {
  
  private final String root;
  private final JavaFlNaming flNaming;
  private final JavaDtNaming dtNaming;
  private final SwitchNaming swNaming;
  private final FormulaNaming frNaming;

  
  private final AstEnvir envir;
  private final String fl;
  private final String dt;
  
  
  public JavaNaming(AstEnvir envir, String root, String fl, String dt) {
    super();
    this.envir = envir;
    this.root = root;
    this.fl = root + "." + fl;
    this.dt = root + "." + dt;
    this.flNaming = new JavaFlNaming(this, envir, this.fl);
    this.dtNaming = new JavaDtNaming(this, this.dt);
    this.swNaming = new JavaSwitchNaming(this);
    this.frNaming = new JavaFormulaNaming(this);
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
  
  @Override
  public FormulaNaming fr() {
    return frNaming;
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

    public JavaNaming build() {
      Assertions.notNull(envir, () -> "ast can't be null!");
      
      return new JavaNaming(
          envir,
          Optional.ofNullable(root).orElse("io.resys.hdes.compiler"),
          Optional.ofNullable(flows).orElse("fl"),
          Optional.ofNullable(decisionTables).orElse("dt"));
    }
  }
}
