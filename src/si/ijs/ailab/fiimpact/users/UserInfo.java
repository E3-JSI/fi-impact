package si.ijs.ailab.fiimpact.users;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by flavio on 12/05/2016.
 */
public class UserInfo
{
  private String name;
  private String accelerator;
  Set<String> accessRights = new TreeSet<>();

  UserInfo(String _name, String _accelerator)
  {
    name = _name;
    accelerator = _accelerator;
    if(accelerator == null)
      accelerator = "";
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
}
