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
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;


@Value.Immutable
public interface DecisionTableMeta extends HdesExecutable.MetaValue {
  
  Map<Integer, DecisionTableMetaEntry> getValues();

  interface DecisionTableStaticValue<T> extends Serializable {
    List<T> getValues();
  }
  
  @Value.Immutable
  interface DecisionTableMetaEntry extends Serializable {
    int getId();
    int getIndex();
    MetaToken getToken();
  }
  
  @Value.Immutable
  interface MetaToken {
    String getValue();
    MetaStamp getStart();
    MetaStamp getEnd();
  }
  
  @Value.Immutable
  interface MetaStamp {
    int getLine();
    int getColumn();
  }
  
}
