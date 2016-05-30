package si.ijs.ailab.fiimpact.settings;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by flavio on 25/05/2016.
 */
public class IOListField
{
  String listId;
  private String column;
  private String label;
  private String fieldid;
  private String usage;
  private String missing;
  private String include_record_when;
  private String transform;
  private String type;
  String plot;
  String graph;
  private SortedMap<String, String> lookup = Collections.synchronizedSortedMap(new TreeMap<String, String>());

  IOListField(String listId, String column, String label, String fieldid, String usage, String missing,
              String include_record_when, String transform, String type, String plot, String graph)
  {
    this.listId = listId;
    this.column = column;
    this.label = label;
    this.fieldid = fieldid;
    this.usage = usage;
    this.missing = missing;
    this.include_record_when = include_record_when;
    this.transform = transform;
    if(type == null || type.equals(""))
      this.type = "text";
    else
      this.type = type;

    if(plot == null || plot.equals(""))
      this.plot = "ignore";
    else
      this.plot = plot;

    if(graph == null || graph.equals(""))
      this.graph = "ignore";
    else
      this.graph = graph;
  }

  public String getTransform()
  {
    return transform;
  }

  public String getColumn()
  {
    return column;
  }

  public String getFieldid()
  {
    return fieldid;
  }

  public String getInclude_record_when()
  {
    return include_record_when;
  }

  public String getLabel()
  {
    return label;
  }

  public String getMissing()
  {
    return missing;
  }

  public String getUsage()
  {
    return usage;
  }

  public boolean isTransformLog()
  {
    return getTransform() != null && getTransform().equals("log");
  }

  public String getType()
  {
    return type;
  }

  public SortedMap<String, String> getLookup()
  {
    return lookup;
  }

  public void addLookup(String key, String value)
  {
    lookup.put(key, value);
  }

  public String getPlot()
  {
    return plot;
  }

  public String getGraph()
  {
    return graph;
  }

  public String getListId()
  {
    return listId;
  }
}
