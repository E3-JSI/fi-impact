package si.ijs.ailab.fiimpact.users;

import org.json.JSONWriter;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by flavio on 12/05/2016.
 */
public class UserInfo
{
  private String name;
  private String description;
  private String accelerator;
  Set<String> accessRights = new TreeSet<>();
  private boolean isDeniedUser=false;

  UserInfo(String _name, String _accelerator)
  {
    name = _name;
    accelerator = _accelerator;
    if(accelerator == null)
      accelerator = "";
  }

  public void setDeniedUser(boolean deniedUser)
  {
    isDeniedUser = deniedUser;
  }

  public boolean isDeniedUser()
  {
    return isDeniedUser;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getDescription()
  {
    return description;
  }

  public String getName()
  {
    return name;
  }

  public String getAccelerator()
  {
    return accelerator;
  }

  public void addAccessRight(String s)
  {
    accessRights.add(s);
  }

  public Set<String> getAccessRights()
  {
    return accessRights;
  }

  public void getProfile(ServletOutputStream outputStream) throws IOException
  {
    OutputStreamWriter w = new OutputStreamWriter(outputStream, "utf-8");
    JSONWriter json = new JSONWriter(w);
    json.object().key("user").value(getName());
    json.key("action").value("get");
    if(isDeniedUser)
      json.key("success").value("false");
    else
      json.key("success").value("true");

    json.key("description").value(getDescription());
    json.key("accelerator").value(getAccelerator());
    json.key("access").array();
    for(String s: getAccessRights())
    {
      json.value(s);
    }
    json.endArray();
    json.endObject();
    w.flush();
    w.close();
  }

  public void setAccelerator(String _accelerator)
  {
    accelerator = _accelerator;
    if(accelerator == null)
      accelerator = "";

  }
  public void setAccessRights(String[] _rights)
  {
    accessRights.clear();
    if(_rights != null)
    {
      for(String s: _rights)
      {
        if(s!=null && !s.equals(""))
          accessRights.add(s);
      }
    }
  }

}
