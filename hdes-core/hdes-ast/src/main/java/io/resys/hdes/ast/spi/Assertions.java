package io.resys.hdes.ast.spi;

/*-
 * #%L
 * hdes-ast
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

import java.util.function.Supplier;

import io.resys.hdes.ast.api.AstNodeException;

public class Assertions {

  public static void notNull(Object object, Supplier<String> msg) {
    if (object == null) {
      throw new AstNodeException(msg.get());
    }
  }

  public static <T> T fail(Supplier<String> msg) {
    throw new AstNodeException(msg.get());
  }

  public static void isTrue(boolean object, Supplier<String> msg) {
    if (object) {
      return;
    }
    throw new AstNodeException(msg.get());
  }
}
