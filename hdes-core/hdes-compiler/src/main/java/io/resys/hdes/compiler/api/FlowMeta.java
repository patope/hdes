package io.resys.hdes.compiler.api;

import java.util.List;

import org.immutables.value.Value;

@Value.Immutable
public interface FlowMeta extends HdesExecutable.Meta {

  List<FlowTask> getTasks();
  
  interface FlowTask {
    String getId();
  }
  
  @Value.Immutable
  interface FlowTaskMonoMeta extends FlowTask {
    HdesExecutable.Meta getDelegate();
  }
  
  @Value.Immutable
  interface FlowTaskFluxMeta extends FlowTask {
    List<HdesExecutable.Meta> getDelegate();
  }
}
