package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.nameserver.contract.NameServer;
import pt.ulisboa.tecnico.nameserver.contract.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;

import static java.lang.System.exit;

public class ClientMain {
    public static void main(String[] args) {



        // get the host and the port
        final String host = args[0];
        final int port = Integer.parseInt(args[1]);

        final ManagedChannel channel;
        channel = ManagedChannelBuilder.forTarget("localhost:5001").usePlaintext().build();
        final NamingServerServiceGrpc.NamingServerServiceBlockingStub stub = NamingServerServiceGrpc.newBlockingStub(channel);

        NameServer.LookupResponse response = stub.lookup(NameServer.LookupRequest.newBuilder().setServiceName("TupleSpaces").build());
        String[] servers = response.getServersList().toArray(new String[0]);
        if(servers.length == 0) {
            channel.shutdown();
            exit(0);
        }

        CommandProcessor parser = new CommandProcessor(new ClientService(servers));
        parser.parseInput();
        channel.shutdown();
    }
}
