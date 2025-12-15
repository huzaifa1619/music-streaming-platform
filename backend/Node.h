#pragma once
#include "Song.h"

class Node {
public:
    Song data;
    Node* left;
    Node* right;

    Node(Song s) {
        data = s;
        left = nullptr;
        right = nullptr;
    }
};
