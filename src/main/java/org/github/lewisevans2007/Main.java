/**
 * Linux FS Generator (LFSG)
 * Generate a Linux file system for containerized applications or testing.
 * GitHub: https://wwww.github.com/lewisevans2007/Linux_FS_Generator
 * Licence: Gnu General Public License v3.0
 * By: Lewis Evans
 */

package org.github.lewisevans2007;

import java.util.*;
import java.io.*;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.Date;
import java.util.logging.SimpleFormatter;
import java.util.logging.LogRecord;
import java.io.IOException;
import java.util.logging.FileHandler;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static void deleteDirectory(File file) {
        LOGGER.info("Deleting directory: " + file);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    deleteDirectory(f);
                }
            }
            file.delete();
        } else {
            file.delete();
        }
    }

    public static void createDirectory(String directory) {
        LOGGER.info("Creating directory: " + directory);
        File dir = new File(directory);
        dir.mkdir();
    }

    public static void main(String[] args) throws IOException {
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }
        String LoggerSilenceType = "";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-s")) {
                LOGGER.setLevel(Level.SEVERE);
                LoggerSilenceType = "Silent";
            } else if (args[i].equals("-v")) {
                LOGGER.setLevel(Level.ALL);
                LoggerSilenceType = "Verbose";
            } else {
                LOGGER.setLevel(Level.WARNING);
                LoggerSilenceType = "Normal";
            }
        }

        // Create a custom formatter
        SimpleFormatter formatter = new SimpleFormatter() {
            private final String format = "[%1$-7s] - [%2$tb %2$td, %2$tY %2$tl:%2$tM:%2$tS %2$Tp] %3$s %n";

            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(format, lr.getLevel(), new Date(lr.getMillis()), lr.getMessage());
            }
        };

        // Create a console handler
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(formatter);

        // Create a file handler
        FileHandler fileHandler = new FileHandler("LFSG.log");
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(formatter);

        // Add handlers to the logger
        LOGGER.addHandler(consoleHandler);
        LOGGER.addHandler(fileHandler);
        LOGGER.info("Starting Linux FS Generator (LFSG)");
        if (LoggerSilenceType.equals("Silent")) {
            LOGGER.info("Logger mode: Silent");
        } else if (LoggerSilenceType.equals("Verbose")) {
            LOGGER.info("Logger mode: Verbose");
        } else {
            LOGGER.info("Logger mode: Normal");
        }

        LOGGER.info("Generating Linux Root File System");
        List<String> rootDirectories = new ArrayList<String>();
        rootDirectories.add("bin");
        rootDirectories.add("sbin");
        rootDirectories.add("boot");
        rootDirectories.add("dev");
        rootDirectories.add("etc");
        rootDirectories.add("home");
        rootDirectories.add("lib");
        rootDirectories.add("lib64");
        rootDirectories.add("media");
        rootDirectories.add("mnt");
        rootDirectories.add("opt");
        rootDirectories.add("proc");
        rootDirectories.add("root");
        rootDirectories.add("usr");
        rootDirectories.add("usr/bin");
        rootDirectories.add("usr/include");
        rootDirectories.add("usr/lib");
        rootDirectories.add("usr/lib64");
        rootDirectories.add("usr/local");
        rootDirectories.add("usr/local/bin");
        rootDirectories.add("usr/local/include");
        rootDirectories.add("usr/local/lib");
        rootDirectories.add("usr/local/lib64");
        LOGGER.info("Done generating Linux Root File System");

        List<String> users = new ArrayList<String>();
        List<String> ignoreDirectories = new ArrayList<String>();
        String output = "";
        LOGGER.info("Parsing arguments");
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-u")) {
                users = Arrays.asList(args[i + 1].split(","));
                LOGGER.info("Found users arguments: " + users);
            } else if (args[i].equals("-o")) {
                output = args[i + 1];
                LOGGER.info("Found output argument: " + output);
            }
        }

        if (users.size() == 0) {
            LOGGER.severe("No users specified. Exiting.");
            System.exit(0);
        }
        if (output.equals("")) {
            LOGGER.severe("No output specified. Exiting.");
            System.exit(0);
        }


        createDirectory("LFSG_Temp");
        for (String rootDirectory : rootDirectories) {
            if (rootDirectory.contains("/")) {
                String[] rootDirectorySplit = rootDirectory.split("/");
                String rootDirectoryParent = rootDirectorySplit[0];
                String rootDirectoryChild = rootDirectorySplit[1];
                createDirectory("LFSG_Temp/" + rootDirectoryParent + "/" + rootDirectoryChild);
            } else {
                createDirectory("LFSG_Temp/" + rootDirectory);
            }
        }
        for (String user : users) {
            if (ignoreDirectories.contains("home")) {
                continue;
            } else {
                createDirectory("LFSG_Temp/home/" + user);
            }
        }

        LOGGER.info("Compressing to output: " + output);
        try {
            FileOutputStream fos = new FileOutputStream(output);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
            TarArchiveOutputStream taos = new TarArchiveOutputStream(gzos);
            taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            for (String rootDirectory : rootDirectories) {
                if (ignoreDirectories.contains(rootDirectory)) {
                    continue;
                } else {
                    if (rootDirectory.contains("/")) {
                        LOGGER.info("Adding directory: " + rootDirectory);
                        String[] rootDirectorySplit = rootDirectory.split("/");
                        String rootDirectoryParent = rootDirectorySplit[0];
                        String rootDirectoryChild = rootDirectorySplit[1];
                        File dir = new File("LFSG_Temp/" + rootDirectoryParent + "/" + rootDirectoryChild);
                        TarArchiveEntry entry = new TarArchiveEntry(dir, rootDirectoryParent + "/" + rootDirectoryChild);
                        taos.putArchiveEntry(entry);
                        taos.closeArchiveEntry();
                    } else {
                        LOGGER.info("Adding directory: " + rootDirectory);
                        File dir = new File("LFSG_Temp/" + rootDirectory);
                        TarArchiveEntry entry = new TarArchiveEntry(dir, rootDirectory);
                        taos.putArchiveEntry(entry);
                        taos.closeArchiveEntry();
                    }
                }
            }
            for (String user : users) {
                LOGGER.info("Adding directory: home/" + user);
                if (ignoreDirectories.contains("home")) {
                    continue;
                } else {
                    File dir = new File("LFSG_Temp/home/" + user);
                    TarArchiveEntry entry = new TarArchiveEntry(dir, "home/" + user);
                    taos.putArchiveEntry(entry);
                    taos.closeArchiveEntry();
                }
            }
            taos.finish();
            taos.close();
            gzos.close();
            bos.close();
            fos.close();
            LOGGER.info("Done compressing to output: " + output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("Deleting temporary directory");
        File tempdir = new File("LFSG_Temp");
        deleteDirectory(tempdir);
        LOGGER.info("Done deleting temporary directory");
        LOGGER.info("Process complete");
    }
}