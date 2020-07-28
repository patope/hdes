package io.resys.hdes.executor.spi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class HdesMath {

  public static class DecimalOperations {
    private final List<BigDecimal> values;
    public DecimalOperations(List<BigDecimal> values) {
      super();
      this.values = values;
    }
    
    public BigDecimal min() {
      return values.stream().min(Comparator.naturalOrder()).orElse(null);
    }
    public BigDecimal max() {
      return values.stream().max(Comparator.naturalOrder()).orElse(null);
    }    
    public BigDecimal sum() {
      return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public BigDecimal avg() {
      if(values.size() == 0) {
        return BigDecimal.ZERO;
      }
      BigDecimal sum = sum();
      if(sum.compareTo(BigDecimal.ZERO) == 0) {
        return BigDecimal.ZERO;
      }
      return sum.divide(new BigDecimal(values.size()));
    }
  }

  public static class IntegerOperations {
    private final List<Integer> values;
    public IntegerOperations(List<Integer> values) {
      super();
      this.values = values;
    }
    
    public Integer min() {
      return values.stream().min(Comparator.naturalOrder()).orElse(null);
    }
    public Integer max() {
      return values.stream().max(Comparator.naturalOrder()).orElse(null);
    }    
    public Integer sum() {
      return values.stream().reduce(0, Integer::sum);
    }
    public BigDecimal avg() {
      if(values.size() == 0) {
        return BigDecimal.ZERO;
      }
      Integer sum = sum();
      if(sum == 0) {
        return BigDecimal.ZERO;
      }
      return new BigDecimal(sum).divide(new BigDecimal(values.size()));
    }
  }
  
  public static class Builder {
    private final List<BigDecimal> decimals = new ArrayList<>();
    private final List<Integer> integers = new ArrayList<>();
    
    public Builder decimal(BigDecimal ... values) {
      decimals.addAll(Arrays.asList(values));
      return this;
    }
    
    public Builder decimal(Collection<BigDecimal> values) {
      decimals.addAll(values);
      return this;
    }
    
    public Builder integer(Collection<Integer> values) {
      integers.addAll(values);
      return this;
    }
    
    public Builder integer(Integer ... values) {
      integers.addAll(Arrays.asList(values));
      return this;
    }
    
    public DecimalOperations toDecimal() {
      List<BigDecimal> values = new ArrayList<>();
      decimals.stream().filter(d -> d != null).forEach(d -> values.add(d));
      integers.stream().filter(d -> d != null).forEach(d -> values.add(new BigDecimal(d)));      
      return new DecimalOperations(values);
    }
    public IntegerOperations toInteger() {
      List<Integer> values = new ArrayList<>();
      decimals.stream().filter(d -> d != null).forEach(d -> values.add(d.intValue()));
      integers.stream().filter(d -> d != null).forEach(d -> values.add(d));      
      return new IntegerOperations(values);
    }
  }
  
  public static Builder builder() {
    return new Builder();
  }
}
