package io.resys.hdes.ast.test;

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

import java.io.IOException;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import io.resys.hdes.ast.HdesLexer;
import io.resys.hdes.ast.HdesParser;
import io.resys.hdes.ast.spi.visitors.ast.HdesParserAstNodeVisitor;
import io.resys.hdes.ast.spi.visitors.ast.util.Nodes.TokenIdGenerator;

public class FlowAstNodeTest {
  @Test
  public void basic() throws IOException {
    parse("define flow: x description: 'very descriptive value' headers: {} tasks: {}");
  }

  @Test
  public void inputs() throws IOException {
    parse(
        "define flow: x\n" +
            "description: 'very descriptive value'\n" +
            "headers: {\n " +
              "arg1.x1 INTEGER required IN, \n" +
              "arg2.x1 INTEGER required IN\n" +
            "}\n" +
            "tasks: {" +
            "}\n");
  }

  @Test
  public void nestedInputs() throws IOException {
    parse(
        "define flow: x\n" +
            "description: 'very descriptive value'\n" +
            "headers: {\n " +
              "arg0 OBJECT required IN: {} \n" +
            "}\n" +
            "tasks: {" +
            "}\n");
    
    parse(
        "define flow: x\n" +
            "description: 'very descriptive value'\n" +
            "headers: {\n " +
              "person OBJECT required IN: {" +
                "firstName INTEGER optional IN,\n" +
                "lastName INTEGER required IN\n" +
              "} \n" +
            "}\n" +
            "tasks: {" +
            "}\n");
  }
  
  @Test
  public void taskWhenThen() throws IOException {
    parse(
        "define flow: x\n" +
            "description: 'descriptive ' \n" +
            "headers: {\n "
            + "arg1.x1 INTEGER optional IN,\n"
            + "arg2.x1 INTEGER required IN\n" +
            "}\n" +
            "tasks: {\n" +
            "firstTask: {\n"
            + "when: arg2.x1 > 10 then: nextTask,\n"
            + "when: ? then: nextTask\n" +
            "}, " +
            "nextTask: {"
            + "then: end-as: {}" +
            "} " +
            
            "}\n");
  }

  @Test
  public void taskThen() throws IOException {
    parse(
        "define flow: x\n" +
            "description: 'descriptive ' \n" +
            "headers: {\n "
            + "arg1.x1 INTEGER optional IN,\n"
            + "arg2.x1 INTEGER required IN\n" +
            "}\n" +
            "tasks: {\n" +
            
            "firstTask: {"
            + "then: nextTask \n"
            + "decision-table: XXX uses: {}"
            + "}, " +
            "nextTask: {"
            + "then: end-as: {}" +
            "} " +
            "}\n");
  }

  @Test
  public void taskManualTask() throws IOException {
    parse(
        "define flow: x description: 'descriptive '\n" +
          
          "headers: {\n"
          + "arg1.x1 INTEGER optional IN,\n"
          + "arg2.x1 INTEGER required IN\n" +
          "}\n" +
          
          "tasks: {\n" +
            "firstTask: {\n" +
              "then: end-as: {}" +
              "manual-task: bestManualTask uses: {}\n" + 
            "}\n" +

          "}\n");
  }

  @Test
  public void taskServiceTask() throws IOException {
    parse(
        "define flow: x description: 'descriptive '\n" +
            "headers: {\n"
            + "arg1.x1 INTEGER optional IN,\n"
            + "arg2.x1 INTEGER optional IN\n" +
            "}\n" + 
            "tasks: {\n" +
            "firstTask: {\n"
            + "then: end-as: {}"
            + "service: bestServiceTask uses: {} \n"
            + "}\n" +
            "}\n");
  }

  @Test
  public void taskFlowTaskOverArray() throws IOException {
    parse(
        "define flow: x description: 'descriptive '\n" +
            "headers: {\n"
            + "arg1.x1 INTEGER optional IN,\n"
            + "arg2.x1 INTEGER optional IN,\n"
            + "x ARRAY of INTEGER required IN\n" +
            "}\n" +
            "tasks: {\n" +
            "firstTask: {\n"
            + "then: end-as: {}"
            + "service: bestFlowTask uses: {} \n"
            + "} from: x then: end-as: {} \n" +
            "}\n");
  }

  @Test
  public void taskOverDTOutputArray() throws IOException {
    parse(
        "define flow: x description: 'descriptive '\n" +
            "headers: {\n"
            + "arg1.x1 INTEGER optional IN,\n"
            + "arg2.x1 INTEGER optional IN,\n"
            + "code INTEGER optional OUT,\n"
            + "summary ARRAY of OBJECT required OUT: {\n"
            + "  value INTEGER required OUT\n"
            + "}\n" +
            "}\n" +

          "tasks: {\n" +
            
            "firstTask: {\n"
            + "then: nextTask \n"
            + "decision-table: bestDtTask uses: {} \n"
            + "},\n" +
            
            "nextTask: {\n"
            + "then: end-as: { summary: { key: firstTask.value, value: nextTask.value } }\n"
            + "service: DoSmth uses: { value : firstTask.key } \n"
            + "} from: firstTask then: end-as: { code: 5 } \n" +
            
          "}\n");
  }

  @Test
  public void taskDTArray() throws IOException {
    parse(
        "define flow: x description: 'descriptive '\n" +
            "headers: {\n"
            + "arg1.x1 INTEGER optional IN,\n"
            + "arg2.x1 INTEGER optional IN\n" +
            "}\n" +
            "tasks: {\n" +
            "firstTask: {\n"
            + "then: end-as: {}"
            + "decision-table: bestDtTask uses: {} \n"
            + "}\n" +
            "}\n");
  }

  @Test
  public void mapping() throws IOException {
    parse(
        "define flow: x description: 'descriptive '\n" +
            "headers: {\n"
            + "arg1.x1 INTEGER optional IN,\n"
            + "arg2.x1 INTEGER optional IN\n" +
            "}\n" +
            "tasks: {\n" +
            "firstTask: {\n"

            + "then: end-as: {\n"
              + "input1: arg1.x1,\n"
              + "input2: arg2.x1\n"
            + "}\n"

            + "decision-table: bestDtTask \n"
            + "uses: {\n"
              + "input1: arg1.x1,\n"
              + "input2: arg2.x1\n"
            + "}\n"  

        + "}}\n");
  }
  
  

  public void parse(String value) {
    HdesLexer lexer = new HdesLexer(CharStreams.fromString(value));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    HdesParser parser = new HdesParser(tokens);
    parser.addErrorListener(new ErrorListener());
    ParseTree tree = parser.hdesBody();
    //tree.accept(new HdesParserConsoleVisitor());
    tree.accept(new HdesParserAstNodeVisitor(new TokenIdGenerator()));
  }

  public static class ErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
        Object offendingSymbol,
        int line,
        int charPositionInLine,
        String msg,
        RecognitionException e) {
      String error = "line " + line + ":" + charPositionInLine + " " + msg;
      throw new IllegalArgumentException(error);
    }
  }
}
