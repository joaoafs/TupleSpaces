package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;

import java.util.List;

public class ClientService {

    private TupleSpacesGrpc.TupleSpacesBlockingStub blockingStub;

    private final ManagedChannel channel;

    public ClientService(String host, String port) {

        final String target = host + ":" + port;

        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

        blockingStub = TupleSpacesGrpc.newBlockingStub(channel);
    }


    public void setPut(String tuple){


        TupleSpacesCentralized.PutRequest putRequest = TupleSpacesCentralized.PutRequest.newBuilder().setNewTuple(tuple).build();

        TupleSpacesCentralized.PutResponse putResponse = blockingStub.put(putRequest);

    }
    public String getRead(String tuple){

        TupleSpacesCentralized.ReadRequest readRequest = TupleSpacesCentralized.ReadRequest.newBuilder().setSearchPattern(tuple).build();

        TupleSpacesCentralized.ReadResponse readResponse = blockingStub.read(readRequest);

        return readResponse.getResult();
    }

    public String getTake(String tuple){

        TupleSpacesCentralized.TakeRequest takeRequest = TupleSpacesCentralized.TakeRequest.newBuilder().setSearchPattern(tuple).build();
        TupleSpacesCentralized.TakeResponse takeResponse = blockingStub.take(takeRequest);
        return takeResponse.getResult();

    }

    public List<String> getTupleStateList(){
        TupleSpacesCentralized.getTupleSpacesStateRequest getTupleSpacesStateRequest = TupleSpacesCentralized.getTupleSpacesStateRequest.newBuilder().build();
        TupleSpacesCentralized.getTupleSpacesStateResponse getTupleSpacesStateResponse = blockingStub.getTupleSpacesState(getTupleSpacesStateRequest);
        return getTupleSpacesStateResponse.getTupleList();
    }


    public void shutDown(){
        channel.shutdown();
    }
}