JaasDev
=======

JAAS (Java Authentication and Authorization Service) for Tomcat/Rundeck Integration


Setup Tomcat 7, which will be used  with a simple build environment for building and deploying jaas.

Rundeck will be setup in a tomcat container resembling factory defaults with respect to the 

    $CATALINA_BASE/conf/tomcat-users.xml 

file.   Subsequent changes will be made to adapt it for JAAS.

This process is documented with respect to a CentOS 6.2 64-bit environment, however, should not be big stretch to use an earlier or even different linux distro.

=======

System Java/Javac, apache ant, and git:

    [root@ip-10-190-25-201]# yum install java-1.6.0-openjdk-devel
    [root@ip-10-190-25-201]# yum install ant
    [root@ip-10-190-25-201 ec2-user]# yum install git

Add tomcat user/group and become tomcat user

    [root@ip-10-190-25-201 ec2-user]# groupadd tomcat
    [root@ip-10-190-25-201 ec2-user]# useradd -g tomcat tomcat
    [root@ip-10-190-25-201 ec2-user]# su - tomcat

Install Tomcat 7

    [tomcat@ip-10-190-25-201 ~]$  curl -O 'http://apache.ziply.com/tomcat/tomcat-7/v7.0.29/bin/apache-tomcat-7.0.29.tar.gz'
    [tomcat@ip-10-190-25-201 ~]$ tar zxf apache-tomcat-7.0.29.tar.gz 

Configure/Source the .bashrc for Tomcat operation

    [tomcat@ip-10-190-25-201 ~]$ tail -5 .bashrc
    export CATALINA_HOME=$HOME/apache-tomcat-7.0.29
    export CATALINA_BASE=${CATALINA_HOME}
    export RDECK_BASE=${CATALINA_BASE}/rundeck_base
    export CATALINA_OPTS="-Drdeck.base=$RDECK_BASE -Drundeck.config.location=$RDECK_BASE/rundeck-config.properties"
    [tomcat@ip-10-190-25-201 ~]$ source  .bashrc


Configure the Rundeck War
=====================

Create the rundeck base directory

    mkdir -p ${RDECK_BASE}

Download and Explode the rundeck war

    [tomcat@ip-10-190-25-201 ~]$ cd $CATALINA_BASE/webapps
    [tomcat@ip-10-190-25-201 webapps]$ curl -o rundeck.war http://build.rundeck.org/job/rundeck-master/lastSuccessfulBuild/artifact/rundeckapp/target/rundeck-1.4.3.war
    [chuck@centos-62-64-vm3 webapps]$ mkdir rundeck
    [chuck@centos-62-64-vm3 webapps]$  cd rundeck
    [chuck@centos-62-64-vm3 rundeck]$  unzip ../rundeck.war

Remove the following jar files from the exploded rundeck war, these files conflict with the Tomcat 7 server libraries:

    [chuck@centos-62-64-vm3 rundeck]$ rm -f ${CATALINA_BASE}/webapps/rundeck/WEB-INF/lib/servlet-api-2.5-20081211.jar \
    ${CATALINA_BASE}/webapps/rundeck/WEB-INF/lib/jasper-runtime-5.5.15.jar \
    ${CATALINA_BASE}/webapps/rundeck/WEB-INF/lib/jasper-compiler-jdt-5.5.15.jar \
    ${CATALINA_BASE}/webapps/rundeck/WEB-INF/lib/jasper-compiler-5.5.15.jar

Create the Rundeck Config Properties

NOTE:  CATALINA_BASE is:  /home/tomcat/apache-tomcat-7.0.29, adjust if this does not match your environment

    [tomcat@ip-10-190-25-201 rundeck]$ cd $RDECK_BASE
    [tomcat@ip-10-190-25-201 rundeck_base]$ mkdir data
    [chuck@centos-62-64-vm3 rundeck_base]$ vi rundeck-config.properties 
    [chuck@centos-62-64-vm3 rundeck_base]$ cat rundeck-config.properties 

    loglevel.default=INFO
    rss.enabled=true
    reportservice.log4j.port=4435
    dataSource.dbCreate = update
    dataSource.url = jdbc:hsqldb:file:/home/tomcat/apache-tomcat-7.0.29/rundeck_base/data/grailsdb;shutdown=true
    rundeck.v14.rdbsupport=false


Create the tomcat-users.xml file

    [tomcat@ip-10-190-25-201 rundeck_base]$ cd $CATALINA_BASE/conf
    [tomcat@ip-10-190-25-201 conf]$ vi tomcat-users.xml 
    [chuck@centos-62-64-vm3 apache-tomcat-7.0.29]$ cat tomcat-users.xml 

    <?xml version='1.0' encoding='utf-8'?>
    <tomcat-users>
       <role rolename="tomcat"/>
       <role rolename="role1"/>
       <role rolename="admin"/>
       <user username="admin" password="admin" roles="admin,user"/>
       <user username="deploy" password="deploy" roles="deploy,user"/>
       <user username="tomcat" password="tomcat" roles="tomcat"/>
       <user username="both" password="tomcat" roles="tomcat,role1"/>
       <user username="role1" password="tomcat" roles="role1"/>
    </tomcat-users>

Start Tomcat

    [tomcat@ip-10-190-25-201 conf]$ cd $CATALINA_HOME
    [tomcat@ip-10-190-25-201 apache-tomcat-7.0.29]$ bin/startup.sh 


Verify Tomcat and Rundeck Operation using factory default admin/admin username and password:

    http://YOURNODE:8080/rundeck/

Checkout the Source
===============

(read-only)

    [tomcat@ip-10-190-25-201 workspace]$ git clone git://github.com/connaryscott/JaasDev.git
    [tomcat@ip-10-190-25-201 workspace]$ cd JaasDev
    [tomcat@ip-10-190-25-201 JaasDev]$

Build the jar
===============

    [tomcat@ip-10-190-25-201 JaasDev]$ ant install
    ...
    ...
    install:
     [copy] Copying 1 file to ${CATALINA_HOME}/lib
     [copy] Copying 1 file to ${CATALINA_HOME}/conf
     [copy] Copying 1 file to ${CATALINA_HOME}/webapps/rundeck/META-INF

The following files will be installed:

    $CATALINA_HOME/lib/JaasTutorial.jar
    $CATALINA_BASE/conf/sample_jaas.config 
    $CATALINA_BASE/webapps/rundeck/META-INF/context.xml 



After verifying rundeck is working, make noted adjustment to the $HOME/.bashrc file and re-source it
===============

    #export CATALINA_OPTS="-Drdeck.base=$RDECK_BASE -Drundeck.config.location=$RDECK_BASE/rundeck-config.properties"
    export CATALINA_OPTS="-Djava.security.auth.login.config=${CATALINA_BASE}/conf/sample_jaas.config -Drdeck.base=$RDECK_BASE -Drundeck.config.location=$RDECK_BASE/rundeck-config.properties"
    [tomcat@ip-10-190-25-201 JaasDev]$ source $HOME/.bashrc


Restart the Tomcat Server
===============

    [tomcat@ip-10-190-25-201 JaasDev]$ ${CATALINA_HOME}/bin/shutdown.sh 
    [tomcat@ip-10-190-25-201 JaasDev]$ ${CATALINA_HOME}/bin/startup.sh 

Rundeck should now respond to either tomcat-users.xml or a hardcoded username/password served by the JaasTutorial jar

    http://YOURNODE:8080/rundeck/

verify rundeck login by using both:

    admin/admin
    -and-
    testUser/testPassword


Set up OpenLDAP
===============

Install openldap packages

    [root@centos-62-64-vm3 conf]# yum install openldap-servers  openldap-clients

Configure the slapd cert and key files:

    [root@centos-62-64-vm3 conf]# openssl req -new -x509 -nodes -out /etc/pki/tls/certs/slapdcert.pem -keyout  /etc/pki/tls/certs/slapdkey.pem -days 365
    [root@centos-62-64-vm3 conf]# chown root:ldap /etc/pki/tls/certs/slapdcert.pem  /etc/pki/tls/certs/slapdkey.pem
    [root@centos-62-64-vm3 conf]# chmod 644 /etc/pki/tls/certs/slapdcert.pem
    [root@centos-62-64-vm3 conf]# chmod 740 /etc/pki/tls/certs/slapdkey.pem
 
Enable Secure LDAP (LDAPS):

    [root@centos-62-64-vm3 conf]# vi /etc/sysconfig/ldap
    [root@centos-62-64-vm3 conf]# diff /etc/sysconfig/ldap /etc/sysconfig/ldap.sav
    16c16
    < SLAPD_LDAPS=yes
    ---
    > SLAPD_LDAPS=no

Get the openldap factory default password hash:

    [root@centos-62-64-vm3 conf]# slappasswd -s secret
    {SSHA}vkoe6x9wUYkHsjZWC8Nlwdxg80k2FwDR

Adjust OpenLDAP configuration for password and dtolabs domain (make note of hashed password):

    [root@centos-62-64-vm3 conf]# cp /etc/openldap/slapd.d/cn\=config/olcDatabase\=\{2\}bdb.ldif  /etc/openldap/slapd.d/cn\=config/olcDatabase\=\{2\}bdb.ldif.sav
    [root@centos-62-64-vm3 conf]# vi /etc/openldap/slapd.d/cn\=config/olcDatabase\=\{2\}bdb.ldif
    [root@centos-62-64-vm3 ldap]# diff  /etc/openldap/slapd.d/cn\=config/olcDatabase\=\{2\}bdb.ldif  /etc/openldap/slapd.d/cn\=config/olcDatabase\=\{2\}bdb.ldif.sav 
    5c5
    < olcSuffix: dc=dtolabs,dc=com
    ---
    > olcSuffix: dc=my-domain,dc=com
    10c10
    < olcRootDN: cn=Manager,dc=dtolabs,dc=com
    ---
    > olcRootDN: cn=Manager,dc=my-domain,dc=com
    39,41d38
    < olcRootPW: {SSHA}vkoe6x9wUYkHsjZWC8Nlwdxg80k2FwDR
    < olcTLSCertificateFile: /etc/pki/tls/certs/slapdcert.pem
    < olcTLSCertificateKeyFile: /etc/pki/tls/certs/slapdkey.pem


    [root@centos-62-64-vm3 ldap]# cp /etc/openldap/slapd.d/cn\=config/olcDatabase\=\{1\}monitor.ldif  /etc/openldap/slapd.d/cn\=config/olcDatabase\=\{1\}monitor.ldif.sav
    [root@centos-62-64-vm3 ldap]# vi /etc/openldap/slapd.d/cn\=config/olcDatabase\=\{1\}monitor.ldif   
    [root@centos-62-64-vm3 ldap]# diff  /etc/openldap/slapd.d/cn\=config/olcDatabase\=\{1\}monitor.ldif  /etc/openldap/slapd.d/cn\=config/olcDatabase\=\{1\}monitor.ldif.sav 
    5c5
    <  l,cn=auth" read  by dn.base="cn=manager,dc=dtolabs,dc=com" read  by * none
    ---
    >  l,cn=auth" read  by dn.base="cn=manager,dc=my-domain,dc=com" read  by * none

Start and configure OpenLDAP for auto startup:

    [root@centos-62-64-vm3 conf]#  /sbin/service slapd start
    [root@centos-62-64-vm3 conf]#  /sbin/chkconfig slapd on


Verify OpenLDAP responds

    [root@centos-62-64-vm3 conf]# ldapsearch -w secret -D'cn=Manager,dc=DTOLABS,dc=COM' -x -b '' -s base '(objectclass=*)' namingContexts
    # extended LDIF
    #
    # LDAPv3
    # base <> with scope baseObject
    # filter: (objectclass=*)
    # requesting: namingContexts 
    #
    
    #
    dn:
    namingContexts: dc=dtolabs,dc=com
    
    # search result
    search: 2
    result: 0 Success
    
    # numResponses: 2
    # numEntries: 1
    
    [root@centos-62-64-vm3 conf]# echo $?
    0


Setup the namespace, groups, and member users:

    [tomcat@ip-10-70-13-107 JaasDev]$ cd ldap/
    [tomcat@ip-10-70-13-107 ldap]$ ./upload.sh
    [tomcat@ip-10-70-13-107 ldap]$ ./search.sh

    # extended LDIF
    #
    # LDAPv3
    # base <dc=dtolabs,dc=com> with scope subtree
    # filter: (objectclass=*)
    # requesting: ALL
    #
    
    # dtolabs.com
    dn: dc=dtolabs,dc=com
    objectClass: dcObject
    objectClass: organization
    o: dtolabs.com
    dc: dtolabs
    
    # People, dtolabs.com
    dn: ou=People,dc=dtolabs,dc=com
    objectClass: organizationalUnit
    objectClass: top
    ou: People
    
    # roles, dtolabs.com
    dn: ou=roles,dc=dtolabs,dc=com
    objectClass: organizationalUnit
    objectClass: top
    ou: roles
    
    # rfirefly, People, dtolabs.com
    dn: uid=rfirefly,ou=People,dc=dtolabs,dc=com
    uid: rfirefly
    cn: Rufus Firefly
    objectClass: account
    objectClass: posixAccount
    objectClass: top
    objectClass: shadowAccount
    userPassword:: e0NSWVBUfW95R3RDNnYyTTl3TUE=
    shadowLastChange: 15140
    shadowMax: 99999
    shadowWarning: 7
    loginShell: /bin/bash
    uidNumber: 5000
    gidNumber: 5000
    homeDirectory: /home/rfirefly
    
    # tspaulding, People, dtolabs.com
    dn: uid=tspaulding,ou=People,dc=dtolabs,dc=com
    uid: tspaulding
    cn: Captain Spaulding
    objectClass: account
    objectClass: posixAccount
    objectClass: top
    objectClass: shadowAccount
    userPassword:: e0NSWVBUfW95R3RDNnYyTTl3TUE=
    shadowLastChange: 15140
    shadowMax: 99999
    shadowWarning: 7
    loginShell: /bin/bash
    uidNumber: 5001
    gidNumber: 5001
    homeDirectory: /home/tspaulding
    
    # odriftwood, People, dtolabs.com
    dn: uid=odriftwood,ou=People,dc=dtolabs,dc=com
    uid: odriftwood
    cn: Otis Driftwood
    objectClass: account
    objectClass: posixAccount
    objectClass: top
    objectClass: shadowAccount
    userPassword:: e0NSWVBUfW95R3RDNnYyTTl3TUE=
    shadowLastChange: 15140
    shadowMax: 99999
    shadowWarning: 7
    loginShell: /bin/bash
    uidNumber: 5002
    gidNumber: 5002
    homeDirectory: /home/odriftwood
    
    # admin, roles, dtolabs.com
    dn: cn=admin,ou=roles,dc=dtolabs,dc=com
    objectClass: top
    objectClass: posixGroup
    userPassword:: e2NyeXB0fXg=
    gidNumber: 1001
    cn: admin
    memberUid: rfirefly
    
    # user, roles, dtolabs.com
    dn: cn=user,ou=roles,dc=dtolabs,dc=com
    objectClass: top
    objectClass: posixGroup
    userPassword:: e2NyeXB0fXg=
    gidNumber: 1002
    cn: user
    memberUid: rfirefly
    memberUid: tspaulding
    memberUid: odriftwood
    
    # search result
    search: 2
    result: 0 Success
    
    # numResponses: 9
    # numEntries: 8
