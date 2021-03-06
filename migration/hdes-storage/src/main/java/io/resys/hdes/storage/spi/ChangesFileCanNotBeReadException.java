package io.resys.hdes.storage.spi;

/*-
 * #%L
 * hdes-storage
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

import java.io.File;

import io.resys.hdes.datatype.api.exceptions.HdesException;
import io.resys.hdes.datatype.spi.Assert;

public class ChangesFileCanNotBeReadException extends HdesException {
  private static final long serialVersionUID = 9163955084870511877L;

  public ChangesFileCanNotBeReadException(String message, Exception e) {
    super(message, e);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private File file;
    private Exception original;
    
    public Builder file(File file) {
      this.file = file;
      return this;
    }
    public Builder original(Exception original) {
      this.original = original;
      return this;
    }
    public ChangesFileCanNotBeReadException build() {
      Assert.notNull(file, () -> "file can't be null");
      Assert.notNull(original, () -> "original can't be null");
      
      String message = "Failed reading: " + file.getName() + ","  + System.lineSeparator() + "original message: " + original.getMessage();
      return new ChangesFileCanNotBeReadException(message, original);
    }
  }
}
