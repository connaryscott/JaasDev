JaasDev
=======

JAAS (Java Authentication and Authorization Service) for Tomcat/Rundeck Integration


Setup Tomcat Environment for Rundeck Build and Test
=======

System Java/Javac, apache ant, and git:

root@ip-10-190-25-201]# yum install java-1.6.0-openjdk-devel

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

chuck@centos-62-64-vm3 apache-tomcat-7.0.29]$ cat tomcat-users.xml 

`
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
`

Start Tomcat

[tomcat@ip-10-190-25-201 conf]$ cd $CATALINA_HOME
[tomcat@ip-10-190-25-201 apache-tomcat-7.0.29]$ bin/startup.sh 
