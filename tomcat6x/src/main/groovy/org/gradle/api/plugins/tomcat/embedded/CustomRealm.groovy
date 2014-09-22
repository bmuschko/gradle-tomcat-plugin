package org.gradle.api.plugins.tomcat.embedded

import org.apache.catalina.realm.UserDatabaseRealm
import org.apache.catalina.users.MemoryUserDatabase

class CustomRealm extends UserDatabaseRealm {

    public CustomRealm() {
	database = new MemoryUserDatabase()
    }
    
    void createUser(String username, String password) {
	database.createUser(username, password, username)
    }
    
    void createRoles(String username, List<String> roles) {
	def user = database.findUser(username)
	for(String role : roles) {
	    database.createRole(role, role)
	    user.addRole(database.findRole(role))
	}
    }
    
}
