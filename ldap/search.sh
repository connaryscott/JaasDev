#@!/bin/bash
DC="dc=dtolabs,dc=com"
DN="cn=Manager,${DC}"
PASSWORD="secret"

#LDAP_URL=ldap://10.70.13.107/
#LDAP_URL=ldap://localhost/

if [ -n "${LDAP_URL}" ]
then
    H_ARG="-H ${LDAP_URL}"
else
    H_ARG=
fi

ldapsearch ${H_ARG} -w "${PASSWORD}" -b "${DC}" -x -D"${DN}"
#ldapsearch ${H_ARG} -b "${DC}" -x 
#echo ldapsearch ${H_ARG} -b "${DC},ou=roles" -x 
