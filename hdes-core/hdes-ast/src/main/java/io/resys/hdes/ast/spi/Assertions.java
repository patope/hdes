package io.resys.hdes.ast.spi;

import java.util.function.Supplier;

import io.resys.hdes.ast.api.AstNodeException;

public class Assertions {
  
  public static void notNull(Object object, Supplier<String> msg) {
    if(object == null) {
      throw new AstNodeException(msg.get());
    }
  }

  public static void isTrue(boolean object, Supplier<String> msg) {
    if(!object) {
      throw new AstNodeException(msg.get());
    }
  }
}
