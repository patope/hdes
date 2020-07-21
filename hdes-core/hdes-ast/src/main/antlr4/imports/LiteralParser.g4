parser grammar LiteralParser;
options { tokenVocab = HdesLexer; }

literal
  : IntegerLiteral
  | DecimalLiteral
  | BooleanLiteral
  | StringLiteral;

typeName: 'static' | 'instance' | Identifier | typeName '.' Identifier;
