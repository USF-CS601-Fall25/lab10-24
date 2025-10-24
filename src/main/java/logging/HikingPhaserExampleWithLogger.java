package logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.concurrent.Phaser;

// Demonstrates how to use logger - refer to this example as you work on MultiThreadedFileWordCounter
public class HikingPhaserExampleWithLogger {
    private static final Logger logger = LogManager.getLogger(HikingPhaserExampleWithLogger.class);

    static class Hiker implements Runnable {
        private final Phaser phaser;
        private final String name;
        private final boolean stopAtBaseCamp;

        public Hiker(Phaser phaser, String name, boolean stopAtBaseCamp) {
            this.phaser = phaser;
            this.name = name;
            this.stopAtBaseCamp = stopAtBaseCamp;

            // Logs to both console (INFO) and file (DEBUG)
            logger.debug("Registered worker " + name);
            phaser.register(); // Register this hiker as a participant
        }

        @Override
        public void run() {
            try {
                // Phase 0: Hike to base camp
                logger.info("{} hiking to base camp...", name);
                Thread.sleep((long) (Math.random() * 1000));
                logger.info("{} reached base camp!", name);
                phaser.arriveAndAwaitAdvance();

                // Some hikers stop early
                if (stopAtBaseCamp) {
                    logger.warn("{} decides to stay at base camp and rest. Deregistering...", name);
                    phaser.arriveAndDeregister();
                    return;
                }

                // Phase 1: Hike to summit
                logger.info("{} hiking to the summit...", name);
                Thread.sleep((long) (Math.random() * 1000));
                logger.info("{} reached the summit!", name);
                phaser.arriveAndAwaitAdvance();

                phaser.arriveAndDeregister();
                logger.debug("{} finished all phases and is leaving the group.", name);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("{} was interrupted: {}", name, e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Phaser phaser = new Phaser(1); // main thread registered
        logger.info("Main thread registered. Starting hikers...");

        // Three hikers — one stops early
        new Thread(new Hiker(phaser, "Alice", false)).start();
        new Thread(new Hiker(phaser, "Bob", true)).start();
        new Thread(new Hiker(phaser, "Charlie", false)).start();

        // Observe and log phase transitions
        for (int phase = 0; phase < 2; phase++) {
            int currentPhase = phaser.getPhase();
            phaser.arriveAndAwaitAdvance();
            logger.info("Phase {} completed", currentPhase);
        }

        phaser.arriveAndDeregister();
        logger.info("All phases complete!");
    }
}

