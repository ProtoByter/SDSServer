grammar SDSRule;

@header {
package me.protobyte.sdsserver.rules;
}

WS                  : [\t ]+ -> skip ;
DISPLAY             : 'DISPLAY' ;
ON                  : 'ON' ;
BETWEEN             : 'BETWEEN' ;
EVERY               : 'EVERY' ;
DIGIT               : [0-9] ;
DIGIT_6             : [0-6] ;
TIME                : DIGIT DIGIT ':' DIGIT DIGIT ;
TIMEUNIT            : ('s'|'m'|'h') ;
PERIOD              : DIGIT_6 DIGIT TIMEUNIT
                    | DIGIT TIMEUNIT ;
NAME                : [a-zA-Z0-9.]+ ;

name                : NAME ;
time                : TIME ;
period              : PERIOD ;
on                  : ON name ;
between             : BETWEEN time time ;
every               : EVERY period time
                    | EVERY period ;
display             : DISPLAY name ;

sds_rule            : ( on | between | every | display ) ;

sds_statement       : sds_rule+ EOF;