/*
 * DataStructures.h
 * Custom implementations of Hash Table, Linked List, Deque, BST, and Graph
 * 
 * DSA Justification:
 * - Hash Table: O(1) average for insert/delete/lookup - used for favorites, playlists
 * - Linked List: O(1) insert/delete at ends - used for playlist songs, queue
 * - Deque: O(1) front/back operations - used for recently played (fixed size)
 * - BST: O(log n) search - used for song search by title
 * - Graph: Used for recommendation engine with BFS/DFS traversal
 */

#ifndef DATA_STRUCTURES_H
#define DATA_STRUCTURES_H

#include <iostream>
#include <string>
#include <fstream>
#include <cstring>

using namespace std;

const int MAX_SONGS = 1000;
const int MAX_PLAYLISTS = 100;
const int MAX_RECENT = 20;
const int HASH_SIZE = 1009;  // Prime number for better distribution

// ==================== SONG STRUCTURE ====================
struct Song {
    int id;
    char title[256];
    char artist[256];
    char genre[64];
    int duration;           // in seconds
    char path[512];
    char imagePath[512];
    
    Song() : id(0), duration(0) {
        title[0] = artist[0] = genre[0] = path[0] = imagePath[0] = '\0';
    }
    
    void set(int _id, const char* _title, const char* _artist, 
             const char* _genre, int _duration, const char* _path, const char* _imagePath) {
        id = _id;
        strncpy(title, _title, 255); title[255] = '\0';
        strncpy(artist, _artist, 255); artist[255] = '\0';
        strncpy(genre, _genre, 63); genre[63] = '\0';
        duration = _duration;
        strncpy(path, _path, 511); path[511] = '\0';
        strncpy(imagePath, _imagePath, 511); imagePath[511] = '\0';
    }
    
    void print() const {
        cout << id << "," << title << "," << artist << "," 
             << genre << "," << duration << "," << path << "," << imagePath;
    }
    
    string toString() const {
        return to_string(id) + "," + title + "," + artist + "," + 
               genre + "," + to_string(duration) + "," + path + "," + imagePath;
    }
};

// ==================== LINKED LIST NODE ====================
/*
 * DSA: Singly Linked List Node
 * Used for: Playlist songs, search results, queue
 * Time Complexity: Insert O(1), Delete O(1) with pointer, Search O(n)
 */
struct ListNode {
    int songId;
    ListNode* next;
    
    ListNode(int id = 0) : songId(id), next(nullptr) {}
};

// ==================== SINGLY LINKED LIST ====================
class LinkedList {
private:
    ListNode* head;
    ListNode* tail;
    int size;
    
public:
    LinkedList() : head(nullptr), tail(nullptr), size(0) {}
    
    ~LinkedList() {
        clear();
    }
    
    // O(1) - Insert at end
    void append(int songId) {
        ListNode* newNode = new ListNode(songId);
        if (!head) {
            head = tail = newNode;
        } else {
            tail->next = newNode;
            tail = newNode;
        }
        size++;
    }
    
    // O(1) - Insert at front
    void prepend(int songId) {
        ListNode* newNode = new ListNode(songId);
        newNode->next = head;
        head = newNode;
        if (!tail) tail = newNode;
        size++;
    }
    
    // O(n) - Remove specific song
    bool remove(int songId) {
        if (!head) return false;
        
        if (head->songId == songId) {
            ListNode* temp = head;
            head = head->next;
            if (!head) tail = nullptr;
            delete temp;
            size--;
            return true;
        }
        
        ListNode* prev = head;
        ListNode* curr = head->next;
        while (curr) {
            if (curr->songId == songId) {
                prev->next = curr->next;
                if (curr == tail) tail = prev;
                delete curr;
                size--;
                return true;
            }
            prev = curr;
            curr = curr->next;
        }
        return false;
    }
    
    // O(n) - Check if contains
    bool contains(int songId) const {
        ListNode* curr = head;
        while (curr) {
            if (curr->songId == songId) return true;
            curr = curr->next;
        }
        return false;
    }
    
    // O(1) - Remove from front
    int removeFirst() {
        if (!head) return -1;
        int songId = head->songId;
        ListNode* temp = head;
        head = head->next;
        if (!head) tail = nullptr;
        delete temp;
        size--;
        return songId;
    }
    
    // O(1) - Remove from back (needs O(n) to find prev in singly linked)
    int removeLast() {
        if (!head) return -1;
        if (head == tail) {
            int songId = head->songId;
            delete head;
            head = tail = nullptr;
            size--;
            return songId;
        }
        
        ListNode* curr = head;
        while (curr->next != tail) {
            curr = curr->next;
        }
        int songId = tail->songId;
        delete tail;
        tail = curr;
        tail->next = nullptr;
        size--;
        return songId;
    }
    
    void clear() {
        while (head) {
            ListNode* temp = head;
            head = head->next;
            delete temp;
        }
        tail = nullptr;
        size = 0;
    }
    
    int getSize() const { return size; }
    bool isEmpty() const { return size == 0; }
    ListNode* getHead() const { return head; }
    
    // Get all song IDs as array
    void toArray(int* arr, int& count) const {
        count = 0;
        ListNode* curr = head;
        while (curr && count < MAX_SONGS) {
            arr[count++] = curr->songId;
            curr = curr->next;
        }
    }
};

// ==================== DOUBLY LINKED LIST NODE ====================
struct DListNode {
    int songId;
    DListNode* prev;
    DListNode* next;
    
    DListNode(int id = 0) : songId(id), prev(nullptr), next(nullptr) {}
};

// ==================== DEQUE (Double-ended Queue) ====================
/*
 * DSA: Deque using Doubly Linked List
 * Used for: Recently Played songs (fixed size, LRU-like behavior)
 * Time Complexity: All operations O(1)
 * 
 * Behavior:
 * - New song added to front
 * - If song exists, move to front
 * - If max size reached, remove from back
 */
class Deque {
private:
    DListNode* front;
    DListNode* back;
    int size;
    int maxSize;
    
public:
    Deque(int max = MAX_RECENT) : front(nullptr), back(nullptr), size(0), maxSize(max) {}
    
    ~Deque() {
        clear();
    }
    
    // O(1) - Add to front
    void pushFront(int songId) {
        // First check if already exists - if so, move to front
        if (moveToFront(songId)) return;
        
        // If at max capacity, remove from back
        if (size >= maxSize) {
            popBack();
        }
        
        DListNode* newNode = new DListNode(songId);
        if (!front) {
            front = back = newNode;
        } else {
            newNode->next = front;
            front->prev = newNode;
            front = newNode;
        }
        size++;
    }
    
    // O(n) - Find and move to front
    bool moveToFront(int songId) {
        DListNode* curr = front;
        while (curr) {
            if (curr->songId == songId) {
                // Already at front
                if (curr == front) return true;
                
                // Remove from current position
                if (curr->prev) curr->prev->next = curr->next;
                if (curr->next) curr->next->prev = curr->prev;
                if (curr == back) back = curr->prev;
                
                // Move to front
                curr->prev = nullptr;
                curr->next = front;
                front->prev = curr;
                front = curr;
                
                return true;
            }
            curr = curr->next;
        }
        return false;
    }
    
    // O(1) - Remove from back
    int popBack() {
        if (!back) return -1;
        int songId = back->songId;
        DListNode* temp = back;
        
        if (front == back) {
            front = back = nullptr;
        } else {
            back = back->prev;
            back->next = nullptr;
        }
        
        delete temp;
        size--;
        return songId;
    }
    
    // O(n) - Check if contains
    bool contains(int songId) const {
        DListNode* curr = front;
        while (curr) {
            if (curr->songId == songId) return true;
            curr = curr->next;
        }
        return false;
    }
    
    void clear() {
        while (front) {
            DListNode* temp = front;
            front = front->next;
            delete temp;
        }
        back = nullptr;
        size = 0;
    }
    
    int getSize() const { return size; }
    
    // Get all song IDs (front to back = most recent first)
    void toArray(int* arr, int& count) const {
        count = 0;
        DListNode* curr = front;
        while (curr && count < maxSize) {
            arr[count++] = curr->songId;
            curr = curr->next;
        }
    }
};

// ==================== HASH TABLE ====================
/*
 * DSA: Hash Table with Chaining
 * Used for: Favorites (O(1) add/remove/check), Song lookup by ID
 * 
 * Hash Function: Simple modulo with prime table size
 * Collision Resolution: Chaining (linked list at each bucket)
 * Time Complexity: Average O(1), Worst O(n)
 */
struct HashNode {
    int key;           // songId
    HashNode* next;
    
    HashNode(int k) : key(k), next(nullptr) {}
};

class HashTable {
private:
    HashNode* table[HASH_SIZE];
    int count;
    
    int hash(int key) const {
        return ((key % HASH_SIZE) + HASH_SIZE) % HASH_SIZE;
    }
    
public:
    HashTable() : count(0) {
        for (int i = 0; i < HASH_SIZE; i++) {
            table[i] = nullptr;
        }
    }
    
    ~HashTable() {
        clear();
    }
    
    // O(1) average - Insert
    bool insert(int key) {
        if (contains(key)) return false;  // No duplicates
        
        int idx = hash(key);
        HashNode* newNode = new HashNode(key);
        newNode->next = table[idx];
        table[idx] = newNode;
        count++;
        return true;
    }
    
    // O(1) average - Remove
    bool remove(int key) {
        int idx = hash(key);
        HashNode* curr = table[idx];
        HashNode* prev = nullptr;
        
        while (curr) {
            if (curr->key == key) {
                if (prev) {
                    prev->next = curr->next;
                } else {
                    table[idx] = curr->next;
                }
                delete curr;
                count--;
                return true;
            }
            prev = curr;
            curr = curr->next;
        }
        return false;
    }
    
    // O(1) average - Check if exists
    bool contains(int key) const {
        int idx = hash(key);
        HashNode* curr = table[idx];
        while (curr) {
            if (curr->key == key) return true;
            curr = curr->next;
        }
        return false;
    }
    
    void clear() {
        for (int i = 0; i < HASH_SIZE; i++) {
            HashNode* curr = table[i];
            while (curr) {
                HashNode* temp = curr;
                curr = curr->next;
                delete temp;
            }
            table[i] = nullptr;
        }
        count = 0;
    }
    
    int getCount() const { return count; }
    
    // Get all keys
    void toArray(int* arr, int& cnt) const {
        cnt = 0;
        for (int i = 0; i < HASH_SIZE && cnt < MAX_SONGS; i++) {
            HashNode* curr = table[i];
            while (curr && cnt < MAX_SONGS) {
                arr[cnt++] = curr->key;
                curr = curr->next;
            }
        }
    }
};

// ==================== BST NODE ====================
struct BSTNode {
    Song song;
    BSTNode* left;
    BSTNode* right;
    
    BSTNode(const Song& s) : song(s), left(nullptr), right(nullptr) {}
};

// ==================== BINARY SEARCH TREE ====================
/*
 * DSA: Binary Search Tree
 * Used for: Song searching by title (partial matching via traversal)
 * 
 * Time Complexity:
 * - Balanced: O(log n) search, insert, delete
 * - Worst (unbalanced): O(n)
 * 
 * Search Strategy:
 * - Exact match: Standard BST search
 * - Partial match: In-order traversal with string comparison
 */
class BST {
private:
    BSTNode* root;
    int size;
    
    // Helper: Compare titles (case-insensitive)
    int compareTitle(const char* a, const char* b) const {
        while (*a && *b) {
            char ca = tolower(*a);
            char cb = tolower(*b);
            if (ca != cb) return ca - cb;
            a++; b++;
        }
        return tolower(*a) - tolower(*b);
    }
    
    // Helper: Check if title contains substring (case-insensitive)
    bool containsSubstring(const char* str, const char* sub) const {
        if (!sub[0]) return true;
        
        int strLen = strlen(str);
        int subLen = strlen(sub);
        
        for (int i = 0; i <= strLen - subLen; i++) {
            bool match = true;
            for (int j = 0; j < subLen && match; j++) {
                if (tolower(str[i + j]) != tolower(sub[j])) {
                    match = false;
                }
            }
            if (match) return true;
        }
        return false;
    }
    
    // Recursive insert
    BSTNode* insert(BSTNode* node, const Song& song) {
        if (!node) {
            size++;
            return new BSTNode(song);
        }
        
        int cmp = compareTitle(song.title, node->song.title);
        if (cmp < 0) {
            node->left = insert(node->left, song);
        } else {
            node->right = insert(node->right, song);
        }
        return node;
    }
    
    // In-order traversal for partial search
    void searchPartial(BSTNode* node, const char* query, Song* results, int& count, int maxResults) const {
        if (!node || count >= maxResults) return;
        
        searchPartial(node->left, query, results, count, maxResults);
        
        if (count < maxResults) {
            // Check title and artist for partial match
            if (containsSubstring(node->song.title, query) || 
                containsSubstring(node->song.artist, query)) {
                results[count++] = node->song;
            }
        }
        
        searchPartial(node->right, query, results, count, maxResults);
    }
    
    // Clear tree
    void clear(BSTNode* node) {
        if (!node) return;
        clear(node->left);
        clear(node->right);
        delete node;
    }
    
    // In-order traversal to array
    void toArray(BSTNode* node, Song* arr, int& count) const {
        if (!node || count >= MAX_SONGS) return;
        toArray(node->left, arr, count);
        if (count < MAX_SONGS) arr[count++] = node->song;
        toArray(node->right, arr, count);
    }
    
public:
    BST() : root(nullptr), size(0) {}
    
    ~BST() {
        clear(root);
    }
    
    void insert(const Song& song) {
        root = insert(root, song);
    }
    
    // Search with partial matching
    int search(const char* query, Song* results, int maxResults) const {
        int count = 0;
        searchPartial(root, query, results, count, maxResults);
        return count;
    }
    
    // Get song by ID (requires full traversal - O(n))
    bool findById(int id, Song& result) const {
        return findById(root, id, result);
    }
    
    bool findById(BSTNode* node, int id, Song& result) const {
        if (!node) return false;
        if (node->song.id == id) {
            result = node->song;
            return true;
        }
        return findById(node->left, id, result) || findById(node->right, id, result);
    }
    
    void clear() {
        clear(root);
        root = nullptr;
        size = 0;
    }
    
    int getSize() const { return size; }
    
    void getAllSongs(Song* arr, int& count) const {
        count = 0;
        toArray(root, arr, count);
    }
};

#endif // DATA_STRUCTURES_H