package io.resys.hdes.compiler.spi.java;

import java.util.List;

import io.resys.hdes.executor.api.FlowMeta.FlowTask;

public class FlowUtil {

  public static class MutableFlowTasks {
    
    public static MutableFlowTasks create() {
      return new MutableFlowTasks();
    }
    
    public MutableFlowTasks add(FlowTask task) {
      return this;
    }
    
    public FlowTask get(String name) {
      return null;
    }
    
    public List<FlowTask> build() {
      return null;
    }
  }
}
