package io.resys.hdes.runtime.api;

import java.util.List;

import io.resys.hdes.compiler.api.HdesCompiler.Resource;

public interface HdesRuntime {
  
  interface EnvirBuilder {
    EnvirBuilder from(List<Resource> resources);
    HdesRuntimeEnvir build();
  }

  
  interface HdesRuntimeEnvir {
    <T> T get(String name);
  }
}
