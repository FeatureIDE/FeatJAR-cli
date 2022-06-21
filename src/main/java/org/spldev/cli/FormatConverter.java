/* -----------------------------------------------------------------------------
 * Command Line Interface - Reference frontend for the library
 * Copyright (C) 2021  Elias Kuiter
 * 
 * This file is part of Command Line Interface.
 * 
 * Command Line Interface is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Command Line Interface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Command Line Interface.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/cli> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.cli;

import org.spldev.formula.io.FormulaFormatManager;
import org.spldev.formula.structure.Formula;
import org.spldev.formula.structure.Formulas;
import org.spldev.util.cli.CLI;
import org.spldev.util.cli.CLIFunction;
import org.spldev.util.data.Result;
import org.spldev.util.io.FileHandler;
import org.spldev.util.io.format.Format;
import org.spldev.util.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class FormatConverter implements CLIFunction {
	private final List<Format<Formula>> formats = FormulaFormatManager.getInstance().getExtensions();

	@Override
	public String getName() {
		return "convert";
	}

	@Override
	public String getDescription() {
		return "Converts feature models between various formats";
	}

	@Override
	public void run(List<String> args) {
		String input = CLI.SYSTEM_INPUT;
		String output = CLI.SYSTEM_OUTPUT;
		Format<Formula> outFormat = null;
		boolean recursive = false;
		boolean dryRun = false;
		boolean cnf = false;
		String fileNameFilter = null;
		String verbosity = CLI.DEFAULT_VERBOSITY;

		for (final ListIterator<String> iterator = args.listIterator(); iterator.hasNext();) {
			final String arg = iterator.next();
			switch (arg) {
			case "-f": {
				final String name = CLI.getArgValue(iterator, arg).toLowerCase();
				outFormat = formats.stream()
					.filter(f -> Objects.equals(name, f.getName().toLowerCase()))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Unknown format: " + name));
				break;
			}
			case "-o": {
				output = CLI.getArgValue(iterator, arg);
				break;
			}
			case "-i": {
				input = CLI.getArgValue(iterator, arg);
				break;
			}
			case "-r": {
				recursive = true;
				break;
			}
			case "-name": {
				fileNameFilter = CLI.getArgValue(iterator, arg);
				break;
			}
			case "-v": {
				verbosity = CLI.getArgValue(iterator, arg);
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

		CLI.installLogger(verbosity);

		if (outFormat == null) {
			throw new IllegalArgumentException("No output format specified!");
		}
		if (!CLI.isValidInput(input)) {
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
			final Predicate<String> fileNamePredicate = fileNameFilter == null ? (s -> true)
				: Pattern.compile(fileNameFilter).asMatchPredicate();
			try {
				final Stream<Path> fileStream = recursive ? Files.walk(rootIn) : Files.list(rootIn);
				boolean finalCnf = cnf;
				fileStream //
					.filter(Files::isRegularFile) //
					.filter(f -> fileNamePredicate.test(f.getFileName().toString())) //
					.forEach(inputFile -> {
						final Path outputDirectory = rootOut.resolve(rootIn.relativize(inputFile.getParent()));
						final Path outputFile = outputDirectory
							.resolve(FileHandler.getFileNameWithoutExtension(inputFile.getFileName()) + "."
								+ format.getFileExtension());
						Logger.logInfo(inputFile + " -> " + outputFile);
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
			Logger.logInfo(input + " -> " + output);
			if (convert) {
				convert(input, output, outFormat, cnf);
			}
		}
	}

	private void convert(String inputFile, String outputFile, Format<Formula> outFormat, boolean cnf) {
		try {
			final Result<Formula> parse = CLI.loadFile(inputFile, FormulaFormatManager.getInstance());
			if (parse.isPresent()) {
				Formula formula = parse.get();
				if (cnf) {
					formula = Formulas.toCNF(formula).get();
				}
				CLI.saveFile(formula, outputFile, outFormat);
			} else {
				Logger.logProblems(parse.getProblems());
			}
		} catch (final Exception e) {
			Logger.logError(e);
		}
	}

	@Override
	public String getHelp() {
		final StringBuilder helpBuilder = new StringBuilder();
		helpBuilder.append("\tParameters:\n");
		helpBuilder.append("\t\t-i <Path>    Specify path to input feature model file(s) (default: system:in.xml)\n");
		helpBuilder.append("\t\t-o <Path>    Specify path to output feature model file(s) (default: system:out)\n");
		helpBuilder.append("\t\t-f <Format>  Specify format by identifier. One of:\n");
		formats.forEach(f -> helpBuilder.append("\t\t                 ").append(f.getName().toLowerCase()).append(
			"\n"));
		helpBuilder.append("\t\t-r           Proceed recursively\n");
		helpBuilder.append("\t\t-name        Specify file name filter as regular expression\n");
		helpBuilder.append("\t\t-dry         Perform dry run\n");
		helpBuilder.append("\t\t-cnf         Transform into CNF before conversion\n");
		helpBuilder.append("\t\t-v <Level>   Specify verbosity. One of: none, error, info, debug, progress\n");
		return helpBuilder.toString();
	}
}
