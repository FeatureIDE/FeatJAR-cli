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
import de.featjar.base.cli.CLIArgumentParser;
import de.featjar.base.cli.Command;
import de.featjar.base.cli.CommandLineInterface;
import de.featjar.base.log.IndentStringBuilder;
import de.featjar.cli.analysis.AnalysisCommand;
import de.featjar.cli.analysis.AnalysisCommands;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.structure.formula.Formula;

import java.util.Optional;

/**
 * ...
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public class FormulaAnalyzer implements Command {

    private AnalysisCommand<?> analysisCommand;

    @Override
    public String getDescription() {
        return "Computes an analysis result for a given formula";
    }

    @Override
    public void run(CLIArgumentParser argumentParser) {
        String input = argumentParser.parseOption("--input").orElse(CommandLineInterface.SYSTEM_INPUT);
        final String analysisCommandName = argumentParser.parseRequiredOption("--analysis");
        analysisCommand = argumentParser.getRequiredExtension(Feat.extensionPoint(AnalysisCommands.class), analysisCommandName);
        final Formula formula = CommandLineInterface.loadFile(input, Feat.extensionPoint(FormulaFormats.class)).orElseThrow();
        analysisCommand.setFormula(formula);
        analysisCommand.run(argumentParser);
    }

    @Override
    public boolean appendUsage(IndentStringBuilder sb) {
        // todo: abstract this away into a helper that is passed the name, value, description, and default (if any)
        sb.appendLine("--input <path>: Path to formula file (default: " + CommandLineInterface.SYSTEM_INPUT + ")");
        sb.appendLine("--analysis <Name>: Analysis whose result to compute. One of:").addIndent();
        Feat.extensionPoint(AnalysisCommands.class).getExtensions().forEach(a ->
                sb.appendLine(String.format("%s: %s", a.getIdentifier(), Optional.ofNullable(a.getDescription()).orElse(""))));
        sb.removeIndent();
        if (analysisCommand != null && analysisCommand.getUsage() != null) {
            sb.appendLine();
            sb.appendLine(String.format("Analysis %s has following flags and options:", analysisCommand.getIdentifier()));
            sb.addIndent();
            analysisCommand.appendUsage(sb);
            sb.removeIndent();
        }
        return true;
    }
}
