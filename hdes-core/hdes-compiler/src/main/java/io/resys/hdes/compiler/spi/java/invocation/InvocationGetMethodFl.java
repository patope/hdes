package io.resys.hdes.compiler.spi.java.invocation;

import java.util.Optional;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.AstNode.Invocation;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.AstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;

public class InvocationGetMethodFl {
  
  public final static String ACCESS_STATE_VALUE = "stateValue";
  
  public CodeBlock apply(
      AstNodeVisitorContext ctx, 
      Invocation node, 
      TypeDef typeDef) {
    
    final FlowBody flow = TypeDefFinder.getBody(ctx);
    final String typeName = node.getValue();
    final Optional<String> lambdaContext = TypeDefFinder.getLambda(ctx).map(runningCtx -> {
      LambdaExpression lambda = (LambdaExpression) runningCtx.getValue();
      return lambda.getParams().get(0).getValue();
    });

    if(lambdaContext.isPresent() && 
        (typeName.equals(lambdaContext.get()) || typeName.startsWith(lambdaContext.get() + ".")) && 
        node instanceof TypeInvocation) {
      
      String name = JavaSpecUtil.methodVarCall(node.getValue());
      return CodeBlock.builder().add(name + (typeDef.getRequired() ? "" : ".get()")).build();
      
    }

    // getFlow input or task output
    String[] pathName = node.getValue().split("\\.");
    String taskName = pathName[0];
    Optional<FlowTaskNode> task = TypeDefFinder.getTask(flow.getTask(), taskName);
    if(task.isPresent()) {
      String nextPath = node.getValue().substring(node.getValue().indexOf(".") + 1);
      return CodeBlock.builder()
          .add("input.$L.$L.getDelegate().getValue().$L", 
              JavaSpecUtil.methodCall(ACCESS_STATE_VALUE),
              JavaSpecUtil.methodCall(taskName),
              JavaSpecUtil.methodCall(nextPath))
          .build();      
    }
    
    return CodeBlock.builder()
        .add("input.$L", JavaSpecUtil.methodCall(InvocationGetMethod.ACCESS_INPUT_VALUE) + (typeDef.getRequired() ? "" : ".get()"))
        .build();
  }
}
