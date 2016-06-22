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

  //these are "merge" fields from fiModelNew
  //I put them in a seprate map to keep backward compatibility, and also fill in some defaults, ...
  private final Map<String, IOListField> calculatedFieldsById;

  private IOListField usageID;
  private IOListField usageCleanUrl;

  IOListDefinition(String id, int _startAtRow)
  {
    this.id = id;
    startAtRow = _startAtRow;
    fields = new ArrayList<>();
    fieldsByColumn = new HashMap<>();
    fieldsById = new HashMap<>();
    calculatedFieldsById = new HashMap<>();
  }

  void addField(IOListField ioListField)
  {
    fields.add(ioListField);
    if(!(ioListField.getColumn() == null || ioListField.getColumn().equals("")))
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

  void addCalculatedField(IOListField ioListField)
  {
    calculatedFieldsById.put(ioListField.getFieldid(), ioListField);
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

  public Map<String, IOListField> getCalculatedFieldsById()
  {
    return calculatedFieldsById;
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
