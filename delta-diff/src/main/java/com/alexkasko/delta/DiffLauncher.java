package com.alexkasko.delta;

import org.apache.commons.cli.*;
import org.apache.commons.io.IOCase;

import java.io.File;

import static java.lang.System.out;

/**
 * Delta diff launcher class
 *
 * @author alexkasko
 * Date: 11/18/11
 */
public class DiffLauncher {
    private static final String HELP_OPTION = "help";
    private static final String OUTPUT_OPTION = "out";
    private static final String CASE_SENSITIVE_OPTION = "case-sensitive";

    /**
     * app entry point
     */
    public static void main(String[] args) throws Exception {
       Options options = new Options();
        try {
            options.addOption("h", HELP_OPTION, false, "show this page");
            options.addOption("o", OUTPUT_OPTION, true, "output file path");
            options.addOption("c", CASE_SENSITIVE_OPTION, true, "case sensitive [y/n]");
            CommandLine cline = new GnuParser().parse(options, args);
            String[] argList = cline.getArgs();
            if (cline.hasOption(HELP_OPTION)) {
                throw new ParseException("Printing help page:");
            } else if(2 == argList.length && cline.hasOption(OUTPUT_OPTION)) {
                final IOCase caseSensitive;
                if (cline.hasOption(CASE_SENSITIVE_OPTION)) {
                    String val = cline.getOptionValue(CASE_SENSITIVE_OPTION);
                    if ("y".equalsIgnoreCase(val)) {
                        caseSensitive = IOCase.SENSITIVE;
                    } else if ("n".equalsIgnoreCase(val)) {
                        caseSensitive = IOCase.INSENSITIVE;
                    } else {
                        throw new ParseException("Invalid case arg: [" + val + "], should be [y] or [n]");
                    }
                } else caseSensitive = IOCase.SYSTEM;
                new DirDeltaCreator().create(new File(argList[0]), new File(argList[1]), new File(cline.getOptionValue(OUTPUT_OPTION)), caseSensitive);
            } else {
                throw new ParseException("Incorrect arguments received!");
            }
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            out.println(e.getMessage());
            formatter.printHelp("java -jar delta-diff.jar [-c y/n] dir1 dir2 -o out.zip", options);
        }
    }
}
