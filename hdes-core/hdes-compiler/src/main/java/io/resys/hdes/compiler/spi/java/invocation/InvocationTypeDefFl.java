package io.resys.hdes.compiler.spi.java.invocation;

import java.util.Optional;

import io.resys.hdes.ast.api.AstEnvir;
import io.resys.hdes.ast.api.nodes.AstNode.Body;
import io.resys.hdes.ast.api.nodes.AstNode.Invocation;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.AstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodInvocation;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.TaskRef;
import io.resys.hdes.ast.api.nodes.ImmutableAstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.ImmutableInstanceInvocation;
import io.resys.hdes.compiler.api.HdesCompilerException;

public class InvocationTypeDefFl implements InvocationTypeDef {

  private final AstEnvir envir;
  
  public InvocationTypeDefFl(AstEnvir envir) {
    super();
    this.envir = envir;
  }

  @Override
  public TypeDef apply(Invocation invocation, AstNodeVisitorContext ctx) {
    try {
      FlowBody flow = TypeDefFinder.getBody(ctx);
      if(invocation instanceof MethodInvocation) {
        MethodInvocation method = (MethodInvocation) invocation;
        if(method.getType().isPresent()) {
          Invocation delegate = method.getType().get();
          String[] pathName = delegate.getValue().split("\\.");
          Optional<FlowTaskNode> task = TypeDefFinder.getTask(flow.getTask(), pathName[0]); 
          
          if(task.isPresent() && !task.get().getRef().isEmpty()) {
            TaskRef taskRef = task.get().getRef().get();
            return InvocationTypeDef.builder()
                .envir(envir).body(envir.getBody(taskRef.getValue())).build()
                .apply(deconstruct(delegate), ImmutableAstNodeVisitorContext.builder().parent(ctx).value(delegate).build());
          }
        }
        
        return new InvocationTypeDefGeneric(envir).apply(invocation, ctx);
      }
      
      
      String[] pathName = invocation.getValue().split("\\.");
      Optional<FlowTaskNode> task = TypeDefFinder.getTask(flow.getTask(), pathName[0]);
      
      if(task.isPresent() && !task.get().getRef().isEmpty()) {
        TaskRef taskRef = task.get().getRef().get();
        Body delegate = envir.getBody(taskRef.getValue());
        return InvocationTypeDef.builder()
            .envir(envir).body(delegate).build()
            .apply(deconstruct(invocation), ImmutableAstNodeVisitorContext.builder().parent(ctx).value(delegate).build());
      }

      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(invocation));
    } catch(HdesCompilerException e) {
      throw e;
    } catch(Exception e) {
      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(invocation), e);
    }
  }
  
  private Invocation deconstruct(Invocation src) {
    String nextPath = "instance." + src.getValue().substring(src.getValue().indexOf(".") + 1);
    return ImmutableInstanceInvocation.builder().from(src).value(nextPath).build();
  }
}
