package pt.ulisboa.tecnico.tuplespaces.client.grpc;


import java.util.ArrayList;
import java.util.Iterator;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.PutResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.client.ClientObserver;
import pt.ulisboa.tecnico.tuplespaces.client.ResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;

public class ClientService {
    private final ArrayList<ManagedChannel> channels;
    private final ArrayList<TupleSpacesGrpc.TupleSpacesStub> stubs;
    private TupleSpacesGrpc.TupleSpacesBlockingStub blockingStub;
    OrderedDelayer delayer;

    public ClientService(String[] servers) {
        channels = new ArrayList<>();
        stubs = new ArrayList<>();

        int i = 0;
        while (i < servers.length) {
            String serverAddress = servers[i];
            String[] parts = serverAddress.split(":");
            String host = parts[0];
            String port = parts[1];
            String target = host + port;

            ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                    .usePlaintext()
                    .build();
            channels.add(channel);
            stubs.add(TupleSpacesGrpc.newStub(channel));

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
                    TupleSpacesCentralized.PutRequest.newBuilder().setNewTuple(tuple).build(),
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
                    TupleSpacesCentralized.ReadRequest.newBuilder().setSearchPattern(tuple).build(),
                    new ClientObserver<ReadResponse>(c)
            );
        }
        c.waitUntilAllReceived(1);
        return c.getString();
    }


    public String getTake(String tuple) {

        TupleSpacesCentralized.TakeRequest takeRequest = TupleSpacesCentralized.TakeRequest.newBuilder().setSearchPattern(tuple).build();
        TupleSpacesCentralized.TakeResponse takeResponse = blockingStub.take(takeRequest);
        return takeResponse.getResult();

    }

    public String[] getTupleSpacesStateList(Integer id) throws InterruptedException{
        ResponseCollector c = new ResponseCollector();
        stubs.get(id).getTupleSpacesState(TupleSpacesCentralized.getTupleSpacesStateRequest.newBuilder().build(), new ClientObserver<getTupleSpacesStateResponse>(c));
        c.waitUntilAllReceived(1);
        return c.getTuples().toArray(new String[0]);
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
