import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Node implements Runnable {
    private int id;
    private int color;
    private int numNodes;
    private int maxDeg;
    private List<int[]> higherIdNeighbors;
    private List<int[]> lowerIdNeighbors;
    private Set<Integer> neighborsColors;
    public List<DatagramSocket> SocketList;


    public Node(int id, int numNodes, int maxDeg, int[][] neighbors) {
        this.id = id;
        this.color = id;
        this.numNodes = numNodes;
        this.maxDeg = maxDeg;
        this.higherIdNeighbors = new ArrayList<>();
        this.lowerIdNeighbors = new ArrayList<>();
        this.neighborsColors = new HashSet<>();
        this.SocketList =  Collections.synchronizedList(new ArrayList<>());


        for (int[] neighbor : neighbors) {
            if (neighbor[0] > this.id) {
                this.higherIdNeighbors.add(neighbor);
            } else {
                this.lowerIdNeighbors.add(neighbor);
            }
        }
    }

    public void run() {
        if (this.higherIdNeighbors.isEmpty()) {
            this.color = 0;
            for (int[] neighbor : this.lowerIdNeighbors) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                send_message(this.color, neighbor[1]);
            }
//            System.out.println(this.id + " got colored");
        } else {
            receive_messages();
            setMinimalNonConflictingColor();
//            System.out.println(this.id + " got colored");

            for (int[] neighbor : this.lowerIdNeighbors) {
                send_message(this.color, neighbor[1]);
            }
        }
    }

    public void receive_messages() {
        List<Thread> receivers = new ArrayList<>();

        for (int[] neighbor : this.higherIdNeighbors) {
            int receiverPort = neighbor[2];
            Thread thread = new Thread(() -> receive_message(receiverPort));
            receivers.add(thread);
            thread.start();
        }

        for (Thread thread : receivers) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    private void receive_message(int receiverPort) {
        DatagramSocket socket = null;
        try {
            int bufferSize = 65535;
            byte[] buffer = new byte[bufferSize];

            socket = new DatagramSocket(null); // pass null here
            this.SocketList.add(socket);
            socket.setReuseAddress(true); // enable address reuse
            socket.bind(new InetSocketAddress(receiverPort)); // bind the socket manually

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer);
            ObjectInputStream objectStream = new ObjectInputStream(byteStream);
            int receivedNumber = (int) objectStream.readObject();
            objectStream.close();
            this.neighborsColors.add(receivedNumber);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed())   {
                socket.close();
            }
        }
    }

    private void send_message(int message, int receiverPort) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(message);
            objectStream.flush();
            objectStream.close();//test
            byte[] buffer = byteStream.toByteArray();

            InetAddress receiverAddress = InetAddress.getByName("localhost");

            try (DatagramSocket socket = new DatagramSocket()) {
                this.SocketList.add(socket);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiverAddress, receiverPort);
                socket.send(packet);
                byteStream.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void closeSockets() {
        for (DatagramSocket socket : SocketList) {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }



    public int getColor() {
        return color;
    }

    private void setColor(int color) {
        this.color = color;
    }

    private void setMinimalNonConflictingColor() {
        int minimalColor = 0;
        while (this.neighborsColors.contains(minimalColor)) {
            minimalColor++;
        }
        this.color = minimalColor;
    }

    public int getNodeId() {
        return id;
    }


}