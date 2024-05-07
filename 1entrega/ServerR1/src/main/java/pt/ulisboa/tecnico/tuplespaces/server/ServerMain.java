package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s port%n", Server.class.getName());
            return;
        }
        // Para vermos os argumentos inseridos pelo utilizador (APAGAR)
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }
        int port = Integer.valueOf(args[0]);
        final BindableService implA = new ServiceImplA();


        try {
            // Create a new server to listen on port.
            Server server = ServerBuilder.forPort(port).addService(implA).build();
            // Start the server.
            server.start();
            // Server threads are running in the background.
            System.out.println("Server started");

            // Do not exit the main thread. Wait until server is terminated.
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}