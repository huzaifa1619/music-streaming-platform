#ifndef RECOMMENDATIONENGINE_H
#define RECOMMENDATIONENGINE_H

#include <iostream>
#include <vector>
using namespace std;

#define MAX_GRAPH_SONGS 1000
#define MAX_EDGES 10

class RecommendationEngine {
private:
    // Graph node defined INSIDE the file (simple & academic)
    struct GraphNode {
        int songId;
        int neighbors[MAX_EDGES];
        int neighborCount;

        GraphNode() {
            songId = -1;
            neighborCount = 0;
        }
    };

    GraphNode graph[MAX_GRAPH_SONGS];
    int nodeCount;

    int findNodeIndex(int songId) {
        for (int i = 0; i < nodeCount; i++) {
            if (graph[i].songId == songId)
                return i;
        }
        return -1;
    }

public:
    RecommendationEngine() {
        nodeCount = 0;
    }

    void addSongNode(int songId) {
        graph[nodeCount].songId = songId;
        nodeCount++;
    }

    void addEdge(int songId1, int songId2) {
        int i1 = findNodeIndex(songId1);
        int i2 = findNodeIndex(songId2);

        if (i1 == -1 || i2 == -1)
            return;

        if (graph[i1].neighborCount < MAX_EDGES)
            graph[i1].neighbors[graph[i1].neighborCount++] = songId2;

        if (graph[i2].neighborCount < MAX_EDGES)
            graph[i2].neighbors[graph[i2].neighborCount++] = songId1;
    }

    // Return a list of recommended song IDs using BFS from startSongId
    vector<int> getRecommendations(int startSongId) {
        vector<int> results;
        bool visited[MAX_GRAPH_SONGS] = {false};
        int queue[MAX_GRAPH_SONGS];
        int front = 0, rear = 0;

        int startIndex = findNodeIndex(startSongId);
        if (startIndex == -1)
            return results;

        visited[startIndex] = true;
        queue[rear++] = startIndex;

        while (front < rear) {
            int current = queue[front++];

            for (int i = 0; i < graph[current].neighborCount; i++) {
                int neighborSongId = graph[current].neighbors[i];
                int neighborIndex = findNodeIndex(neighborSongId);

                if (neighborIndex != -1 && !visited[neighborIndex]) {
                    visited[neighborIndex] = true;
                    results.push_back(neighborSongId);
                    queue[rear++] = neighborIndex;
                }
            }
        }

        return results;
    }
};

#endif
