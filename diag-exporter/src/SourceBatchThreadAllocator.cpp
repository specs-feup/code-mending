#include "SourceBatchThreadAllocator.h"

#include <queue>

unsigned SourceFile::idCounter = 0;

std::vector<ThreadSourceBatch> SourceBatchThreadAllocator::allocateGreedyBinarySearch() {
    return {};
}

std::vector<ThreadSourceBatch> SourceBatchThreadAllocator::allocateGreedyMinHeap() {
    auto threadBatchesQueue = std::priority_queue<ThreadSourceBatch>();

    auto threadBatches = std::vector<ThreadSourceBatch>();

    // Initialize thread batches min heap
    for (unsigned i = 0; i < threadNum; i++) {
        threadBatchesQueue.emplace();
    }

    // sort sourceFileSizes in descending order
    std::sort(sourceFiles.begin(), sourceFiles.end(), [](const auto &lhs, const auto &rhs) {
        return lhs.size > rhs.size;
    });

    // allocate from biggest to smallest files to to batches with smaller loads
    for (const auto &sourceFileSize : sourceFiles) {
        auto threadBatch = threadBatchesQueue.top();
        threadBatchesQueue.pop();

        threadBatch.sourceFiles.push_back(sourceFileSize);
        threadBatch.threadLoad += sourceFileSize.size;

        threadBatchesQueue.push(threadBatch);
    }

    while (!threadBatchesQueue.empty()) {
        threadBatches.push_back(threadBatchesQueue.top());
        threadBatchesQueue.pop();
    }

    return threadBatches;
}
