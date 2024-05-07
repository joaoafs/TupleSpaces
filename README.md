# TupleSpaces - Distributed Systems Project 2024

Welcome to the GitHub repository for the TupleSpaces project by **Group T16**. This repository is part of our Distributed Systems course assignment for 2024. We chose the **"Bring 'em on!"** difficulty level.

## Project Description

TupleSpaces is a framework designed for building distributed systems where communication and data sharing between processes are done through a virtual shared space. This concept simplifies the design of distributed applications by allowing processes to interact through tuples, a form of immutable, ordered lists of elements.

Our project implements a robust system utilizing TupleSpaces to manage distributed interactions seamlessly. The system is split into various modules:

- **Servers** (`Server1`, `Server2`, `Server3`): Handle different stages of the tuple lifecycle.
- **Client**: Interface for interacting with the tuple space.
- **Contract**: Defines the messages and services used by clients and servers.
- **NamingServer**: Manages future naming services for the system.

For more details, visit our [Project Statement](https://github.com/tecnico-distsys/TupleSpaces).

## Getting Started

### Prerequisites

The Project is configured with Java 17 (which is only compatible with Maven >= 3.8), but if you want to use Java 11 you
can too -- just downgrade the version in the POMs.

To confirm that you have them installed and which versions they are, run in the terminal:

```s
javac -version
mvn -version
```

### Installation

To compile and install all modules:*(fill the table below with the team members, and then delete this line)*

```s
mvn clean install
```

## Built With

* [Maven](https://maven.apache.org/) - Build and dependency management tool;
* [gRPC](https://grpc.io/) - RPC framework.

### Team Members

| Number | Name          | GitHub                                    | Email                                          |
|--------|---------------|-------------------------------------------|------------------------------------------------|
| 98943  | João Amadeu   | [joaoafs](https://github.com/joaoafs)     | [Email](mailto:joaoamadeusantos@tecnico.ulisboa.pt) |
| 102477 | Diogo Cadete  | [diogojcadete](https://github.com/diogojcadete) | [Email](mailto:diogojcadete@tecnico.ulisboa.pt)     |
| 103845 | João Maia     | [Mr-Maia](https://github.com/Mr-Maia)     | [Email](mailto:joaomiguelmaia@tecnico.ulisboa.pt)   |

