# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

# add_page_numbers.py
# Copyright (C) 2014 Fracpete (fracpete at waikato dot ac dot nz)

import os
import sys
import getopt
import logging
import StringIO
from PyPDF2 import PdfFileWriter, PdfFileReader
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4


# logging setup
logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger("ppn")


def add_page_numbers(inputfile, outputfile, startno=None, endno=None, fontname="Helvetica", fontsize=12,
                     pagenoformat="- %i -", pagesize=A4, posx=280, posy=800):
    """
    Adds page numbers to the input PDF file and stores the modified PDF in output.
    Optionally, the page range can be limited.
    :param inputfile: the input PDF
    :type inputfile: str
    :param outputfile: the output PDF
    :type outputfile: str
    :param startno: the first page to number, 1-based, use None to start from first page
    :type startno: int
    :param endno: the last page to number, 1-based, use None to end with last page
    :type endno: int
    :param fontname: the name of the font to use, eg 'Helvetica'
    :type fontname: str
    :param fontsize: the size of the font, eg 12
    :type fontsize: int
    :param pagenoformat: the format string for the page number, eg '- %i -'
    :type pagenoformat: str
    :param pagesize: the page size, eg A4
    :type pagesize: object
    :param posx: the X position for the page number
    :type posx: int
    :param posy: the Y position for the page number
    :type posy: int
    """
    inputpdf = PdfFileReader(open(inputfile, "rb"))
    outputpdf = PdfFileWriter()

    if startno is None:
        startno = 1
    if endno is None:
        endno = inputpdf.getNumPages()
    for i in xrange(inputpdf.getNumPages()):
        page = i + 1
        current = inputpdf.getPage(i)
        # add page number?
        # taken from here: http://stackoverflow.com/a/17538003
        if (page >= startno) and (page <= endno):
            packet = StringIO.StringIO()
            can = canvas.Canvas(packet, pagesize=pagesize)
            can.setFont(fontname, fontsize)
            can.drawString(posx, posy, pagenoformat % page)
            can.save()
            packet.seek(0)
            pagenopdf = PdfFileReader(packet)
            logger.info("Page " + str(page) + " added")
            current.mergePage(pagenopdf.getPage(0))
        else:
            logger.info("Page " + str(page))
        outputpdf.addPage(current)

    outputstream = file(outputfile, "wb")
    outputpdf.write(outputstream)


def main(args):
    """
    Adds the page numbers.
    Options:
        -i input.pdf
        -o output.pdf
        [-s pageno (first page to number, 1-based)]
        [-e pageno (last page to number, 1-based)]
        [-F fontname, eg 'Helvetica']
        [-S fontsize, eg 12]
        [-f pagenumberformat, eg '- %i -']
        [-x horizontal position for page no]
        [-y vertical position for page no]
    """

    usage = "Usage: add_page_numbers.py -i input.pdf -o output.pdf [-s pageno (first page to number, 1-based)] " \
            + "[-e pageno (last page to number, 1-based)] [-F fontname] [-S fontsize] [-f pagenoformat] " \
            + "[-x posx] [-y posy]"

    optlist, optargs = getopt.getopt(args, "i:o:s:e:F:S:f:x:y:h")
    inputfile = None
    outputfile = None
    startno = None
    endno = None
    fontname = "Helvetica"
    fontsize = 12
    pagenoformat = "- %i -"
    posx = 280
    posy = 800
    for opt in optlist:
        if opt[0] == "-h":
            print(usage)
            return
        elif opt[0] == "-i":
            inputfile = opt[1]
        elif opt[0] == "-o":
            outputfile = opt[1]
        elif opt[0] == "-s":
            startno = int(opt[1])
        elif opt[0] == "-e":
            endno = int(opt[1])
        elif opt[0] == "-F":
            fontname = opt[1]
        elif opt[0] == "-S":
            fontsize = int(opt[1])
        elif opt[0] == "-f":
            pagenoformat = opt[1]
        elif opt[0] == "-x":
            posx = int(opt[1])
        elif opt[0] == "-y":
            posy = int(opt[1])

    # check parameters
    if inputfile is None:
        raise Exception("No input PDF file provided ('-i ...')!")
    if not os.path.exists(inputfile) or not os.path.isfile(inputfile):
        raise Exception("Input file is either not a file or does not exist ('-i ...'): " + inputfile)
    if outputfile is None:
        raise Exception("No output PDF file provided ('-o ...')!")

    logger.info("input: " + inputfile)
    logger.info("output: " + outputfile)

    try:
        add_page_numbers(
            inputfile=inputfile, outputfile=outputfile, startno=startno, endno=endno, fontname=fontname,
            fontsize=fontsize, pagenoformat=pagenoformat, posx=posx, posy=posy)
    except Exception, ex:
        print(ex)


if __name__ == "__main__":
    try:
        main(sys.argv[1:])
    except Exception, e:
        print(e)
