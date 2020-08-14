package io.resys.hdes.executor.spi;

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

import io.resys.hdes.executor.api.HdesExecutable;
import io.resys.hdes.executor.api.HdesExecutable.InputValue;
import io.resys.hdes.executor.api.HdesExecutable.MetaValue;
import io.resys.hdes.executor.api.HdesExecutable.OutputValue;
import io.resys.hdes.executor.api.ImmutableHdesExecution;

public abstract class HdesExecutableTemplate<I extends InputValue, M extends MetaValue, O extends OutputValue> implements HdesExecutable<I, M, O> {

  public ImmutableHdesExecution<I, M, O> execution(I input, O output, M meta) {
    ImmutableHdesExecution.Builder<I, M, O> builder = ImmutableHdesExecution.builder();
    return builder.inputValue(input).outputValue(output).metaValue(meta).build();
  }
}
