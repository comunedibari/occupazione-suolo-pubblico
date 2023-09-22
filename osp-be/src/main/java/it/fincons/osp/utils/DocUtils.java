package it.fincons.osp.utils;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DocUtils {
    
    public static byte[] docxToPdf(byte[] docx) throws Docx4JException {
        InputStream is = new ByteArrayInputStream(docx);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(is);
            Docx4J.toPDF(wordMLPackage, os);
        }
        finally {
            try {
                os.flush();
                os.close();
            } catch (IOException ioex) {}
        }

        return os.toByteArray();
    }


}
