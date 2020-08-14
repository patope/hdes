package io.resys.hdes.executor.spi;

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
