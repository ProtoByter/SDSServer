grammar SDSRuleParser;

WS                  : [\t ]+ -> skip ;
TIME                : [0-9][0-9]':'[0-9][0-9] ;
PERIOD              : [0-6][0-9]{'s'|'m'|'h'} ;
NAME                : [a-zA-Z0-9]+ ;
DISPLAY             : 'DISPLAY' ;
ON                  : 'ON' ;
BETWEEN             : 'BETWEEN' ;
EVERY               : 'EVERY' ;

on                  : ON TIME ;
between             : BETWEEN TIME TIME ;
every               : EVERY PERIOD TIME ;
display             : DISPLAY NAME ;

sds_rule            : on | between | every | display ;

sds_statement       : sds_rule+ EOF;