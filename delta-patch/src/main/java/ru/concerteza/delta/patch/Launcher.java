package ru.concerteza.delta.patch;

import org.apache.commons.cli.*;

import java.io.File;

import static java.lang.System.out;

/**
 * User: alexey
 * Date: 11/19/11
 */
public class Launcher {

    private static final String HELP_OPTION = "help";

    public static void main(String[] args) throws Exception {
//        System.console().readLine();
       Options options = new Options();
        try {
            options.addOption("h", HELP_OPTION, false, "show this page");
            CommandLine cline = new GnuParser().parse(options, args);
            String[] argList = cline.getArgs();
            if (cline.hasOption(HELP_OPTION)) {
                throw new ParseException("Printing help page:");
            } else if(2 == argList.length) {
                new DirDeltaPatcher().patch(new File(argList[0]), new File(argList[1]));
            } else {
                throw new ParseException("Incorrect arguments received!");
            }
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            out.println(e.getMessage());
            formatter.printHelp("delta-patch dir patch.zip", options);
        }
    }
}
