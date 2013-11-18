/*
 *  Copyright (C) 2007 - 2011 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 * 
 *  GPLv3 + Classpath exception
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geostore.services.rest.shibboleth;

import it.geosolutions.geostore.core.model.User;
import it.geosolutions.geostore.core.model.enums.Role;
import it.geosolutions.geostore.services.UserService;
import it.geosolutions.geostore.services.exception.NotFoundServiceEx;
import it.geosolutions.geostore.services.rest.utils.GeoStorePrincipal;
import it.geosolutions.geostore.services.rest.utils.GeoStoreSecurityContext;

import java.util.List;
import java.util.Map;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.security.AccessDeniedException;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.security.SecurityContext;
import org.apache.log4j.Logger;

/**
 *
 * Class GeoStoreAuthenticationInterceptor. Starting point was JAASLoginInterceptor.
 * 
 * @author ETj (etj at geo-solutions.it)
 * @author Tobia di Pisa (tobia.dipisa at geo-solutions.it)
 */
public class GeoStoreShibbolethAuthenticationInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Logger LOGGER = Logger.getLogger(GeoStoreShibbolethAuthenticationInterceptor.class);

    private UserService userService;
    private String userHeader;
    private boolean createUnexistingUsers = false;

	/**
	 * @param userService the userService to set
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	
	
	public void setUserHeader(String userHeader) {
		this.userHeader = userHeader;
	}

	


	public void setCreateUnexistingUsers(boolean createUnexistingUsers) {
		this.createUnexistingUsers = createUnexistingUsers;
	}



	public GeoStoreShibbolethAuthenticationInterceptor() {
        super(Phase.UNMARSHAL);
    }

    /* (non-Javadoc)
     * @see org.apache.cxf.interceptor.Interceptor#handleMessage(org.apache.cxf.message.Message)
     */
    @Override
    public void handleMessage(Message message) throws Fault {
    	if(LOGGER.isInfoEnabled()){
            LOGGER.info("In handleMessage");
            LOGGER.info("Message --> " + message) ;
    	}
        
    	Map<String, List<String>> headers = (Map<String, List<String>>)message.get(Message.PROTOCOL_HEADERS);
    	String username = null;
    	User user = null;
    	if(headers.containsKey(userHeader)) {
    		username = headers.get(userHeader).get(0);
    		try {
				user =  userService.get(username);
			} catch (NotFoundServiceEx e) {
				if(LOGGER.isInfoEnabled()) {
	            	LOGGER.info("Requested user not found: " + username);
				}
				if(createUnexistingUsers) {
					if(LOGGER.isInfoEnabled()) {
		            	LOGGER.info("Creating now");
					}
					user = new User();
					user.setName(username);
					user.setNewPassword(username);
					user.setRole(Role.USER);					
					try {
						user.setId(userService.insert(user));
					} catch (Exception e1) {
						throw new AccessDeniedException("Not able to create new user");
					}
				} else {	            
		            throw new AccessDeniedException("Not authorized");
				}
			} catch (Exception e) {
	            	LOGGER.error("Exception while checking pw: " + username, e);
	            throw new AccessDeniedException("Authorization error");
			}
    	}
		GeoStoreSecurityContext securityContext = new GeoStoreSecurityContext();
        GeoStorePrincipal principal = user != null ?
                new GeoStorePrincipal(user) : GeoStorePrincipal.createGuest();
        securityContext.setPrincipal(principal);

        message.put(SecurityContext.class, securityContext);    	        
    }
    
}