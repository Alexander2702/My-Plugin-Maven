package maven.plugin;



/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * @phase process-sources
 */
@Mojo(name = "myMojo", defaultPhase = LifecyclePhase.VERIFY)
public class MyMojo extends AbstractMojo {
    // путь к файлу, который сгенерировал плагин Maven Surefire
    @Parameter(defaultValue = "${project.build.directory}/surefire-reports/", name = "directoryReports")
    private File directoryReports;
    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @Parameter(defaultValue = "${project.build.directory}/test-report.html", name = "outputDirectory")
    private File outputDirectory;

    public File getDirectoryReports() {
        return directoryReports;
    }

    public void setDirectoryReports(File directoryReports) {
        this.directoryReports = directoryReports;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void execute() throws MojoExecutionException {
        try {
            createReport();
        } catch (IOException e) {
            throw new MojoExecutionException("Error create Report Test", e);
        }
    }

    private void createReport() throws IOException {
        Map<String, String> contentFile = new HashMap<>();

        try (Stream<Path> stream = Files.list(directoryReports.toPath())
                .filter(path -> path.toString().endsWith(".txt"))) {
            stream.forEach(resultFile -> {
                String nameFile = resultFile.getFileName().toString();
                String stateTest = getStateTestFromFile(resultFile);
                contentFile.put(nameFile, stateTest);
            });
        }
        generateHtmlReport(contentFile);
    }

    private void generateHtmlReport(Map<String, String> contentFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputDirectory.toPath())) {

            writer.write("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n");
            writer.write("<title>Test Report</title>\n</head>\n<body>\n");
            writer.write("<h1>Результаты тестов</h1>\n");
            writer.write("<table border=\"1\" cellpadding=\"5\" cellspacing=\"0\">\n");
            writer.write("  <tr>\n    <th>Файл теста</th>\n    <th>Статус</th>\n  </tr>\n");

            for (Map.Entry<String, String> entry : contentFile.entrySet()) {
                writer.write("  <tr>\n");
                writer.write("    <td>" + entry.getKey() + "</td>\n");
                writer.write("    <td>" + entry.getValue() + "</td>\n");
                writer.write("  </tr>\n");
            }

            writer.write("</table>\n</body>\n</html>");
        }
    }

    private String getStateTestFromFile(Path resultFile) {
        try {
            return Files.readAllLines(resultFile).stream()
                    .anyMatch(str -> str.contains("<<< FAILURE")) ? "FAILED" : "PASSED";
        } catch (IOException e) {
            getLog().error("Couldn't read test file " + resultFile, e);
            return "ERROR";
        }
    }
}
