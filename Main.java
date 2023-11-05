import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

public class Main {
    static final String TERMINATED = "terminated by time";
    static final String FAILED = "termination failed";

    static final String DIR_PATH = "inputFiles/";
//    static final String[] FILES={"medium/input2.txt","medium/input3.txt"};
    static final String[] FILES = {
            "tiny/input1.txt", "tiny/input2.txt","tiny/input3.txt",
        "small/input1.txt", "small/input2.txt","small/input3.txt",
        "medium/input1.txt","medium/input2.txt","medium/input3.txt"

            };
//    static final String[] FILES = {"small/input2.txt","small/input3.txt"};

    public static void main(String[] args) {
        for (String path : FILES) {
            checkPorts();
            try {
                System.out.println("starting file " + DIR_PATH + path);
                Scanner scannerInput = new Scanner(new File(DIR_PATH + path));
                int numNodes = Integer.parseInt(scannerInput.next());
                Manager m = new Manager();

                Thread startThread = new Thread(() -> startManager(m, DIR_PATH + path));
                Thread terminateThread = new Thread(() -> {
                    try {
                        Thread.sleep(numNodes * 5 * 1000L);
                        System.out.println(TERMINATED);
                        endManager(m, numNodes);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

                startThread.start();
                terminateThread.start();
                terminateThread.join();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static void startManager(Manager m, String path) {
        long startTime = System.currentTimeMillis();
        m.readInput(path);
        m.addShutdownHook();
        String coloringOutput = m.start();
        long endTime = System.currentTimeMillis();
        System.out.println(coloringOutput +"\nElapsed Time in milli seconds: "+ (endTime-startTime));
//        m.addShutdownHook();

    }

    private static void endManager(Manager m, int numNodes) throws InterruptedException {
        Thread terminateThread = new Thread(() -> System.out.println(m.terminate()));
        Thread failedThread = new Thread(() -> {
            try {
                Thread.sleep(numNodes * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(FAILED);
            checkPorts();
        });

        terminateThread.start();
        failedThread.start();
        failedThread.join();
    }

    private static void checkPorts() {
        int counter = 0;
        String host = "localhost"; // Replace with the desired host or IP address
        for (int port = 30000; port <= 50000; port++) {
            try (DatagramSocket serverSocket = new DatagramSocket(port)) {
                continue;
//                System.out.println("Port " + port + " is open: true");
            } catch (IOException e) {
                System.out.println("Port " + port + " is open: false");
                counter++;
            }
        }
        System.out.println(counter);

    }

}

