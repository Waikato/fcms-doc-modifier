package nz.ac.waikato.cms.examples

import com.itextpdf.text.*
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import nz.ac.waikato.cms.doc.ScriptedPDFOverlayProcessor

import java.awt.Color

class PdfOverlayExample implements ScriptedPDFOverlayProcessor {

    @Override
    String overlay(File pdfTemplate, int row, Map<String, String> params, File outputDir) {
        String          result
        String          name
        String          title
        PdfReader       reader
        PdfStamper      stamper
        PdfContentByte  cb
        ColumnText	    ct
        Font            fontName
        Font            fontTitle
        File            outputFile

        result  = null
        name    = params.get("name")
        title   = params.get("title")

        outputFile = new File(outputDir.getAbsolutePath() + File.separator + row + ".pdf")
        fontTitle = FontFactory.getFont("Helvetica", 12F, new BaseColor(Color.WHITE.getRGB()))
        fontName  = FontFactory.getFont("Helvetica", 18F, new BaseColor(Color.WHITE.getRGB()))

        // read the file
        reader  = new PdfReader(new FileInputStream(pdfTemplate.getAbsolutePath()))
        stamper = new PdfStamper(reader, new FileOutputStream(outputFile.getAbsolutePath()))
        cb      = stamper.getOverContent(1)

        // title
        ct = new ColumnText(cb)
        ct.setSimpleColumn(0, 50, (float) (reader.getPageSize(1).getWidth()), 200, 0, Element.ALIGN_CENTER)
        ct.setText(new Phrase(title, fontTitle))
        ct.go()

        // name
        ct = new ColumnText(cb)
        ct.setSimpleColumn(0, 50, (float) (reader.getPageSize(1).getWidth()), 180, 0, Element.ALIGN_CENTER)
        ct.setText(new Phrase(name, fontName))
        ct.go()

        // close document
        stamper.close()

        return result
    }
}
