#ifndef RECENTLYPLAYEDMANAGER_H
#define RECENTLYPLAYEDMANAGER_H

#include <fstream>
#include <iostream>
#include <string>

using namespace std;

#define MAX_RECENT 20

class RecentlyPlayedManager {
private:

    struct RecentNode {
    int songId;
    RecentNode* prev;
    RecentNode* next;

    RecentNode(int id) {
        songId = id;
        prev = NULL;
        next = NULL;
    }
};

    RecentNode* head;
    RecentNode* tail;
    int size;
    string fileName;

public:
    RecentlyPlayedManager(const string& userId) {
        head = tail = NULL;
        size = 0;
        fileName = "recently_played_" + userId + ".csv";
        loadFromCSV();
    }

    void addSong(int songId) {
        removeIfExists(songId);

        RecentNode* node = new RecentNode(songId);
        node->next = head;
        if (head != NULL)
            head->prev = node;
        head = node;

        if (tail == NULL)
            tail = node;

        size++;
        if (size > MAX_RECENT)
            removeLast();

        saveToCSV();
    }

    void removeIfExists(int songId) {
        RecentNode* curr = head;
        while (curr != NULL) {
            if (curr->songId == songId) {
                if (curr->prev)
                    curr->prev->next = curr->next;
                if (curr->next)
                    curr->next->prev = curr->prev;
                if (curr == head)
                    head = curr->next;
                if (curr == tail)
                    tail = curr->prev;

                delete curr;
                size--;
                return;
            }
            curr = curr->next;
        }
    }

    void removeLast() {
        if (tail == NULL)
            return;

        RecentNode* temp = tail;
        tail = tail->prev;
        if (tail)
            tail->next = NULL;
        else
            head = NULL;

        delete temp;
        size--;
    }

    void loadFromCSV() {
        ifstream file(fileName);
        if (!file.is_open())
            return;

        int songId;
        while (file >> songId) {
            addSong(songId);
        }
        file.close();
    }

    void saveToCSV() {
        ofstream file(fileName, ios::trunc);
        RecentNode* curr = head;
        while (curr != NULL) {
            file << curr->songId << endl;
            curr = curr->next;
        }
        file.close();
    }

    // Return a vector of recent song IDs (most recent first)
    vector<int> getRecents() {
        vector<int> list;
        RecentNode* curr = head;
        while (curr != NULL) {
            list.push_back(curr->songId);
            curr = curr->next;
        }
        return list;
    }
};

#endif
