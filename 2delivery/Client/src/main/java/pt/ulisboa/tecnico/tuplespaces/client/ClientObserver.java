package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateResponse;
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
        else {
            collector.addString("");
        }
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
    }
}