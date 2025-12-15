#pragma once
#include <string>
using namespace std;

class Song {
public:
    int id;
    string title;
    string artist;
    string genre;
    int duration;
    string path;
    string imagePath;

    Song(int i = 0,
         string t = "",
         string a = "",
         string g = "",
         int d = 0,
         string p = "")
    {
        id = i;
        title = t;
        artist = a;
        genre = g;
        duration = d;
        path = p;
    }
};
