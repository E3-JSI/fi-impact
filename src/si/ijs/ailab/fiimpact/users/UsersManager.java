package si.ijs.ailab.fiimpact.users;

import javax.management.*;
import javax.servlet.ServletOutputStream;
import javax.xml.parsers.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.lang.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONWriter;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import si.ijs.ailab.util.AIUtils;

public class UsersManager
{
  private static final  String ROLE_TOMCAT = "fiimpact";
  private static final  String ROLE_DEFAULT_FI = "admin";

  private Map<String, UserInfo> users;

  //role id, role description
  private Map<String, String> roles;

  private final static Logger logger = LogManager.getLogger(UsersManager.class.getName());
  private static UsersManager usersManager;
  private UserInfo deniedUserInfo;
  private File usersDefFile;

  private UsersManager(Path _webappRoot)
  {
    logger.info("Root: {}", _webappRoot);

    users = Collections.synchronizedMap(new HashMap<String , UserInfo>());
    roles = Collections.synchronizedMap(new HashMap<String , String>());

    usersDefFile = _webappRoot.resolve("WEB-INF").resolve("user-roles.xml").toFile();
    loadUsersDef();
    deniedUserInfo = new UserInfo("#ACCESS_DENIED#", null);
    deniedUserInfo.setDeniedUser(true);
  }

  private synchronized void loadUsersDef()
  {

    Document doc;
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder;

    try
    {
      dBuilder = dbFactory.newDocumentBuilder();
      doc = dBuilder.parse(usersDefFile);
      doc.getDocumentElement().normalize();

      NodeList nList = doc.getElementsByTagName("user");
      for(int i = 0; i < nList.getLength(); i++)
      {
        Node nNode = nList.item(i);
        Element eElement = (Element) nNode;
        UserInfo ui = new UserInfo(eElement.getAttribute("name"), eElement.getAttribute("accelerator"));
        String firstLogin = eElement.getAttribute("first-login");
        ui.setFirstLogin(firstLogin.equals("true"));

        getTomcatUserInfo(ui);
        users.put(eElement.getAttribute("name"), ui);
        NodeList nlAccess = eElement.getElementsByTagName("access");
        for(int j=0; j < nlAccess.getLength(); j++)
          ui.accessRights.add(((Element)nlAccess.item(j)).getAttribute("id"));
      }
      nList = doc.getElementsByTagName("role");
      for(int i = 0; i < nList.getLength(); i++)
      {
        Node nNode = nList.item(i);
        Element eElement = (Element) nNode;
        roles.put(eElement.getAttribute("id"), eElement.getAttribute("description"));
      }
    }
    catch (SAXException | IOException | ParserConfigurationException e)
    {
      logger.error("Error loading list definition.", e);
    }
  }

  private synchronized void saveUsersDef()
  {
    try
    {
      org.w3c.dom.Document doc;
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      doc = db.newDocument();
      Element root = doc.createElement("user-management");
      doc.appendChild(root);
      Element eRoles = doc.createElement("roles");
      root.appendChild(eRoles);
      Element eUsers = doc.createElement("users");
      root.appendChild(eUsers);

      for(Map.Entry<String, UserInfo> userInfoEntry: users.entrySet())
      {
        UserInfo userInfo = userInfoEntry.getValue();
        Element eUser = doc.createElement("user");
        eUsers.appendChild(eUser);
        eUser.setAttribute("name", userInfo.getName());
        if(userInfo.getAccelerator() != null && !userInfo.getAccelerator().equals(""))
          eUser.setAttribute("accelerator", userInfo.getAccelerator());

        if(userInfo.isFirstLogin())
          eUser.setAttribute("first-login", "true");
        else
          eUser.setAttribute("first-login", "false");

        for(String s : userInfo.getAccessRights())
        {
          Element eAccess = doc.createElement("access");
          eUser.appendChild(eAccess);
          eAccess.setAttribute("id", s);
        }
      }
      for(Map.Entry<String, String> roleEntry: roles.entrySet())
      {
        Element eRole = doc.createElement("role");
        eRoles.appendChild(eRole);
        eRole.setAttribute("id", roleEntry.getKey());
        eRole.setAttribute("description", roleEntry.getValue());
      }
      OutputStream os = new FileOutputStream(usersDefFile);
      AIUtils.save(doc, os);

    }
    catch (FileNotFoundException|ParserConfigurationException e)
    {
      logger.error("Error saving users definition.", e);
    }
  }


    public UserInfo getUserInfo(String name)
  {
    UserInfo userInfo = users.get(name);
    if(userInfo == null)
      userInfo = deniedUserInfo;
    return userInfo;
  }

  private void logMbeanInfo(MBeanInfo info)
  {
    logger.debug("{}: {}", info.getClassName(), info.getDescription());
    MBeanAttributeInfo[] mBeanAttributeInfos = info.getAttributes();
    for(int i = 0; i < mBeanAttributeInfos.length; i++)
    {
      MBeanAttributeInfo mBeanAttributeInfo = mBeanAttributeInfos[i];
      logger.debug(mBeanAttributeInfo.toString());
    }
    MBeanOperationInfo[] mBeanOperationInfos = info.getOperations();
    for(int i = 0; i < mBeanOperationInfos.length; i++)
    {
      MBeanOperationInfo mBeanOperationInfo = mBeanOperationInfos[i];
      logger.debug(mBeanOperationInfo.toString());
    }
  }

  private void getTomcatUserInfo(UserInfo userInfo)
  {
    try
    {
      logger.info("Get tomcat user info for {}", userInfo.getName());
      if(userInfo.isDeniedUser())
      {
        logger.warn("Denied user!");
      }
      else
      {
        ArrayList list = MBeanServerFactory.findMBeanServer(null);
        MBeanServer mbeanServer = (MBeanServer) list.get(0);
        ObjectName onUserDatabase = new ObjectName("Users:type=UserDatabase,database=UserDatabase");
        String userIDString = (String) mbeanServer.invoke(onUserDatabase, "findUser", new String[]{userInfo.getName()}, new String[]{String.class.getName()});
        if(userIDString == null)
        {
          logger.error("User does not exist");
          userInfo.setDeniedUser(true);
        }
        else
        {
          ObjectName onUser = new ObjectName(userIDString);
          MBeanInfo info = mbeanServer.getMBeanInfo(onUser);
          //logMbeanInfo(info);
          String[] tomcatRoles = (String[]) mbeanServer.getAttribute(onUser, "roles");
          boolean bFoundRole = false;
          for(String s: tomcatRoles)
          {
            ObjectName onRole = new ObjectName(s);
            String role = (String) mbeanServer.getAttribute(onRole, "rolename");
            logger.debug("role: {}", role);
            bFoundRole = role.equals(ROLE_TOMCAT);
            if(bFoundRole)
              break;
          }
          if(!bFoundRole)
          {
            logger.error("User {} does not have Tomcat access rights.", userInfo.getName());

            userInfo.setDeniedUser(true);
          }
          else
          {
            String description = (String) mbeanServer.getAttribute(onUser, "fullName");
            userInfo.setDescription(description);
          }
        }
      }
    }
      catch (IntrospectionException|AttributeNotFoundException|MalformedObjectNameException|ReflectionException|InstanceNotFoundException|MBeanException e)
      {
        logger.error("Error getting user info", e);
        userInfo.setDeniedUser(true);

      }
  }

  synchronized public void addUser(ServletOutputStream outputStream, String userName, String password, String accelerator, String description, UserInfo adminUserInfo) throws IOException
  {
    //user-create
    try
    {
      logger.info("Create user {}/{}", userName, description);
      if(accelerator == null)
        accelerator = "";

      ArrayList list = MBeanServerFactory.findMBeanServer(null);
      MBeanServer mbeanServer = (MBeanServer) list.get(0);
      ObjectName onUserDatabase= new ObjectName("Users:type=UserDatabase,database=UserDatabase");

      String userIDString = (String) mbeanServer.invoke(onUserDatabase,"findUser",new String[]{userName},new String[]{String.class.getName()});

      if(!adminUserInfo.getAccelerator().equals("") && !adminUserInfo.getAccelerator().equals(accelerator))
      {
        writeUserErrorResult(outputStream, userName, "user-create", "User not added. Please provide the correct accelerator");
        logger.error("Administrator {} ({}) has no privileges to add users for accelerator {}", adminUserInfo.getName(), adminUserInfo.getAccelerator(), accelerator);

      }
      else if(userIDString != null)
      {
        writeUserErrorResult(outputStream, userName, "user-create", "User already exists");
        logger.error("Error adding user - already exists");
      }
      else
      {

        userIDString = (String) mbeanServer.invoke(onUserDatabase, "createUser", new String[]{userName, password, description}, new String[]{String.class.getName(), String.class.getName(), String.class.getName()});

        //"Users:type=User,username=\""+userName+"\",database=UserDatabase";
        ObjectName onUser = new ObjectName(userIDString);
        MBeanInfo info = mbeanServer.getMBeanInfo(onUser);
        //logMbeanInfo(info);
        String addResult = (String) mbeanServer.invoke(onUser, "addRole", new String[]{ROLE_TOMCAT}, new String[]{String.class.getName()});
        mbeanServer.invoke(onUserDatabase, "save", new Object[0], new String[0]);
        logger.info("Tomcat user created: {}/{}", userName, addResult);
        UserInfo userInfo = new UserInfo(userName, accelerator);
        userInfo.setDescription(description);
        userInfo.addAccessRight(ROLE_DEFAULT_FI);
        users.put(userName, userInfo);
        saveUsersDef();
        userInfo.getProfile(outputStream, "user-create");
        logger.info("FI-IMPACT user created: {}/{}", userName, description);
      }
    }
    catch (IntrospectionException|MalformedObjectNameException|ReflectionException|InstanceNotFoundException|MBeanException e)
    {
      writeUserErrorResult(outputStream, userName, "user-create", e.getMessage());
      logger.error("Error adding user", e);
    }
  }

  public static synchronized UsersManager getUsersManager(Path _webappRoot)
  {
    if(usersManager == null)
    {
      usersManager = new UsersManager(_webappRoot);
    }
    return usersManager;
  }

  public static UsersManager getUsersManager()
  {
    return usersManager;
  }


  synchronized public void  deleteUser(ServletOutputStream outputStream, String userName, UserInfo adminUserInfo) throws IOException
  {
    //"user-delete"
    try
    {
      logger.info("Delete user {}", userName);
      UserInfo userInfo = users.get(userName);
      if(userInfo == null)
      {
        writeUserErrorResult(outputStream, userName, "user-delete", "User not defined");
        logger.error("Error deleting user - FI user does not exist");

      }
      else if(userInfo.getName().equals(adminUserInfo.getName()))
      {
        writeUserErrorResult(outputStream, userName, "user-delete", "Self-delete not allowed");
        logger.error("Error deleting user - cant delete yourself");
      }
      else if(!adminUserInfo.getAccelerator().equals("") && !adminUserInfo.getAccelerator().equals(userInfo.getAccelerator()))
      {
        writeUserErrorResult(outputStream, userName, "user-delete", "User not defined");
        logger.error("Administrator {} ({}) has no privileges to delete users for accelerator {}", adminUserInfo.getName(), adminUserInfo.getAccelerator(), userInfo.getAccelerator());
      }
      else
      {

        ArrayList list = MBeanServerFactory.findMBeanServer(null);
        MBeanServer mbeanServer = (MBeanServer) list.get(0);
        ObjectName onUserDatabase = new ObjectName("Users:type=UserDatabase,database=UserDatabase");

        String userIDString = (String) mbeanServer.invoke(onUserDatabase, "findUser", new String[]{userName}, new String[]{String.class.getName()});
        if(userIDString == null)
        {
          writeUserErrorResult(outputStream, userName, "user-delete", "User not defined");
          logger.error("Error deleting user - Tomcat user does not exist");
        }
        else
        {
          userIDString = (String) mbeanServer.invoke(onUserDatabase, "removeUser", new String[]{userName}, new String[]{String.class.getName()});
          mbeanServer.invoke(onUserDatabase, "save", new Object[0], new String[0]);
          logger.info("Tomcat user deleted: {}/{}", userName, userIDString);
          users.remove(userName);
          saveUsersDef();
          logger.info("FI-IMPACT user edeleted: {}/{}", userName);
          writeUserDeleteResult(outputStream, userName, "user-delete");
        }
      }
    }
    catch (MalformedObjectNameException|ReflectionException|InstanceNotFoundException|MBeanException e)
    {
      writeUserErrorResult(outputStream, userName, "user-delete", e.getMessage());
      logger.error("Error adding user", e);
    }
  }

  private void writeUserErrorResult(ServletOutputStream outputStream, String userName, String action, String errorMessage) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    JSONWriter json = new JSONWriter(w);
    json.object();
    json.key("user").value(userName);
    json.key("action").value(action);
    json.key("success").value("false");
    json.key("error").value(errorMessage);
    json.endObject();
    w.flush();
    w.close();
  }

  private void writeUserDeleteResult(ServletOutputStream outputStream, String userName, String action) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    JSONWriter json = new JSONWriter(w);
    json.object();
    json.key("user").value(userName);
    json.key("action").value(action);
    json.key("success").value("true");
    json.endObject();
    w.flush();
    w.close();
  }

  synchronized public void changeUserPassword(ServletOutputStream outputStream, String userName, String password, UserInfo adminUserInfo) throws IOException
  {
    try
    {
      logger.info("Change password {}", userName);
      UserInfo userInfo = users.get(userName);
      if(userInfo == null)
      {
        writeUserErrorResult(outputStream, userName, "user-password", "User not defined");
        logger.error("Error changing password - FI user does not exist");

      }
      else if(!adminUserInfo.getAccelerator().equals("") && !adminUserInfo.getAccelerator().equals(userInfo.getAccelerator()))
      {
        writeUserErrorResult(outputStream, userName, "user-password", "User does not exist");
        logger.error("Administrator {} ({}) has no privileges to manage users for accelerator {}", adminUserInfo.getName(), adminUserInfo.getAccelerator(), userInfo.getAccelerator());
      }
      else
      {

        ArrayList list = MBeanServerFactory.findMBeanServer(null);
        MBeanServer mbeanServer = (MBeanServer) list.get(0);
        ObjectName onUserDatabase = new ObjectName("Users:type=UserDatabase,database=UserDatabase");

        String userIDString = (String) mbeanServer.invoke(onUserDatabase, "findUser", new String[]{userName}, new String[]{String.class.getName()});
        if(userIDString == null)
        {
          writeUserErrorResult(outputStream, userName, "user-password", "User not defined");
          logger.error("Error changing password - Tomcat user does not exist");
        }
        else
        {
          logger.info("Change password for {}", userIDString);
          ObjectName onUser = new ObjectName(userIDString);
          MBeanInfo info = mbeanServer.getMBeanInfo(onUser);
          //logMbeanInfo(info);
          mbeanServer.setAttribute(onUser, new Attribute("password", password));

          mbeanServer.invoke(onUserDatabase, "save", new Object[0], new String[0]);
          logger.info("Tomcat user password changed: {}", userName);
          if(!userInfo.isFirstLogin())
          {
            userInfo.setFirstLogin(true);
            saveUsersDef();
          }
          userInfo.getProfile(outputStream, "user-password");
        }
      }
    }
    catch (InvalidAttributeValueException|AttributeNotFoundException|IntrospectionException|MalformedObjectNameException|ReflectionException|InstanceNotFoundException|MBeanException e)
    {
      writeUserErrorResult(outputStream, userName, "user-password", e.getMessage());
      logger.error("Error adding user", e);
    }

  }

  synchronized public void changeMyPassword(ServletOutputStream outputStream, UserInfo userInfo, String oldPassword, String newPassword) throws IOException
  {
    try
    {
      logger.info("Change password {}", userInfo.getName());
      if(userInfo == null)
      {
        writeUserErrorResult(outputStream, userInfo.getName(), "user-my-password", "User not defined");
        logger.error("Error changing password - FI user does not exist");
      }
      else
      {
        ArrayList list = MBeanServerFactory.findMBeanServer(null);
        MBeanServer mbeanServer = (MBeanServer) list.get(0);
        ObjectName onUserDatabase = new ObjectName("Users:type=UserDatabase,database=UserDatabase");

        String userIDString = (String) mbeanServer.invoke(onUserDatabase, "findUser", new String[]{userInfo.getName()}, new String[]{String.class.getName()});
        if(userIDString == null)
        {
          writeUserErrorResult(outputStream, userInfo.getName(), "user-my-password", "User not defined");
          logger.error("Error changing password - Tomcat user does not exist");
        }
        else
        {
          logger.info("Change password for {}", userIDString);
          ObjectName onUser = new ObjectName(userIDString);
          MBeanInfo info = mbeanServer.getMBeanInfo(onUser);
          //logMbeanInfo(info);
          String currentPassword = (String)mbeanServer.getAttribute(onUser, "password");
          logger.debug("old: {}");
          if(!oldPassword.equals(currentPassword))
          {
            writeUserErrorResult(outputStream, userInfo.getName(), "user-my-password", "Please enter old password");
            logger.error("Error changing password - old password does not match.");
          }
          else
          {
            mbeanServer.setAttribute(onUser, new Attribute("password", newPassword ));
            mbeanServer.invoke(onUserDatabase, "save", new Object[0], new String[0]);
            logger.info("Tomcat user password changed: {}", userIDString);
            if(userInfo.isFirstLogin())
            {
              userInfo.setFirstLogin(false);
              saveUsersDef();
            }
            userInfo.getProfile(outputStream, "user-my-password");
            logger.info("FI-IMPACT user password changedset: {}", userInfo.getName());
          }
        }
      }
    }
    catch (InvalidAttributeValueException|AttributeNotFoundException|IntrospectionException|MalformedObjectNameException|ReflectionException|InstanceNotFoundException|MBeanException e)
    {
      writeUserErrorResult(outputStream, userInfo.getName(), "user-my-password", e.getMessage());
      logger.error("Error adding user", e);
    }

  }

  synchronized public void setUserAccelerator(ServletOutputStream outputStream, String userName, String accelerator, UserInfo adminUserInfo) throws IOException
  {
    logger.info("set accelerator{}/{}", userName, accelerator);
    UserInfo userInfo = users.get(userName);
    if(userInfo == null)
    {
      writeUserErrorResult(outputStream, userInfo.getName(), "user-accelerator", "User not defined");
      logger.error("Error getting user - FI user does not exist");

    }
    else if(!adminUserInfo.getAccelerator().equals("") && !adminUserInfo.getAccelerator().equals(userInfo.getAccelerator()))
    {
      writeUserErrorResult(outputStream, userName, "user-accelerator", "User does not exist");
      logger.error("Administrator {} ({}) has no privileges to manage users for accelerator {}", adminUserInfo.getName(), adminUserInfo.getAccelerator(), userInfo.getAccelerator());
    }
    else
    {
      userInfo.setAccelerator(accelerator);
      saveUsersDef();
      userInfo.getProfile(outputStream, "user-accelerator");
      logger.info("FI-IMPACT user accelerator set: {}/{}", userName, accelerator);
    }
  }

  synchronized public void replaceUserRoles(ServletOutputStream outputStream, String userName, String[] roles, UserInfo adminUserInfo) throws IOException
  {
    //user-roles
    logger.info("set roles for {}", userName);
    UserInfo userInfo = users.get(userName);
    if(userInfo == null)
    {
      writeUserErrorResult(outputStream, userInfo.getName(), "user-roles", "User not defined");
      logger.error("Error getting user - FI user does not exist");
    }
    else if(!adminUserInfo.getAccelerator().equals("") && !adminUserInfo.getAccelerator().equals(userInfo.getAccelerator()))
    {
      writeUserErrorResult(outputStream, userName, "user-roles", "User does not exist");
      logger.error("Administrator {} ({}) has no privileges to manage users for accelerator {}", adminUserInfo.getName(), adminUserInfo.getAccelerator(), userInfo.getAccelerator());
    }
    else
    {
      boolean bCanSetRoles = true;
      for(String newRole: roles)
      {
        bCanSetRoles = adminUserInfo.getAccessRights().contains(newRole);
        if(!bCanSetRoles)
          break;
      }
      if(!bCanSetRoles)
      {
        writeUserErrorResult(outputStream, userName, "user-roles", "Cant set superset of own roles");
        logger.error("Administrator {} ({}) has no privileges to manage roles for {}", adminUserInfo.getName(), adminUserInfo.getAccelerator(), userInfo.getName());
      }
      else
      {
        userInfo.setAccessRights(roles);
        saveUsersDef();
        logger.info("FI-IMPACT user roles set: {}/{}", userName, userInfo.getAccessRights().toString());
        userInfo.getProfile(outputStream, "user-roles");
      }
    }
  }

  synchronized public void getRoles(ServletOutputStream outputStream, UserInfo adminUserInfo) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    JSONWriter json = new JSONWriter(w);
    json.object();
    for(Map.Entry<String, String> eRoles: roles.entrySet())
    {
      if(adminUserInfo.getAccessRights().contains(eRoles.getKey()))
        json.key(eRoles.getKey()).value(eRoles.getValue());
    }
    json.endObject();
    w.flush();
    w.close();
  }

  public void getUsersList(ServletOutputStream outputStream, UserInfo adminUserInfo) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    logger.info("Total {} users", users.size());
    JSONWriter json = new JSONWriter(w);
    json.object();
    json.key("users").array();
    int cnt = 0;
    for (UserInfo userInfo: users.values())
    {

      if(!userInfo.isDeniedUser() && (adminUserInfo.getAccelerator().equals("") || adminUserInfo.getAccelerator().equals(userInfo.getAccelerator())))
      {
        cnt++;
        json.object();
        json.key("user").value(userInfo.getName());
        json.key("description").value(userInfo.getDescription());
        json.key("accelerator").value(userInfo.getAccelerator());
        json.endObject();
      }
    }
    json.endArray();
    json.endObject();
    w.flush();
    w.close();
    logger.info("Returned {} users", cnt);

  }

  public void getUserProfile(ServletOutputStream outputStream, String userName, UserInfo adminUserInfo) throws IOException
  {
    UserInfo userInfo = getUserInfo(userName);
    if(adminUserInfo.getAccelerator().equals("") || adminUserInfo.getAccelerator().equals(userInfo.getAccelerator()))
      userInfo.getProfile(outputStream, "user-get");
    else
      writeUserErrorResult(outputStream, userName, "user-get", "User does not exist");

    logger.error("Administrator {} ({}) has no privileges to add users for accelerator {}", adminUserInfo.getName(), adminUserInfo.getAccelerator(), userInfo.getAccelerator());
  }
}
