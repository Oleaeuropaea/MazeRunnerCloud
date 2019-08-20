package pt.ulisboa.tecnico.meic.cnv.mazerunner.instrumentation;



import BIT.highBIT.*;
import pt.ulisboa.tecnico.meic.cnv.mazerunner.mazerunnercloud.webserver.WebServerMain;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class InstrumentMaze {


    public static class Metrics {
        public long branches_taken;
        public String inputs;

        public Metrics( long branches_taken) {
            this.branches_taken = branches_taken;
        }
    }

   private static HashMap<Long, Metrics> metricsPerThread = new HashMap<Long, Metrics>();

   public static void main(String[] args) {
        ArrayList<String> files = new ArrayList<String>();

        extractFileNames(args, files);

        for (String infilename : files) {
            if (infilename.endsWith(".class")) {
                System.out.println("Instrumenting File:"+infilename);


                ClassInfo ci = new ClassInfo(infilename);
                Vector routines = ci.getRoutines();

                // loop through all input class routines
                for (Enumeration e = routines.elements(); e.hasMoreElements(); ) {
                    int instr_count = 0;
                    Routine routine = (Routine) e.nextElement();
                    Instruction[] instructions = routine.getInstructions();

                    // System.out.println(routine.getMethodName());

                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        Instruction instr = (Instruction) instructions[bb.getEndAddress()];
                        short instr_type = InstructionTable.InstructionTypeTable[instr.getOpcode()];
                        String instr_name = InstructionTable.OpcodeName[instr.getOpcode()];

                        if (instr_name == "goto") {
                            // System.out.println(instr_name);
                            instr.addBefore("pt/ulisboa/tecnico/meic/cnv/mazerunner/instrumentation/InstrumentMaze",
                                            "countBranches", new Integer(1));
                        }
                    }
                }

                 ci.addAfter("pt/ulisboa/tecnico/meic/cnv/mazerunner/instrumentation/InstrumentMaze", "writeMetricToDynamo", ci.getClassName());

                ci.write(infilename);
            }
        }
    }


    private static void extractFileNames(String[] directories, ArrayList<String> files) {
        for (String dirName : directories) {    //For each directory in the arguments

            if (Files.isDirectory(Paths.get(dirName))) {

                File dir = new File(dirName);
                for (String file : dir.list()) {

                    if (!Files.isDirectory(Paths.get(file))) {
                        if (!dirName.equals(".")) {
                            files.add(dir + System.getProperty("file.separator") + file);
                        } else {
                            files.add(file);
                        }
                    }
                }
            }
        }
    }

    public static synchronized void init(String foo) {
        //initDataBase();
    }

    public static synchronized void insertInputByThread(String constructorArgs) {
        metricsPerThread.put(new Long(Thread.currentThread().getId()), new Metrics(0));
        Long threadID = new Long(Thread.currentThread().getId());
        Metrics aux = metricsPerThread.get(threadID);
        aux.inputs = constructorArgs;
    }

    public static synchronized void countBranches(int incr) {
        Metrics aux = metricsPerThread.get(new Long(Thread.currentThread().getId()));

        aux.branches_taken += 1;
        metricsPerThread.put(new Long(Thread.currentThread().getId()), aux);
    }

    public static synchronized void writeMetricToFileCSV(String notUsed) {
        Long threadID = new Long(Thread.currentThread().getId());
        Metrics aux = metricsPerThread.get(threadID);
        String stat_line = threadID + "," + aux.inputs + "," + aux.branches_taken + "\n";
        String abs_path = System.getProperty("user.dir") + File.separator + "stats.csv";

        try {
            Files.write(Paths.get(abs_path), stat_line.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Th  freadID , Inputs, Branches Taken\n" + stat_line);

    }
    public static synchronized void writeMetricToDynamo(String notUsed) {
         Long threadID = new Long(Thread.currentThread().getId());
         Metrics aux = metricsPerThread.get(threadID);
         String stat_line = threadID + "," + aux.inputs + "," + aux.branches_taken + "\n";
         List<String> elements = Arrays.asList(aux.inputs.split(","));

             WebServerMain.awsDynamoDatabase.addRow(elements.get(0),Integer.parseInt(elements.get(1)),Integer.parseInt(elements.get(2)),
                     Integer.parseInt(elements.get(3)),Integer.parseInt(elements.get(4)),Integer.parseInt(elements.get(5)),
                     elements.get(6),aux.branches_taken);

         System.out.println("ThreadID , Inputs, Instructions Count \n" + stat_line);

     }

}
