package CSElabs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.*;

public class FileOperation {
    private static File currentDirectory = new File(System.getProperty("user.dir"));
    // private static File currentDirectory = new File("C:\\Users\\Esmond\\Desktop\\lab4test");
    
    public static void main(String[] args) throws java.io.IOException {

        String commandLine;

        BufferedReader console = new BufferedReader
                (new InputStreamReader(System.in));

        while (true) {
            // read what the user entered
            System.out.print("jsh>");
            commandLine = console.readLine();

            // clear the space before and after the command line
            commandLine = commandLine.trim();

            // if the user entered a return, just loop again
            if (commandLine.equals("")) {
                continue;
            }
            // if exit or quit
            else if (commandLine.equalsIgnoreCase("exit") | commandLine.equalsIgnoreCase("quit")) {
                System.exit(0);
            }

            // check the command line, separate the words
            String[] commandStr = commandLine.split(" ");
            ArrayList<String> command = new ArrayList<String>();
            for (int i = 0; i < commandStr.length; i++) {
                command.add(commandStr[i]);
            }

            // TODO: implement code to handle create here
            if (command.get(0).equals("create")) {
                Java_create(currentDirectory, command.get(1));
            }

            // TODO: implement code to handle delete here
            else if (command.get(0).equals("delete")) {
                Java_delete(currentDirectory, command.get(1));
            }

            // TODO: implement code to handle display here
            else if (command.get(0).equals("display")) {
                Java_cat(currentDirectory, command.get(1));
            }

            // TODO: implement code to handle list here
            else if (command.get(0).equals("list")) {
                if (command.size() == 1) {
                    Java_ls(currentDirectory, "", "");
                } else if (command.size() == 2) {
                    Java_ls(currentDirectory, command.get(1), "");
                } else if (command.size() == 3) {
                    Java_ls(currentDirectory, command.get(1), command.get(2));
                }
            }

            // TODO: implement code to handle find here
            else if (command.get(0).equals("find")) {
                if(!Java_find(currentDirectory, command.get(1))) {
                    System.out.println("File not found");
                }
            }

            // TODO: implement code to handle tree here
            else if (command.get(0).equals("tree")) {
                if (command.size() == 1) {
                    Java_tree(currentDirectory, getDepthOfTree(currentDirectory), "", 0);
                } else if (command.size() == 2) {
                    Java_tree(currentDirectory, Integer.parseInt(command.get(1)), "", 0);
                } else if (command.size() == 3) {
                    Java_tree(currentDirectory, Integer.parseInt(command.get(1)), command.get(2), 0);
                }
            }

            // other commands
            ProcessBuilder pBuilder = new ProcessBuilder(command);
            pBuilder.directory(currentDirectory);
            try{
                Process process = pBuilder.start();
                // obtain the input stream
                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                // read what is returned by the command
                String line;
                while ( (line = br.readLine()) != null)
                    System.out.println(line);

                // close BufferedReader
                br.close();
            }
            // catch the IOexception and resume waiting for commands
            catch (IOException ex){
//                System.out.println(ex);
                continue;
            }
        }
    }

    /**
     * Create a file
     * @param dir - current working directory
     * @param name - name of the file to be created
     */
    public static void Java_create(File dir, String name) {
        // TODO: create a file
        File file = new File(dir, name);
        boolean check;
        try {
            check = file.createNewFile();
            if (!check) {
                System.out.println("File creation was not successful.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a file
     * @param dir - current working directory
     * @param name - name of the file to be deleted
     */
    public static void Java_delete(File dir, String name) {
        // TODO: delete a file
        File file = new File(dir, name);
        boolean check = file.delete();
        if (!check) {
            System.out.println("File deletion was not successful.");
        }
    }

    /**
     * Display the file
     * @param dir - current working directory
     * @param name - name of the file to be displayed
     */
    public static void Java_cat(File dir, String name) {
        File file = new File(dir, name);
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader in = new BufferedReader(fileReader);
            String line;

            while((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to sort the file list
     * @param list - file list to be sorted
     * @param sort_method - control the sort type
     * @return sorted list - the sorted file list
     */
    private static File[] sortFileList(File[] list, String sort_method) {
        // sort the file list based on sort_method
        // if sort based on name
        if (sort_method.equals("")){
            return list;
        }
        else if (sort_method.equalsIgnoreCase("name")) {
            Arrays.sort(list, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return (f1.getName()).compareTo(f2.getName());
                }
            });
        }
        else if (sort_method.equalsIgnoreCase("size")) {
            Arrays.sort(list, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f1.length()).compareTo(f2.length());
                }
            });
        }
        else if (sort_method.equalsIgnoreCase("time")) {
            Arrays.sort(list, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });
        }
        return list;
    }

    /**
     * List the files under directory
     * @param dir - current directory
     * @param display_method - control the list type
     * @param sort_method - control the sort type
     */
    public static void Java_ls(File dir, String display_method, String sort_method) {
        // TODO: list files
        File[] list = dir.listFiles();

        if (display_method.equals("")) {
            for (File file: list) {
                System.out.println(file.getName());
            }
        } else if (display_method.equals("property")) {
            list = sortFileList(list, sort_method);
            for (File file: list) {
                String name = String.format("%1$-20s", file.getName());
                String size = String.format("Size: %1$-10s", String.valueOf(file.length()));
                String date = String.format("Last Modified: %1$-30s", String.valueOf(new Date(file.lastModified())));
                System.out.println(name + size + date);
            }
        }
    }

    /**
     * Find files based on input string
     * @param dir - current working directory
     * @param name - input string to find in file's name
     * @return flag - whether the input string is found in this directory and its subdirectories
     */
    public static boolean Java_find(File dir, String name) {
        boolean flag = false;
        // TODO: find files
        File[] list = dir.listFiles();
        for (File file: list) {
            if (file.isDirectory()) {
                boolean placeholder = Java_find(file, name);
                if (!flag) {
                    flag = placeholder;
                }
            } else {
                if (file.getName().contains(name)) {
                    System.out.println(file.getAbsolutePath());
                    flag = true;
                }
            }
        }
        return flag;
    }

    /**
     * Print file structure under current directory in a tree structure
     * @param dir - current working directory
     * @param depth - maximum sub-level file to be displayed
     * @param sort_method - control the sort type
     * @param count - depth of tree structure
     */
    public static void Java_tree(File dir, int depth, String sort_method, int count) {
        // TODO: print file tree
        File[] list = sortFileList(dir.listFiles(), sort_method);
        depth--;
        for (File file: list) {
            if (count == 0) {
                System.out.println(file.getName());
            } else {
                String formatting = "|-" + file.getName();
                for (int i = 0; i < count; i++) { formatting = "  " + formatting; }
                System.out.println(formatting);
            }
            if (depth > 0 && file.isDirectory()) {
                Java_tree(file, depth, sort_method, count+1);
            }
        }
    }


    // TODO: define other functions if necessary for the above functions
    /**
     * Find the maximum depth of a directory
     * @param dir - current working directory
     * @return depthOfTree
     */
    public static int getDepthOfTree(File dir) {
        File[] list = dir.listFiles();
        ArrayList<File> subdirectories = new ArrayList<>();
        for (File file: list) {
            if (file.isDirectory()) {
                subdirectories.add(file);
            }
        }
        if (subdirectories.size() == 0) {
            return 1;
        } else {
            ArrayList<Integer> maxDepth = new ArrayList<>();
            for (File file: subdirectories) {
                maxDepth.add(getDepthOfTree(file));
            }
            return 1 + Collections.max(maxDepth);
        }
    }
}