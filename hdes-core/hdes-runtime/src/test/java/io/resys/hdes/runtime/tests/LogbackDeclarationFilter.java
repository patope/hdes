package io.resys.hdes.runtime.tests;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class LogbackDeclarationFilter extends Filter<ILoggingEvent> {

  private String marker;
  
  @Override
  public FilterReply decide(ILoggingEvent event) {    
    if (event.getMessage().contains(marker)) {
      return FilterReply.ACCEPT;
    }
    return FilterReply.DENY;
  }

  public String getMarker() {
    return marker;
  }

  public void setMarker(String marker) {
    this.marker = marker;
  }
}
