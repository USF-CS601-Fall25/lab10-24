package filecounter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

// TODO:
// 1) Change MultiThreadedFileWordCounter to use Phaser for thread coordination instead of  Futures
// 2) Use Logger to log when you create workers, when each worker is done etc. Refer to the HikingPhaserExampleWithLogger
public class MultiThreadedFileWordCounter {
    private int count;
    private final ExecutorService poolManager = Executors.newCachedThreadPool();
    // TODO: Create a Phaser
    public MultiThreadedFileWordCounter() {
        count = 0;
    }

    /**
     * Counts words in one file
     */
    class CounterWorker implements Runnable {
        private final Path file;

        public CounterWorker(Path file) {
            this.file = file;
        }

        @Override
        public void run() {
            int localWordCount = 0;
            //System.out.println(file);
            try (BufferedReader br = Files.newBufferedReader(file)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] words = line.split("[,;!\\. ]+");
                    localWordCount += words.length;
                }
            } catch (IOException e) {
                System.out.println(e);
            }
            updateCount(localWordCount);
        }
    }

    public synchronized void updateCount(int localWordCount) {
        count += localWordCount;
    }

    public synchronized int getCount() {
        return count;
    }

    /**
     * A recursive method that traverses a given directory to find text files,
     * creates a Runnable task (CountWorker) for each  file to count words,
     * and submits it to the thread pool.
     * @param directory
     * @param futures
     */
    // TODO: Change this method to use Phaser instead of futures
    public void countWordsInFiles(Path directory, List<Future<Void>> futures) {
        try (DirectoryStream<Path> filesAndFolders = Files.newDirectoryStream(directory)) {
            for (Path path : filesAndFolders) {
                if (Files.isDirectory(path)) {
                    countWordsInFiles(path, futures);
                } else if (path.toString().endsWith(".txt")) {
                    // TODO: Instead of Future, use Phaser
                    Future<Void> future = poolManager.submit(new CounterWorker(path), null);
                    futures.add(future);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }


    /**
     * Traverses a given directory to find text files and
     * counts words in each file. Updates the total count.
     * @param dir
     */
    // TODO: Change this method to use Phaser instead of futures
    public void traverseAndGatherResults(Path dir) {
        List<Future<Void>> futures = new ArrayList<>();
        countWordsInFiles(dir, futures);
        for (Future<Void> future : futures) {
            try {
                future.get(); // wait for the result of the task
            } catch (InterruptedException | ExecutionException e) {
                System.out.println(e);
            }
        }
    }

    /**
     * Shut down the pool in two steps.
     */
    public void shutdownPool() {
        poolManager.shutdown();  // tell the pool not to accept new tasks
        try {
            poolManager.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); // wait for current tasks to finish - not really needed in this example, since we previously waited for all futures to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}

