
#include <ConnectionHandler.h>
#include <KeyBoardReader.h>
#include <ConnectionReader.h>
#include <thread>

using namespace std;

int main(int argc, char *argv[]) {

    string host = argv[1];
    short port = atoi(argv[2]);

    if (argc < 3) {
        std::cerr << "Usage:" << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }

    ConnectionHandler handler(host, port);
    if (!handler.connect()) {
        std::cerr << "cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    bool *logout = new bool;
    bool *shouldTerminate = new bool;

    // Creating 2 threads to run user
    KeyBoardReader keyBoardReader(&handler, shouldTerminate, logout);
    ConnectionReader connectionReader(&handler, shouldTerminate, logout);
    // This thread will manage receiving massages from server and printing result on screen
    thread thread2(&ConnectionReader::run, &connectionReader);
    // This thread will manage receiving massages from user through keyboard typing and sending message to server
    thread thread1(&KeyBoardReader::run, &keyBoardReader);
    thread2.join();
    thread1.join();

    delete shouldTerminate;
    delete logout;
}
