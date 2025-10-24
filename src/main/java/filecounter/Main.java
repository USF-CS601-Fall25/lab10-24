package filecounter;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        MultiThreadedFileWordCounter fileCounter = new MultiThreadedFileWordCounter();
        fileCounter.traverseAndGatherResults(Paths.get("dir"));
        fileCounter.shutdownPool();
        System.out.println(fileCounter.getCount());
    }
}
