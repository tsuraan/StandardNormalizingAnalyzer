package net.tsuraan.lucene;

import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import java.text.Normalizer;
import java.nio.CharBuffer;
import java.io.IOException;

/** A filter that performs Unicode normalization over tokens.  This is pretty
 * much a copy of the lowercase filter, but instead of replacing chars with
 * their lower versions, it replaces full tokens (I hope) with their normalized
 * versions.  
 */
public class NormalizingFilter extends TokenFilter
{
  private TermAttribute termAtt;
  private Normalizer.Form form;

  public NormalizingFilter(TokenStream in) {
    super(in);
    this.termAtt = addAttribute(TermAttribute.class);
    this.form = Normalizer.Form.NFKC;
  }

  public NormalizingFilter(TokenStream in, Normalizer.Form form) {
    super(in);
    this.termAtt = addAttribute(TermAttribute.class);
    this.form = form;
  }

  @Override
  public boolean incrementToken() throws IOException {
    if(input.incrementToken()) {
      CharBuffer b = CharBuffer.wrap(
          termAtt.termBuffer(), 
          0,
          termAtt.termLength());
      if(!Normalizer.isNormalized(b, this.form)) {
        String normal = Normalizer.normalize(b, this.form);
        termAtt.setTermBuffer(normal);
      }
      return true;
    }
    else {
      return false;
    }
  }
}
