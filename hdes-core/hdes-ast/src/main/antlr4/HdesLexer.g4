lexer grammar HdesLexer;

DirectionType: IN | OUT;
RequiredType: REQUIRED | OPTIONAL;
DropdownType: DROPDOWN_SINGLE | DROPDOWN_MULTIPLE;

ScalarType
  : INTEGER
  | DECIMAL
  | DATE_TIME
  | DATE
  | TIME
  | STRING
  | BOOLEAN;

// BETWEEN/AND/OR operators
BETWEEN: B E T W E E N;
AND: A N D;
OR: O R;
NOT_OP: 'not';

fragment A : [aA];
fragment B : [bB];
fragment D : [dD];
fragment E : [eE];
fragment N : [nN];
fragment O : [oO];
fragment R : [rR];
fragment T : [tT];
fragment W : [wW];

DEFINE: 'define';
DEF_FL: 'flow';
DEF_DT: 'decision-table';
DEF_MT: 'manual-task';
DEF_EN: 'expression';
DEF_SE: 'service';

BODY: 'BODY';

IN: 'IN';
OUT: 'OUT';
DEBUG_VALUE: 'debug-value';
REQUIRED: 'required';
OPTIONAL: 'optional';

INTEGER: 'INTEGER';
DECIMAL: 'DECIMAL';
DATE_TIME: 'DATE_TIME';
DATE: 'DATE';
TIME: 'TIME';
STRING: 'STRING';
BOOLEAN: 'BOOLEAN';
OBJECT: 'OBJECT';
ARRAY: 'ARRAY';

DESC: 'description';
HEADERS: 'headers';
OF: 'of';
AS: 'as';
TO: 'to';

FORMULA: 'formula';

LAMBDA: '->';

// DT
ALL: 'ALL';
FIRST: 'FIRST';
MATRIX: 'MATRIX';

// MANUAL TASK
CLASS: 'class';
DROPDOWN_SINGLE: 'single-choice';
DROPDOWN_MULTIPLE: 'multiple-choice'; 
DROPDOWN: 'dropdown';
DROPDOWNS: 'dropdowns'; 

SHOW: 'show';
ACTIONS: 'actions';
MESSAGE: 'message';
MESSAGE_ERROR: 'error';
MESSAGE_INFO: 'info';
MESSAGE_WARNING: 'warning';

FORM: 'form';
FROM: 'from';
GROUPS: 'groups';
GROUP: 'group';
FIELDS: 'fields';
FIELD: 'field';
DEFAULT_VALUE: 'default-value';

// FLOW
TASKS: 'tasks';
USES: 'uses';
WHEN: 'when';
THEN: 'then';
END: 'end';

// MARKS
QUESTION_MARK: '?';
COLON: ':';
DOT: '.';
COMMA: ',';
NOT: '!';

// BLOCKS
PARENTHESES_START: '(';
PARENTHESES_END: ')';
BLOCK_START: '{';
BLOCK_END: '}';

// mathematical operators
ADD: '+';
SUBTRACT: '-';
MULTIPLY: '*';
DIVIDE: '/';
INCREMENT: '++';
DECREMENT: '--';

// equality operators
EQ_NOTEQUAL: '!=';
EQ_EQUAL: '=';
EQ_LESS: '<';
EQ_LESS_THEN: '<=';
EQ_GREATER: '>';
EQ_GREATER_THEN: '>=';

// integer literal
IntegerLiteral: '0' | NonZeroDigit (Digits? | Underscores Digits);

fragment Digit : '0' | NonZeroDigit;
fragment NonZeroDigit: [1-9];
fragment Digits: Digit (DigitsAndUnderscores? Digit)?;
fragment DigitsAndUnderscores: DigitOrUnderscore+;
fragment DigitOrUnderscore: Digit | '_';
fragment Underscores: '_'+;

// decimal literal
DecimalLiteral: Digits '.' Digits? | '.' Digits | Digits;
fragment SignedInteger: Sign? Digits;
fragment Sign: [+-];

// boolean literals
BooleanLiteral: 'true' | 'false' ;

// string literal
StringLiteral: '\'' Characters? '\'';
fragment Characters: Character+;
fragment Character: ~['\\] | Escape;

// things to escape
fragment Escape: '\\' [btnfr"'\\];

// naming convention
Identifier: Letters LettersAndDigits*;
fragment Letters: [a-zA-Z$_];
fragment LettersAndDigits: [a-zA-Z0-9$_];

// comments and white spaces
WHITE_SPACE : [ \t\r\n\u000C]+ -> channel(HIDDEN);
COMMENT_BLOCK : '/*' .*? '*/' -> channel(HIDDEN);
COMMENT_LINE : '//' ~[\r\n]* -> channel(HIDDEN);
