#ifndef USERAUTHMANAGER_H
#define USERAUTHMANAGER_H

#include <fstream>
#include <iostream>
#include <string>
using namespace std;

class UserAuthManager {
private:
    string usersFile;

public:
    UserAuthManager() {
        usersFile = "users.csv";
        initializeUsersFile();
    }

    void initializeUsersFile() {
        ifstream file(usersFile);
        if(!file.is_open()) {
            ofstream nf(usersFile);
            nf << "fullname,username,password\n";    // simple CSV
            nf.close();
        }
    }

    bool signUp(const string& username, const string& password, const string& fullname) {
    initializeUsersFile();
    ifstream in(usersFile);
    string line;

    while(getline(in,line)){
        if(line.find(","+username+",") != string::npos) return false; // exists
    }
    in.close();

    ofstream out(usersFile, ios::app);
    out << fullname << "," << username << "," << password << "\n";
    out.close();
    return true;
}


    bool login(const string& username, const string& password) {
        initializeUsersFile();
        ifstream in(usersFile);
        string f,u,p;

        while(in.good()){
            getline(in,f,','); // fullname
            getline(in,u,','); // username
            getline(in,p);     // password

            if(u == username && p == password) return true;
        }
        return false;
    }
};

#endif
