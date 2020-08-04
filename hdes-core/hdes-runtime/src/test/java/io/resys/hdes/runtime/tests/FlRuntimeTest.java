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
import io.resys.hdes.compiler.spi.java.JavaHdesCompiler;
import io.resys.hdes.executor.api.DecisionTableMeta;
import io.resys.hdes.executor.api.HdesExecutable;
import io.resys.hdes.executor.api.HdesExecutable.DecisionTable;
import io.resys.hdes.executor.api.HdesExecutable.OutputValue;
import io.resys.hdes.runtime.api.HdesRuntime.RuntimeEnvir;
import io.resys.hdes.runtime.api.HdesRuntime.RuntimeTask;
import io.resys.hdes.runtime.spi.ImmutableHdesRuntime;

public class FlRuntimeTest {
  private static final HdesCompiler compiler = JavaHdesCompiler.config().build();
  private static final ObjectMapper objectMapper = new ObjectMapper();
  
  
  
  //@Test 
  public void simpleFlow() {
    String src = "define flow: NameScoreFlow description: 'descriptive'\n" +
        "headers: {\n" + 
        "  type INTEGER required IN,\n" + 
        "  firstName STRING required IN,\n" +
        "  lastName STRING required IN,\n" +
        "  clientScore INTEGER required OUT\n" +
        "}\n" + 
        "tasks: {\n" + 
        "  FirstNameTask: {\n" + 
        "    then: switch\n" + 
        "    decision-table: NameScoreDt uses: { name: firstName } },\n" + 
        "  switch: {\n" + 
        "    when: FirstNameTask.score > 10 then: LastNameTask,\n" + 
        "    when: ? then: end as: { clientScore: FirstNameTask.score } },\n" + 
        "  LastNameTask: {\n" + 
        "    then: end as: { clientScore: FirstNameTask.score + LastNameTask.score }\n" + 
        "    decision-table: NameScoreDt uses: { name: lastName } }\n" + 
        "}";
    
    Map<String, Serializable> data = new HashMap<>();
    data.put("type", 11);
    data.put("firstName", "BOB");
    data.put("lastName", "SAM");
    
    HdesExecutable.Execution<DecisionTableMeta, ? extends OutputValue> output = runFlow("ExpressionDT", src, data);
    Assertions.assertEquals(output.getMeta().getValues().size(), 1);
    Assertions.assertEquals(output.getMeta().getValues().get(0).getIndex(), 0);
  }
  
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static HdesExecutable.Execution<DecisionTableMeta, ? extends OutputValue> runFlow(
      String name, 
      String src, 
      Map<String, Serializable> data) {
    
    String nameScoreDt = "define decision-table: NameScoreDt\n" + 
        "headers: {\n" + 
        "  name     STRING required IN,\n" +
        "} MATRIX from STRING to INTEGER: {\n" + 
        "         { 'BOB', 'SAM',  ?  },\n" +  
        "  score: {    20,    50,  60 } \n" + 
        "}";
    
    try {
      List<Resource> resources = compiler.parser()
          .add(name, src)
          .add("NameScoreDt", nameScoreDt)
          .build();
      RuntimeEnvir runtime = ImmutableHdesRuntime.builder().from(resources).build();
      RuntimeTask task = runtime.get(name);
      HdesExecutable.InputValue input = objectMapper.convertValue(data, task.getInput());
      DecisionTable dt = (DecisionTable) task.getValue();
      HdesExecutable.Execution<DecisionTableMeta, ? extends OutputValue> output = dt.apply(input);
      
      return output;
    } catch(ClassNotFoundException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}