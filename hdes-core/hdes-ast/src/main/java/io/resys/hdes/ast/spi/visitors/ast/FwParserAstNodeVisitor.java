package io.resys.hdes.ast.spi.visitors.ast;

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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.immutables.value.Value;

import io.resys.hdes.ast.HdesParser;
import io.resys.hdes.ast.HdesParser.AsPointerContext;
import io.resys.hdes.ast.HdesParser.EndMappingContext;
import io.resys.hdes.ast.HdesParser.FlBodyContext;
import io.resys.hdes.ast.HdesParser.FromPointerContext;
import io.resys.hdes.ast.HdesParser.MappingArgContext;
import io.resys.hdes.ast.HdesParser.MappingArgsContext;
import io.resys.hdes.ast.HdesParser.MappingContext;
import io.resys.hdes.ast.HdesParser.MappingValueContext;
import io.resys.hdes.ast.HdesParser.NextTaskContext;
import io.resys.hdes.ast.HdesParser.TaskArgsContext;
import io.resys.hdes.ast.HdesParser.TaskPointerContext;
import io.resys.hdes.ast.HdesParser.TaskRefContext;
import io.resys.hdes.ast.HdesParser.TaskTypesContext;
import io.resys.hdes.ast.HdesParser.TasksContext;
import io.resys.hdes.ast.HdesParser.ThenPointerContext;
import io.resys.hdes.ast.HdesParser.WhenThenPointerArgsContext;
import io.resys.hdes.ast.HdesParser.WhenThenPointerContext;
import io.resys.hdes.ast.api.AstNodeException;
import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.Headers;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.FlowNode;
import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.Mapping;
import io.resys.hdes.ast.api.nodes.FlowNode.MappingValue;
import io.resys.hdes.ast.api.nodes.FlowNode.RefTaskType;
import io.resys.hdes.ast.api.nodes.FlowNode.TaskRef;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;
import io.resys.hdes.ast.api.nodes.ImmutableBodyId;
import io.resys.hdes.ast.api.nodes.ImmutableEndPointer;
import io.resys.hdes.ast.api.nodes.ImmutableFlowBody;
import io.resys.hdes.ast.api.nodes.ImmutableFlowTaskNode;
import io.resys.hdes.ast.api.nodes.ImmutableLoopPointer;
import io.resys.hdes.ast.api.nodes.ImmutableMapping;
import io.resys.hdes.ast.api.nodes.ImmutableMappingArray;
import io.resys.hdes.ast.api.nodes.ImmutableMappingExpression;
import io.resys.hdes.ast.api.nodes.ImmutableTaskRef;
import io.resys.hdes.ast.api.nodes.ImmutableThenPointer;
import io.resys.hdes.ast.api.nodes.ImmutableWhenThen;
import io.resys.hdes.ast.api.nodes.ImmutableWhenThenPointer;
import io.resys.hdes.ast.spi.visitors.ast.HdesParserAstNodeVisitor.RedundentDescription;
import io.resys.hdes.ast.spi.visitors.ast.util.FlowTreePointerParser;
import io.resys.hdes.ast.spi.visitors.ast.util.FlowTreePointerParser.FwRedundentOrderedTasks;
import io.resys.hdes.ast.spi.visitors.ast.util.Nodes;
import io.resys.hdes.ast.spi.visitors.ast.util.Nodes.TokenIdGenerator;

public class FwParserAstNodeVisitor extends MtParserAstNodeVisitor {
  
  public FwParserAstNodeVisitor(TokenIdGenerator tokenIdGenerator) {
    super(tokenIdGenerator);
  }
  
  // Internal only
  @Value.Immutable
  public interface FwRedundentTasks extends FlowNode {
    List<FlowTaskNode> getValues();
  }  
  @Value.Immutable
  public interface FwRedundentMapping extends FlowNode {
    List<Mapping> getValues();
  }
  @Value.Immutable
  public interface FwRedundentRefTaskType extends FlowNode {
    RefTaskType getValue();
  }

  @Value.Immutable
  public interface FwRedundentFromLoop extends FlowNode {
    FlowTaskPointer getThenPointer();
    TypeInvocation getInputType();
    
    Optional<TypeInvocation> getOutputType();
    Optional<ExpressionBody> getWhereExpression();
  }
  
  @Value.Immutable
  public interface FwRedundentAs extends FlowNode {
    TypeInvocation getValue();
  }
  
  @Override
  public FlowBody visitFlBody(FlBodyContext ctx) {
    Nodes children = nodes(ctx);
    Headers headers = children.of(Headers.class).get();
    TypeInvocation id = children.of(TypeInvocation.class).get();
    
    FwRedundentTasks redundentTasks = children.of(FwRedundentTasks.class).get();
    FwRedundentOrderedTasks tasks = new FlowTreePointerParser().visit(id, redundentTasks);
    
    for(FlowTaskNode unclaimed : tasks.getUnclaimed()) {
      // TODO:: error handling
      break;
    }
    
    
    return ImmutableFlowBody.builder()
        .headers(headers)
        .token(token(ctx))
        .id(ImmutableBodyId.builder().token(id.getToken()).value(id.getValue()).build())
        .description(children.of(RedundentDescription.class).map(e -> e.getValue()))
        .task(tasks.getFirst())
        .unreachableTasks(tasks.getUnclaimed())
        .build();
  }

  @Override
  public FwRedundentTasks visitTasks(TasksContext ctx) {
    return nodes(ctx).of(FwRedundentTasks.class).orElse(
        ImmutableFwRedundentTasks.builder()
        .token(token(ctx))
        .build());
  }

  @Override
  public FwRedundentTasks visitTaskArgs(TaskArgsContext ctx) {
    Nodes nodes = nodes(ctx);
    AstNode.Token token = token(ctx);
    return ImmutableFwRedundentTasks.builder()
        .token(token)
        .values(nodes.list(FlowTaskNode.class))
        .build();
  }

  @Override
  public EndPointer visitEndMapping(EndMappingContext ctx) {
    return ImmutableEndPointer.builder()
        .token(token(ctx))
        .name("end")
        .values(nodes(ctx).of(FwRedundentMapping.class).get().getValues())
        .build();
  }

  @Override
  public FlowTaskNode visitNextTask(NextTaskContext ctx) {
    Nodes nodes = nodes(ctx);
    Optional<FwRedundentFromLoop> loop = nodes.of(FwRedundentFromLoop.class);
    FlowTaskPointer inner = nodes.of(FlowTaskPointer.class).get();
    
    
    final FlowTaskPointer next;
    if(loop.isPresent()) {
      next = ImmutableLoopPointer.builder()
          .token(loop.get().getToken())
          .afterPointer(loop.get().getThenPointer())
          .where(loop.get().getWhereExpression())
          .inputType(loop.get().getInputType())
          .outputType(loop.get().getOutputType())
          .insidePointer(inner)
          .build();
    } else {
      next = inner;
    }
    
    return ImmutableFlowTaskNode.builder()
        .token(token(ctx))
        .id(nodes.of(TypeInvocation.class).get().getValue())
        .next(next)
        .ref(nodes.of(TaskRef.class))
        .build();
  }
  
  @Override
  public FwRedundentFromLoop visitFromPointer(FromPointerContext ctx) {
    Nodes nodes = nodes(ctx);
    FlowTaskPointer taskPointer = nodes.of(FlowTaskPointer.class).get();
    return ImmutableFwRedundentFromLoop.builder()
        .token(token(ctx))
        .inputType(nodes.of(TypeInvocation.class).get())
        .outputType(nodes.of(FwRedundentAs.class).map(f -> f.getValue()))
        .whereExpression(nodes.of(ExpressionBody.class))
        .thenPointer(taskPointer)
        .build();
  }
  
  @Override
  public FwRedundentAs visitAsPointer(AsPointerContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableFwRedundentAs.builder()
        .token(token(ctx))
        .value(nodes.of(TypeInvocation.class).get())
        .build();
  }
  
  @Override
  public FlowTaskPointer visitTaskPointer(TaskPointerContext ctx) {
    return (FlowTaskPointer) first(ctx);
  }
  
  @Override
  public WhenThenPointer visitWhenThenPointerArgs(WhenThenPointerArgsContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableWhenThenPointer.builder()
        .token(token(ctx))
        .values(nodes.list(WhenThen.class))
        .build();
  }

  @Override
  public WhenThen visitWhenThenPointer(WhenThenPointerContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableWhenThen.builder()
        .token(token(ctx))
        .when(nodes.of(ExpressionBody.class))
        .then(nodes.of(FlowTaskPointer.class).get())
        .build();
  }

  @Override
  public FlowTaskPointer visitThenPointer(ThenPointerContext ctx) {
    Nodes nodes = nodes(ctx);
    Optional<EndPointer> endPointer = nodes.of(EndPointer.class);
    if(endPointer.isPresent()) {
      return endPointer.get();
    }
    return ImmutableThenPointer.builder()
      .token(token(ctx))
      .name(nodes.of(TypeInvocation.class).map(e -> e.getValue()).get())
      .build();
  }
  
  
  @Override
  public TaskRef visitTaskRef(TaskRefContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableTaskRef.builder()
        .token(token(ctx))
        .type(nodes.of(FwRedundentRefTaskType.class).get().getValue())
        .value(nodes.of(TypeInvocation.class).get().getValue())
        .mapping(nodes.of(FwRedundentMapping.class).get().getValues())
        .build();
  }

  @Override
  public FwRedundentMapping visitMapping(MappingContext ctx) {
    return ImmutableFwRedundentMapping.builder()
        .token(token(ctx))
        .values(nodes(ctx).of(FwRedundentMapping.class).map(e -> e.getValues()).orElse(Collections.emptyList()))
        .build();
  }

  @Override
  public FwRedundentMapping visitMappingArgs(MappingArgsContext ctx) {
    return ImmutableFwRedundentMapping.builder()
        .token(token(ctx))
        .values(nodes(ctx).list(Mapping.class))
        .build();
  }

  @Override
  public Mapping visitMappingArg(MappingArgContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableMapping.builder()
        .token(token(ctx))
        .left(nodes.of(TypeInvocation.class).get().getValue())
        .right(nodes.of(MappingValue.class).get())
        .build();
  }

  @Override
  public MappingValue visitMappingValue(MappingValueContext ctx) {
    Nodes nodes = nodes(ctx);
    AstNode.Token token = token(ctx);
    
    if(nodes.of(ExpressionBody.class).isPresent()) {
      return ImmutableMappingExpression.builder().token(token).value(nodes.of(ExpressionBody.class).get()).build();
      
    } else if(nodes.of(FwRedundentMapping.class).isPresent()) {
      return ImmutableMappingArray.builder().token(token).values(nodes.of(FwRedundentMapping.class).get().getValues()).build();
    } else {
      // TODO:: error handling
      throw new AstNodeException("Unknown mapping value: " + ctx.getText() + "!");
    }
  }

  @Override
  public FwRedundentRefTaskType visitTaskTypes(TaskTypesContext ctx) {
    TerminalNode terminalNode = (TerminalNode) ctx.getChild(0);
    RefTaskType type = null;
    switch(terminalNode.getSymbol().getType()) {
    case HdesParser.DEF_MT: type = RefTaskType.MANUAL_TASK; break;
    case HdesParser.DEF_FL: type = RefTaskType.FLOW_TASK; break;
    case HdesParser.DEF_SE: type = RefTaskType.SERVICE_TASK; break;
    case HdesParser.DEF_DT: type = RefTaskType.DECISION_TABLE; break;
    // TODO:: error handling
    default: throw new AstNodeException("Unknown task type: " + ctx.getText() + "!");
    }
    return ImmutableFwRedundentRefTaskType.builder().token(token(ctx)).value(type).build();
  }
}
