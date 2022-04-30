
#include <KeyBoardReader.h>

#include "ConnectionHandler.h"
#include <boost/algorithm/string.hpp>


using namespace std;


KeyBoardReader::KeyBoardReader(ConnectionHandler *handler, bool *lO, bool *t) : handler(handler), logOut(lO),
                                                                                shouldTerminate(t) {}

void KeyBoardReader::run() {
    *logOut = false;
    *shouldTerminate = false;


    while (!(*shouldTerminate)) {
        while (*logOut) {
            if (*shouldTerminate) {
                break;
            }
        }
	if(*shouldTerminate) break;
        const short bufsize = 1024;
        char buf[bufsize];
        cin.getline(buf, bufsize);
        string line(buf);
        string toSend;
        vector<string> command;
        boost::split(command, line, boost::is_any_of(" "));
        char opcode[2];
        // Case 1 - register
        if (command[0] == "REGISTER") {
            short op = 1;
            shortToBytes(op, opcode);
            string UserName = command.at(1);
            string Password = command.at(2);
            string Birthday = command.at(3);
            handler->sendBytes(opcode, 2);
            handler->sendLine(UserName);
            handler->sendLine(Password);
            handler->sendLine(Birthday);
            // Case 2 - login
        } else if (command[0] == "LOGIN") {
            short op = 2;
            shortToBytes(op, opcode);
            handler->sendBytes(opcode, 2);
            string Username = command.at(1);
            string Password = command.at(2);
            string Captcha;
            if (command.size() > 3)
                Captcha = command.at(3);
            else
                Captcha = "0";
            handler->sendLine(Username);
            handler->sendLine(Password);
            if (Captcha == "0") {
                char bytes[1];
                bytes[0] = '2';
                handler->sendBytes(bytes, 1);
            } else {
                char bytes[1];
                bytes[0] = Captcha[0];
                handler->sendBytes(bytes, 1);
            }
            // Case 3 - logout
        } else if (command[0] == "LOGOUT") {
            short op = 3;
            shortToBytes(op, opcode);
            handler->sendBytes(opcode, 2);
            *logOut = true;
            // Case 4 - follow or unfollow
        } else if (command[0] == "FOLLOW") {
            short op = 4;
            shortToBytes(op, opcode);
            char byte[1];
            string FollowUnfollow = command.at(1);
            string UserName = command.at(2);
            if (FollowUnfollow == "0")
                byte[0] = '0';
            else
                byte[0] = '1';
            handler->sendBytes(opcode, 2);
            handler->sendBytes(byte, 1);
            handler->sendLine(UserName);
            // Case 5 - post
        } else if (command[0] == "POST") {
            shortToBytes(5, opcode);
            string content = command[1];
            for (size_t i = 2; i < command.size(); i++)
                content += " " + command[i];
            handler->sendBytes(opcode, 2);
            handler->sendLine(content);
            // Case 6 - private massage
        } else if (command[0] == "PM") {
            shortToBytes(6, opcode);
            string userName = command[1];
            handler->sendBytes(opcode, 2);
            handler->sendLine(userName);
            string content = command[2];
            for (size_t i = 3; i < command.size(); i++) {
                content += " " + command[i];
            }
            handler->sendLine(content);
            // Case 7 - logstat
        } else if (command[0] == "LOGSTAT") {
            short op = 7;
            shortToBytes(op, opcode);
            handler->sendBytes(opcode, 2);
            // Case 8 - register
        } else if (command[0] == "STAT") {
            short op = 8;
            shortToBytes(op, opcode);
            for (size_t i = 1; i < command.size(); i++)
                toSend = toSend + command.at(i) + "|";
            toSend.pop_back();
            handler->sendBytes(opcode, 2);
            handler->sendLine(toSend);
        }
        // Case 9 - block
        if (command[0] == "BLOCK") {
            short op = 12;
            shortToBytes(op, opcode);
            string UserName = command.at(1);
            handler->sendBytes(opcode, 2);
            handler->sendLine(UserName);
        }
        char finish[1]; // Always finishing massage with ; according to protocol
        finish[0] = ';';
        handler->sendBytes(finish, 1);
    }
}

// Used this from moodle
void KeyBoardReader::shortToBytes(short num, char *bytesArr) {
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}
