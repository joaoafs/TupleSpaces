package pt.ulisboa.tecnico.tuplespaces.client.grpc;


import java.util.ArrayList;
import java.util.Iterator;

import com.google.protobuf.ProtocolStringList;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.PutResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Request;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Response;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase2Request;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase2Response;
import pt.ulisboa.tecnico.tuplespaces.client.ClientObserver;
import pt.ulisboa.tecnico.tuplespaces.client.ResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;

public class ClientService {
    private final ArrayList<ManagedChannel> channels;
    private final ArrayList<TupleSpacesReplicaGrpc.TupleSpacesReplicaStub> stubs;


    OrderedDelayer delayer;

    public ClientService(String[] servers) {
        channels = new ArrayList<>();
        stubs = new ArrayList<>();

        int i = 0;
        while (i < servers.length) {
            String serverAddress = servers[i];

            ManagedChannel channel = ManagedChannelBuilder.forTarget(serverAddress)
                    .usePlaintext()
                    .build();
            channels.add(channel);
            stubs.add(TupleSpacesReplicaGrpc.newStub(channel));

            i++;
        }

        delayer = new OrderedDelayer(stubs.size());
    }


    public void setPut(String tuple) throws InterruptedException {
        ResponseCollector c = new ResponseCollector();
        Iterator<Integer> iterator = delayer.iterator();
        while (iterator.hasNext()) {
            Integer i = iterator.next();
            stubs.get(i).put(
                    TupleSpacesReplicaXuLiskov.PutRequest.newBuilder().setNewTuple(tuple).build(),
                    new ClientObserver<PutResponse>(c)
            );
        }
        c.waitUntilAllReceived(stubs.size());
    }


    public String getRead(String tuple) throws InterruptedException {
        ResponseCollector c = new ResponseCollector();
        Iterator<Integer> iterator = delayer.iterator();
        while (iterator.hasNext()) {
            Integer i = iterator.next();
            stubs.get(i).read(
                    TupleSpacesReplicaXuLiskov.ReadRequest.newBuilder().setSearchPattern(tuple).build(),
                    new ClientObserver<ReadResponse>(c)
            );
        }
        c.waitUntilAllReceived(1);
        return c.getString();
    }


    public ArrayList<ProtocolStringList> takePhase1(String tuple, int clientId) throws InterruptedException{
        ResponseCollector c = new ResponseCollector();
        for (Integer id : delayer) {
            stubs.get(id).takePhase1(TupleSpacesReplicaXuLiskov.TakePhase1Request.newBuilder().setClientId(clientId).setSearchPattern(tuple).build(), new ClientObserver<TakePhase1Response>(c));

        }
        c.waitUntilAllReceived(stubs.size());
        return c.getLists();
    }

    public void takePhase1Release(int clientId) throws InterruptedException{
        ResponseCollector c = new ResponseCollector();
        for (Integer id : delayer) {
            stubs.get(id).takePhase1Release(TupleSpacesReplicaXuLiskov.TakePhase1ReleaseRequest.newBuilder().setClientId(clientId).build(), new ClientObserver<TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse>(c));
        }
        c.waitUntilAllReceived(stubs.size());
    }

    public void takePhase2(String tuple, int clientId) throws InterruptedException{
        ResponseCollector c = new ResponseCollector();
        for (Integer id : delayer) {
            stubs.get(id).takePhase2(TakePhase2Request.newBuilder().setTuple(tuple).setClientId(clientId).build(), new ClientObserver<TakePhase2Response>(c));
        }
        c.waitUntilAllReceived(stubs.size());
    }


    public String[] getTupleSpacesStateList(Integer id) throws InterruptedException{
        ResponseCollector c = new ResponseCollector();
        stubs.get(id).getTupleSpacesState(TupleSpacesReplicaXuLiskov.getTupleSpacesStateRequest.newBuilder().build(), new ClientObserver<getTupleSpacesStateResponse>(c));
        c.waitUntilAllReceived(1);
        //return c.getTuples().toArray(new String[0]);
        return null;
    }

    public void setDelay(int id, int delay) {
        delayer.setDelay(id, delay);
    }

    public void shutDownChannels(){
        for(ManagedChannel channel: channels){
            channel.shutdownNow();
        }

    }

}
