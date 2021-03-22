package com.ericsson.soiv.utils;

import com.ericsson.jive.core.execution.Jive;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.IOException;

import static java.io.File.separator;

// Created by Deepak
// Updated by ZMUKMAN

public class ReadAndValidateInvoicePdf {

    public static String readAndValidateInvoicePdf(Session session, TransactionSpecification tSpec) {

        String dirName = "./tmp"+separator;
        String path = RemoteHostUtility.downloadFromRemoteHost(session,tSpec.getInvoicePath()
                ,dirName);
        String textFromPage = null;
        try {
            assert path != null;
            textFromPage = readPdf(new File(path));
            Jive.log("Read Invoice PDF For Page number : " +tSpec.getInvoiceCharge()
                    + " & Content : "+textFromPage);
        } catch (Exception e){
            Jive.failAndContinue("Error in the Reading PDF File "+e);
        }
        finally { session.disconnect();
            if(tSpec.getRemoveInvoicePdfFile()){
            RemoteHostUtility.removeFileFromLocal(dirName, tSpec.getInvoicePath());
            }
        }
        return textFromPage;
     }

    public static String readPdf(File file) {

        String pdfFileInText = null;
        try (PDDocument document = PDDocument.load(file)) {

            document.getClass();

            if (!document.isEncrypted()) {

                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);

                PDFTextStripper tStripper = new PDFTextStripper();

                pdfFileInText = tStripper.getText(document);

            }

        } catch (InvalidPasswordException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pdfFileInText;
    }
}





