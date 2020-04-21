package io.resys.hdes.compiler.spi;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import io.resys.hdes.ast.api.nodes.AstNode.ObjectTypeDefNode;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.TaskRef;

public interface NamingContext {
  
  FlNamingContext fl();
  DtNamingContext dt();
  ClassName immutable(ClassName src);
  
  interface DtNamingContext {
    String pkg();
    ClassName interfaze(DecisionTableBody node);
    TypeName superinterface(DecisionTableBody node);
    ClassName impl(DecisionTableBody node);
    
    ClassName input(DecisionTableBody node);
    ClassName inputSuperinterface(DecisionTableBody node);
    
    ClassName output(DecisionTableBody node);
    ClassName outputSuperinterface(DecisionTableBody node);
  }
 
  interface FlNamingContext {
    String pkg();
    
    ClassName ref(TaskRef ref);
    ClassName refInput(TaskRef ref);
    ClassName refOutput(TaskRef ref);
    String refMethod(TaskRef ref);
    
    ClassName interfaze(FlowBody node);
    TypeName superinterface(FlowBody node); 
    
    ClassName state(FlowBody node);
    TypeName stateSuperinterface(FlowBody node);
    
    ClassName impl(FlowBody node);
    ClassName input(FlowBody node);
    ClassName input(FlowBody node, ObjectTypeDefNode object);
    
    ClassName output(FlowBody node);
    
    ClassName taskState(FlowBody body, FlowTaskNode task);
    TypeName taskStateSuperinterface(FlowBody body, FlowTaskNode task);
  }
}