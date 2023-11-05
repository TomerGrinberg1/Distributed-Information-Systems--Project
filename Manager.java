
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Manager {
    private List<Node> nodes;
    private int numNodes;
    private int maxDegree;
    public int numFinished;
    public boolean finish;
    public Manager() {
        this.nodes = new ArrayList<>();
        this.numFinished=0;
        this.finish=false;

    }

    public void readInput(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            this.numNodes = Integer.parseInt(reader.readLine());
            this.maxDegree = Integer.parseInt(reader.readLine());
            String input;

            while ((input = reader.readLine()) != null) {
                int nodeId = Integer.parseInt(input.substring(0, input.indexOf(" ")));
                String neighborsStr = input.substring(input.indexOf("[[") + 2, input.lastIndexOf("]]"));
                String[] neighborParts = neighborsStr.split("], \\[");
                int[][] neighborArray = new int[neighborParts.length][3];

                for (int i = 0; i < neighborParts.length; i++) {
                    String[] neighborValues = neighborParts[i].split(", ");
                    for (int j = 0; j < 3; j++) {
                        neighborArray[i][j] = Integer.parseInt(neighborValues[j]);
                    }
                }

                Node node = new Node(nodeId, numNodes, maxDegree, neighborArray);
                this.nodes.add(node);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String start() {
        List<Thread> threads = new ArrayList<>();
        for (Node node : nodes) {
            Thread thread = new Thread(node);
            threads.add(thread);
            thread.start();
//            node.closeSockets();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        numFinished++;
        if (numFinished==numNodes){
            this.finish=true;

        }



        StringBuilder result = new StringBuilder();
        for (Node node : nodes) {
            result.append(node.getNodeId()).append(",").append(node.getColor()).append("\n");
        }

        return result.toString();
    }
    public void closeAllSockets() {
        for (Node node : nodes) {
            node.closeSockets();
        }
    }
    public void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeAllSockets));
    }
    public String terminate() {
        closeAllSockets();
        StringBuilder result = new StringBuilder();
        if (!nodes.isEmpty()) {
            for (Node node : nodes) {
                result.append(node.getNodeId()).append(",").append(node.getColor()).append("\n");
            }
        } else {
            result.append("The algorithm has already finished and returned a legal coloring of the graph");
        }
        return result.toString();
    }
}