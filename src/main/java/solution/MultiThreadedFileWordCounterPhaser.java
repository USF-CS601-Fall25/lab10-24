package solution;


import logging.HikingPhaserExampleWithLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;

// Change MultiThreadedFileWordCounter to use Phaser for thread coordination instead of  Futures
// Use Logger to log when you create workers, when each worker is done etc.
public class MultiThreadedFileWordCounterPhaser {
    private int count;
    private final ExecutorService poolManager = Executors.newCachedThreadPool();
    private Phaser phaser = new Phaser(1);
    private static final Logger logger = LogManager.getLogger(HikingPhaserExampleWithLogger.class);

    public MultiThreadedFileWordCounterPhaser() {
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
            try (BufferedReader br = Files.newBufferedReader(file)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] words = line.split("[,;!\\. ]+");
                    localWordCount += words.length;
                }
            } catch (IOException e) {
                System.out.println(e);
            }
            logger.debug("Counter Worker for file  " + file + " found " + localWordCount);

            updateCount(localWordCount);
            logger.debug("Counter Worker for file " + file + " deregistered");
            phaser.arriveAndDeregister();
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
     */
    public void countWordsInFiles(Path directory) {
        try (DirectoryStream<Path> filesAndFolders = Files.newDirectoryStream(directory)) {
            for (Path path : filesAndFolders) {
                if (Files.isDirectory(path)) {
                    countWordsInFiles(path);
                } else if (path.toString().endsWith(".txt")) {
                    phaser.register();
                    poolManager.submit(new CounterWorker(path));
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
    public void traverseAndGatherResults(Path dir) {
        countWordsInFiles(dir);
        phaser.arriveAndAwaitAdvance();
        logger.debug("All words in all files in " + dir + " have been counted.");
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

    public static void main(String[] args) {
        MultiThreadedFileWordCounterPhaser fileCounter = new MultiThreadedFileWordCounterPhaser();
        fileCounter.traverseAndGatherResults(Paths.get("dir"));
        fileCounter.shutdownPool();
        System.out.println(fileCounter.getCount());
    }

}

