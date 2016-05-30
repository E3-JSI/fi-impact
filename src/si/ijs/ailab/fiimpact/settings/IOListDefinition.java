package si.ijs.ailab.fiimpact.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by flavio on 25/05/2016.
 */
public class IOListDefinition
{
  private final String id;
  private final int startAtRow;
  private final ArrayList<IOListField> fields;
  private final Map<String, IOListField> fieldsByColumn;
  private final Map<String, IOListField> fieldsById;
  private IOListField usageID;
  private IOListField usageCleanUrl;

  IOListDefinition(String id, int _startAtRow)
  {
    this.id = id;
    startAtRow = _startAtRow;
    fields = new ArrayList<>();
    fieldsByColumn = new HashMap<>();
    fieldsById = new HashMap<>();
  }

  void addField(IOListField ioListField)
  {
    fields.add(ioListField);
    fieldsByColumn.put(ioListField.getColumn(), ioListField);
    fieldsById.put(ioListField.getFieldid(), ioListField);

    if(ioListField.getUsage() != null)
    {
      switch(ioListField.getUsage())
      {
        case "id":
          usageID = ioListField;
          break;
        case "clean-url":
          usageCleanUrl = ioListField;
          break;
      }
    }
  }

  public ArrayList<IOListField> getFields()
  {
    return fields;
  }

  public Map<String, IOListField> getFieldsByColumn()
  {
    return fieldsByColumn;
  }

  public Map<String, IOListField> getFieldsById()
  {
    return fieldsById;
  }

  public String toString()
  {
    StringBuilder str = new StringBuilder();
    for(IOListField ioListField : fields)
      str.append("\n").append(ioListField);
    return str.toString();
  }

  public String getId()
  {
    return id;
  }

  public IOListField getUsageID()
  {
    return usageID;
  }

  public IOListField getUsageCleanUrl()
  {
    return usageCleanUrl;
  }

  public int getStartAtRow()
  {
    return startAtRow;
  }
}
