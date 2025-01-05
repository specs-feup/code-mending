#ifndef SOURCEBATCHALLOCATION_H
#define SOURCEBATCHALLOCATION_H

#include <vector>
#include <string>

struct SourceFile {
    std::string path;
    unsigned size;
    unsigned id = idCounter++;
private:
    static unsigned idCounter;
};

struct ThreadSourceBatch {
    std::vector<SourceFile> sourceFiles{};
    unsigned threadLoad = 0; // sum of file sizes allocated to that thread

    // operator overloading for min heap
    bool operator<(const ThreadSourceBatch &rhs) const {
        return threadLoad > rhs.threadLoad;
    }
};

class SourceBatchThreadAllocator {
private:
    unsigned threadNum;
    std::vector<SourceFile> sourceFiles;

public:
    SourceBatchThreadAllocator(const unsigned threadNum, const std::vector<SourceFile> &sourceFileSizes)
                            : threadNum(threadNum), sourceFiles(sourceFileSizes) {}

    /*
    * Minimize Maximum Load:
    *       Reduce the maximum total file size processed by any single thread.
    *       This ensures no thread is disproportionately burdened, leading to a balanced and efficient workload.
    * Formally:
        Minimize max (Thread Loads)
        where thread load = sum of file sizes allocated to that thread

    * n = number of threads
    * m = number of files
     */
    std::vector<ThreadSourceBatch> allocateGreedyBinarySearch();

    //
    // O(m * log(m) + m * log(n))
    std::vector<ThreadSourceBatch> allocateGreedyMinHeap();
};

#endif //SOURCEBATCHALLOCATION_H
