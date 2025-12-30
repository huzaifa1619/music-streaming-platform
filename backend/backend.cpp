#include <iostream>
#include <string>
#include <fstream>
#include <ctime>
#include "BackendController.h"

using namespace std;

int main() {

    ios::sync_with_stdio(false);   // REQUIRED FIX - no buffering
    cin.tie(nullptr);
    cout.tie(nullptr);

    BackendController backend;
    cout << "READY" << endl;       // first handshake output

    string currentUser = "";
    string cmd;

    while (cin >> cmd) {

        if (cmd == "SIGNUP") {
            string user, pass, fullname;
            cin >> user >> pass >> fullname;

            bool success = backend.signUp(user, pass, fullname);
            cout << (success ? "OK" : "EXISTS") << endl;
}


        else if (cmd == "LOGIN") {
            string user, pass;
            cin >> user >> pass;
            bool success = backend.login(user, pass);
            cout << (success ? "OK" : "FAIL") << endl;
        }

        else if (cmd == "SET_USER") {
            string user;
            cin >> user;
            backend.initializeSystem(user);
            currentUser = user;
            cout << "OK" << endl;   // REQUIRED - prevents freeze
        }

        else if (cmd == "GET_ALL") {
            int total = backend.getTotalSongs();
            for (int i = 0; i < total; i++) {
                Song s = backend.getSongByIndex(i);
                cout << s.songId << "," << s.title << "," << s.artist << ","
                     << s.genre << "," << s.duration << ","
                     << s.filePath << "," << s.imagePath << "," << s.dateAdded << endl;
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
            string title; cin >> title;
            Song* s = backend.searchSongByTitle(title);
            if (s) {
                cout << s->songId << "," << s->title << "," << s->artist << ","
                     << s->genre << "," << s->duration << ","
                     << s->filePath << "," << s->imagePath << endl;
            }
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
            string fname = "favorites_" + currentUser + ".csv";
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
            int pid; cin >> pid;
            string name;
            getline(cin, name);
            if (!name.empty() && name[0] == ' ') name = name.substr(1);
            backend.createPlaylist(pid, name);
            cout << "OK" << endl;
        }

        else if (cmd == "PL_ADD") {
            int pid, sid; cin >> pid >> sid;
            backend.addSongToPlaylist(pid, sid);
            cout << "OK" << endl;
        }

        else if (cmd == "PL_GET") {
            int pid; cin >> pid;
            vector<int> songs = backend.getPlaylistSongIds(pid);
            for (int rid : songs) {
                Song s = backend.getSongById(rid);
                if (s.songId == rid && s.songId != -1) {
                    cout << s.songId << "," << s.title << "," << s.artist << ","
                         << s.genre << "," << s.duration << ","
                         << s.filePath << "," << s.imagePath << endl;
                }
            }
            cout << "END" << endl;
        }

        else if (cmd == "PL_REMOVE") {
            int pid, sid; cin >> pid >> sid;
            backend.removeSongFromPlaylist(pid, sid);
            cout << "OK" << endl;
        }

        else if (cmd == "PL_LIST") {
            if (currentUser.empty()) { cout << "END" << endl; continue; }
            auto pls = backend.getPlaylists();
            for (auto &p : pls) {
                cout << p.first << "," << p.second << endl;
            }
            cout << "END" << endl;
        }

        else if (cmd == "RECOMMEND") {
            int id; cin >> id;
            vector<int> recs = backend.recommendFromSong(id);
            for (int rid : recs) {
                Song s = backend.getSongById(rid);
                if (s.songId == rid && s.songId != -1) {
                    cout << s.songId << "," << s.title << "," << s.artist << ","
                         << s.genre << "," << s.duration << ","
                         << s.filePath << "," << s.imagePath << endl;
                }
            }
            cout << "END" << endl;
        }

        else if (cmd == "GET_RECENT") {
            if (currentUser.empty()) { cout << "END" << endl; continue; }
            vector<int> recs = backend.getRecentSongIds();
            for (int rid : recs) {
                Song s = backend.getSongById(rid);
                if (s.songId == rid && s.songId != -1) {
                    cout << s.songId << "," << s.title << "," << s.artist << ","
                         << s.genre << "," << s.duration << ","
                         << s.filePath << "," << s.imagePath << endl;
                }
            }
            cout << "END" << endl;
        }

        else if (cmd == "EXIT") break;
    }
    return 0;
}
