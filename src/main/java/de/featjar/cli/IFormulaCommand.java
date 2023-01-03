package de.featjar.cli;

import de.featjar.base.cli.CommandLineInterface;
import de.featjar.base.cli.ICommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.StringOption;

public interface IFormulaCommand extends ICommand {
    Option<String> INPUT_OPTION =
            new StringOption("--input")
                    .setDescription("Path to input formula file")
                    .setDefaultValue(CommandLineInterface.STANDARD_INPUT);

    Option<String> OUTPUT_OPTION =
            new StringOption("--output")
                    .setDescription("Path to output formula file")
                    .setDefaultValue(CommandLineInterface.STANDARD_OUTPUT);
}
