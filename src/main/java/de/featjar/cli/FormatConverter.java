/*
 * Copyright (C) 2022 Elias Kuiter
 *
 * This file is part of cli.
 *
 * cli is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * cli is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with cli. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-cli> for further information.
 */
package de.featjar.cli;

import de.featjar.base.Feat;
import de.featjar.base.FeatJAR;
import de.featjar.base.cli.CLIArgumentParser;
import de.featjar.base.data.Computation;
import de.featjar.base.log.IndentFormatter;
import de.featjar.base.log.IndentStringBuilder;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.structure.formula.Formula;
import de.featjar.base.cli.CommandLine;
import de.featjar.base.cli.Command;
import de.featjar.base.data.Result;
import de.featjar.base.io.IOObject;
import de.featjar.base.io.format.Format;
import de.featjar.formula.transformer.ToCNF;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Command line interface for sampling algorithms.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class FormatConverter implements Command {
    @Override
    public String getDescription() {
        return "Converts feature models between various formats";
    }

    private static List<Format<Formula>> getFormats() {
        return Feat.extensionPoint(FormulaFormats.class).getExtensions();
    }

    @Override
    public void run(CLIArgumentParser argumentParser) {
        String input = CommandLine.SYSTEM_INPUT;
        String output = CommandLine.SYSTEM_OUTPUT;
        Format<Formula> outFormat = null;
        boolean recursive = false;
        boolean dryRun = false;
        boolean cnf = false;
        String fileNameFilter = null;
        String verbosity = CommandLine.DEFAULT_MAXIMUM_VERBOSITY;

        List<String> args = new ArrayList<>(); //todo

        for (final ListIterator<String> iterator = args.listIterator(); iterator.hasNext(); ) {
            final String arg = iterator.next();
            switch (arg) {
                case "-f": {
                    final String name = CommandLine.getArgValue(iterator, arg).toLowerCase();
                    outFormat = getFormats().stream()
                            .filter(f -> Objects.equals(name, f.getName().toLowerCase()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Unknown format: " + name));
                    break;
                }
                case "-o": {
                    output = CommandLine.getArgValue(iterator, arg);
                    break;
                }
                case "-i": {
                    input = CommandLine.getArgValue(iterator, arg);
                    break;
                }
                case "-r": {
                    recursive = true;
                    break;
                }
                case "-name": {
                    fileNameFilter = CommandLine.getArgValue(iterator, arg);
                    break;
                }
                case "-v": {
                    verbosity = CommandLine.getArgValue(iterator, arg);
                    break;
                }
                case "-dry": {
                    dryRun = true;
                    break;
                }
                case "-cnf": {
                    cnf = true;
                    break;
                }
            }
        }

        String finalVerbosity = verbosity;
        FeatJAR featJAR = new FeatJAR(new FeatJAR.Configuration().log(cfg -> cfg.logAtMost(finalVerbosity)));

        if (outFormat == null) {
            throw new IllegalArgumentException("No output format specified!");
        }
        if (!CommandLine.isValidInput(input)) {
            throw new IllegalArgumentException("No input directory or file does not exist!");
        }
        final boolean directory = Files.isDirectory(Paths.get(input));

        if (directory && !Files.exists(Paths.get(output))) {
            try {
                Files.createDirectory(Paths.get(output));
            } catch (final IOException e) {
                throw new IllegalArgumentException("Output directory could not be created!");
            }
        }

        final boolean convert = !dryRun;
        if (directory) {
            final Format<Formula> format = outFormat;
            final Path rootIn = Paths.get(input);
            final Path rootOut = Paths.get(output);
            final Predicate<String> fileNamePredicate = fileNameFilter == null
                    ? (s -> true)
                    : Pattern.compile(fileNameFilter).asMatchPredicate();
            try {
                final Stream<Path> fileStream = recursive ? Files.walk(rootIn) : Files.list(rootIn);
                boolean finalCnf = cnf;
                fileStream //
                        .filter(Files::isRegularFile) //
                        .filter(f -> fileNamePredicate.test(f.getFileName().toString())) //
                        .forEach(inputFile -> {
                            final Path outputDirectory = rootOut.resolve(rootIn.relativize(inputFile.getParent()));
                            final Path outputFile = outputDirectory.resolve(
                                    IOObject.getFileNameWithoutExtension(inputFile.getFileName()) + "."
                                            + format.getFileExtension());
                            Feat.log().info(inputFile + " -> " + outputFile);
                            if (convert) {
                                try {
                                    Files.createDirectories(outputDirectory);
                                } catch (final IOException e) {
                                    throw new RuntimeException(e);
                                }
                                convert(inputFile.toString(), outputFile.toString(), format, finalCnf);
                            }
                        });
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            Feat.log().info(input + " -> " + output);
            if (convert) {
                convert(input, output, outFormat, cnf);
            }
        }

        featJAR.close();
    }

    private void convert(String inputFile, String outputFile, Format<Formula> outFormat, boolean cnf) {
        try {
            final Result<Formula> parse = CommandLine.loadFile(inputFile, Feat.extensionPoint(FormulaFormats.class));
            if (parse.isPresent()) {
                Formula expression = parse.get();
                if (cnf) {
                    expression = Computation.of(expression).then(ToCNF::new).getResult().get();
                }
                CommandLine.saveFile(expression, outputFile, outFormat);
            } else {
                Feat.log().problems(parse.getProblems());
            }
        } catch (final Exception e) {
            Feat.log().error(e);
        }
    }

    @Override
    public boolean appendUsage(IndentStringBuilder sb) {
        return false;
//        final StringBuilder helpBuilder = new StringBuilder();
//        helpBuilder.append("\tParameters:\n");
//        helpBuilder.append("\t\t-i <Path>    Specify path to input feature model file(s) (default: system:in.xml)\n");
//        helpBuilder.append("\t\t-o <Path>    Specify path to output feature model file(s) (default: system:out)\n");
//        helpBuilder.append("\t\t-f <Format>  Specify format by identifier. One of:\n");
//        getFormats().forEach(f -> helpBuilder
//                .append("\t\t                 ")
//                .append(f.getName().toLowerCase())
//                .append("\n"));
//        helpBuilder.append("\t\t-r           Proceed recursively\n");
//        helpBuilder.append("\t\t-name        Specify file name filter as regular expression\n");
//        helpBuilder.append("\t\t-dry         Perform dry run\n");
//        helpBuilder.append("\t\t-cnf         Transform into CNF before conversion\n");
//        helpBuilder.append("\t\t-v <Level>   Specify verbosity. One of: none, error, info, debug, progress\n");
//        return helpBuilder.toString();
    }
}
