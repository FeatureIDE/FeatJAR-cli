/*
 * Copyright (C) 2023 Elias Kuiter
 *
 * This file is part of FeatJAR-cli.
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
package cli;

/**
 * Tests sampling algorithms.
 *
 * @author Sebastian Krieter
 */
public class ExpressionAnalyzerTest {

    //    static {
    //        ExtensionManager.install();
    //    }
    //
    //    private static final Path modelDirectory = Paths.get("src/test/resources/testFeatureModels");
    //    private static final String resultPrefix = "Result:\n";
    //
    //    private final List<String> modelNames = Arrays.asList( //
    //            "basic", //
    //            "simple", //
    //            "car", //
    //            "gpl_medium_model");
    //
    //    private static final ByteArrayOutputStream newOut = new ByteArrayOutputStream();
    //    private static final PrintStream orgOut = System.out;
    //
    //    @BeforeEach
    //    public void setStreams() {
    //        System.setOut(new PrintStream(newOut));
    //    }
    //
    //    @AfterEach
    //    public void restoreInitialStreams() {
    //        System.setOut(orgOut);
    //    }
    //
    //    // @Test
    //    public void _void() {
    //        final String result = analyze(modelDirectory.resolve(modelNames.get(0) + ".xml"), "void");
    //        assertEquals("false", result);
    //    }
    //
    //    private static String analyze(final Path modelFile, String algorithm) {
    //        try {
    //            final Path inFile = Files.createTempFile("input", ".xml");
    //            try {
    //                Files.write(inFile, Files.readAllBytes(modelFile));
    //                final ArrayList<String> args = new ArrayList<>();
    //                args.add("-a");
    //                args.add(algorithm);
    //                args.add("-i");
    //                args.add(inFile.toString());
    //                new AnalysisCommand().run(args);
    //                final String output = newOut.toString();
    //                final int index = output.indexOf(resultPrefix);
    //                return (index < 0)
    //                        ? null
    //                        : output.substring(index + resultPrefix.length()).trim();
    //            } finally {
    //                Files.deleteIfExists(inFile);
    //            }
    //        } catch (final IOException e) {
    //            e.printStackTrace();
    //            fail(e.getMessage());
    //        }
    //        return null;
    //    }
}
