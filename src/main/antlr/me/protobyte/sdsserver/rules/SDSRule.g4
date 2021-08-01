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
NEWLINE             : ('\r\n'|'\n') ;
NAME                : [a-zA-Z0-9.]+ ;

name                : NAME ;
time                : TIME ;
period              : PERIOD ;
on                  : ON name ;
between             : BETWEEN time time ;
every               : EVERY period time
                    | EVERY period ;
display             : DISPLAY name ;

combo_boed          : between on every display ;
combo_bode          : between on display every ;
combo_beod          : between every on display ;
combo_bedo          : between every display on ;
combo_bdoe          : between display on every ;
combo_bdeo          : between display every on ;
combo_obed          : on between every display ;
combo_obde          : on between display every ;
combo_oebd          : on every between display ;
combo_oedb          : on every display between ;
combo_odbe          : on display between every ;
combo_odeb          : on display every between ;
combo_ebod          : every between on display ;
combo_ebdo          : every between display on ;
combo_eobd          : every on between display ;
combo_eodb          : every on display between ;
combo_edbo          : every display between on ;
combo_edob          : every display on between ;
combo_dboe          : display between on every ;
combo_dbeo          : display between every on ;
combo_dobe          : display on between every ;
combo_doeb          : display on every between ;
combo_debo          : display every between on ;
combo_deob          : display every on between ;

combo               : ( combo_boed |  combo_bode |  combo_beod |  combo_bedo |  combo_bdoe |  combo_bdeo |  combo_obed |  combo_obde |  combo_oebd |  combo_oedb |  combo_odbe |  combo_odeb |  combo_ebod |  combo_ebdo |  combo_eobd |  combo_eodb |  combo_edbo |  combo_edob |  combo_dboe |  combo_dbeo |  combo_dobe |  combo_doeb |  combo_debo |  combo_deob ) ;

sds_rule            : combo ;

sds_statement       : sds_rule+ NEWLINE ;

sds_statements      : sds_statement+ EOF ;