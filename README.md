This is a project to dip my feet into generating zines from Markdown
without using Processing.

The original impitus to not use Processing sprouted when I wanted
to use Maven for dependency management. Maven is not really making
life easy, but I continue down this path regardless. At the very
least maybe I'll be able to turn some of the pieces (PDF handling,
markdown parsing) into jars that can be easily included in Processing,
but don't rely on PApplet.

## usage

Build using `mvn package`

Run using `mvn exec:java -D exec.mainClass=net.paperbon.app.App`

## setup

I started by generating the project with Maven: 
`mvn archetype:generate -DgroupId=net.paperbon.app -DartifactId=java-markdown-imperative-zine-layout`

I then edited [pom.xml](java-markdown-imperative-zine-layout/pom.xml)
to include the PDF export and markdown parsing libraries I was
interested in.


## libraries

### PDF Export

I investigated [iText](http://itextpdf.com/) 
and [PDFBox](https://pdfbox.apache.org/). 
I chose the PDFBox because it uses a permissive Apache license.
A streatch goal would be to get comfortale enough with the PDF library
to replace the outdated Processing PDF exporter with this one.

### Markdown Parsing

I investigated [pegdown](http://pegdown.org/) 
and [flexmark](https://github.com/vsch/flexmark-java).
I chose flexmark mainly because of the extensions it has available.
The TOC extension was the original impetus. 
Bonus points for implementing core features on top of 
the extension architecture, and being fast.


