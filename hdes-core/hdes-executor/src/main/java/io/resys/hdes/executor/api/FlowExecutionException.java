package io.resys.hdes.executor.api;

/*-
 * #%L
 * hdes-executor
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
