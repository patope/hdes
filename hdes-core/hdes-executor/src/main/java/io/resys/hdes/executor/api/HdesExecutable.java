package io.resys.hdes.executor.api;

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

import java.io.Serializable;

import javax.annotation.Nullable;

import org.immutables.value.Value;


public interface HdesExecutable<I extends HdesExecutable.InputValue, M extends HdesExecutable.MetaValue, O extends HdesExecutable.OutputValue> {
  
  // Core
  
  enum SourceType { FL, MT, DT, ST, SW, FR }
  enum ExecutionStatus { COMPLETED, WAITING, RUNNING, ERROR }
    
  interface OutputValue extends Serializable {}
  interface InputValue extends Serializable {}

  // Generic output to encapsulate function output value with metadata associated with it
  @Value.Immutable
  interface HdesExecution<I extends InputValue, M extends MetaValue, O extends OutputValue> extends Serializable {
    M getMetaValue();
    I getInputValue();
    O getOutputValue();
  }
  
  interface MetaValue extends Serializable {
    String getId();
    ExecutionStatus getStatus();
    long getStart();
    @Nullable
    Long getEnd();
    @Nullable
    Long getTime();
  }
  
  // Single command style method
  HdesExecution<I, M, O> apply(I input);
  
  // Source from what executable was created
  SourceType getSourceType();
  
  // Markers for sub types
  interface DecisionTable<I extends InputValue, V extends OutputValue> extends HdesExecutable<I, DecisionTableMeta, V> {}
  interface Flow<I extends InputValue, V extends OutputValue> extends HdesExecutable<I, FlowMetaValue, V> {}
  interface Formula<I extends InputValue, V extends OutputValue> extends HdesExecutable<I, FormulaMeta, V> {}
  interface Switch<I extends InputValue, V extends OutputValue> extends HdesExecutable<I, SwitchMeta, V> {}

  
  interface ManualTask<I extends InputValue, M extends MetaValue, V extends OutputValue> extends HdesExecutable<I, M, V> {}
  interface ServiceTask<I extends InputValue, M extends MetaValue, V extends OutputValue> extends HdesExecutable<I, M, V> {}

}
