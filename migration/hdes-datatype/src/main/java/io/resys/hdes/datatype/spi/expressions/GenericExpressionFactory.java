package io.resys.hdes.datatype.spi.expressions;

/*-
 * #%L
 * hdes-datatype
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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.datatype.api.DataTypeService;

public class GenericExpressionFactory implements ExpressionFactory {
  private final ObjectMapper objectMapper;
  
  public GenericExpressionFactory(ObjectMapper objectMapper) {
    super();
    this.objectMapper = objectMapper;
  }
  
  @Override
  public DataTypeService.ExpressionBuilder builder() {
    return new DelegateExpressionBuilder(objectMapper);
  }
  
}