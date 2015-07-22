HyperLinkGrades
===============

Example parameters for performing case-insensitive search (4th param) and
avoiding completed ones (5th param):

```
java -cp "lib/*" \
  nz.ac.waikato.cms.doc.HyperLinkGrades \ 
  CMS-Undergrad.pdf \
  ".*(possible a sem 15 completer|possible a sem 15 completion|potential sem a 2015 completion|potential a sem 2015 completion).*" \
  CMS-Undergrad_indexed.pdf \
  false \
  true
```
