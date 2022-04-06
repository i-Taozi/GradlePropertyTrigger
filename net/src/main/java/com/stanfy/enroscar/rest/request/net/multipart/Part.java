package com.stanfy.enroscar.rest.request.net.multipart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.util.EncodingUtils;

import android.util.Log;

import com.stanfy.enroscar.rest.Utils;

/**
 * Abstract class for one Part of a multipart post object.
 *
 * @author <a href="mailto:mattalbright@yahoo.com">Matthew Albright</a>
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author <a href="mailto:adrian@ephox.com">Adrian Sutton</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 *
 * @since 2.0
 */
public abstract class Part {

  /** Logging tag. */
  private static final String TAG = "Part";
  /** Debug flag. */
  private static final boolean DEBUG = Utils.DEBUG_IO;

  /** Carriage return/linefeed. */
  protected static final String CRLF = "\r\n";

  /** Carriage return/linefeed as a byte array. */
  protected static final byte[] CRLF_BYTES = EncodingUtils.getAsciiBytes(CRLF);

  /** Content dispostion characters. */
  protected static final String QUOTE = "\"";

  /** Content dispostion as a byte array. */
  protected static final byte[] QUOTE_BYTES =
    EncodingUtils.getAsciiBytes(QUOTE);

  /** Extra characters. */
  protected static final String EXTRA = "--";

  /** Extra characters as a byte array. */
  protected static final byte[] EXTRA_BYTES =
    EncodingUtils.getAsciiBytes(EXTRA);

  /** Content dispostion characters. */
  protected static final String CONTENT_DISPOSITION = "Content-Disposition: form-data; name=";

  /** Content dispostion as a byte array. */
  protected static final byte[] CONTENT_DISPOSITION_BYTES =
    EncodingUtils.getAsciiBytes(CONTENT_DISPOSITION);

  /** Content type header. */
  protected static final String CONTENT_TYPE = "Content-Type: ";

  /** Content type header as a byte array. */
  protected static final byte[] CONTENT_TYPE_BYTES =
    EncodingUtils.getAsciiBytes(CONTENT_TYPE);

  /** Content charset. */
  protected static final String CHARSET = "; charset=";

  /** Content charset as a byte array. */
  protected static final byte[] CHARSET_BYTES =
    EncodingUtils.getAsciiBytes(CHARSET);

  /** Content type header. */
  protected static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding: ";

  /** Content type header as a byte array. */
  protected static final byte[] CONTENT_TRANSFER_ENCODING_BYTES =
    EncodingUtils.getAsciiBytes(CONTENT_TRANSFER_ENCODING);

  /**
   * The ASCII bytes to use as the multipart boundary.
   */
  private byte[] boundaryBytes;

  /**
   * Return the name of this part.
   * @return The name.
   */
  public abstract String getName();

  /**
   * Returns the content type of this part.
   * @return the content type, or <code>null</code> to exclude the content type header
   */
  public abstract String getContentType();

  /**
   * Return the character encoding of this part.
   * @return the character encoding, or <code>null</code> to exclude the character
   * encoding header
   */
  public abstract String getCharSet();

  /**
   * Return the transfer encoding of this part.
   * @return the transfer encoding, or <code>null</code> to exclude the transfer encoding header
   */
  public abstract String getTransferEncoding();

  /**
   * Gets the part boundary to be used.
   * @return the part boundary as an array of bytes.
   *
   * @since 3.0
   */
  protected byte[] getPartBoundary() {
    return boundaryBytes;
  }

  /**
   * Sets the part boundary.  Only meant to be used by
   * {@link Part#sendParts(OutputStream, Part[], byte[])}
   * and {@link Part#getLengthOfParts(Part[], byte[])}
   * @param boundaryBytes An array of ASCII bytes.
   * @since 3.0
   */
  public void setPartBoundary(final byte[] boundaryBytes) {
    this.boundaryBytes = boundaryBytes;
  }

  /**
   * Tests if this part can be sent more than once.
   * @return <code>true</code> if {@link #sendData(OutputStream)} can be successfully called
   * more than once.
   * @since 3.0
   */
  public boolean isRepeatable() {
    return true;
  }

  /**
   * Write the start to the specified output stream.
   * @param out The output stream
   * @throws IOException If an IO problem occurs.
   */
  protected void sendStart(final OutputStream out) throws IOException {
    if (DEBUG) { Log.v(TAG, "enter sendStart(OutputStream out)"); }
    out.write(EXTRA_BYTES);
    out.write(getPartBoundary());
    out.write(CRLF_BYTES);
  }

  /**
   * Write the content disposition header to the specified output stream.
   *
   * @param out The output stream
   * @throws IOException If an IO problem occurs.
   */
  protected void sendDispositionHeader(final OutputStream out) throws IOException {
    if (DEBUG) { Log.v(TAG, "enter sendDispositionHeader(OutputStream out)"); }
    out.write(CONTENT_DISPOSITION_BYTES);
    out.write(QUOTE_BYTES);
    out.write(EncodingUtils.getAsciiBytes(getName()));
    out.write(QUOTE_BYTES);
  }

  /**
   * Write the content type header to the specified output stream.
   * @param out The output stream
   * @throws IOException If an IO problem occurs.
   */
  protected void sendContentTypeHeader(final OutputStream out) throws IOException {
    if (DEBUG) { Log.v(TAG, "enter sendContentTypeHeader(OutputStream out)"); }
    final String contentType = getContentType();
    if (contentType != null) {
      out.write(CRLF_BYTES);
      out.write(CONTENT_TYPE_BYTES);
      out.write(EncodingUtils.getAsciiBytes(contentType));
      final String charSet = getCharSet();
      if (charSet != null) {
        out.write(CHARSET_BYTES);
        out.write(EncodingUtils.getAsciiBytes(charSet));
      }
    }
  }

  /**
   * Write the content transfer encoding header to the specified
   * output stream.
   *
   * @param out The output stream
   * @throws IOException If an IO problem occurs.
   */
  protected void sendTransferEncodingHeader(final OutputStream out) throws IOException {
    if (DEBUG) { Log.v(TAG, "enter sendTransferEncodingHeader(OutputStream out)"); }
    final String transferEncoding = getTransferEncoding();
    if (transferEncoding != null) {
      out.write(CRLF_BYTES);
      out.write(CONTENT_TRANSFER_ENCODING_BYTES);
      out.write(EncodingUtils.getAsciiBytes(transferEncoding));
    }
  }

  /**
   * Write the end of the header to the output stream.
   * @param out The output stream
   * @throws IOException If an IO problem occurs.
   */
  protected void sendEndOfHeader(final OutputStream out) throws IOException {
    if (DEBUG) { Log.v(TAG, "enter sendEndOfHeader(OutputStream out)"); }
    out.write(CRLF_BYTES);
    out.write(CRLF_BYTES);
  }

  /**
   * Write the data to the specified output stream.
   * @param out The output stream
   * @throws IOException If an IO problem occurs.
   */
  protected abstract void sendData(OutputStream out) throws IOException;

  /**
   * Return the length of the main content.
   *
   * @return long The length.
   * @throws IOException If an IO problem occurs
   */
  protected abstract long lengthOfData() throws IOException;

  /**
   * Write the end data to the output stream.
   * @param out The output stream
   * @throws IOException If an IO problem occurs.
   */
  protected void sendEnd(final OutputStream out) throws IOException {
    if (DEBUG) { Log.v(TAG, "enter sendEnd(OutputStream out)"); }
    out.write(CRLF_BYTES);
  }

  /**
   * Write all the data to the output stream.
   * If you override this method make sure to override
   * #length() as well
   *
   * @param out The output stream
   * @throws IOException If an IO problem occurs.
   */
  public void send(final OutputStream out) throws IOException {
    if (DEBUG) { Log.v(TAG, "enter send(OutputStream out)"); }
    sendStart(out);
    sendDispositionHeader(out);
    sendContentTypeHeader(out);
    sendTransferEncodingHeader(out);
    sendEndOfHeader(out);
    sendData(out);
    sendEnd(out);
  }


  /**
   * Return the full length of all the data.
   * If you override this method make sure to override
   * #send(OutputStream) as well
   *
   * @return long The length.
   * @throws IOException If an IO problem occurs
   */
  public long length() throws IOException {
    if (DEBUG) { Log.v(TAG, "enter length()"); }
    if (lengthOfData() < 0) {
      return -1;
    }
    final ByteArrayOutputStream overhead = new ByteArrayOutputStream();
    sendStart(overhead);
    sendDispositionHeader(overhead);
    sendContentTypeHeader(overhead);
    sendTransferEncodingHeader(overhead);
    sendEndOfHeader(overhead);
    sendEnd(overhead);
    return overhead.size() + lengthOfData();
  }

  /**
   * Return a string representation of this object.
   * @return A string representation of this object.
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.getName();
  }

  /**
   * Write all parts and the last boundary to the specified output stream.
   *
   * @param out The stream to write to.
   * @param parts The parts to write.
   * @param partBoundary The ASCII bytes to use as the part boundary.
   *
   * @throws IOException If an I/O error occurs while writing the parts.
   *
   * @since 3.0
   */
  public static void sendParts(final OutputStream out, final Part[] parts, final byte[] partBoundary)
  throws IOException {

    if (parts == null) {
      throw new IllegalArgumentException("Parts may not be null");
    }
    if (partBoundary == null || partBoundary.length == 0) {
      throw new IllegalArgumentException("partBoundary may not be empty");
    }
    for (int i = 0; i < parts.length; i++) {
      // set the part boundary before the part is sent
      parts[i].setPartBoundary(partBoundary);
      parts[i].send(out);
    }
    out.write(EXTRA_BYTES);
    out.write(partBoundary);
    out.write(EXTRA_BYTES);
    out.write(CRLF_BYTES);
  }

  /**
   * Gets the length of the multipart message including the given parts.
   *
   * @param parts The parts.
   * @param partBoundary The ASCII bytes to use as the part boundary.
   * @return The total length
   *
   * @throws IOException If an I/O error occurs while writing the parts.
   *
   * @since 3.0
   */
  public static long getLengthOfParts(final Part[] parts, final byte[] partBoundary) throws IOException {
    if (DEBUG) { Log.v(TAG, "getLengthOfParts(Parts[])"); }
    if (parts == null) {
      throw new IllegalArgumentException("Parts may not be null");
    }
    long total = 0;
    for (int i = 0; i < parts.length; i++) {
      // set the part boundary before we calculate the part's length
      parts[i].setPartBoundary(partBoundary);
      final long l = parts[i].length();
      if (l < 0) {
        return -1;
      }
      total += l;
    }
    total += EXTRA_BYTES.length;
    total += partBoundary.length;
    total += EXTRA_BYTES.length;
    total += CRLF_BYTES.length;
    return total;
  }
}
