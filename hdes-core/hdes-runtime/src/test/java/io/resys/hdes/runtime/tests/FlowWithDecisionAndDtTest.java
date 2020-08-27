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
import org.junit.jupiter.api.Test;

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

public class FlowWithDecisionAndDtTest {
  private static final HdesCompiler compiler = JavaHdesCompiler.config().build();
  private static final ObjectMapper objectMapper = new ObjectMapper();
  
  @Test
  public void simpleFlow() {
    String src = "define flow: NameScoreFlow description: 'descriptive'\n" +
        "headers: {\n" + 
        "  type INTEGER required IN,\n" + 
        "  firstName STRING required IN,\n" +
        "  lastName STRING required IN,\n" +
        "  clientScore INTEGER optional OUT\n" +
        "}\n" + 
        "tasks: {\n" + 
        "  FirstNameTask: {\n" + 
        "    then: decision\n" + 
        "    decision-table: NameScoreDt uses: { value: firstName } },\n" + 
        "  decision: {\n" + 
        "    when: FirstNameTask.value > 10 then: LastNameTask,\n" + 
        "    when: ? then: end-as: { clientScore: FirstNameTask.value } },\n" + 
        "  LastNameTask: {\n" + 
        "    then: end-as: { clientScore: FirstNameTask.value + LastNameTask.value }\n" + 
        "    decision-table: NameScoreDt uses: { value: lastName } }\n" + 
        "}";
    
    Map<String, Serializable> data = new HashMap<>();
    data.put("type", 11);
    data.put("firstName", "BOB");
    data.put("lastName", "SAM");

    HdesExecution<? extends InputValue, FlowMetaValue, ? extends OutputValue> output = runFlow("NameScoreFlow", src, data);
    Assertions.assertEquals(output.getOutputValue().toString(), "NameScoreFlowOut{clientScore=70}");
  }
  
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static HdesExecution<? extends InputValue, FlowMetaValue, ? extends OutputValue> runFlow(String name, String src, Map<String, Serializable> data) {
    String nameScoreDt = "define decision-table: NameScoreDt\n" + 
        "headers: {\n" + 
        "  value     STRING required IN\n" +
        "} MATRIX from STRING to INTEGER: {\n" + 
        "         { 'BOB', 'SAM',  ?  },\n" +  
        "  value: {    20,    50,  60 } \n" + 
        "}";
    
    try {
      List<Resource> resources = compiler.parser()
          .add(name, src)
          .add("NameScoreDt", nameScoreDt)
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