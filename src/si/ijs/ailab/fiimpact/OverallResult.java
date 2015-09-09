package si.ijs.ailab.fiimpact;

import org.json.JSONArray;
import org.json.JSONObject;
import si.ijs.ailab.util.AIStructures;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by flavio on 08/09/2015.
 */
class OverallResult
{

  String id;
  int n;
  double sum;
  double average;
  //double min;
  //double max;

  public static class ScoreBoundaries
  {
    double min, lo_med, med_hi, max;
  }

  private class ResultGraph
  {
    String id;
    ScoreBoundaries boundaries;
    ArrayList<AIStructures.AIInteger> graphValues;

    ResultGraph(String _id, ScoreBoundaries _scoreBoundaries)
    {
      id = _id;
      boundaries = _scoreBoundaries;
      graphValues = new ArrayList<>();
      for (int i = 0; i < 5; i++)
      {
        AIStructures.AIInteger val = new AIStructures.AIInteger();
        val.val = 0;
        graphValues.add(val);
      }
    }


    private void add(ArrayList<SurveyData> surveys)
    {
      for (SurveyData sd : surveys)
        add(sd);
    }

    public void add(SurveyData surveyData)
    {
      int slot;
      Double score = surveyData.results.get(id);
      if (score == null)
        score = -1.0;

      slot = getSlot(score);
      AIStructures.AIInteger cnt = graphValues.get(slot);
      cnt.val++;

      surveyData.resultDerivatives.put(id + "_GRAPH_SLOT", (double) slot);
    }

    private int getSlot(double d)
    {
      int slot;
      if (d < boundaries.min)
        slot = 0;
      else if (d <= boundaries.lo_med)
        slot = 1;
      else if (d <= boundaries.med_hi)
        slot = 2;
      else if (d <= boundaries.max)
        slot = 3;
      else
        slot = 4;
      return slot;
    }


  }

  ArrayList<SurveyData> surveys;
  ResultGraph graph;

  public OverallResult(String _id, ScoreBoundaries scoreBoundaries)
  {
    id = _id;
    n = 0;
    average = 0.0;
    sum = 0.0;

    graph = new ResultGraph(id, scoreBoundaries);
    surveys = new ArrayList<>();
  }
  public double getSpeedometerPercent(double d)
  {
    if(d <= graph.boundaries.min)
      return 0.0;
    else if(d >= graph.boundaries.max)
      return 1.0;
    else
      return (d-graph.boundaries.min)/(graph.boundaries.max-graph.boundaries.min);
  }

  public double getSpeedometerPercentLM()
  {
    return getSpeedometerPercent(graph.boundaries.lo_med);
  }
  public double getSpeedometerPercentMH()
  {
    return getSpeedometerPercent(graph.boundaries.med_hi);
  }

  public void add(SurveyData sd)
  {

    Double r = sd.results.get(id);
    if (r != null)
      surveys.add(sd);
  }

  public void calculate()
  {

    Collections.sort(surveys, new SurveyManager.SurveyDataComparator(id));
    n = surveys.size();
    average = 0.0;
    sum = 0.0;

    int beforeYou = 0;
    int sameAsYou = 0;
    double beforeYouResult = -1.0;

    for (SurveyData sd : surveys)
    {
      Double r = sd.results.get(id);
      sum += r;

      //logger.debug("score: {}", sd.results.get(resultType));
      double percent = Math.round((((double) beforeYou) / n) * 100);
      sd.resultDerivatives.put(id + "_R", percent);
      Double yourResult = sd.results.get(id);
      if (yourResult == null)
        yourResult = 0.0;

      if (beforeYouResult == yourResult)
      {
        sameAsYou++;
      } else
      {
        beforeYou = beforeYou + sameAsYou + 1;
        sameAsYou = 0;
        beforeYouResult = yourResult;
      }

    }

    average = sum / (double) n;
    graph.add(surveys);
  }

  public JSONObject toJSON()
  {
    JSONObject jsonAverage = new JSONObject();
    jsonAverage.put("id", id);
    jsonAverage.put("average", SurveyManager.getDecimalFormatter4().format(average));
    jsonAverage.put("average_slot", graph.getSlot(average));
    JSONArray jsonHistogram = toJSONHistogram();
    jsonAverage.put("histogram", jsonHistogram);
    return jsonAverage;
  }

  public JSONArray toJSONHistogram()
  {
    JSONArray jsonHistogram = new JSONArray();
    for (AIStructures.AIInteger cnt : graph.graphValues)
      jsonHistogram.put(cnt.val);
    return jsonHistogram;

  }

  public int getAverageSlot()
  {
    return graph.getSlot(average);
  }
}
