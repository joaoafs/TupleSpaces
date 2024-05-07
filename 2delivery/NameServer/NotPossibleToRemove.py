class NotPossibleToRemove(Exception):
    def __init__(self):
        super().__init__("Not possible to remove the server")
