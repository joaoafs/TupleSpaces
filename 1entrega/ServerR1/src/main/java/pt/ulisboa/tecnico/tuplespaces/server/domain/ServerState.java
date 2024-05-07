package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;

public class ServerState {

  private List<String> tuples;

  public ServerState() {
    this.tuples = new ArrayList<String>();

  }

  public synchronized void put(String tuple) {
    if (!tuples.contains(tuple)){
      tuples.add(tuple);
    }
  }

  private synchronized String getMatchingTuple(String pattern) {
    for (String tuple : this.tuples) {
      if (tuple.matches(pattern)) {
        return tuple;
      }
    }
    return null;
  }

  public synchronized String read(String pattern) {
    return getMatchingTuple(pattern);
  }

  public synchronized String take(String pattern) {
    String matchingTuple = getMatchingTuple(pattern);
    if (matchingTuple != null) {
      tuples.remove(matchingTuple);
      return matchingTuple;
    }
    return null;
  }

  public synchronized List<String> getTupleSpacesState() {
    List<String> tupleSpaceState = new ArrayList<>(this.tuples);
    if (!tupleSpaceState.isEmpty()){
      return tupleSpaceState;
    }
    return null;
  }
}
