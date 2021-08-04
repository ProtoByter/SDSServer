grammar SDSAuth;

@header {
package me.protobyte.sdsserver.config;
}

WS                  : [\t\n\r ]+ -> skip ;
COMMENT             : '//' ~('\r' | '\n')* -> skip ;
ENTRY_START         : '{' ;
ENTRY_END           : '}' ;
SEPARATOR           : ',' ;
STRING	            : '"' ~('\r' | '\n' | '"')* '"';

string              : STRING ;
user_entry          : 'U:' string ;
realm_entry         : 'R:' string ;
pw_entry            : 'P:' string ;
digest              : 'D' ;
oauth               : 'O' ;
type_entry          : 'T:' ;

combo_urp           : user_entry SEPARATOR realm_entry SEPARATOR pw_entry ;
combo_upr           : user_entry SEPARATOR pw_entry SEPARATOR realm_entry ;
combo_rup           : realm_entry SEPARATOR user_entry SEPARATOR pw_entry ;
combo_rpu           : realm_entry SEPARATOR pw_entry SEPARATOR user_entry ;
combo_pur           : pw_entry SEPARATOR user_entry SEPARATOR realm_entry ;
combo_pru           : pw_entry SEPARATOR realm_entry SEPARATOR user_entry ;

combo               :
                    | combo_urp
                    | combo_upr
                    | combo_rup
                    | combo_rpu
                    | combo_pur
                    | combo_pru ;



d_entry_non_end     : ENTRY_START type_entry digest SEPARATOR combo ENTRY_END SEPARATOR ;

d_end_entry         : ENTRY_START type_entry digest SEPARATOR combo ENTRY_END ;

o_entry_non_end     : ENTRY_START type_entry oauth SEPARATOR user_entry ENTRY_END SEPARATOR ;

o_entry_end         : ENTRY_START type_entry oauth SEPARATOR user_entry ENTRY_END ;

entry_end           : ( d_end_entry | o_entry_end ) ;

entry_non_end       : ( d_entry_non_end | o_entry_non_end ) ;

entries             : entry_non_end* entry_end EOF ;