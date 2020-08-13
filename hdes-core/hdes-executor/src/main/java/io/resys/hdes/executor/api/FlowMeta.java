package io.resys.hdes.executor.api;

import java.io.Serializable;

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

import java.util.List;

import org.immutables.value.Value;

@Value.Immutable
public interface FlowMeta extends HdesExecutable.Meta {

  FlowState getState();  
  
  interface FlowState extends Serializable {
    String getId();
    long getStart();
    //ExecutionStatus getStatus();
  }
  
  interface FlowTaskMeta extends Serializable {
    String getId();
  }
  
  @Value.Immutable
  interface FlowTaskMetaMono<M extends HdesExecutable.Meta, T extends HdesExecutable.OutputValue> extends FlowTaskMeta {
    HdesExecutable.Execution<M, T> getDelegate();
  }
  
  @Value.Immutable
  interface FlowTaskMetaFlux<M extends HdesExecutable.Meta, T extends HdesExecutable.OutputValue> extends FlowTaskMeta {
    List<HdesExecutable.Execution<M, T>> getDelegate();
  }
}
