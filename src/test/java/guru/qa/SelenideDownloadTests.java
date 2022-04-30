package guru.qa;

import com.codeborne.pdftest.PDF;
import com.codeborne.pdftest.matchers.ContainsExactText;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Selenide.$;
import static org.hamcrest.MatcherAssert.assertThat;

public class SelenideDownloadTests {
    ClassLoader cl = SelenideDownloadTests.class.getClassLoader();
    String pdfName = "junit.pdf";
    String xlsxName = "sample.xlsx";
    String csvName = "teacherscsv.csv";
    String zipName = "arv.zip";

    @Test
    void downloadTest() throws Exception {
        Selenide.open("https://github.com/junit-team/junit5/blob/main/README.md");
        File textFile = $("#raw-url").download();
        try (InputStream is = new FileInputStream(textFile)) {
            byte[] fileContent = is.readAllBytes();
            String strContent = new String(fileContent, StandardCharsets.UTF_8);
            org.assertj.core.api.Assertions.assertThat(strContent).contains("JUnit 5");
        }
    }

    @Test
    void pdfParsingTest() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("pdf/" + pdfName)) {
            assert stream != null;
            PDF pdf = new PDF(stream);
            Assertions.assertEquals(166, pdf.numberOfPages);
            assertThat(pdf, new ContainsExactText("123"));
        }
    }

    @Test
    void xlsParsingTest() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("xls/" + xlsxName)) {
            assert stream != null;
            XLS xls = new XLS(stream);
            String stringCellValue = xls.excel.getSheetAt(0).getRow(28).getCell(4).getStringCellValue();
            org.assertj.core.api.Assertions.assertThat(stringCellValue).contains("France");
        }
    }

    @Test
    void csvParsingTest() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("csv/" + csvName)) {
            assert stream != null;
            try (CSVReader reader = new CSVReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

                List<String[]> content = reader.readAll();
                org.assertj.core.api.Assertions.assertThat(content).contains(
                        new String[]{"Name", "Surname"},
                        new String[]{"Dmitrii", "Tuchs"},
                        new String[]{"Artem", "Eroshenko"}
                );
            }
        }
    }

    @Test
    void zipParsingTest() throws Exception {
        ZipFile zf = new ZipFile(new File("src/test/resources/zip/" + zipName));
        ZipInputStream is = new ZipInputStream(Objects.requireNonNull(cl.getResourceAsStream("zip/" + zipName)));
        ZipEntry entry;
        while ((entry = is.getNextEntry()) != null) {
            if (entry.getName().equals(pdfName)) {
                try (InputStream stream = zf.getInputStream(entry)) {
                    assert stream != null;
                    PDF pdf = new PDF(stream);
                    Assertions.assertEquals(166, pdf.numberOfPages);
                    assertThat(pdf, new ContainsExactText("123"));
                }
            }
            if (entry.getName().equals(csvName)) {
                try (InputStream stream = zf.getInputStream(entry)) {
                    assert stream != null;
                    try (CSVReader reader = new CSVReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

                        List<String[]> content = reader.readAll();
                        org.assertj.core.api.Assertions.assertThat(content).contains(
                                new String[]{"Name", "Surname"},
                                new String[]{"Dmitrii", "Tuchs"},
                                new String[]{"Artem", "Eroshenko"}
                        );
                    }
                }
            }
            if (entry.getName().equals(xlsxName)) {
                try (InputStream stream = zf.getInputStream(entry)) {
                    assert stream != null;
                    XLS xls = new XLS(stream);
                    String stringCellValue = xls.excel.getSheetAt(0).getRow(28).getCell(4).getStringCellValue();
                    org.assertj.core.api.Assertions.assertThat(stringCellValue).contains("France");
                }
            }
        }
    }
}