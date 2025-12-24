#ifndef BACKENDCONTROLLER_H
#define BACKENDCONTROLLER_H

#include <string>
#include <iostream>

#include "SongDatabase.h"
#include "SongSearchBST.h"
#include "FavoritesManager.h"
#include "RecentlyPlayedManager.h"
#include "PlaylistManager.h"
#include "RecommendationEngine.h"

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

    FavoritesManager* Favorites;
    RecentlyPlayedManager* recents;
    PlaylistManager* playlists;

    string currentUser;

public:
    BackendController() {
        Favorites = nullptr;
        recents = nullptr;
        playlists = nullptr;
    }

    // ================= SYSTEM INIT =================
    void InitializeSystem(const string& userId) {
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

        Favorites = new FavoritesManager(userId);
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
        Favorites->addFavorite(songId);
    }

    void removeFavorite(int songId) {
        Favorites->removeFavorite(songId);
    }

    bool isFavorite(int songId) {
        return Favorites->isFavorite(songId);
    }

    // ================= PLAYLIST =================
    void createPlaylist(int playlistId, const string& name) {
        playlists->createPlaylist(playlistId, name);
    }

    void addSongToPlaylist(int playlistId, int songId) {
        playlists->addSongToPlaylist(playlistId, songId);
    }

    // ================= RECOMMEND =================
    void recommendFromSong(int songId) {
        recommender.recommendSongs(songId);
    }
};

#endif
