#ifndef SONGDATABASE_H
#define SONGDATABASE_H

#include <fstream>
#include <sstream>
#include <iostream>
#include "Song.h"

using namespace std;

#define MAX_SONGS 1000

class SongDatabase {
private:
    Song songs[MAX_SONGS];
    int songCount;

public:
    SongDatabase() {
        songCount = 0;
    }

    int getSongCount() {
        return songCount;
    }

    Song getSongAt(int index) {
        return songs[index];
    }

    void loadFromCSV(const string& fileName) {
        ifstream file(fileName);
        if (!file.is_open()) {
            cout << "Failed to open songs.csv" << endl;
            return;
        }

        string line;
        getline(file, line); // skip header

        while (getline(file, line) && songCount < MAX_SONGS) {
            stringstream ss(line);
            string temp;

            Song s;

            getline(ss, temp, ',');
            s.songId = stoi(temp);

            getline(ss, s.title, ',');
            getline(ss, s.artist, ',');
            getline(ss, s.genre, ',');

            getline(ss, temp, ',');
            s.duration = stoi(temp);

            getline(ss, s.filePath, ',');
            getline(ss, s.imagePath, ',');
            getline(ss, s.dateAdded, ',');

            songs[songCount++] = s;
        }

        file.close();
        cout << "Loaded " << songCount << " songs from CSV" << endl;
    }
};

#endif
