parser grammar ExpressionParser;
options { tokenVocab = HdesLexer; }
import LiteralParser;


methodName: Identifier;

// method invocation
methodInvocation
  : methodName '(' methodArgs? ')'
  | typeName '.' methodName '(' methodArgs? ')' ('.' expression)?;
methodArgs: methodArg (',' methodArg)*;
methodArg: expression;

primary
  : literal
  | typeName
  | '(' expression ')'
  | methodInvocation;

// final output
enBody: expression;

// expressions
expression: conditionalExpression | primary | lambdaExpression;

// lambda
lambdaExpression: lambdaParameters '->' lambdaBody;
lambdaParameters: typeName | '(' typeName (',' typeName)* ')';
lambdaBody: expression;

conditionalExpression
  : conditionalOrExpression
  | conditionalOrExpression BETWEEN expression AND conditionalExpression
  | conditionalOrExpression '?' expression ':' conditionalExpression; 

conditionalOrExpression
  : conditionalAndExpression
  | conditionalOrExpression OR conditionalAndExpression;

conditionalAndExpression
  : andExpression
  | conditionalAndExpression AND conditionalOrExpression;

andExpression
  : equalityExpression
  | andExpression AND equalityExpression;

equalityExpression
  : relationalExpression
  | equalityExpression '=' relationalExpression
  | equalityExpression '!=' relationalExpression;

relationalExpression
  : additiveExpression
  | relationalExpression '<' additiveExpression
  | relationalExpression '<=' additiveExpression
  | relationalExpression '>' additiveExpression
  | relationalExpression '>=' additiveExpression;

additiveExpression
  : multiplicativeExpression
  | additiveExpression '+' multiplicativeExpression
  | additiveExpression '-' multiplicativeExpression;

multiplicativeExpression
  : unaryExpression
  | multiplicativeExpression '*' unaryExpression
  | multiplicativeExpression '/' unaryExpression;

// unary operation is an operation with only one operand
unaryExpression
  : preIncrementExpression
  | preDecrementExpression
  | unaryExpressionNotPlusMinus
  | '+' unaryExpression
  | '-' unaryExpression
  | primary;
  
preIncrementExpression: '++' unaryExpression;
preDecrementExpression: '--' unaryExpression;
unaryExpressionNotPlusMinus: postfixExpression | '!' unaryExpression;
postfixExpression: typeName ('++' | '--')*;


