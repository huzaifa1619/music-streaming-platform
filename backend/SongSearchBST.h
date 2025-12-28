#ifndef SONGSEARCHBST_H
#define SONGSEARCHBST_H

#include <iostream>
#include <string>
#include "Song.h"

using namespace std;

class SongSearchBST {
private:

    struct BSTNode {
    Song data;
    BSTNode* left;
    BSTNode* right;

    BSTNode(Song s) {
        data = s;
        left = NULL;
        right = NULL;
    }
};

    BSTNode* root;

    BSTNode* insertNode(BSTNode* node, Song s) {
        if (node == NULL)
            return new BSTNode(s);

        if (s.title < node->data.title)
            node->left = insertNode(node->left, s);
        else
            node->right = insertNode(node->right, s);

        return node;
    }

    BSTNode* searchNode(BSTNode* node, const string& title) {
        if (node == NULL)
            return NULL;

        if (node->data.title == title)
            return node;

        if (title < node->data.title)
            return searchNode(node->left, title);
        else
            return searchNode(node->right, title);
    }

    void inorderTraversal(BSTNode* node) {
        if (node == NULL)
            return;

        inorderTraversal(node->left);
        cout << node->data.title << " - "
             << node->data.artist << endl;
        inorderTraversal(node->right);
    }

public:
    SongSearchBST() {
        root = NULL;
    }

    void insertSong(Song s) {
        root = insertNode(root, s);
    }

    Song* searchByTitle(const string& title) {
        BSTNode* result = searchNode(root, title);
        if (result != NULL)
            return &result->data;
        return NULL;
    }

    void displayAllSongsAZ() {
        inorderTraversal(root);
    }
};

#endif
