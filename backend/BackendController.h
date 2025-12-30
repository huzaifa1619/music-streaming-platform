#ifndef BACKENDCONTROLLER_H
#define BACKENDCONTROLLER_H

#include <string>
#include <iostream>
#include <vector>
#include <utility>

#include "SongDatabase.h"
#include "SongSearchBST.h"
#include "FavoritesManager.h"
#include "RecentlyPlayedManager.h"
#include "PlaylistManager.h"
#include "RecommendationEngine.h"
#include "UserAuthManager.h"

using namespace std;

/*
 * BackendController
 * Central coordinator between CLI (backend.cpp) and managers
 * No I/O formatting logic
 */

class BackendController {
private:
    SongDatabase songDB;
    SongSearchBST searchTree;
    RecommendationEngine recommender;

    FavoritesManager* favorites;
    RecentlyPlayedManager* recents;
    PlaylistManager* playlists;
    UserAuthManager* authManager;

    string currentUser;

public:
    BackendController() {
        favorites = nullptr;
        recents = nullptr;
        playlists = nullptr;
        authManager = new UserAuthManager();
    }

    // ================= USER AUTH =================
    bool signUp(const string& username, const string& password, const string& fullname) {
    return authManager->signUp(username, password, fullname);
}


    bool login(const string& username, const string& password) {
        return authManager->login(username, password);
    }

    // ================= SYSTEM INIT =================
    void initializeSystem(const string& userId) {
        currentUser = userId;

        songDB.loadFromCSV("C:\\Users\\PMLS\\Desktop\\Music Streaming Platform\\songs.csv");

        for (int i = 0; i < songDB.getSongCount(); i++) {
            Song s = songDB.getSongAt(i);
            searchTree.insertSong(s);
            recommender.addSongNode(s.songId);
        }

        // simple similarity: same artist
        for (int i = 0; i < songDB.getSongCount(); i++) {
            for (int j = i + 1; j < songDB.getSongCount(); j++) {
                if (songDB.getSongAt(i).artist ==
                    songDB.getSongAt(j).artist) {
                    recommender.addEdge(
                        songDB.getSongAt(i).songId,
                        songDB.getSongAt(j).songId
                    );
                }
            }
        }

        favorites = new FavoritesManager(userId);
        recents   = new RecentlyPlayedManager(userId);
        playlists = new PlaylistManager(userId);
    }

    // ================= SONG ACCESS =================
    int getTotalSongs() {
        return songDB.getSongCount();
    }

    Song getSongByIndex(int index) {
        return songDB.getSongAt(index);
    }

    Song* searchSongByTitle(const string& title) {
        return searchTree.searchByTitle(title);
    }

    // ================= PLAY =================
    void songPlayed(int songId) {
        recents->addSong(songId);
    }

    // ================= FAVORITES =================
    void addFavorite(int songId) {
        favorites->addFavorite(songId);
    }

    void removeFavorite(int songId) {
        favorites->removeFavorite(songId);
    }

    bool isFavorite(int songId) {
        return favorites->isFavorite(songId);
    }

    // ================= PLAYLIST =================
    void createPlaylist(int playlistId, const string& name) {
        playlists->createPlaylist(playlistId, name);
    }

    void addSongToPlaylist(int playlistId, int songId) {
        playlists->addSongToPlaylist(playlistId, songId);
    }

    void removeSongFromPlaylist(int playlistId, int songId) {
        playlists->removeSongFromPlaylist(playlistId, songId);
    }

    // Return list of playlists (id,name)
    vector<pair<int,string>> getPlaylists() {
        if (!playlists) return vector<pair<int,string>>();
        return playlists->listPlaylists();
    }

    // Return song ids for a given playlist
    vector<int> getPlaylistSongIds(int playlistId) {
        if (!playlists) return vector<int>();
        return playlists->getSongsForPlaylist(playlistId);
    }

    // ================= RECOMMEND =================
    // Return list of recommended song IDs
    vector<int> recommendFromSong(int songId) {
        return recommender.getRecommendations(songId);
    }

    // Fetch Song by ID (returns Song with songId=-1 if not found)
    Song getSongById(int songId) {
        Song s;
        s.songId = -1;
        for (int i = 0; i < songDB.getSongCount(); i++) {
            Song cand = songDB.getSongAt(i);
            if (cand.songId == songId) return cand;
        }
        return s;
    }

    // Return recent song IDs for current user (most recent first)
    vector<int> getRecentSongIds() {
        if (!recents) return vector<int>();
        return recents->getRecents();
    }
};

#endif
