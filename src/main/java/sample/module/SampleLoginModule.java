/*
 * @(#)SampleLoginModule.java	1.18 00/01/11
 *
 * Copyright 2000-2002 Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the following 
 * conditions are met:
 * 
 * -Redistributions of source code must retain the above copyright  
 * notice, this  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduct the above copyright 
 * notice, this list of conditions and the following disclaimer in 
 * the documentation and/or other materials provided with the 
 * distribution.
 * 
 * Neither the name of Oracle and/or its affiliates. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY 
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY 
 * DAMAGES OR LIABILITIES  SUFFERED BY LICENSEE AS A RESULT OF  OR 
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR 
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, 
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER 
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or 
 * intended for use in the design, construction, operation or 
 * maintenance of any nuclear facility. 
 */

package sample.module;

import java.util.*;
import java.io.IOException;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.spi.*;


import sample.principal.Role;
import sample.principal.UserPrincipal;

import sample.Log;



/**
 * <p> This sample LoginModule authenticates users with a password.
 * 
 * <p> This LoginModule only recognizes one user:	testUser
 * <p> testUser's password is:	testPassword
 *
 * <p> If testUser successfully authenticates itself,
 * a <code>UserPrincipal</code> with the testUser's user name
 * is added to the Subject.
 *
 * <p> This LoginModule recognizes the debug option.
 * If set to true in the login Configuration,
 * debug messages will be output to the output stream, System.out.
 *
 * @version 1.18, 01/11/00
 */
public class SampleLoginModule implements LoginModule {

   /** the following configuration is supported by the immediately following LoginModule data members:
     *   debug="false"
     *   providerUrl="ldap://foo"
     *   contextFactory="com.sun.jndi.ldap.LdapCtxFactory"
     *   hostname="10.70.13.107"
     *   port="389"
     *   userBaseDn="ou=People,dc=dtolabs,dc=com"
     *   userRdnAttribute="uid"
     *   userIdAttribute="uid"
     *   userPasswordAttribute="userPassword"
     *   userObjectClass="posixAccount"
     *   roleBaseDn="ou=roles,dc=dtolabs,dc=com"
     *   roleNameAttribute="cn"
     *   roleMemberAttribute="memberUid"
     *   roleUsernameMemberAttribute="memberUid"
     *   roleObjectClass="posixGroup"
     */

   /* ***BEGIN LoginModule data members*** */

   /** 
     * configurable option
     */ 
    private boolean _debug = false;

   /**
     * Provider URL
     */
    private String _providerUrl;
    
    /**
      * Context.INITIAL_CONTEXT_FACTORY
      */
    private String _contextFactory;

    /**
      * hostname of the ldap server
      */
    private String _hostname;

    /**
      * port of the ldap server
      */
    private int _port = 389;

    /**
      * base DN where users are to be searched from
      */
    private String _userBaseDn;

    /**
      * attribute that the principal is located
      */
    private String _userRdnAttribute = "uid";

    /**
      * attribute that the principal is located
      */
    private String _userIdAttribute = "cn";

    /**
      * name of the attribute that a users password is stored under
      * NOTE: not always accessible, see force binding login
      */
    private String _userPasswordAttribute = "userPassword";

    /**
      * object class of a user
      */
    private String _userObjectClass = "inetOrgPerson";

    /**
      * base DN where role membership is to be searched from
      */
    private String _roleBaseDn;

    /**
      * the name of the attribute that a role would be stored under
      */
    private String _roleNameAttribute = "roleName";

    /**
      * name of the attribute that a user DN would be under a role class
      */
    private String _roleMemberAttribute = "uniqueMember";

    /**
      * object class of roles
      */
    private String _roleObjectClass = "groupOfUniqueNames";

    /**
      * name of the attribute that a username would be under a role class
      */
    private String _roleUsernameMemberAttribute=null;

   /* ***END LoginModule data members*** */



    /**
      * the name of the attribute that a role wate String _roleNameAttribute = "roleName";
      * private boolean _debug;
      * if the getUserInfo can pull a password off of the user then password
      * comparison is an option for authn, to force binding login checks, set
      * this to true
      */
    private DirContext _rootContext;




    // initial state
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map options;


    // the authentication status
    private boolean succeeded = false;
    private boolean commitSucceeded = false;

    // username and password
    private String username;
    private char[] password;

    // user assigned roles
    private List<String> roleList;


    private Log log; 

    /**
     * Initialize this <code>LoginModule</code>.
     *
     * <p>
     *
     * @param subject the <code>Subject</code> to be authenticated. <p>
     *
     * @param callbackHandler a <code>CallbackHandler</code> for communicating
     *			with the end user (prompting for user names and
     *			passwords, for example). <p>
     *
     * @param sharedState shared <code>LoginModule</code> state. <p>
     *
     * @param options options specified in the login
     *			<code>Configuration</code> for this particular
     *			<code>LoginModule</code>.
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map sharedState, Map options) {

        _debug = Boolean.parseBoolean(String.valueOf(getOption(options, "debug", Boolean
                .toString(_debug))));

        this.log = new Log(this, _debug);

	this.subject = subject;
	this.callbackHandler = callbackHandler;
	this.sharedState = sharedState;
	this.options = options;

        _hostname = (String) options.get("hostname");
        log.debug("hostname: " + _hostname);

        if(options.containsKey("port")) {
            _port = Integer.parseInt((String) options.get("port"));   
            log.debug("_port: " + _port);
        }

        _providerUrl = (String) options.get("providerUrl");
        log.debug("providerUrl: " + _providerUrl);

        _contextFactory = (String) options.get("contextFactory");
        log.debug("contextFactory: " + _contextFactory);

        _userBaseDn = (String) options.get("userBaseDn");
        log.debug("userBaseDn: " + _userBaseDn);

        _roleBaseDn = (String) options.get("roleBaseDn");
        log.debug("roleBaseDn: " + _roleBaseDn);

        _userObjectClass = getOption(options, "userObjectClass", _userObjectClass);
        log.debug("userObjectClass: " + _userObjectClass);

        _userRdnAttribute = getOption(options, "userRdnAttribute", _userRdnAttribute);
        log.debug("userRdnAttribute: " + _userRdnAttribute);

        _userIdAttribute = getOption(options, "userIdAttribute", _userIdAttribute);
        log.debug("userIdAttribute: " + _userIdAttribute);

        _userPasswordAttribute = getOption(options, "userPasswordAttribute", _userPasswordAttribute);
        log.debug("userPasswordAttribute: " + _userPasswordAttribute);

        _roleObjectClass = getOption(options, "roleObjectClass", _roleObjectClass);
        log.debug("roleObjectClass: " + _roleObjectClass);

        _roleMemberAttribute = getOption(options, "roleMemberAttribute", _roleMemberAttribute);
        log.debug("roleMemberAttribute: " + _roleMemberAttribute);

        _roleUsernameMemberAttribute = getOption(options, "roleUsernameMemberAttribute", _roleUsernameMemberAttribute);
        log.debug("roleUsernameMemberAttribute: " + _roleUsernameMemberAttribute);

        _roleNameAttribute = getOption(options, "roleNameAttribute", _roleNameAttribute);
        log.debug("roleNameAttribute: " + _roleNameAttribute);


        try {
            _rootContext = new InitialDirContext(getEnvironment());
        } catch (NamingException ex) {
            throw new IllegalStateException("Unable to establish root context", ex);
        }

    }

    /**
     * Authenticate the user by prompting for a user name and password.
     *
     * <p>
     *
     * @return true in all cases since this <code>LoginModule</code>
     *		should not be ignored.
     *
     * @exception FailedLoginException if the authentication fails. <p>
     *
     * @exception LoginException if this <code>LoginModule</code>
     *		is unable to perform the authentication.
     */
    public boolean login() throws LoginException {

	// callback for the username
	if (callbackHandler == null)
	    throw new LoginException("Error: no CallbackHandler available " +
			"to garner authentication information from the user");

	Callback[] callbacks = new Callback[1];
	callbacks[0] = new NameCallback("user name: ");
	try {
	    callbackHandler.handle(callbacks);
	    username = ((NameCallback)callbacks[0]).getName();
	    log.debug("username: " + username);
            try {
                this.roleList = getUserRoles(_rootContext, username); 
            } catch (NamingException e) {
               log.error("Caught NamingException: " + e.getMessage());
               throw new LoginException(e.toString());
            }
	} catch (java.io.IOException ioe) {
	    throw new LoginException(ioe.toString());
	} catch (UnsupportedCallbackException uce) {
	    throw new LoginException("Error: " + uce.getCallback().toString() +
		" not available to garner authentication information " +
		"from the user");
	}
        succeeded = true;
        return true;
    }


    /**
     * <p> This method is called if the LoginContext's
     * overall authentication succeeded
     * (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules
     * succeeded).
     *
     * <p> If this LoginModule's own authentication attempt
     * succeeded (checked by retrieving the private state saved by the
     * <code>login</code> method), then this method associates a
     * <code>UserPrincipal</code>
     * with the <code>Subject</code> located in the
     * <code>LoginModule</code>.  If this LoginModule's own
     * authentication attempted failed, then this method removes
     * any state that was originally saved.
     *
     * <p>
     *
     * @exception LoginException if the commit fails.
     *
     * @return true if this LoginModule's own login and commit
     *		attempts succeeded, or false otherwise.
     */
    public boolean commit() throws LoginException {
	if (succeeded == false) {
	    return false;
        }

        assignRoles(this.subject, this.username, this.roleList);

        return true;

    }

    private void assignRoles(Subject subject, String username, List<String> roleList) {

       UserPrincipal userPrincipal = new UserPrincipal(username);
       if (!subject.getPrincipals().contains(userPrincipal))
          subject.getPrincipals().add(userPrincipal);


   //[javac] /home/tomcat/workspace/JaasDev/src/main/java/sample/module/SampleLoginModule.java:381: warning: [unchecked] unchecked call to <T>toArray(T[]) as a member of the raw type java.util.List
    //[javac]        String roles[] = (String [])roleList.toArray(new String[0]);

       String roles[] = (String [])roleList.toArray(new String[0]);
       for (int i=0; i<roles.length; i++) {
         log.debug("checking role: " + roles[i]);

          Role role = new Role(roles[i]);

          if (!subject.getPrincipals().contains(role)) {
             
             log.debug("adding role: " + roles[i] + " to user: " + username);
             subject.getPrincipals().add(role);
          }
       }

    }


    /**
     * <p> This method is called if the LoginContext's
     * overall authentication failed.
     * (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules
     * did not succeed).
     *
     * <p> If this LoginModule's own authentication attempt
     * succeeded (checked by retrieving the private state saved by the
     * <code>login</code> and <code>commit</code> methods),
     * then this method cleans up any state that was originally saved.
     *
     * <p>
     *
     * @exception LoginException if the abort fails.
     *
     * @return false if this LoginModule's own login and/or commit attempts
     *		failed, and true otherwise.
     */
    public boolean abort() throws LoginException {
	if (succeeded == false) {
	    return false;
	} else if (succeeded == true && commitSucceeded == false) {
	    // login succeeded but overall authentication failed
	    succeeded = false;
	    username = null;
	    if (password != null) {
		for (int i = 0; i < password.length; i++)
		    password[i] = ' ';
		password = null;
	    }
	} else {
	    // overall authentication succeeded and commit succeeded,
	    // but someone else's commit failed
	    logout();
	}
	return true;
    }

    /**
     * Logout the user.
     *
     * <p> This method removes the <code>UserPrincipal</code>
     * that was added by the <code>commit</code> method.
     *
     * <p>
     *
     * @exception LoginException if the logout fails.
     *
     * @return true in all cases since this <code>LoginModule</code>
     *          should not be ignored.
     */
    public boolean logout() throws LoginException {

//NEW
	//subject.getPrincipals().remove(userPrincipal);

       if (null != username) {
          UserPrincipal userPrincipal = new UserPrincipal(username);
          if (!subject.getPrincipals().contains(userPrincipal))
	     subject.getPrincipals().remove(new UserPrincipal(username));
       }

	succeeded = false;
	succeeded = commitSucceeded;
	username = null;
	if (password != null) {
	    for (int i = 0; i < password.length; i++)
		password[i] = ' ';
	    password = null;
	}
	return true;
    }

    @SuppressWarnings("unchecked")
    private String getOption(Map options, String key, String defaultValue) {
        Object value = options.get(key);

        if (value == null) {
            return defaultValue;
        }

        return (String) value;
    }


    /**
     * get the context for connection
     */
    @SuppressWarnings("unchecked")
    public Hashtable getEnvironment() {
        Properties env = new Properties();

        env.put(Context.INITIAL_CONTEXT_FACTORY, _contextFactory);
        if(_providerUrl != null) {
            env.put(Context.PROVIDER_URL, _providerUrl);
        } else {
            if (_hostname != null) {
                String url = "ldap://" + _hostname + "/";
                if (_port != 0) {
                    url += ":" + _port + "/";
                } 
            
                log.warn("Using hostname and port.  Use providerUrl instead: " + url);
                env.put(Context.PROVIDER_URL, url);
            }
        }
        
        return env;
    }


    private List<String> getUserRoles(DirContext dirContext, String username) throws LoginException,
            NamingException {
        String userDn = _userRdnAttribute + "=" + username + "," + _userBaseDn;
        return getUserRolesByDn(dirContext, userDn, username);
    }


    @SuppressWarnings("unchecked")
    private List<String> getUserRolesByDn(DirContext dirContext, String userDn, String username) throws LoginException,
            NamingException {
        ArrayList<String> roleList = new ArrayList<String>();

        if (dirContext == null || _roleBaseDn == null || (_roleMemberAttribute == null
                                                          && _roleUsernameMemberAttribute == null)
                || _roleObjectClass == null) {
                log.warn("No user roles found: roleBaseDn, roleObjectClass and roleMemberAttribute or roleUsernameMemberAttribute must be specified.");
            return roleList;
        }

        SearchControls ctls = new SearchControls();
        ctls.setDerefLinkFlag(true);
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String filter = "(&(objectClass={0})({1}={2}))";
        final NamingEnumeration results;
        if(null!=_roleUsernameMemberAttribute){
            Object[] filterArguments = { _roleObjectClass, _roleUsernameMemberAttribute, username };
            results = dirContext.search(_roleBaseDn, filter, filterArguments, ctls);
        }else{
            Object[] filterArguments = { _roleObjectClass, _roleMemberAttribute, userDn };
            results = dirContext.search(_roleBaseDn, filter, filterArguments, ctls);
        }


        while (results.hasMoreElements()) {
            SearchResult result = (SearchResult) results.nextElement();

            Attributes attributes = result.getAttributes();

            if (attributes == null) {
                continue;
            }

            Attribute roleAttribute = attributes.get(_roleNameAttribute);

            if (roleAttribute == null) {
                continue;
            }

            NamingEnumeration roles = roleAttribute.getAll();
            while (roles.hasMore()) {
   
                    String role = (String) roles.next();
                    log.debug("Role for user " + userDn + ": " + role); 
                    roleList.add(role);
            }
        }
        if (roleList.size() < 1) {
            log.warn("User '" + username + "' has no role membership; role query configuration may be incorrect");
        }else{
            log.info("JettyCachingLdapLoginModule: User '" + username + "' has roles: " + roleList);
        }

        return roleList;
    }



}
