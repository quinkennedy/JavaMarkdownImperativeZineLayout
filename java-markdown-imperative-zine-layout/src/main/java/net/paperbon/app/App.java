package net.paperbon.app;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.util.Matrix;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.Parser.Builder;
import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.util.collection.iteration.ReversiblePeekingIterable;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.Extension;
import com.vladsch.flexmark.util.options.MutableDataSet;

/**
 * Hello world!
 *
 */
public class App 
{

    private static String text = 
        "Here is my test of great wizards grumpily jumping along";
    private static String dir = 
        "src/main/resources/";

    public static void main( String[] args ) throws IOException
    {
        //create the document
        PDDocument doc = new PDDocument();
        try{
            //create a page and add it to the document
            PDPage page = new PDPage();
            doc.addPage(page);
            //load the font we want to use
            //  this method is specifically for TTF fonts,
            //  other font formats may require different handling
            PDFont font = PDType0Font.load(doc, new File(dir + "leaguespartan-bold.ttf"));

            Node mdRoot = parseMD(dir + "markdown_a.md");
            if (mdRoot != null){
                System.out.println(mdRoot);
                ReversiblePeekingIterable<Node> children = mdRoot.getChildren();
                int numTitles = 0;
                int numChapters = 0;
                boolean titleLeaveSpace = false;
                for(Node child : children){
                    System.out.println(child);
                    if (child instanceof Heading){
                        if (((Heading)child).getLevel() == 1){
                            System.out.println("title: " + ((Heading)child).getText().unescape());
                            numTitles++;
                            titleLeaveSpace |= (!titleLeaveSpace &&
                                               child.getNext() != null &&
                                               !(child.getNext() instanceof Heading));
                        } else if (((Heading)child).getLevel() == 2){
                            System.out.println("chapter: " + ((Heading)child).getText().unescape());
                            numChapters++;
                        }
                    }
                }
            }

            //get the stream for adding content to the PDF page
            PDPageContentStream contents = new PDPageContentStream(doc, page);
            //first lets just write some text, and draw a box around it
            drawText(contents, font, 12, 1 + text);
            //then lets try applying some transformations
            rotate(contents, 1);
            drawText(contents, font, 12, 2 + text);
            translate(contents, 20, 20);
            drawText(contents, font, 12, 3 + text);

            //close the page stream
            contents.close();
            //save out the PDF
            doc.save("my.pdf");
            System.out.println("done");
        } catch (Exception e) {
            System.out.println("oops");
        } finally {
            //close the PDF
            doc.close();
        }
    }

    private static Node parseMD(String filePath) throws IOException {
        FileReader file = null;
        try {
            file = new FileReader(filePath);
        } catch (FileNotFoundException e){
            System.out.println("can't find "+filePath);
        }
        if (file != null){
            //create extensions we want to include in the parser
            Extension toc = TocExtension.create();
            //group extensions into a list
            Iterable<Extension> extensions = Arrays.asList(new Extension[]{toc});
            //put list in a DataHolder
            MutableDataSet data = new MutableDataSet();
            data.set(Parser.EXTENSIONS, extensions);
            //get a parser _builder_ with the selected extensions
            Builder builder = Parser.builder(data);
            //build the actual parser
            Parser parser = builder.build();
            //get a reader for the Markdown file
            BufferedReader reader = new BufferedReader(file);
            //parse the markdown file
            Node document = parser.parseReader(reader);
            return document;
        } else {
            return null;
        }
    }

    private static void drawText(PDPageContentStream contents, 
                          PDFont font, 
                          int fontSize, 
                          String text) throws IOException {
        contents.beginText();
        contents.setFont(font, fontSize);
        contents.newLineAtOffset(0, 0);
        contents.showText(text);
        contents.setLeading(fontSize * 1.2);
        contents.newLine();
        contents.showText(text);
        contents.endText();
        float textWidth = font.getStringWidth(text) * fontSize / 1000;
        PDFontDescriptor fontDesc = font.getFontDescriptor();
        float ascent = fontSize;
        float descent = 0;
        if (fontDesc != null){
          ascent = fontDesc.getAscent() * fontSize / 1000;
          descent = fontDesc.getDescent() * fontSize / 1000;
        }
        contents.addRect(0, descent, textWidth, ascent - descent);
        contents.stroke();
    }

    private static void rotate(PDPageContentStream contents,
                               float angle) throws IOException {
        Matrix mat = new Matrix();
        mat.rotate(angle);
        contents.transform(mat);
    }

    private static void translate(PDPageContentStream contents,
                                  float x,
                                  float y) throws IOException {
        Matrix mat = new Matrix();
        mat.translate(x, y);
        contents.transform(mat);
    }
}
