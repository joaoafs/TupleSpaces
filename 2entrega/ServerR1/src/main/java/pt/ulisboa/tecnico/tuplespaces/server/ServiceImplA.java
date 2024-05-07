package pt.ulisboa.tecnico.tuplespaces.server;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;
import static io.grpc.Status.INVALID_ARGUMENT;

import java.util.List;
import io.grpc.stub.StreamObserver;


public class ServiceImplA extends TupleSpacesGrpc.TupleSpacesImplBase {

    private ServerState serverState = new ServerState();


    public void put(TupleSpacesCentralized.PutRequest request, StreamObserver<TupleSpacesCentralized.PutResponse> responseObserver){

        String tuple = request.getNewTuple();

        serverState.put(tuple);

        TupleSpacesCentralized.PutResponse response = TupleSpacesCentralized.PutResponse.newBuilder().build();

        // Send a single response through the stream.
        responseObserver.onNext(response);
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();

    }

    @Override
    public void read(TupleSpacesCentralized.ReadRequest request, StreamObserver<TupleSpacesCentralized.ReadResponse> responseObserver){
        String tuple = request.getSearchPattern();
        String readTuple =serverState.read(tuple);

        if(readTuple == null){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Input has to be a existing tuple").asRuntimeException());
        }
        else {
            TupleSpacesCentralized.ReadResponse response = TupleSpacesCentralized.ReadResponse.newBuilder().setResult(readTuple).build();
            // Send a single response through the stream.
            responseObserver.onNext(response);
            // Notify the client that the operation has been completed.
            responseObserver.onCompleted();
        }
    }



    @Override
    public void take(TupleSpacesCentralized.TakeRequest request, StreamObserver<TupleSpacesCentralized.TakeResponse> responseObserver){

        String tuple = request.getSearchPattern();
        String takeTuple = serverState.take(tuple);

        if (takeTuple == null){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Input has to be an existing tuple").asRuntimeException());
        }
        else {

            TupleSpacesCentralized.TakeResponse response = TupleSpacesCentralized.TakeResponse.newBuilder().setResult(takeTuple).build();
            // Send a single response through the stream.
            responseObserver.onNext(response);
            // Notify the client that the operation has been completed.
            responseObserver.onCompleted();
        }
    }


    public void getTupleSpacesState(TupleSpacesCentralized.getTupleSpacesStateRequest request, StreamObserver<TupleSpacesCentralized.getTupleSpacesStateResponse> responseObserver){

        List<String> tupleStateList = serverState.getTupleSpacesState();

        if(tupleStateList.isEmpty()){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("There are no tuples").asRuntimeException());
        }
        else {

            TupleSpacesCentralized.getTupleSpacesStateResponse response = TupleSpacesCentralized.getTupleSpacesStateResponse.newBuilder().addAllTuple(tupleStateList).build();
            // Send a single response through the stream.
            responseObserver.onNext(response);
            // Notify the client that the operation has been completed.
            responseObserver.onCompleted();
        }

    }

}
