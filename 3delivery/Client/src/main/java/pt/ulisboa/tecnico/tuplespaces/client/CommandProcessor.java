package pt.ulisboa.tecnico.tuplespaces.client;

import com.google.protobuf.ProtocolStringList;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import io.grpc.StatusRuntimeException;
import java.util.List;
import pt.ulisboa.tecnico.tuplespaces.client.exception.ServerEntryNotFound;

public class CommandProcessor {

    private static final String SPACE = " ";
    private static final String BGN_TUPLE = "<";
    private static final String END_TUPLE = ">";
    private static final String PUT = "put";
    private static final String READ = "read";
    private static final String TAKE = "take";
    private static final String SLEEP = "sleep";
    private static final String SET_DELAY = "setdelay";
    private static final String EXIT = "exit";
    private static final String GET_TUPLE_SPACES_STATE = "getTupleSpacesState";
    private final ClientService clientService;


    private final int clientId;

    public CommandProcessor(ClientService clientService) {

        this.clientService = clientService;
        this.clientId = (int) (Math.random() * 100);
    }

    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String[] split = line.split(SPACE);
             switch (split[0]) {
                case PUT:
                    this.put(split);
                    break;

                case READ:
                    this.read(split);
                    break;

                case TAKE:
                    this.take(split);
                    break;

                case GET_TUPLE_SPACES_STATE:
                    this.getTupleSpacesState(split);
                    break;

                case SLEEP:
                    this.sleep(split);
                    break;

                case SET_DELAY:
                    this.setdelay(split);
                    break;

                case EXIT:
                    clientService.shutDownChannels();
                    break;

                default:
                    this.printUsage();
                    break;
             }
        }
    }

    private void put(String[] split){

        // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }

        System.out.println("OK\n");
        // calls function with tupple
        try{
            clientService.setPut(split[1]);
        }catch(StatusRuntimeException ignored){
        }catch(InterruptedException e){
            System.out.println("Caught exception with description: " +
                    e.getMessage()); // The same exception description provided in the server side
        }



    }

    private void read(String[] split){
        // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }


        System.out.println("OK");


        // read the tuple
        try {
            // take the tuple
            System.out.println(clientService.getRead(split[1]) + "\n");
        } catch (StatusRuntimeException ignored) {
        }catch(InterruptedException e){
            System.out.println("Caught exception with description: " +
                    e.getMessage()); // The same exception description provided in the server side
        }
    }

    /*

    private void take(String[] split) {
        // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }

        // get the tuple
//        String tuple = split[1];
        System.out.println("OK");
        // take the tuple
        try {
            // take the tuple
            System.out.println(clientService.getTake(split[1]) + "\n");
        } catch (StatusRuntimeException ignored) {
        }
    }
    */


    private void getTupleSpacesState(String[] split) {
        if (split.length != 2) {
            this.printUsage();
            return;
        }
        String qualifier = split[1];

        // get the tuple spaces state
        System.out.println("OK");

        try {
            List<String> tupleStateList = null;
            if (qualifier.equals("A")) {
                tupleStateList = Arrays.asList(clientService.getTupleSpacesStateList(0));
            } else if (qualifier.equals("B")) {
                tupleStateList = Arrays.asList(clientService.getTupleSpacesStateList(1));
            } else if (qualifier.equals("C")) {
                tupleStateList = Arrays.asList(clientService.getTupleSpacesStateList(2));
            } else {
                System.out.println("Invalid qualifier.");
                return;
            }

            if (tupleStateList != null) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < tupleStateList.size(); i++) {
                    sb.append(tupleStateList.get(i));
                    if (i < tupleStateList.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]\n");
                System.out.println(sb.toString());
            } else {
                System.out.println("[]\n");
            }
        } catch (StatusRuntimeException e) {
            System.out.println("[]\n");
        } catch (InterruptedException e) {
            System.out.println("Caught exception with description: " +
                    e.getMessage());
        }
    }



    private void sleep(String[] split) {
      if (split.length != 2){
        this.printUsage();
        return;
      }
      Integer time;

      // checks if input String can be parsed as an Integer
      try {
         time = Integer.parseInt(split[1]);
      } catch (NumberFormatException e) {
        this.printUsage();
        return;
      }

      try {
        Thread.sleep(time*1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    private void setdelay(String[] split) {
      if (split.length != 3){
        this.printUsage();
        return;
      }
      String qualifier = split[1];
      Integer time;

      // checks if input String can be parsed as an Integer
      try {
        time = Integer.parseInt(split[2]);
      } catch (NumberFormatException e) {
        this.printUsage();
        return;
      }

      // register delay <time> for when calling server <qualifier>
      System.out.println("TODO: implement setdelay command (only needed in phases 2+3)");
    }

    private void printUsage() {
        System.out.println("Usage:\n" +
                "- put <element[,more_elements]>\n" +
                "- read <element[,more_elements]>\n" +
                "- take <element[,more_elements]>\n" +
                "- getTupleSpacesState <server>\n" +
                "- sleep <integer>\n" +
                "- setdelay <server> <integer>\n" +
                "- exit\n");
    }

    private boolean inputIsValid(String[] input){
        if (input.length < 2 
            ||
            !input[1].substring(0,1).equals(BGN_TUPLE) 
            || 
            !input[1].endsWith(END_TUPLE)
            || 
            input.length > 2
            ) {
            this.printUsage();
            return false;
        }
        else {
            return true;
        }
    }
    private String getIntersection(ArrayList<ProtocolStringList> listOfReservations) {
        if (listOfReservations == null || listOfReservations.size() <= 1) {
            return null;
        }

        for (String string : listOfReservations.get(0)) {
            boolean foundInAll = true;
            for (int i = 1; i < listOfReservations.size(); i++) {

                if (!listOfReservations.get(i).contains(string)) {
                    foundInAll = false;
                    break;
                }
            }
            if (foundInAll) {
                return string;
            }
        }

        return null;
    }


    private void take(String[] split) {
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }
        String tuple = split[1];
        try {
            String toTake = null;
            do {
                ArrayList<ProtocolStringList> listOfReservations = clientService.takePhase1(tuple, clientId);
                toTake = getIntersection(listOfReservations);
                if (toTake == null) {
                    clientService.takePhase1Release(clientId);
                }
            } while (toTake == null);

            clientService.takePhase2(toTake, clientId);

            System.out.println("OK");
            System.out.println(toTake);
            System.out.println("");
        } catch (InterruptedException e) {

            System.out.println("Caught exception with description: " + e.getMessage());
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }
    }



    public ClientService getClientService() {
        return clientService;
    }
}
