#!/bin/bash
DC="dc=dtolabs,dc=com"
DN="cn=Manager,${DC}"
PASSWORD="secret"

ldapadd  -c -x -w "${PASSWORD}" -D "${DN}" -f dtolabs_base.ldif
ldapadd  -c -x -w "${PASSWORD}" -D "${DN}" -f dtolabs_users.ldif
ldapadd  -c -x -w "${PASSWORD}" -D "${DN}" -f dtolabs_groups.ldif
ldapmodify  -c -x -w "${PASSWORD}" -D "${DN}" -f dtolabs_groupadd.ldif
ldapmodify  -c -x -w "${PASSWORD}" -D "${DN}" -f dtolabs_passwords.ldif
