HyperLinkGrades
===============

Example parameters for performing case-insensitive search and avoiding completed ones:

```
java -cp "lib/*" \
  nz.ac.waikato.cms.doc.HyperLinkGrades \ 
  CMS-Undergrad.pdf \
  ".*(possible a sem 15 completer|possible a sem 15 completion|potential sem a 2015 completion|potential a sem 2015 completion).*" \
  CMS-Undergrad_indexed.pdf \
  --casesensitive false \
  --nocompletions true
```

HyperLinkGradesGUI
==================

Starting the HyperLinkGrades user interface:

* Linux/Mac: `bin/hyperlinkgrades`
* Windows: `bin\hyperlinkgrades.bat`
* with the following command:
```
  java -cp "lib/*" \
    nz.ac.waikato.cms.gui.HyperLinkGradesGUI
```

OverlayFilename
===============

Example parameters for overlaying the filename (no path, no extension) on a PDF:

```
java -cp "lib/*" \
  nz.ac.waikato.cms.doc.OverlayFilename \ 
  mypdf.pdf \
  mypdf_out.pdf \
  --strippath true \
  --stripext true
```

OverlayFilenameGUI
==================

Starting the HyperLinkGrades user interface:

* Linux/Mac: `bin/overlayfilename`
* Windows: `bin\overlayfilename.bat`
* with the following command:
```
  java -cp "lib/*" \
    nz.ac.waikato.cms.gui.OverlayFilenameGUI
```
