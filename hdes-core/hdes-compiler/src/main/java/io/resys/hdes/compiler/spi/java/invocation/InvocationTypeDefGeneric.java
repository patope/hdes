package io.resys.hdes.compiler.spi.java.invocation;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import io.resys.hdes.ast.api.AstEnvir;
import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.Body;
import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.InstanceInvocation;
import io.resys.hdes.ast.api.nodes.AstNode.Invocation;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarType;
import io.resys.hdes.ast.api.nodes.AstNode.StaticInvocation;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.AstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodInvocation;
import io.resys.hdes.ast.api.nodes.ImmutableObjectDef;
import io.resys.hdes.ast.api.nodes.ImmutableScalarDef;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.invocation.InvocationSpec.InvocationSpecParams;

public class InvocationTypeDefGeneric implements InvocationTypeDef {
  private final AstEnvir envir;
  
  public InvocationTypeDefGeneric(AstEnvir envir) {
    super();
    this.envir = envir;
  }

  @Override
  public TypeDef apply(Invocation invocation, AstNodeVisitorContext ctx) {
    try {
      if(invocation instanceof TypeInvocation) {
        return accept((TypeInvocation) invocation, ctx);
      } else if(invocation instanceof StaticInvocation) {
        
        String[] pathName = invocation.getValue().split("\\.");
        Body body = TypeDefFinder.getBody(ctx);
        Optional<TypeDef> typeDef = TypeDefFinder.getTypeDef(body.getHeaders().getStatics().get(), pathName);
        if(typeDef.isPresent()) {
          return typeDef.get();
        }
        throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(invocation));
        
      } else if(invocation instanceof InstanceInvocation) {

        String[] pathName = invocation.getValue().split("\\.");
        Body body = TypeDefFinder.getBody(ctx);
        Optional<TypeDef> typeDef = TypeDefFinder.getTypeDef(body.getHeaders().getInstance().get(), pathName);
        if(typeDef.isPresent()) {
          return typeDef.get();
        }
        throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(invocation));
        
      }
      return accept((MethodInvocation) invocation, ctx);
    } catch(HdesCompilerException e) {
      throw e;
    } catch(Exception e) {
      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(invocation), e);
    }
  }
  
  private TypeDef accept(TypeInvocation typeName, AstNodeVisitorContext ctx) {
    String[] pathName = typeName.getValue().split("\\.");
        
    AstNodeVisitorContext lambdaCtx = TypeDefFinder.getLambda(ctx).orElse(null);
    if(lambdaCtx != null) {
      AstNodeVisitorContext lambdaParent = lambdaCtx.getParent().get();
      MethodInvocation lambdaParentNode = (MethodInvocation) lambdaParent.getValue();
      LambdaExpression expression = (LambdaExpression) lambdaCtx.getValue();
      TypeInvocation lambdaName = expression.getParams().get(0);
      ObjectDef lambdaOn = (ObjectDef) apply(lambdaParentNode.getType().get(), lambdaParent);

      Optional<TypeDef> typeDef = TypeDefFinder.getTypeDef(
          ImmutableObjectDef.builder()
          .from(lambdaOn)
          .name(lambdaName.getValue())
          .build(), 
          pathName);
      
      if(typeDef.isPresent()) {
        return typeDef.get();
      }
    }
    
    // Find from inputs
    Body body = TypeDefFinder.getBody(ctx);
    Optional<TypeDef> typeDef = TypeDefFinder.getTypeDef(body, pathName);
    if(typeDef.isPresent()) {
      return typeDef.get();
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(typeName));
  }

  private TypeDef accept(MethodInvocation method, AstNodeVisitorContext ctx) {
    final boolean isGlobal = method.getType().isEmpty();
    
    if(isGlobal) {
      Set<ScalarType> typesUsed = new HashSet<>();
      for(AstNode value : method.getValues()) {
        InvocationSpecParams params = InvocationSpec.builder().envir(envir).ctx(ctx).build(value);
        if(params.getReturnType() instanceof ObjectDef) {
          ObjectDef objectDef = (ObjectDef) params.getReturnType();
          for(TypeDef typeDef : objectDef.getValues()) {
            if(typeDef instanceof ScalarDef) {
              ScalarDef scalarDef = (ScalarDef) typeDef;
              typesUsed.add(scalarDef.getType());
            }
          }
          
        } else {
          ScalarDef scalarDef = (ScalarDef) params.getReturnType();
          typesUsed.add(scalarDef.getType());
        }
      }
      
      ScalarType scalar = typesUsed.contains(ScalarType.DECIMAL) && 
          typesUsed.contains(ScalarType.INTEGER) ? ScalarType.DECIMAL : typesUsed.iterator().next();
      
      // figure return type
      return ImmutableScalarDef.builder()
          .array(false)
          .required(true)
          .token(method.getToken())
          .name(method.getValue())
          .direction(DirectionType.IN)
          .type(scalar)
          .build();
    }
    
    // Non global methods 
    Invocation typeName = method.getType().get();
    TypeDef typeDef = apply(typeName, ctx);
    
    if(method.getValue().equals("map")) {
      return typeDef;
    }
    
    throw new HdesCompilerException(HdesCompilerException.builder().unknownFunctionCall(method, method.getValue()));
  }
}
