class ServerAlreadyRegistered(Exception):
    def __init__(self):
        super().__init__("Not possible to register the server")
