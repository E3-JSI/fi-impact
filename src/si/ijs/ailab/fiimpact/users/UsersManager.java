package si.ijs.ailab.fiimpact.users;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class UsersManager
{

  private final Path webappRoot;
  private Map<String, UserInfo> users;

  private final static Logger logger = LogManager.getLogger(UsersManager.class.getName());
  private static UsersManager usersManager;
  private UserInfo deniedUserInfo;

  private UsersManager(Path _webappRoot)
  {
    webappRoot = _webappRoot;
    logger.info("Root: {}", _webappRoot);

    users = new HashMap<>();
    File usersDefFile = webappRoot.resolve("WEB-INF").resolve("user-roles.xml").toFile();
    loadUsersDef(usersDefFile);
    deniedUserInfo = new UserInfo("#ACCESS_DENIED#", null);
  }

  private void loadUsersDef(File usersDefFile)
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
        users.put(eElement.getAttribute("name"), ui);
        NodeList nlAccess = eElement.getElementsByTagName("access");
        for(int j=0; j < nlAccess.getLength(); j++)
          ui.accessRights.add(((Element)nlAccess.item(j)).getAttribute("id"));
      }
    }
    catch (SAXException | IOException | ParserConfigurationException e)
    {
      logger.error("Error loading list definition.", e);
    }

  }

  public UserInfo getUserInfo(String name)
  {
    UserInfo userInfo = users.get(name);
    if(userInfo == null)
      userInfo = deniedUserInfo;
    return userInfo;
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


}
