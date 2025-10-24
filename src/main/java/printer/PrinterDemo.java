package printer;

public class PrinterDemo {
    public static void main(String[] args) {
        Printer printer = new Printer();

        // User sending documents to the printer to print
        Runnable userTask = () -> {
            for (int i = 1; i <= 4; i++) {
                try {
                    printer.print("Document-" + i);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        // Technician refills paper occasionally
        Runnable technicianTask = () -> {
            try {
                while (true) {
                    Thread.sleep(1000);
                    printer.refill();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        // Two users printing, one technician refilling paper
        new Thread(userTask, "User-1").start();
        new Thread(userTask, "User-2").start();
        new Thread(technicianTask, "Technician").start();
    }
}
