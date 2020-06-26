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

import java.io.Serializable;
import java.util.List;

import org.immutables.value.Value;

public interface HdesExecutable<I extends HdesExecutable.InputValue, M extends HdesExecutable.Meta, V extends HdesExecutable.OutputValue> {
  
  // Core
  
  enum SourceType { FL, MT, DT, ST }
  enum ExecutionStatus { COMPLETED, WAITING, RUNNING, ERROR }
  
  interface Meta extends Serializable {
    String getId();
    ExecutionStatus getStatus();
    long getStart();
    long getEnd();
    long getTime();
    List<ExecutableError> getErrors();
  }
  
  interface OutputValue extends Serializable {}
  interface InputValue extends Serializable {}

  @Value.Immutable
  interface ExecutableError {
    String getId();
    String getTrace();
    String getValue();
  }
  
  @Value.Immutable
  interface MetaToken {
    String getValue();
    MetaStamp getStart();
    MetaStamp getEnd();
  }
  
  @Value.Immutable
  interface MetaStamp {
    //long getTime();
    int getLine();
    int getColumn();
  }
  
  // Generic output to encapsulate function output value with metadata associated with it
  @Value.Immutable
  interface Output<M extends Meta, V extends OutputValue> extends Serializable {
    M getMeta();
    V getValue();
  }
  
  // Single command style method
  Output<M, V> apply(I input);
  
  // Source from what executable was created
  SourceType getSourceType();
  
  // Markers for sub types
  interface DecisionTable<I extends InputValue, V extends OutputValue> extends HdesExecutable<I, DecisionTableMeta, V> {}
  interface Flow<I extends InputValue, M extends FlowMeta, V extends OutputValue> extends HdesExecutable<I, M, V> {}
  
  interface ExpressionTask<I extends InputValue, M extends ExpressionTaskMeta, V extends OutputValue> extends HdesExecutable<I, M, V> {}
  interface ManualTask<I extends InputValue, M extends Meta, V extends OutputValue> extends HdesExecutable<I, M, V> {}
  interface ServiceTask<I extends InputValue, M extends Meta, V extends OutputValue> extends HdesExecutable<I, M, V> {}

}
