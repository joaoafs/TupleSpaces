import sys

sys.path.insert(1, '../Contract/target/generated-sources/protobuf/python')
import grpc
from concurrent import futures

import re

import NameServer_pb2 as pb
import NameServer_pb2_grpc as pb2_grpc

ERR = 1
NO_ERR = 0
PORT = 5001


class ServerEntry:
    def __init__(self, address, qualifier):
        self.address = address
        self.qualifier = qualifier

    def getAddress(self):
        return self.address

    def getQualifier(self):
        return self.qualifier

    def __eq__(self, other):
        return isinstance(other, ServerEntry) and self.address == other.address

    def __hash__(self):
        return hash((self.address, self.qualifier))


class ServiceEntry:
    def __init__(self, name):
        self.name = name
        self.ServerEntries = []  # Usar consistentemente ServerEntries

    def addServerEntry(self, serverEntry):
        if serverEntry in self.ServerEntries:
            return ERR
        else:
            self.ServerEntries.append(serverEntry)  # Corrigido para usar o nome correto
            return NO_ERR

    def getName(self):
        return self.name

    def getServerEntries(self):  # Método renomeado para getServerEntries
        return self.ServerEntries

    def appendServerEntries(self, other):
        entries = set(self.ServerEntries + other.getServerEntries())
        self.ServerEntries = list(entries)

    def __eq__(self, other):
        if isinstance(other, ServiceEntry):
            return self.name == other.name
        return False

    def __hash__(self):
        return hash(self.name)


class NamingServer:
    def __init__(self):
        self.map = {}

    def addServiceEntry(self, serverEntry, name):
        if serverEntry.getQualifier() not in self.map:
            serviceEntry = ServiceEntry(name)
            serviceEntry.addServerEntry(serverEntry)
            self.map[name] = serviceEntry
            return NO_ERR
        else:
            return ERR

    def removeServerEntry(self, server_qualifier):
        for service_entry in self.map.values():
            server_entries = service_entry.getServerEntries()
            for server_entry in server_entries:
                if server_entry.getQualifier() == server_qualifier:
                    server_entries.remove(server_entry)
                    return NO_ERR
        return ERR

    def getServiceEntry(self, name):
        return self.map.get(name, None)

    def getMap(self):
        return self.map


class NamingServerServiceImpl(pb2_grpc.NamingServerServiceServicer):
    def __init__(self, *args, **kwargs):
        self.naming_server = NamingServer()

    def register(self, request, context):

        print("Register")
        service_name = request.name
        server_target = request.target
        server_qualifier = request.qualifier

        serverEntry = ServerEntry(server_target, server_qualifier)

        pattern = r'^[^:]+:[0-9]{4}$'
        if re.match(pattern, server_target) is None:
            print("erro 1")
            return pb.RegisterResponse(result="Not possible to register the server")

        if self.naming_server.addServiceEntry(serverEntry, service_name) == ERR:
            print("erro 2")
            return pb.RegisterResponse(result="Not possible to register the server")
        print("sucesso")
        return pb.RegisterResponse(success="")

    def lookup(self, request, context):
        print("Lookup")
        service_name = request.name
        server_qualifier = request.qualifier
        serverList = []

        # Verifica se o serviço está registado
        serviceEntry = self.naming_server.getServiceEntry(service_name)
        if serviceEntry is None:
            context.set_code(grpc.StatusCode.NOT_FOUND)
            context.set_details("Service with specified name not found.")
            return pb.LookupResponse(servers=serverList)

        print(server_qualifier)

        for server in serviceEntry.getServerEntries():
            if server.getQualifier() == server_qualifier:
                serverList.append(server.getAddress())
            else:
                serverList.append(server.getAddress())
        return pb.LookupResponse(servers=serverList)

    def delete(self, request, context):
        server_qualifier = request.qualifier

        # Verifica se o serviço está registrado
        # Remove o servidor com o qualificador especificado
        if self.naming_server.removeServerEntry(server_qualifier) == NO_ERR:
            return pb.DeleteResponse(success="")
        else:
            context.set_code(grpc.StatusCode.NOT_FOUND)
            context.set_details("Server with specified qualifier not found.")
            return pb.DeleteResponse(success="Error")


if __name__ == '__main__':
    try:
        # check number of arguments
        if len(sys.argv) > 1:
            print("Too many arguments!")
            print("Usage: python server.py")
            exit(1)
        server = grpc.server(futures.ThreadPoolExecutor(max_workers=3))
        pb2_grpc.add_NamingServerServiceServicer_to_server(NamingServerServiceImpl(), server)
        server.add_insecure_port('[::]:' + str(PORT))
        server.start()
        print("Server listening on port " + str(PORT))
        print("Press CTRL+C to terminate")
        server.wait_for_termination()

    except KeyboardInterrupt:
        print("Name Server stopped.")
        exit(0)
