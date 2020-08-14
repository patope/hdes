package io.resys.hdes.executor.api;

import io.resys.hdes.executor.api.FlowMetaValue.FlowState;

public class FlowExecutionException extends RuntimeException {
  
  private static final long serialVersionUID = 2509713376857138221L;
  private final String msg;
  private final FlowState state;
  
  public FlowExecutionException(FlowState state, String msg) {
    super(msg);
    this.msg = msg;
    this.state = state;
  }

  public String getMsg() {
    return msg;
  }

  public FlowState getState() {
    return state;
  }
}
