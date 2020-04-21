parser grammar FlowParser;
options { tokenVocab = HdesLexer; }
import CommonParser;

taskTypes
  : MANUAL_TASK
  | FLOW_TASK 
  | DT_TASK
  | ST_TASK;
objectDataType: OBJECT | ARRAY;

flBody: typeName description? headers tasks;

tasks: 'tasks' ':' '{' taskArgs? '}';
taskArgs: nextTask (',' nextTask)*;
nextTask: typeName ':' '{' pointer taskRef? '}';

pointer: whenThenArgs | then ;
whenThenArgs: whenThen (',' whenThen)*;
whenThen: 'when' ':' whenExpression then; 
whenExpression: StringLiteral;
then: 'then' ':' (endMapping | typeName);
endMapping: 'end' mapping;


taskRef: taskTypes ':' typeName mapping;  
mapping: 'mapping' objectDataType ':' '{' mappingArgs? '}';
mappingArgs: mappingArg (',' mappingArg)*;

mappingArg: typeName ':' mappingValue;
mappingValue: typeName | literal;

