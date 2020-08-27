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

import io.resys.hdes.executor.api.HdesExecutable.HdesExecution;
import io.resys.hdes.executor.api.HdesExecutable.InputValue;
import io.resys.hdes.executor.api.HdesExecutable.MetaValue;
import io.resys.hdes.executor.api.HdesExecutable.OutputValue;

@Value.Immutable
public interface FlowMetaValue extends HdesExecutable.MetaValue {
  
  FlowState getState();
  
  interface FlowState extends Serializable {
  }
  
  interface FlowTaskState extends Serializable {
    String getId();
  }
  
  @Value.Immutable
  interface FlowTaskMono<I extends InputValue, M extends MetaValue, T extends OutputValue> extends FlowTaskState {
    HdesExecution<I, M, T> getDelegate();
  }
  
  @Value.Immutable
  interface FlowTaskMulti<I extends InputValue, M extends MetaValue, T extends OutputValue> extends FlowTaskState {
    List<HdesExecution<I, M, T>> getDelegate();
  }
  
  @Value.Immutable
  interface FlowTaskFlux<I extends InputValue, M extends MetaValue, T extends OutputValue, S extends FlowState> extends FlowTaskState {
    List<HdesExecution<I, M, T>> getDelegate();
    List<S> getSubStates();
  }
  
}
