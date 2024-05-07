package pt.ulisboa.tecnico.tuplespaces.client;

import java.util.ArrayList;

import com.google.protobuf.ProtocolStringList;

public class ResponseCollector {
    ArrayList<String> collectedResponses;
    ProtocolStringList collectedTuples;

    public ResponseCollector() {
        collectedResponses = new ArrayList<String>();
    }

    synchronized public void addString(String s) {
        collectedResponses.add(s);
        notifyAll();
    }


    synchronized public String getString() {
        return new String(collectedResponses.get(0));
    }

    synchronized public void setStrings(ProtocolStringList strings){
        collectedTuples = strings;
        collectedResponses.add("");
        notifyAll();
    }


    synchronized public ProtocolStringList getTuples(){
        return collectedTuples;
    }

    synchronized public void waitUntilAllReceived(int n) throws InterruptedException {
        while (collectedResponses.size() < n){
            wait();
        }

    }
}