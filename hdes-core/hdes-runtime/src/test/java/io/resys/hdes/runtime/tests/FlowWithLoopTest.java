package io.resys.hdes.runtime.tests;

import java.io.Serializable;
import java.util.HashMap;

/*-
 * #%L
 * hdes-runtime
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

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.compiler.api.HdesCompiler;
import io.resys.hdes.compiler.api.HdesCompiler.Resource;
import io.resys.hdes.compiler.spi.JavaHdesCompiler;
import io.resys.hdes.executor.api.FlowMetaValue;
import io.resys.hdes.executor.api.HdesExecutable;
import io.resys.hdes.executor.api.HdesExecutable.Flow;
import io.resys.hdes.executor.api.HdesExecutable.HdesExecution;
import io.resys.hdes.executor.api.HdesExecutable.InputValue;
import io.resys.hdes.executor.api.HdesExecutable.OutputValue;
import io.resys.hdes.runtime.api.HdesRuntime.RuntimeEnvir;
import io.resys.hdes.runtime.api.HdesRuntime.RuntimeTask;
import io.resys.hdes.runtime.spi.ImmutableHdesRuntime;

public class FlowWithLoopTest {
  private static final HdesCompiler compiler = JavaHdesCompiler.config().build();
  private static final ObjectMapper objectMapper = new ObjectMapper();
  
  
  //@Test
  public void simpleFlow() {
    String src = "define flow: CalculateRiskCoversFlow\n" +
        "headers: {\n" + 
        "  covers ARRAY of OBJECT required IN: {" +
        "    type STRING required IN," +
        "    value DECIMAL required IN" +
        "  },\n" +
        "  total DECIMAL optional OUT\n" +
        "}\n" + 
        "tasks: {\n" + 
        
        "  CalculateDiscounts: { \n" + 
        "    then: end-as: {} \n" +
        "    decision-table: RiskCoverDiscounts uses: { value: covers.type }\n"
        + "} from: covers then: CalculateFinalPrice,\n" + 

        
        "  CalculateFinalPrice: {\n" + 
        "    then: end-as: { total: sum(CalculateDiscounts.map(discount -> discount.value)) }\n"
        + "}\n" + 
        
        "}";
    
    Map<String, Serializable> data = new HashMap<>();
    data.put("factor1", 11);
    data.put("factor2", 20);

    HdesExecution<? extends InputValue, FlowMetaValue, ? extends OutputValue> output = runFlow("CalculateRiskCoversFlow", src, data);
    Assertions.assertEquals(output.getOutputValue().toString(), "CascoPricingFlowOut{total=31}");
  }
  
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static HdesExecution<? extends InputValue, FlowMetaValue, ? extends OutputValue> runFlow(String name, String src, Map<String, Serializable> data) {
    
    String riskCoverDiscounts = "define decision-table: RiskCoverDiscounts\n" +  
            "headers: {\n" + 
            "  value       STRING required IN\n" +
            "} MATRIX from STRING to INTEGER: {\n" + 
            "         { 'S', 'M',  ?  },\n" +  
            "  value: {  20,  50,  60 } \n" + 
            "}";
    try {
      List<Resource> resources = compiler.parser()
          .add(name, src)
          .add("RiskCoverDiscounts", riskCoverDiscounts)
          .build();
      
      RuntimeEnvir runtime = ImmutableHdesRuntime.builder().from(resources).build();
      RuntimeTask task = runtime.get(name);
      HdesExecutable.InputValue input = objectMapper.convertValue(data, task.getInput());
      Flow fl = (Flow) task.getValue();
      HdesExecution<? extends InputValue, FlowMetaValue, ? extends OutputValue> output = fl.apply(input);
      
      return output;
    } catch(ClassNotFoundException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}