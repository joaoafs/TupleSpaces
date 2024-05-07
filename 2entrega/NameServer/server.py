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
        self.ServerEntries = []

    def addServerEntry(self, serverEntry):
        if serverEntry in self.ServerEntries:
            return ERR
        else:
            self.ServerEntrys.append(serverEntry)
            return NO_ERR

    def getName(self):
        return self.name

    def getServerEntrys(self):
        return self.ServerEntrys

    def appendServerEntrys(self, other):
        entries = set(self.ServerEntries + other.getServerEntrys())
        self.ServerEntrys = list(entries)

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
            server_entries = service_entry.getServerEntrys()
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

        service_name = request.name
        server_target = request.target
        server_qualifier = request.qualifier

        pattern = r'^[^:]+:[0-9]{4}$'
        if re.match(pattern, server_target) is None:
            return pb.RegisterResponse(result = "Not possible to register the server")

        serverEntry = ServerEntry(server_target,server_qualifier)

        if self.naming_server.addServiceEntry(serverEntry, service_name) == ERR:
            return pb.RegisterResponse(result = "Not possible to register the server")
        return pb.RegisterResponse(success = True)

    def lookup(self, request, context):

        service_name = request.name
        server_qualifier = request.qualifier
        serverList = []

        # Verifica se o serviço está registado
        for server in self.naming_server.getServiceEntry(service_name).getServerEntrys():
            if server.getQualifier() == server_qualifier:
                serverList.append(server.getAddress())
        return pb.LookupResponse(servers = serverList)

    def delete(self, request, context):
        server_qualifier = request.qualifier

        # Verifica se o serviço está registrado
        # Remove o servidor com o qualificador especificado
        if self.naming_server.removeServerEntry(server_qualifier) == NO_ERR:
            return pb.DeleteResponse(success=True)
        else:
            context.set_code(grpc.StatusCode.NOT_FOUND)
            context.set_details("Server with specified qualifier not found.")
            return pb.DeleteResponse(success=False)



# define the port
PORT = 5001



if __name__ == '__main__':
    try:
        # print received arguments>
        print("Received arguments:")
        for i in range(1, len(sys.argv)):
            print("  " + sys.argv[i])

        # check number of arguments
        if len(sys.argv) > 1:
            print("Too many arguments!")
            print("Usage: python server.py")
            exit(1)

        server = grpc.server(futures.ThreadPoolExecutor(max_workers=3))

        pb2_grpc.add_NamingServerServiceServicer_to_server(NamingServerServiceImpl(), server)
        server.add_insecure_port('[::]:'+str(PORT))
        server.start()
        print("Server listening on port " + str(PORT))
        print("Press CTRL+C to terminate")
        server.wait_for_termination()

    except KeyboardInterrupt:
        print("Name Server stopped.")
        exit(0)