package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;

public class ServerState {

  private List<String> tuples;
  private String qualifier;

  public ServerState() {
    this.tuples = new ArrayList<String>();
  }

  public void setQualifier(String serverQualifier) {
    qualifier = serverQualifier;
  }

  public synchronized void put(String tuple) {
    if (!tuples.contains(tuple)) {
      tuples.add(tuple);
      notifyAll(); // Notify all waiting threads that new tuple is added
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
    while (true) {
      String matchingTuple = getMatchingTuple(pattern);
      if (matchingTuple != null) {
        return matchingTuple;
      } else {
        try {
          wait(); // Wait until new tuple is added
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  public synchronized String take(String pattern) {
    while (true) {
      String matchingTuple = getMatchingTuple(pattern);
      if (matchingTuple != null) {
        tuples.remove(matchingTuple);
        return matchingTuple;
      } else {
        try {
          wait(); // Wait until new tuple is added
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  public synchronized List<String> getTupleSpacesState() {
    while (tuples.isEmpty()) {
      try {
        wait(); // Wait until tuple space is not empty
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    return new ArrayList<>(this.tuples);
  }
}
