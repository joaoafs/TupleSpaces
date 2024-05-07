package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.*;


class Tuple {
  private final Lock lock = new ReentrantLock();
  private final Condition condition = lock.newCondition();
  private final String data;
  private int clientId;
  private boolean flagged;

  Tuple(String data) {
    this.data = data;
    flagged = false;
  }


  public void awaitCondition() throws InterruptedException {
    lock.lock();
    try {
      condition.await();
    } finally {
      lock.unlock();
    }
  }

  public void signalCondition() {
    lock.lock();
    try {
      condition.signal();
    } finally {
      lock.unlock();
    }
  }

  public boolean compareTuple(String data) {
    return Objects.equals(this.data, data);
  }

  public void flag() {
    flagged = true;
  }

  public void unflag() {
    flagged = false;
  }

  public String getData() {
    return data;
  }

  public int getClientId() {
    return clientId;
  }

  public void setClientId(int clientId) {
    this.clientId = clientId;
  }

  public boolean isFlagged() {
    return flagged;
  }
}

public class ServerState {
  private final List<Tuple> tuples;
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final Lock readLock = lock.readLock();
  private final Lock writeLock = lock.writeLock();
  private final List<Tuple> waiting;

  private boolean isValid(String data) {
    return data.length() > 2;
  }

  public ServerState() {
    this.tuples = new ArrayList<>();
    waiting = new ArrayList<>();
  }

  public synchronized void put(String data) {
    int counter = 0;
    for(Tuple tuple: tuples) {
      if (!(Objects.equals(tuple.getData(), data))) {
       counter ++;
      }
    }
    if (counter == 0){
      Tuple newTuple = new Tuple(data);
      tuples.add(newTuple);
      notifyAll(); // Notify all waiting threads that new tuple is added
    }
  }

  private synchronized String getMatchingTuplePattern(String pattern) {
    for (Tuple tuple : this.tuples) {
      if (tuple.getData().matches(pattern)) {
        return tuple.getData();
      }
    }
    return null;
  }
  private synchronized Tuple getMatchingTuple(String pattern) {
    for (Tuple tuple : this.tuples) {
      if (tuple.getData().matches(pattern)) {
        return tuple;
      }
    }
    return null;
  }

  private ArrayList<Tuple> getMatchingTuples(String pattern) {
    readLock.lock();
    ArrayList<Tuple> list = new ArrayList<>();
    for (Tuple tuple : this.tuples) {
      if (!tuple.isFlagged() && tuple.getData().matches(pattern)) {
        tuple.flag();
        list.add(tuple);
      }
    }
    readLock.unlock();
    return list;
  }

  public synchronized String read(String pattern) {
    while (true) {
      String matchingTuple = getMatchingTuplePattern(pattern);
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



  public ArrayList<String> take(String pattern, int clientId) {
    writeLock.lock();
    List<Tuple> matchingTuples = getMatchingTuples(pattern);
    if (matchingTuples.isEmpty()) {
      Tuple reservation = new Tuple(pattern);
      waiting.add(reservation);
      while (matchingTuples.isEmpty()) {
        writeLock.unlock();
        try {
          reservation.awaitCondition();
          matchingTuples = getMatchingTuples(pattern);
          writeLock.lock();
        } catch (InterruptedException e) {
          System.err.println("Error at Server State: " + e.getMessage());
        }
      }
    }
    writeLock.unlock();
    ArrayList<String> tuplesData = new ArrayList<>();
    for (Tuple tuple : matchingTuples) {
      tuple.setClientId(clientId);
      tuplesData.add(tuple.getData());
    }
    return tuplesData;
  }


  public void denyTake(int clientId) {
    writeLock.lock();
    try {
      for (Tuple tuple : tuples) {
        if (tuple.getClientId() == clientId) {
          tuple.unflag();
        }
      }
    } finally {
      writeLock.unlock();
    }
  }

  public String acceptTake(String data, int clientId) {
    writeLock.lock();
    try {
      Iterator<Tuple> iterator = tuples.iterator();
      while (iterator.hasNext()) {
        Tuple tuple = iterator.next();
        if (tuple.getClientId() == clientId && Objects.equals(tuple.getData(), data)) {
          iterator.remove();
        }
      }
    } finally {
      writeLock.unlock();
    }
    return data;
  }

  public List<String> getTupleSpacesState() {
    readLock.lock();
    List<String> toReturn = new ArrayList<>();
    try {
      for (Tuple tuple : tuples) {
        toReturn.add(tuple.getData());
      }
    } finally {
      readLock.unlock();
    }
    return toReturn;
  }
}