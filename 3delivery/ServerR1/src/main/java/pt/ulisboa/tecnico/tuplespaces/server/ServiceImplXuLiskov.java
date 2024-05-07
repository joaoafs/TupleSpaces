package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.BindableService;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Request;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Response;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1ReleaseRequest;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase2Request;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase2Response;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;

import java.util.ArrayList;
import java.util.List;

import static io.grpc.Status.INVALID_ARGUMENT;

public class ServiceImplXuLiskov extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase {
    private ServerState serverState = new ServerState();


    public void put(TupleSpacesReplicaXuLiskov.PutRequest request, StreamObserver<TupleSpacesReplicaXuLiskov.PutResponse> responseObserver){

        String tuple = request.getNewTuple();

        serverState.put(tuple);

        TupleSpacesReplicaXuLiskov.PutResponse response = TupleSpacesReplicaXuLiskov.PutResponse.newBuilder().build();

        // Send a single response through the stream.
        responseObserver.onNext(response);
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();

    }


    public void read(TupleSpacesReplicaXuLiskov.ReadRequest request, StreamObserver<TupleSpacesReplicaXuLiskov.ReadResponse> responseObserver){
        String tuple = request.getSearchPattern();
        String readTuple =serverState.read(tuple);

        if(readTuple == null){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Input has to be a existing tuple").asRuntimeException());
        }
        else {
            TupleSpacesReplicaXuLiskov.ReadResponse response = TupleSpacesReplicaXuLiskov.ReadResponse.newBuilder().setResult(readTuple).build();
            // Send a single response through the stream.
            responseObserver.onNext(response);
            // Notify the client that the operation has been completed.
            responseObserver.onCompleted();
        }
    }



    public void getTupleSpacesState(TupleSpacesReplicaXuLiskov.getTupleSpacesStateRequest request, StreamObserver<TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse> responseObserver){

        List<String> tupleStateList = serverState.getTupleSpacesState();

        if(tupleStateList.isEmpty()){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("There are no tuples").asRuntimeException());
        }
        else {

            TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse response = TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse.newBuilder().addAllTuple(tupleStateList).build();
            // Send a single response through the stream.
            responseObserver.onNext(response);
            // Notify the client that the operation has been completed.
            responseObserver.onCompleted();
        }

    }

    public void takePhase1(TakePhase1Request request, StreamObserver<TakePhase1Response> responseObserver){
        // StreamObserver is used to represent the gRPC stream between the server and
        // client in order to send the appropriate responses (or errors, if any occur).
        ArrayList<String> tuples = serverState.take(request.getSearchPattern(), request.getClientId());
        if(tuples.isEmpty()){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("There are no Elements").asRuntimeException());
        }
        else{
            TakePhase1Response response = TakePhase1Response.newBuilder().addAllReservedTuples(tuples).build();

            // Send a single response through the stream.
            responseObserver.onNext(response);
            // Notify the client that the operation has been completed.
            responseObserver.onCompleted();}
    }


    public void takePhase1Release(TakePhase1ReleaseRequest request, StreamObserver<TakePhase1ReleaseResponse> responseObserver){
        // StreamObserver is used to represent the gRPC stream between the server and
        // client in order to send the appropriate responses (or errors, if any occur).
        serverState.denyTake(request.getClientId());

        TakePhase1ReleaseResponse response = TakePhase1ReleaseResponse.getDefaultInstance();

        // Send a single response through the stream.
        responseObserver.onNext(response);
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();
    }


    public void takePhase2(TakePhase2Request request, StreamObserver<TakePhase2Response> responseObserver){
        // StreamObserver is used to represent the gRPC stream between the server and
        // client in order to send the appropriate responses (or errors, if any occur).
        String Return = serverState.acceptTake(request.getTuple(), request.getClientId());
        if(Return == null){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("There are no Elements").asRuntimeException());
        }
        else{
            TakePhase2Response response = TakePhase2Response.getDefaultInstance();

            // Send a single response through the stream.
            responseObserver.onNext(response);
            // Notify the client that the operation has been completed.
            responseObserver.onCompleted();}
    }

}
