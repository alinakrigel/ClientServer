
#ifndef CLIENT_KEYBOARDREADER_H
#define CLIENT_KEYBOARDREADER_H
#include "ConnectionHandler.h"
using namespace std;

class KeyBoardReader {

public:
    KeyBoardReader(ConnectionHandler* handler, bool* lO, bool* t);

    void run();
    void shortToBytes(short num, char* bytesArr);
private:
    ConnectionHandler* handler;
    bool* logOut;
    bool* shouldTerminate;

};

#endif //CLIENT_KEYBOARDREADER_H
