package io.resys.hdes.compiler.api;

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

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.hdes.compiler.api.Flow.FlowInput;
import io.resys.hdes.compiler.api.Flow.FlowOutput;
import io.resys.hdes.compiler.api.Flow.FlowState;

public interface Flow<
  I extends FlowInput,
  O extends FlowOutput,
  S extends FlowState<I, O>> {
  
  enum ExecutionStatusType { COMPLETED, RUNNING, PENDING, ERROR }
  interface FlowInput {}
  interface FlowOutput {}
  
  S apply(I input);
  //S apply(S old, ? output);
  
  
  
  
  interface FlowState<I, O> {
    String getId();
    I getInput();
    
    Optional<O> getOutput();
    List<FlowError> getErrors();
    FlowExecutionLog getLog();
  }
  
  @Value.Immutable
  interface FlowExecutionLog {
    String getId();
    Long getStart();
    Optional<FlowExecutionLog> getParent();
    Optional<Long> getDuration();
    Optional<Long> getEnd();
  }

  @Value.Immutable
  interface FlowError {
    String getId();
    String getTrace();
    String getValue();
  }
  
  interface FlowTaskState<I, R> {
    String getId();
    Optional<R> getOutput();
    I getInput();
  }
  
  @FunctionalInterface
  interface FlowTask<S extends FlowState<?, ?>, I, R> {
    R apply(S state, I input);
  }
}
