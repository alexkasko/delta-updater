package ru.concerteza.delta.diff;

import org.apache.commons.cli.*;

import java.io.File;

import static java.lang.System.out;

/**
 * User: alexey
 * Date: 11/18/11
 */
public class Launcher {

    private static final String HELP_OPTION = "help";
    private static final String OUTPUT_OPTION = "out";

    public static void main(String[] args) throws Exception {
//        System.console().readLine();
       Options options = new Options();
        try {
            options.addOption("h", HELP_OPTION, false, "show this page");
            options.addOption("o", OUTPUT_OPTION, true, "output file path");
            CommandLine cline = new GnuParser().parse(options, args);
            String[] argList = cline.getArgs();
            if (cline.hasOption(HELP_OPTION)) {
                throw new ParseException("Printing help page:");
            } else if(2 == argList.length && cline.hasOption(OUTPUT_OPTION)) {
                new DirDeltaCreator().create(new File(argList[0]), new File(argList[1]), new File(cline.getOptionValue(OUTPUT_OPTION)));
            } else {
                throw new ParseException("Incorrect arguments received!");
            }
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            out.println(e.getMessage());
            formatter.printHelp("delta-diff dir1 dir2 -o out.zip", options);
        }
    }
}
