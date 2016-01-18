/*
 *  Copyright (C) 2007 - 2012 GeoSolutions S.A.S.
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
package it.geosolutions.geostore.services.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.geosolutions.geostore.core.model.Attribute;
import it.geosolutions.geostore.core.model.Resource;
import it.geosolutions.geostore.core.model.SecurityRule;
import it.geosolutions.geostore.core.model.User;
import it.geosolutions.geostore.core.model.UserGroup;
import it.geosolutions.geostore.core.model.enums.DataType;
import it.geosolutions.geostore.core.model.enums.Role;
import it.geosolutions.geostore.services.dto.ShortAttribute;
import it.geosolutions.geostore.services.dto.search.BaseField;
import it.geosolutions.geostore.services.dto.search.CategoryFilter;
import it.geosolutions.geostore.services.dto.search.FieldFilter;
import it.geosolutions.geostore.services.dto.search.SearchFilter;
import it.geosolutions.geostore.services.dto.search.SearchOperator;
import it.geosolutions.geostore.services.rest.model.RESTCategory;
import it.geosolutions.geostore.services.rest.model.RESTResource;
import it.geosolutions.geostore.services.rest.model.RESTStoredData;
import it.geosolutions.geostore.services.rest.model.ResourceList;
import it.geosolutions.geostore.services.rest.model.SecurityRuleList;
import it.geosolutions.geostore.services.rest.model.ShortResourceList;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.helpers.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * 
 * @author ETj (etj at geo-solutions.it)
 */
public class GeoStoreClientTest extends BaseGeoStoreClientTest {
    private final static Logger LOGGER = Logger.getLogger(GeoStoreClientTest.class);

    final String DEFAULTCATEGORYNAME = "TestCategory1";

    
    
    @Test
    public void testRemoveAllAttribs() {

        final String KEY_STRING = "stringAtt";

        final Date origDate = new Date();
        final String origString = "OrigStringValue";

        RESTStoredData storedData = new RESTStoredData();
        storedData.setData("we wish you a merry xmas and a happy new year");

        List<ShortAttribute> attrList = new ArrayList<ShortAttribute>();
        attrList.add(new ShortAttribute(KEY_STRING, origString, DataType.STRING));

        String timeid = Long.toString(System.currentTimeMillis());

        createDefaultCategory();

        RESTResource origResource = new RESTResource();
        origResource.setCategory(new RESTCategory(DEFAULTCATEGORYNAME));
        origResource.setName("rest_test_resource_" + timeid);
        origResource.setStore(storedData);
        origResource.setAttribute(attrList);

        Long rid = client.insert(origResource);
        System.out.println("RESOURCE has ID " + rid);

        // test getResource
        {
            Resource loaded = client.getResource(rid);
            System.out.println("RESOURCE: " + loaded);

            // test reloaded attrs
            List<Attribute> loadedAttrs = loaded.getAttribute();
            assertEquals(1, loadedAttrs.size());
        }

        // remove attrib list
        // once updated, the attribs should be the same
        origResource.setAttribute(null);
        client.updateResource(rid, origResource);
        {
            Resource loaded = client.getResource(rid);
            System.out.println("RESOURCE: " + loaded);

            // test reloaded attrs
            List<Attribute> loadedAttrs = loaded.getAttribute();
            assertEquals(1, loadedAttrs.size());
        }

        // reattach a 0-length list
        // once updated, there should be no attribs in the resource
        origResource.setAttribute(new ArrayList<ShortAttribute>());
        assertTrue(origResource.getAttribute().isEmpty());

        client.updateResource(rid, origResource);

        // test getResource
        {
            Resource loaded = client.getResource(rid);
            System.out.println("RESOURCE: " + loaded);

            // test reloaded attrs
            List<Attribute> loadedAttrs = loaded.getAttribute();
            assertEquals(0, loadedAttrs.size());
        }

    }

    @Test
    // @Ignore
    public void testInsertResource() {

        final String KEY_STRING = "stringAtt";
        final String KEY_DATE = "dateAtt";

        final Date origDate = new Date();
        final String origString = "OrigStringValue";

        createDefaultCategory();

        RESTStoredData storedData = new RESTStoredData();
        storedData.setData("we wish you a merry xmas and a happy new year");

        List<ShortAttribute> attrList = new ArrayList<ShortAttribute>();
        attrList.add(new ShortAttribute(KEY_STRING, origString, DataType.STRING));
        attrList.add(ShortAttribute.createDateAttribute(KEY_DATE, origDate));

        String timeid = Long.toString(System.currentTimeMillis());

        RESTResource origResource = new RESTResource();
        origResource.setCategory(new RESTCategory(DEFAULTCATEGORYNAME));
        origResource.setName("rest_test_resource_" + timeid);
        origResource.setStore(storedData);
        origResource.setAttribute(attrList);

        Long rid = client.insert(origResource);
        System.out.println("RESOURCE has ID " + rid);

        // test getResource
        {
            Resource loaded = client.getResource(rid);
            System.out.println("RESOURCE: " + loaded);

            // test reloaded attrs
            List<Attribute> loadedAttrs = loaded.getAttribute();
            assertEquals(2, loadedAttrs.size());

            Attribute satt, datt;

            if (loadedAttrs.get(0).getType() == DataType.STRING) {
                satt = loadedAttrs.get(0);
                datt = loadedAttrs.get(1);
            } else {
                datt = loadedAttrs.get(0);
                satt = loadedAttrs.get(1);
            }

            assertEquals(DataType.STRING, satt.getType());
            assertEquals(KEY_STRING, satt.getName());
            assertEquals(origString, satt.getTextValue());

            assertEquals(DataType.DATE, datt.getType());
            assertEquals(KEY_DATE, datt.getName());
            assertEquals(origDate, datt.getDateValue());

        }
        // test Search
        SearchFilter searchFilter = new FieldFilter(BaseField.NAME, "%" + timeid,
                SearchOperator.LIKE);
        ShortResourceList rlist = client.searchResources(searchFilter);
        assertNotNull(rlist);
        assertEquals(1, rlist.getList().size());
        assertEquals(rid, (Long) rlist.getList().get(0).getId());
    }

    @Test
    public void testUpdateResource() {

        final String KEY_STRING = "stringAtt";
        final String KEY_DATE = "dateAtt";

        final Date origDate = new Date();
        final String origString = "OrigStringValue";

        Long rid;

        createDefaultCategory();

        {
            RESTStoredData storedData = new RESTStoredData();
            storedData.setData("we wish you a merry xmas and a happy new year");

            List<ShortAttribute> attrList = new ArrayList<ShortAttribute>();
            attrList.add(new ShortAttribute("string1", "value1", DataType.STRING));
            attrList.add(new ShortAttribute("string2", "value2", DataType.STRING));
            attrList.add(new ShortAttribute("string3", "value3", DataType.STRING));

            String timeid = Long.toString(System.currentTimeMillis());

            RESTResource origResource = new RESTResource();
            origResource.setCategory(new RESTCategory(DEFAULTCATEGORYNAME));
            origResource.setName("rest_test_resource_" + timeid);
            origResource.setStore(storedData);
            origResource.setAttribute(attrList);

            rid = client.insert(origResource);
        }
        System.out.println("RESOURCE has ID " + rid);

        // test getResource
        String name1 = "rest_test_resource_" + Long.toString(System.currentTimeMillis());
        {
            RESTResource updResource = new RESTResource();
            updResource.setName(name1);

            List<ShortAttribute> attrList = new ArrayList<ShortAttribute>();
            attrList.add(new ShortAttribute("string1", "value1", DataType.STRING)); // same
            attrList.add(new ShortAttribute("string2", "value2.2", DataType.STRING)); // updated
            // attrList.add(new ShortAttribute("string3", "value3", DataType.STRING)); //removed
            attrList.add(new ShortAttribute("string4", "value4", DataType.STRING)); // added

            updResource.setAttribute(attrList);
            client.updateResource(rid, updResource);
        }

        {
            Resource loaded = client.getResource(rid);
            System.out.println("RESOURCE: " + loaded);

            // test reloaded attrs
            List<Attribute> loadedAttrs = loaded.getAttribute();
            assertEquals(3, loadedAttrs.size());

            Map<String, String> attMap = new HashMap<String, String>();
            for (Attribute attribute : loadedAttrs) {
                attMap.put(attribute.getName(), attribute.getTextValue());
            }

            assertEquals("value1", attMap.get("string1"));
            assertEquals("value2.2", attMap.get("string2"));
            assertEquals("value4", attMap.get("string4"));
        }

        // try bad update
        {
            RESTResource res = new RESTResource();
            res.setCategory(new RESTCategory("TestCategory2"));
            try {
                client.updateResource(rid, res);
                fail("Undetected error");
            } catch (UniformInterfaceException e) {
                String response = "COULD NOT READ RESPONSE";
                try {
                    response = IOUtils.toString(e.getResponse().getEntityInputStream());
                } catch (Exception e2) {
                    LOGGER.warn("Error reading response: " + e2.getMessage());
                }
                LOGGER.info("Error condition successfully detected: " + response);
            } catch (Exception e) {
                LOGGER.info("Error condition successfully detected:" + e.getMessage(), e);

            }
        }

        client.deleteResource(rid);
    }

    @Test
    public void testSearchByCategory() {

        createDefaultCategory();

        RESTResource res = new RESTResource();
        res.setCategory(new RESTCategory(DEFAULTCATEGORYNAME));

        String timeid = Long.toString(System.currentTimeMillis());
        res.setName("rest_test_resource_" + timeid);

        Long id = client.insert(res);

        SearchFilter filter = new CategoryFilter(DEFAULTCATEGORYNAME, SearchOperator.EQUAL_TO);
        ShortResourceList resources = client.searchResources(filter);
        assertEquals(1, resources.getList().size());

    }

    // @Test
    // public void testGetResource() {
    // GeoStoreClient client = createClient();
    // Resource resource = client.getResource(261l);
    // System.out.println("Resource is " + resource);
    // if(resource.getAttribute() != null) {
    // System.out.println("Attributes " + resource.getAttribute());
    // } else {
    // System.out.println("No attrs");
    // }
    // }

    @Test
    public void testClearAll() {

        Long catId1 = client.insert(new RESTCategory("Test Category#1"));
        Long catId2 = client.insert(new RESTCategory("Test Category#2"));

        assertEquals(2, client.getCategories().getList().size());

        client.insert(createSampleResource(catId1));
        client.insert(createSampleResource(catId2));
        client.insert(createSampleResource(catId2));

        {
            SearchFilter filter = new FieldFilter(BaseField.NAME, "*", SearchOperator.IS_NOT_NULL);
            ShortResourceList resources = client.searchResources(filter);
            assertEquals(3, resources.getList().size());
        }
    }

    @Test
    public void testGetResourceFull() {

        createDefaultCategory();

        final String DATA = "we wish you a merry xmas and a happy new year";

        RESTStoredData storedData = new RESTStoredData();
        storedData.setData(DATA);

        RESTResource origResource = new RESTResource();
        origResource.setCategory(new RESTCategory(DEFAULTCATEGORYNAME));
        origResource.setName("rest_test_resource_getFull");
        origResource.setStore(storedData);

        Long rid = client.insert(origResource);
        System.out.println("RESOURCE has ID " + rid);

        // make sure data has been saved
        {
            String data = client.getData(rid);
            assertEquals(DATA, data);
        }

        // test getResource
        {
            Resource loaded = client.getResource(rid);
            System.out.println("RESOURCE: " + loaded);
            assertNull(loaded.getData());
        }

        {
            Resource loaded = client.getResource(rid, true);
            System.out.println("RESOURCE: " + loaded);
            assertNotNull(loaded.getData());
        }

    }

    @Test
    public void testSearch01() {

        // SETUP
        createDefaultCategory();

        RESTResource res = new RESTResource();
        res.setCategory(new RESTCategory(DEFAULTCATEGORYNAME));
        res.setName("rest_test_resource_1");
        res.setAttribute(new ArrayList<ShortAttribute>());
        res.getAttribute().add(new ShortAttribute("name1", "value1", DataType.STRING));
        res.getAttribute().add(new ShortAttribute("name2", "value2", DataType.STRING));
        res.setData("pippo");

        Long id = client.insert(res);
        assertNotNull(id);

        // TEST
        SearchFilter filter = new CategoryFilter(DEFAULTCATEGORYNAME, SearchOperator.EQUAL_TO);

        {
            ResourceList resources = client.searchResources(filter, null, null, false, false);
            assertEquals(1, resources.getList().size());
            assertNull(resources.getList().get(0).getAttribute());
            assertNull(resources.getList().get(0).getData());
        }

        {
            ResourceList resources = client.searchResources(filter, null, null, true, false);
            assertEquals(1, resources.getList().size());
            assertNotNull(resources.getList().get(0).getAttribute());
            assertEquals(2, resources.getList().get(0).getAttribute().size());
            assertNull(resources.getList().get(0).getData());
        }

        {
            ResourceList resources = client.searchResources(filter, null, null, true, true);
            assertEquals(1, resources.getList().size());
            assertNotNull(resources.getList().get(0).getAttribute());
            assertEquals(2, resources.getList().get(0).getAttribute().size());
            assertNotNull(resources.getList().get(0).getData());
            assertEquals("pippo", resources.getList().get(0).getData().getData());
        }

    }

    @Test
    public void testDefaultSecurityRules() {
    	createDefaultCategory();

        RESTResource res = new RESTResource();
        res.setCategory(new RESTCategory(DEFAULTCATEGORYNAME));

        String timeid = Long.toString(System.currentTimeMillis());
        res.setName("rest_test_resource_" + timeid);

        Long id = client.insert(res);

        SecurityRuleList rules = client.getSecurityRules(id);
        assertNotNull(rules.getList());
        assertEquals(1, rules.getList().size());
    }
    
    @Test
    public void testupdateSecurityRules() {
    	AdministratorGeoStoreClient adminClient = new AdministratorGeoStoreClient();
    	adminClient.setGeostoreRestUrl("http://localhost:9191/geostore/rest");
    	adminClient.setUsername("admin");
    	adminClient.setPassword("admin");
    	
    	createDefaultCategory();

        RESTResource res = new RESTResource();
        res.setCategory(new RESTCategory(DEFAULTCATEGORYNAME));

        String timeid = Long.toString(System.currentTimeMillis());
        res.setName("rest_test_resource_" + timeid);

        User u1 = new User();
        u1.setName("u1_" + timeid);
        u1.setRole(Role.USER);
        Long userId = adminClient.insert(u1);
        
        UserGroup g1 = new UserGroup();
        g1.setGroupName("g1_"  + timeid);
        Long groupId = adminClient.insertUserGroup(g1);
        
        
        Long id = client.insert(res);
        List<SecurityRule> ruleList = new ArrayList<SecurityRule>();
        
        SecurityRule rule = new SecurityRule();
        rule.setCanRead(true);
        rule.setCanWrite(true);   
        User user = new User();
        user.setId(userId);
        rule.setUser(user);
        ruleList.add(rule);
        
        rule = new SecurityRule();
        rule.setCanRead(true);
        rule.setCanWrite(false); 
        UserGroup group = new UserGroup();
        group.setId(groupId);
        rule.setGroup(group);
        ruleList.add(rule);
        
        SecurityRuleList rules = new SecurityRuleList(ruleList);
        client.updateSecurityRules(id, rules);
        
        SecurityRuleList writtenRules = client.getSecurityRules(id);
        assertNotNull(writtenRules.getList());
        assertEquals(2, rules.getList().size());
    }
    
    protected Long createDefaultCategory() {
        Long catid = client.insert(new RESTCategory(DEFAULTCATEGORYNAME));
        assertNotNull(catid);
        return catid;
    }

}
