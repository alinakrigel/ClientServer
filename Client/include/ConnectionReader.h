
#ifndef CLIENT_CONNECTIONREADER_H
#define CLIENT_CONNECTIONREADER_H

#include "ConnectionHandler.h"


class ConnectionReader {
public:
    ConnectionReader(ConnectionHandler* handler, bool* lO, bool* t);

    void run();
    short bytesToShort(char* bytesArr);
private:
    ConnectionHandler* handler;
    bool* logOut; //indicates when user writes logout and haden't yet received ack
    bool* sholdTerminate;//indicates when user received ack to make logout

};

#endif //CLIENT_CONNECTIONREADER_H
