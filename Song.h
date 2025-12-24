#ifndef SONG_H
#define SONG_H

#include <string>
using namespace std;

struct Song {
    int songId;
    string title;
    string artist;
    string genre;
    int duration;          // in seconds
    string filePath;
    string imagePath;
    string dateAdded;      // YYYY-MM-DD
};

#endif
