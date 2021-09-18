grammar SDSRule;

@header {
package me.protobyte.sdsserver.rules;
}

WS                  : [\t ]+ -> skip ;
COMMENT             : '//' ~('\r' | '\n')* -> skip ;
DISPLAY             : 'DISPLAY' ;
ON                  : 'ON' ;
BETWEEN             : 'BETWEEN' ;
EVERY               : 'EVERY' ;
AT                  : 'AT' ;
TRANSITION          : 'TRANSITION' ;
DIGIT               : [0-9] ;
DIGIT_6             : [0-6] ;
TIME                : DIGIT DIGIT ':' DIGIT DIGIT ;
TIMEUNIT            : ('s'|'m'|'h') ;
PERIOD              : DIGIT_6 DIGIT TIMEUNIT
                    | DIGIT TIMEUNIT ;
SEP                 : ';' | '\r\n' | '\n' ;
NAME                : [a-zA-Z0-9.]+ ;

sep                 : SEP+ ;
name                : NAME ;
time                : TIME ;
period              : PERIOD ;
on                  : ON name ;
between             : BETWEEN time time ;
every               : EVERY period time
                    | EVERY period ;
display             : DISPLAY name ;
at                  : AT time ;
transition          : TRANSITION name ;

sds_rule            : ( on | between | every | display | at | transition ) ;

sds_statement       : sds_rule+ sep ;

sds_statements      : sds_statement* EOF ;