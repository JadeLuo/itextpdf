/*
 * $Id$
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2011 1T3XT BVBA
 * Authors: Balder Van Camp, Emiel Ackermann, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY 1T3XT,
 * 1T3XT DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.tool.xml.pipeline.html;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.WritableDirectElement;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.NoCustomContextException;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.PipelineException;
import com.itextpdf.tool.xml.ProcessObject;
import com.itextpdf.tool.xml.Tag;
import com.itextpdf.tool.xml.css.CSS;
import com.itextpdf.tool.xml.css.CssUtils;
import com.itextpdf.tool.xml.pipeline.AbstractPipeline;
import com.itextpdf.tool.xml.pipeline.WritableElement;
import com.itextpdf.tool.xml.pipeline.ctx.MapContext;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;

/**
 * This pipeline can automagically create documents. Allowing you to parse
 * continuously, without needing to renew the configuration. This class does
 * expect {@link PdfWriterPipeline} to be the last pipe of the line.
 *
 * @author redlab_b
 *
 */
public class AutoDocPipeline extends AbstractPipeline {

	private final FileMaker fm;
	private final String tag;
	private final String opentag;

	/**
	 * Constructor
	 * @param fm a FileMaker to provide a stream for every new document
	 * @param tag the tag on with to create a new document and close it
	 * @param opentag the tag on which to open the document ( {@link Document#open()}
	 *
	 */
	public AutoDocPipeline(final FileMaker fm, final String tag, final String opentag) {
		super(null);
		this.fm = fm;
		this.tag = tag;
		this.opentag = opentag;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.itextpdf.tool.xml.pipeline.AbstractPipeline#open(com.itextpdf.tool
	 * .xml.Tag, com.itextpdf.tool.xml.pipeline.ProcessObject)
	 */
	@Override
	public Pipeline open(final Tag t, final ProcessObject po) throws PipelineException {
		try {
			String tagName = t.getTag();
			if (tag.equals(tagName)) {
				MapContext cc;
				cc = (MapContext) getContext().get(PdfWriterPipeline.class);
				Document d = new Document();
				try {
					OutputStream os = fm.getStream();
					cc.put(PdfWriterPipeline.DOCUMENT, d);
					cc.put(PdfWriterPipeline.WRITER, PdfWriter.getInstance(d, os));
				} catch (IOException e) {
					throw new PipelineException(e);
				} catch (DocumentException e) {
					throw new PipelineException(e);
				}

			}
			if (t.getTag().equalsIgnoreCase(opentag)) {
				MapContext cc;
				cc = (MapContext) getContext().get(PdfWriterPipeline.class);

				Document d = (Document) cc.get(PdfWriterPipeline.DOCUMENT);
				po.add(new WritableElement(new WritableDirectElement() {

					public void write(final PdfWriter writer, final Document d) throws DocumentException {
						CssUtils cssUtils = CssUtils.getInstance();
						float pageWidth = d.getPageSize().getWidth();
						float marginLeft = 0;
						float marginRight = 0;
						float marginTop = 0;
						float marginBottom = 0;
						Map<String, String> css = t.getCSS();
						for (Entry<String, String> entry : css.entrySet()) {
							String key = entry.getKey();
							String value = entry.getValue();
							if (key.equalsIgnoreCase(CSS.Property.MARGIN_LEFT)) {
								marginLeft = cssUtils.parseValueToPt(value, pageWidth);
							} else if (key.equalsIgnoreCase(CSS.Property.MARGIN_RIGHT)) {
								marginRight = cssUtils.parseValueToPt(value, pageWidth);
							} else if (key.equalsIgnoreCase(CSS.Property.MARGIN_TOP)) {
								marginTop = cssUtils.parseValueToPt(value, pageWidth);
							} else if (key.equalsIgnoreCase(CSS.Property.MARGIN_BOTTOM)) {
								marginBottom = cssUtils.parseValueToPt(value, pageWidth);
							}
						}
						d.setMargins(marginLeft, marginRight, marginTop, marginBottom);
						d.open();

					}
				}));
				CssUtils cssUtils = CssUtils.getInstance();
				float pageWidth = d.getPageSize().getWidth();
				float marginLeft = 0;
				float marginRight = 0;
				float marginTop = 0;
				float marginBottom = 0;
				Map<String, String> css = t.getCSS();
				for (Entry<String, String> entry : css.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (key.equalsIgnoreCase(CSS.Property.MARGIN_LEFT)) {
						marginLeft = cssUtils.parseValueToPt(value, pageWidth);
					} else if (key.equalsIgnoreCase(CSS.Property.MARGIN_RIGHT)) {
						marginRight = cssUtils.parseValueToPt(value, pageWidth);
					} else if (key.equalsIgnoreCase(CSS.Property.MARGIN_TOP)) {
						marginTop = cssUtils.parseValueToPt(value, pageWidth);
					} else if (key.equalsIgnoreCase(CSS.Property.MARGIN_BOTTOM)) {
						marginBottom = cssUtils.parseValueToPt(value, pageWidth);
					}
				}
				d.setMargins(marginLeft, marginRight, marginTop, marginBottom);
				d.open();

			}
		} catch (NoCustomContextException e) {
			throw new PipelineException("AutoDocPipeline depends on PdfWriterPipeline.", e);
		}

		return getNext();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.itextpdf.tool.xml.pipeline.AbstractPipeline#close(com.itextpdf.tool
	 * .xml.Tag, com.itextpdf.tool.xml.pipeline.ProcessObject)
	 */
	@Override
	public Pipeline close(final Tag t, final ProcessObject po) throws PipelineException {
		String tagName = t.getTag();
		if (tag.equals(tagName)) {
			MapContext cc;
			try {
				cc = (MapContext) getContext().get(PdfWriterPipeline.class);
				Document d = (Document) cc.get(PdfWriterPipeline.DOCUMENT);
				d.close();
			} catch (NoCustomContextException e) {
				throw new PipelineException("AutoDocPipeline depends on PdfWriterPipeline.", e);
			}
			// TODO clean
		}
		return getNext();
	}


}
