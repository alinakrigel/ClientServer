
#include <ConnectionReader.h>

using namespace std;

ConnectionReader::ConnectionReader(ConnectionHandler *handler, bool *lO, bool *t) : handler(handler), logOut(lO),
                                                                                    sholdTerminate(t) {}

short ConnectionReader::bytesToShort(char *bytesArr) {
    short result = (short) ((bytesArr[0] & 0xff) << 8);
    result += (short) (bytesArr[1] & 0xff);
    return result;
}

void ConnectionReader::run() {
    *logOut = false;
    *sholdTerminate = false;
    while (!(*sholdTerminate)) {
        char *opCodeArr = new char[2];
        handler->getBytes(opCodeArr, 2);
        short opCode = bytesToShort(opCodeArr);
        string output;
        if (opCode == 9) {
            output = "NOTIFICATION ";
            char *postPm = new char[1];
            handler->getBytes(postPm, 1);
            if (postPm[0] == '0')
                output += "PM ";
            else
                output += "Public ";
            string userWhoPost;
            string content;
            handler->getLine(userWhoPost);
            output += userWhoPost.substr(0, (userWhoPost.size() - 1));
            handler->getLine(content);
            output += " " + content.substr(0, (content.size() - 1));
            handler->getBytes(postPm, 1);
            delete[] postPm;
        } else if (opCode == 10) {
            output += "ACK ";
            char *MassageOpcode = new char[2];
            handler->getBytes(MassageOpcode, 2);
            short Mopcode = bytesToShort(MassageOpcode);
            output += to_string(Mopcode) + " ";
            if (Mopcode == 1 || Mopcode == 2 || Mopcode == 5 || Mopcode == 6 ||
                Mopcode == 12) {//in this case there is no additional treatment
                handler->getBytes(MassageOpcode, 1);//reading the last byte ";"
            } else if (Mopcode == 3) {
                handler->getBytes(MassageOpcode, 1);
                *sholdTerminate = true;
            }
                //FollowUnFollow
            else if (Mopcode == 4) {
                string userName;
                handler->getLine(userName);
                output += userName.substr(0, (userName.size() - 1));
                handler->getBytes(opCodeArr, 1);
            } else {
                bool finished = false;
                char *next = new char[1];
                output = "";
                while (!finished) {
                    handler->getBytes(next, 1);
                    if (next[0] != ';') {
                        output += "ACK " + to_string(opCode) + " ";
                        handler->getBytes(MassageOpcode, 1);
                        MassageOpcode[1] = MassageOpcode[0];
                        MassageOpcode[0] = next[0];
                        short age = bytesToShort(MassageOpcode);
                        handler->getBytes(MassageOpcode, 2);
                        short numOfPosts = bytesToShort(MassageOpcode);
                        handler->getBytes(MassageOpcode, 2);
                        short numOfFollowers = bytesToShort(MassageOpcode);
                        handler->getBytes(MassageOpcode, 2);
                        short numOfFollowing = bytesToShort(MassageOpcode);
                        output += to_string(age) + " " + to_string(numOfPosts) + " " + to_string(numOfFollowers) + " " +
                                  to_string(numOfFollowing) + "\n";
                    } else
                        finished = true;
                }
                output = output.substr(0, output.length() - 1);
                delete[] next;
            }
        } else if (opCode == 11) {
            output += "ERROR ";
            char *Mopcode = new char[2];
            handler->getBytes(Mopcode, 2);
            short MO = bytesToShort(Mopcode);
            output += to_string(MO) + " ";
            char *oneByte = new char[1];
            handler->getBytes(oneByte, 1);
            *logOut = false;
            delete[]Mopcode;
            delete[]oneByte;
        }
        if (!output.empty())
            cout << output << endl;
    }
}