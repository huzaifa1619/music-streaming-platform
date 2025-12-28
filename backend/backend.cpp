#include <iostream>
#include <string>
#include <fstream>
#include <ctime>
#include "BackendController.h"

using namespace std;

int main() {
    BackendController backend;
    cout << "READY" << endl;

    string currentUser = "";
    string cmd;
    while (cin >> cmd) {

        if (cmd == "SET_USER") {
            string user;
            cin >> user;
            backend.initializeSystem(user);
            currentUser = user;
            // cout << "OK" << endl;
        }

        else if (cmd == "GET_ALL") {
            int total = backend.getTotalSongs();
            for (int i = 0; i < total; i++) {
                Song s = backend.getSongByIndex(i);
                cout << s.songId << "," << s.title << "," << s.artist << ","
                     << s.genre << "," << s.duration << ","
                     << s.filePath << "," << s.imagePath << "," <<s.dateAdded << endl;
            }
            cout << "END" << endl;
        }

        else if (cmd == "GET_SONG") {
            int id; cin >> id;
            int total = backend.getTotalSongs();
            bool found = false;

            for (int i = 0; i < total; i++) {
                Song s = backend.getSongByIndex(i);
                if (s.songId == id) {
                    cout << s.songId << "," << s.title << "," << s.artist << ","
                        << s.genre << "," << s.duration << ","
                        << s.filePath << "," << s.imagePath << endl;
                    found = true;
                    break;
                }
            }
            if (!found) cout << "ERROR" << endl;
        }

        else if (cmd == "SEARCH") {
            string title;
            cin >> title;
            Song* s = backend.searchSongByTitle(title);
            if (s) {
                cout << s->songId << "," << s->title << "," << s->artist << ","
                     << s->genre << "," << s->duration << ","
                     << s->filePath << "," << s->imagePath << endl;
            }
            cout << "END" << endl;
        }

        else if (cmd == "GET_RECENT") {
            if (currentUser.empty()) { cout << "END" << endl; continue; }
            string fname = string("recently_played_") + currentUser + ".csv";
            ifstream f(fname);
            int sid;
            while (f.is_open() && (f >> sid)) {
                int total = backend.getTotalSongs();
                for (int i = 0; i < total; i++) {
                    Song s = backend.getSongByIndex(i);
                    if (s.songId == sid) {
                        cout << s.songId << "," << s.title << "," << s.artist << ","
                             << s.genre << "," << s.duration << ","
                             << s.filePath << "," << s.imagePath << endl;
                        break;
                    }
                }
            }
            if (f.is_open()) f.close();
            cout << "END" << endl;
        }

        else if (cmd == "PLAY") {
            int id; cin >> id;
            backend.songPlayed(id);
            cout << "OK" << endl;
        }

        else if (cmd == "FAV_ADD") {
            int id; cin >> id;
            backend.addFavorite(id);
            cout << "OK" << endl;
        }

        else if (cmd == "FAV_REMOVE") {
            int id; cin >> id;
            backend.removeFavorite(id);
            cout << "OK" << endl;
        }

        else if (cmd == "FAV_CHECK") {
            int id; cin >> id;
            cout << (backend.isFavorite(id) ? "YES" : "NO") << endl;
        }

        else if (cmd == "FAV_LIST") {
            if (currentUser.empty()) { cout << "END" << endl; continue; }
            string fname = string("favorites_") + currentUser + ".csv";
            ifstream f(fname);
            int sid;
            while (f.is_open() && (f >> sid)) {
                int total = backend.getTotalSongs();
                for (int i = 0; i < total; i++) {
                    Song s = backend.getSongByIndex(i);
                    if (s.songId == sid) {
                        cout << s.songId << "," << s.title << "," << s.artist << ","
                             << s.genre << "," << s.duration << ","
                             << s.filePath << "," << s.imagePath << endl;
                        break;
                    }
                }
            }
            if (f.is_open()) f.close();
            cout << "END" << endl;
        }

        else if (cmd == "PL_CREATE") {
            string name;
            cin >> name;
            for (char &c : name) if (c == '_') c = ' ';
            int pid = (int) time(NULL);
            backend.createPlaylist(pid, name);
            cout << "OK" << endl;
        }

        else if (cmd == "PL_ADD") {
            string name;
            int sid;
            cin >> name >> sid;
            for (char &c : name) if (c == '_') c = ' ';
            int pid = -1;
            string pfile = string("playlists_") + currentUser + ".csv";
            ifstream pf(pfile);
            if (pf.is_open()) {
                int id; string pname;
                while (pf >> id) {
                    pf.ignore();
                    getline(pf, pname);
                    if (pname == name) { pid = id; break; }
                }
                pf.close();
            }
            if (pid != -1) {
                backend.addSongToPlaylist(pid, sid);
                cout << "OK" << endl;
            } else cout << "ERROR" << endl;
        }

        else if (cmd == "PL_GET") {
            string name;
            cin >> name;
            for (char &c : name) if (c == '_') c = ' ';
            int pid = -1;
            string pfile = string("playlists_") + currentUser + ".csv";
            ifstream pf(pfile);
            if (pf.is_open()) {
                int id; string pname;
                while (pf >> id) {
                    pf.ignore();
                    getline(pf, pname);
                    if (pname == name) { pid = id; break; }
                }
                pf.close();
            }
            if (pid != -1) {
                string psfile = string("playlist_songs_") + currentUser + ".csv";
                ifstream ps(psfile);
                string line;
                while (ps.is_open() && getline(ps, line)) {
                    if (line.empty()) continue;
                    size_t comma = line.find(',');
                    if (comma == string::npos) continue;
                    int rpid = stoi(line.substr(0, comma));
                    int sid = stoi(line.substr(comma + 1));
                    if (rpid == pid) {
                        int total = backend.getTotalSongs();
                        for (int i = 0; i < total; i++) {
                            Song s = backend.getSongByIndex(i);
                            if (s.songId == sid) {
                                cout << s.songId << "," << s.title << "," << s.artist << ","
                                     << s.genre << "," << s.duration << ","
                                     << s.filePath << "," << s.imagePath << endl;
                                break;
                            }
                        }
                    }
                }
                if (ps.is_open()) ps.close();
            }
            cout << "END" << endl;
        }

        else if (cmd == "RECOMMEND") {
            int id; cin >> id;
            backend.recommendFromSong(id);
            cout << "END" << endl;
        }

        else if (cmd == "EXIT") {
            break;
        }
    }
    return 0;
}
