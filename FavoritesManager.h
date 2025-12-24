#ifndef FAVORITESMANAGER_H
#define FAVORITESMANAGER_H

#include <fstream>
#include <iostream>
#include <string>

using namespace std;

#define FAVORITES_TABLE_SIZE 101   // prime number for hashing

class FavoritesManager {
private:
    int table[FAVORITES_TABLE_SIZE];
    bool occupied[FAVORITES_TABLE_SIZE];
    string fileName;

    int hash(int songId) {
        return songId % FAVORITES_TABLE_SIZE;
    }

public:
    FavoritesManager(const string& userId) {
        fileName = "favorites_" + userId + ".csv";

        for (int i = 0; i < FAVORITES_TABLE_SIZE; i++) {
            occupied[i] = false;
        }

        loadFromCSV();
    }

    void loadFromCSV() {
        ifstream file(fileName);
        if (!file.is_open())
            return;

        int songId;
        while (file >> songId) {
            addFavorite(songId, false);
        }

        file.close();
    }

    void addFavorite(int songId, bool save = true) {
        int index = hash(songId);

        while (occupied[index]) {
            if (table[index] == songId)
                return; // already exists
            index = (index + 1) % FAVORITES_TABLE_SIZE;
        }

        table[index] = songId;
        occupied[index] = true;

        if (save)
            saveToCSV();
    }

    void removeFavorite(int songId) {
        int index = hash(songId);

        while (occupied[index]) {
            if (table[index] == songId) {
                occupied[index] = false;
                saveToCSV();
                return;
            }
            index = (index + 1) % FAVORITES_TABLE_SIZE;
        }
    }

    bool isFavorite(int songId) {
        int index = hash(songId);

        while (occupied[index]) {
            if (table[index] == songId)
                return true;
            index = (index + 1) % FAVORITES_TABLE_SIZE;
        }
        return false;
    }

    void saveToCSV() {
        ofstream file(fileName, ios::trunc);
        for (int i = 0; i < FAVORITES_TABLE_SIZE; i++) {
            if (occupied[i]) {
                file << table[i] << endl;
            }
        }
        file.close();
    }
};

#endif
