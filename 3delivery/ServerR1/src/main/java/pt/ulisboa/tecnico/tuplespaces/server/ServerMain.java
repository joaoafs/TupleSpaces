package pt.ulisboa.tecnico.tuplespaces.server;

import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.server.ServiceImplXuLiskov;
import pt.ulisboa.tecnico.sequencer.contract.*;
import pt.ulisboa.tecnico.nameserver.contract.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.nameserver.contract.NameServer;
import io.grpc.*;
import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) {
        System.out.println(ServerMain.class.getSimpleName());

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // check arguments
        if (args.length < 2) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s port%n", ServerMain.class.getName());
            return;
        }

        final int port = Integer.parseInt(args[1]);
        final String host = args[0];
        final String qualifier = args[2];
        final String service = args[3];
        final String address = host + ":" + port;
        final BindableService implXuLiskov = new ServiceImplXuLiskov();

        ManagedChannel channel = ManagedChannelBuilder.forTarget(host + ":" + "5001").usePlaintext().build();
        NamingServerServiceGrpc.NamingServerServiceBlockingStub stub = NamingServerServiceGrpc.newBlockingStub(channel);

        NameServer.RegisterResponse response = stub.register(NameServer.RegisterRequest.newBuilder().setName(service).setTarget(address).setQualifier(qualifier).build());

        //stub.register(NameServer.RegisterRequest.newBuilder().setName(service).setQualifier(qualifier).setTarget(address).build());

        // Create a new server to listen on port
        Server server = ServerBuilder.forPort(port).addService(implXuLiskov).build();


        // Start the server
        try{
            server.start();
            // Server threads are running in the background.
            System.out.println("Server started");
        }
        catch (IOException e){
            System.out.println("Caught Exception: " + e.getMessage());
        }

        // Do not exit the main thread. Wait until server is terminated.

        try{
            server.awaitTermination();
        }
        catch (InterruptedException e){
            System.out.println("Caught Exception: " + e.getMessage());
        }

        stub.delete(NameServer.DeleteRequest.newBuilder().setName(service).setTarget(address).build());

        channel.shutdown();
    }
}

