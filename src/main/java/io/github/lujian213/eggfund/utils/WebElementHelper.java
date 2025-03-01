package io.github.lujian213.eggfund.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.openqa.selenium.WebElement;

public class WebElementHelper {
    private WebElementHelper() {
    }

    public static void save(WebElement we, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            String html = we.getAttribute("innerHTML");
            writer.println(html);
        }
    }

    public static void save(WebElement we, StringBuilder sb) {
        sb.append(we.getAttribute("innerHTML"));
    }


    public static WebElement load(File file) throws IOException {
        return new LocalWebElement(file);
    }

    public static WebElement load(String str) {
        return new LocalWebElement(str);
    }

    public static WebElement load(InputStream is) {
        return new LocalWebElement(is);
    }
}