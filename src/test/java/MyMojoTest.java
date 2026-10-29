import maven.plugin.MyMojo;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.codehaus.plexus.PlexusTestCase.getTestFile;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MyMojoTest {

    @Rule
    public MojoRule rule = new MojoRule();

    @Test
    public void testCreateReportHTML() throws Exception {
        // Подготовка: создание временного POM-файла
        File pom = getTestFile("src/test/resources/test-pom.xml");

        // Проверка, что POM-файл существует
        assertNotNull(pom);
        assertTrue(pom.exists());

        // Выполнение плагина
        MyMojo testReportMojo = (MyMojo) rule.lookupMojo("myMojo", pom);
        assertNotNull(testReportMojo);

        // Установка параметров
        testReportMojo.setDirectoryReports(new File("target/surefire-reports"));
        testReportMojo.setOutputDirectory(new File("target/test-report.html"));

        // Выполнение плагина
        testReportMojo.execute();
        // Проверка результата
        File reportFile = new File("target/test-report.html");
        assertTrue("Отчет не был создан", reportFile.exists());
    }
}
