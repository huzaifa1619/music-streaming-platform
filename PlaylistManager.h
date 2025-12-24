#ifndef PLAYLISTMANAGER_H
#define PLAYLISTMANAGER_H

#include <fstream>
#include <iostream>
#include <string>
#include "Song.h"

using namespace std;

#define MAX_PLAYLISTS 50
#define PLAYLIST_TABLE_SIZE 53   // prime number

// Single PlaylistNode definition (ONLY ONCE)
struct PlaylistNode {
    int songId;
    PlaylistNode* next;

    PlaylistNode(int id) {
        songId = id;
        next = NULL;
    }
};

struct Playlist {
    int playlistId;
    string name;
    PlaylistNode* head;
};

class PlaylistManager {
private:
    Playlist playlists[PLAYLIST_TABLE_SIZE];
    bool occupied[PLAYLIST_TABLE_SIZE];
    int count;
    string playlistFile;
    string playlistSongsFile;

    int hash(int playlistId) {
        return playlistId % PLAYLIST_TABLE_SIZE;
    }

public:
    PlaylistManager(const string& userId) {
        playlistFile = "playlists_" + userId + ".csv";
        playlistSongsFile = "playlist_songs_" + userId + ".csv";
        count = 0;

        for (int i = 0; i < PLAYLIST_TABLE_SIZE; i++)
            occupied[i] = false;

        loadPlaylists();
        loadPlaylistSongs();
    }

    void createPlaylist(int playlistId, const string& name) {
        if (count >= MAX_PLAYLISTS)
            return;

        int index = hash(playlistId);
        while (occupied[index])
            index = (index + 1) % PLAYLIST_TABLE_SIZE;

        playlists[index].playlistId = playlistId;
        playlists[index].name = name;
        playlists[index].head = NULL;
        occupied[index] = true;
        count++;

        savePlaylists();
    }

    void addSongToPlaylist(int playlistId, int songId) {
        int index = hash(playlistId);
        while (occupied[index]) {
            if (playlists[index].playlistId == playlistId) {
                PlaylistNode* node = new PlaylistNode(songId);
                node->next = playlists[index].head;
                playlists[index].head = node;
                savePlaylistSongs();
                return;
            }
            index = (index + 1) % PLAYLIST_TABLE_SIZE;
        }
    }

    void loadPlaylists() {
        ifstream file(playlistFile);
        if (!file.is_open())
            return;

        int id;
        string name;
        while (file >> id) {
            file.ignore();
            getline(file, name);
            createPlaylist(id, name);
        }
        file.close();
    }

    void loadPlaylistSongs() {
        ifstream file(playlistSongsFile);
        if (!file.is_open())
            return;

        string line;
        while (getline(file, line)) {
            if (line.empty()) continue;
            size_t comma = line.find(',');
            if (comma == string::npos) continue;
            int pid = stoi(line.substr(0, comma));
            int sid = stoi(line.substr(comma + 1));
            addSongToPlaylist(pid, sid);
        }
        file.close();
    }

    void savePlaylists() {
        ofstream file(playlistFile, ios::trunc);
        for (int i = 0; i < PLAYLIST_TABLE_SIZE; i++) {
            if (occupied[i]) {
                file << playlists[i].playlistId << ","
                     << playlists[i].name << endl;
            }
        }
        file.close();
    }

    void savePlaylistSongs() {
        ofstream file(playlistSongsFile, ios::trunc);
        for (int i = 0; i < PLAYLIST_TABLE_SIZE; i++) {
            if (occupied[i]) {
                PlaylistNode* curr = playlists[i].head;
                while (curr != NULL) {
                    file << playlists[i].playlistId << ","
                         << curr->songId << endl;
                    curr = curr->next;
                }
            }
        }
        file.close();
    }
};

#endif
