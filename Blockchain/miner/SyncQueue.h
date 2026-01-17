#ifndef BLOCKCHAIN_SYNCQUEUE_H
#define BLOCKCHAIN_SYNCQUEUE_H

#include "iostream"
#include <condition_variable>
#include "list"

template<typename T>
class SyncQueue {
public:
    SyncQueue() {

    }

    void add(const T &data) {
        std::unique_lock<std::mutex> lck(myMutex);
        queue.push_back(data);
        myCv.notify_one();
    }

    T read() {
        std::unique_lock<std::mutex> lck(myMutex);
        while (queue.empty()) myCv.wait(lck);
        T result = queue.front();
        queue.pop_front();
        return result;
    }

    bool isEmpty() {
        std::unique_lock<std::mutex> lock(myMutex);
        return queue.empty();
    }

    void clear(){
        std::unique_lock<std::mutex> lck(myMutex);
        std::list<T> empty;
        std::swap(queue, empty);
        myCv.notify_all();
    }

    int getLength(){
        std::unique_lock<std::mutex> lock(myMutex);
        return queue.size();
    }

private:
    SyncQueue(const SyncQueue &);

    SyncQueue &operator=(const SyncQueue &);

    std::list<T> queue;
    std::mutex myMutex;
    std::condition_variable myCv;
};


#endif //BLOCKCHAIN_SYNCQUEUE_H