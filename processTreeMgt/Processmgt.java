/** 
 * Programming Assignment 1
 * Author: Esmond Chuah Hooi Ong, Nguyen Tuan Anh
 * ID: 1000925, 1001294
 * Date: 13/03/2016
 */


import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Processmgt {

    // keep track of the number of nodes which have finished executing
    public static int nodesFinishedCount = 0;


    public static void main(String[] args) {

        // name of text file which represents the structure of the graph
        String textFileName = args[0];

        // set Desktop as working directory
        File currentDirectory = new File(System.getProperty("user.home"), "Desktop");

        // create nodes with embedded information on graph relationships, based on the parsed text file
        ArrayList<Node> nodes = createNodes(textFileName);

        // execute nodes based on their "status"
        executeNodes(nodes, currentDirectory);
    }


    /**
     * Create "Node" objects with relevant attributes based on the parsed text file
     * @param fileName - name of text file which represents the structure of the graph
     * @return ArrayList of "Node" objects, each representing a valid user program
     */
    private static ArrayList<Node> createNodes(String fileName) {

        ArrayList<Node> nodes = new ArrayList<>();
        try {
            FileReader reader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;

            // number the nodes from 0 to (n-1) from the order the nodes appear in the text file
            // n = total number of nodes
            int id = 0;

            // reads the text file line by line and parse out information
            while ((line = bufferedReader.readLine()) != null) {
                String[] nodeProperties = line.split(":");
                ArrayList<Integer> childrenIDs = new ArrayList<>();

                // nodeProperties[1] represents the list of children
                for (String temp: nodeProperties[1].split(" ")) {
                    if (temp.equals("none")) {
                        childrenIDs = null;
                        break;
                    } else {
                        try {
                            childrenIDs.add(Integer.parseInt(temp));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid ID for child-node");
                        }
                    }
                }

                // instantiate a new "Node" object with attributes (in order): ID, program name with arguments, list of children IDs, input file, output file
                // add to the ArrayList of "Node" objects
                try {
                    nodes.add(new Node(id, nodeProperties[0], childrenIDs, nodeProperties[2], nodeProperties[3]));
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Unable to initialise node: some program information not specified in the text file \nProgram may not be properly executed");
                } catch (Exception e) {
                    System.out.println("Unable to initialise node: invalid node properties \nGraph sequence may be compromised");
                }
                id++;

            }
            reader.close();

        } catch (FileNotFoundException e) {
            System.out.println("No such file or directory found");
        } catch (IOException e) {
            System.out.println("IO Exception found!!");
            e.printStackTrace();
        }

        // add parent-node references to each node based on existing information on childrenIDs of each node
        try {
            for (Node node: nodes) {
                if (node.getChildrenIDs() != null) {
                    for (Integer childrenID: node.getChildrenIDs()) {
                        nodes.get(childrenID).addParent(node.getId());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Children not found. Could not add parent-node references");
        }


        // if node does not have a parent node i.e. the program is control and data-independent
        // set node's status as READY
        for (Node node: nodes) {
            if (node.getNumberOfParents() == 0) {
                node.setStatus(Node.READY);
            }
        }
        return nodes;
    }


    /**
     * Execute nodes based on their statuses
     * @param nodes - ArrayList of nodes in the 'process tree'
     * @param currentDirectory - working directory
     */
    private static void executeNodes(ArrayList<Node> nodes, File currentDirectory) {

        // initiate a temporary ArrayList of threads
        ArrayList<ProcessThread> threads = new ArrayList<>();

        // while there are still nodes which have not been executed
        while (nodesFinishedCount < nodes.size()) {

            // end and remove threads which have finished executing
            for (Iterator<ProcessThread> iterator = threads.iterator(); iterator.hasNext();) {
                ProcessThread thread = iterator.next();
                if (thread.getFlag()) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        System.out.println("Thread is interrupted.");
                    }
                    iterator.remove();
                }
            }

            // check for nodes which are ready to be executed
            // if all its parent nodes have finished executing
            for (Node node: nodes) {
                node.checkStatus();
            }

            // start a new thread for each node which is ready to be executed
            for (Node node: nodes) {
                if (node.getStatus() == Node.READY) {
                    System.out.println("Node " + node.getId() + " starts running ...");
                    ProcessThread newThread = new ProcessThread(node, nodes, currentDirectory);
                    newThread.start();

                    // add thread to the temporary list for tracking
                    threads.add(newThread);

                    // set node's status as RUNNING
                    node.setStatus(Node.RUNNING);
                }
            }
        }

        // all nodes have been executed
        System.out.println("COMPLETED!!!!!");
    }
}


/**
 * Create ProcessThread which is a subclass of Thread that overrides run() method
 * to start the process with the command (echo and cat), input files and output files 
 * as stated in the original text file via Node class objects and its properties.
 * Also get currentDirectory as input for file searching and file creation.
 */
class ProcessThread extends Thread {

    private ArrayList<Node> nodes;  // set a reference to the list of nodes
    private Node runningNode; 
    private File currentDirectory;
    private boolean flag = false;   // flag set to 'true' once thread has finished executing
    private ProcessBuilder pb;

    /**
     * Constructor of the class, taking in 3 parameters - the runningNode class object, the 
     * current file directory and the list of nodes
     *
     * @param runningNode - the current Node object that has running status
     * @param currentDirectory - the current directory where files are created and read
     * @param nodes - the list of nodes reference
     */
    public ProcessThread(Node runningNode, ArrayList<Node> nodes, File currentDirectory) {
        this.runningNode = runningNode;
        this.nodes = nodes;
        this.currentDirectory = currentDirectory;
    }

    public void run() {
        try {
            // create ProcessBuilder for "echo" command
            if (runningNode.getCommand().contains("echo")) {
                pb = new ProcessBuilder("/bin/bash","-c",runningNode.getCommand());

            // create ProcessBuilder for "cat" command
            }  else if (runningNode.getCommand().contains("cat")) {
                String[] catfiles = runningNode.getCommand().split(" ");
                pb = new ProcessBuilder(catfiles);
            }

            // set working directory
            pb.directory(currentDirectory);

            // find input file and redirect it as input to handle case if input of node is not "stdin"
            if (!runningNode.getInput().equalsIgnoreCase("stdin")) {
                File in = new File(currentDirectory.getAbsolutePath() + "/" + runningNode.getInput());
                pb.redirectInput(in);
            }

            // creates output file and redirect output to it to handle case if output of node is not "stdout"
            if (!runningNode.getOutput().equalsIgnoreCase("stdout")) {
                File out = new File(currentDirectory.getAbsolutePath() + "/" + runningNode.getOutput());
                pb.redirectOutput(out);
            }

            // start process
            Process p = pb.start();
            p.waitFor();

            // if program runs successfully
            System.out.println("Node " + runningNode.getId() + " done.");

            // to ensure thread safety for race conditions
            synchronized (this) {

                // set node's status as finished
                runningNode.setStatus(Node.FINISHED);

                // notify all its child nodes that it has finished executing
                if (runningNode.getChildrenIDs() != null) {
                    for (Integer childrenID: runningNode.getChildrenIDs()) {
                        nodes.get(childrenID).incrementNumberOfParentsDone();
                    }
                }

                // increment number of nodes which have finished executing
                Processmgt.nodesFinishedCount++;
                System.out.println("Number of nodes finished: " + Processmgt.nodesFinishedCount);

                // signify that thread has finished executing
                flag = true;
            }

        } catch (Exception ex) {
            System.out.println("Program did not run successfully. This may impede other nodes from executing.");
        }
    }

    /*** GETTER METHODS ***/

    public Node getRunningNode() {
        return runningNode;
    }

    public boolean getFlag() {
        return flag;
    }
}


/**
 * This class simulates a node in a graph.
 * It contains relevant information regarding its relationship with other nodes such as parent nodes, child nodes.
 * It also contains program information such as the command, input file, output file and most importantly, its current status.
 */
class Node {

    // constants
    public static final int INELIGIBLE = 0;
    public static final int READY = 1;
    public static final int RUNNING = 2;
    public static final int FINISHED = 3;

    // attributes
    private int id;
    private String command; // program name with arguments
    private String input;   // input file
    private String output;  // output file

    private ArrayList<Integer> parentIDs;   // ID of parent nodes
    private ArrayList<Integer> childrenIDs; // ID of children nodes

    private int numberOfParentsDone = 0;    // number of parent nodes done executing

    private int status = 0; // 0: INELIGIBLE; 1: READY; 2: RUNNING; 3: FINISHED

    /*** CONSTRUCTOR ***/
    public Node(int id, String command, ArrayList<Integer> childrenIDs, String input, String output) {
        this.id = id;
        this.command = command;
        this.input = input;
        this.output = output;
        this.childrenIDs = childrenIDs;
        this.parentIDs = new ArrayList<>();
    }

    /**
     * Check whether this node is ready to be executed
     * Set node's status as READY if all parent nodes have finished executing
     */
    public void checkStatus() {
        if (numberOfParentsDone == parentIDs.size() && status == INELIGIBLE) {
            setStatus(READY);
        }
    }

    /**
     * Add parent-node ID to ArrayList of parent-node IDs
     * @param parentID - ID of parent-node
     */
    public void addParent(int parentID) {
        parentIDs.add(parentID);
    }

    /**
     * Increment the number of parent nodes which have finished executing
     */
    public void incrementNumberOfParentsDone() {
        numberOfParentsDone++;
    }

    /*** GETTER METHODS ***/
    public int getId() {
        return id;
    }

    public String getCommand() {
        return command;
    }

    public int getNumberOfParents() {
        return parentIDs.size();
    }

    public ArrayList<Integer> getChildrenIDs() {
        return childrenIDs;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public int getStatus() {
        return status;
    }

    /*** SETTER METHODS ***/
    public void setStatus(int status) {
        this.status = status;
    }
}