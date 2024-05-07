package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.TakePhase1Response;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse;
import io.grpc.stub.StreamObserver;

public class ClientObserver<T> implements StreamObserver<T> {

    ResponseCollector collector;

    public ClientObserver (ResponseCollector c) {
        collector = c;
    }

    @Override
    public void onNext(T r) {
        if(r instanceof ReadResponse) {
            ReadResponse response = (ReadResponse) r;
            collector.addString(response.getResult());
        }
        if(r instanceof getTupleSpacesStateResponse){
            getTupleSpacesStateResponse response = (getTupleSpacesStateResponse) r;
            collector.setStrings(response.getTupleList());
        }
        if(r instanceof TakePhase1Response){
            TakePhase1Response response = (TakePhase1Response) r;
            collector.addList(response.getReservedTuplesList());
        }
        else {
            collector.addString("");
        }
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
        collector.stopWaiting();
    }

    @Override
    public void onCompleted() {
    }
}