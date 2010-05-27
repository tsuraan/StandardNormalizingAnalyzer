package net.tsuraan.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;
import java.util.Set;
import java.io.IOException;
import java.io.Reader;
import java.io.File;

/** This is an analyzer that wraps the StandardAnalyzer with a final Unicode
 * NormalizingFilter, so that unicode entities that don't make sense, like
 * ligatures or wide ascii characters, are replaced with sane versions of
 * themselves.  This uses the default normalization of the NormalizingFilter,
 * which is NFKC.  If that's undesirable, it's pretty easy to change.
 */
public class StandardNormalizingAnalyzer extends StandardAnalyzer
{
  public StandardNormalizingAnalyzer(Version v)
  {
    super(v);
  }

  public StandardNormalizingAnalyzer(Version v, Set<?> stopWords)
  {
    super(v, stopWords);
  }

  public StandardNormalizingAnalyzer(Version v, File stopwords)
    throws IOException
  {
    super(v, stopwords);
  }

  public StandardNormalizingAnalyzer(Version v, Reader stopwords)
    throws IOException
  {
    super(v, stopwords);
  }

  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    return new NormalizingFilter(super.tokenStream(fieldName, reader));
  }

  @Override
  public TokenStream reusableTokenStream(String fieldName, Reader reader)
    throws IOException
  {
    /* This uses the parent's reusable token stream, but makes a new
     * NormalizingFilter.  That's probably not the right way to do things, but
     * I'm not entirely sure why StandardAnalyzer's reusableTokenStream is so
     * complicated, so I don't know exactly what the right thing to do here is.
     */
    return new NormalizingFilter(super.reusableTokenStream(fieldName, reader));
  }
}
