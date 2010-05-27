package net.tsuraan.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;
import java.util.Set;
import java.io.IOException;
import java.io.Reader;
import java.io.File;

/** This analyzer mimics the behaviour of the built-in StandardAnalyzer, but
 * tacks a final NormalizingFilter onto the end of the filter chain so that any
 * funny unicode entities are replaced with good characters instead.  This
 * class isn't as flexible as the real StandardAnalyzer; Version is hard-coded
 * to 3.0, stopwords aren't configurable, and it also uses the default mode of
 * the Normalizing Filter.  Adding those options into the constructor wouldn't
 * be a ton of work, but I just don't need it.
 */
public final class StandardNormalizingAnalyzer extends Analyzer
{
  private final boolean replaceInvalidAcronym = true;
  private final boolean enableStopPositionIncrements;
  private final Version matchVersion = Version.LUCENE_30;
  private static final int DEFAULT_MAX_TOKEN_LENGTH = 255;
  private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

  public StandardNormalizingAnalyzer()
  {
    this.enableStopPositionIncrements =
      StopFilter.getEnablePositionIncrementsVersionDefault(matchVersion);
  }

  @Override
  public TokenStream tokenStream(String _fieldName, Reader reader)
  {
    Streams streams = genStreams(reader);
    streams.tokenStream.setMaxTokenLength(this.maxTokenLength);
    return streams.filteredTokenStream;
  }

  @Override
  public TokenStream reusableTokenStream(String _fieldName, Reader reader)
    throws IOException
  {
    Streams streams = (Streams)getPreviousTokenStream();
    if(streams == null) {
      streams = genStreams(reader);
      setPreviousTokenStream(streams);
    }
    else {
      streams.tokenStream.reset(reader);
    }
    streams.tokenStream.setMaxTokenLength(this.maxTokenLength);
    streams.tokenStream.setReplaceInvalidAcronym(this.replaceInvalidAcronym);
    return streams.filteredTokenStream;
  }

  private final class Streams
  {
    StandardTokenizer tokenStream;
    TokenStream filteredTokenStream;
  }

  /** Generate a new token stream and filter chain for it
   */
  private Streams genStreams(Reader reader)
  {
    Streams streams = new Streams();
    streams.tokenStream = new StandardTokenizer(this.matchVersion, reader);
    streams.filteredTokenStream = new StandardFilter(streams.tokenStream);
    streams.filteredTokenStream = new NormalizingFilter(
        streams.filteredTokenStream);
    streams.filteredTokenStream = new LowerCaseFilter(
        streams.filteredTokenStream);
    streams.filteredTokenStream = new StopFilter(
        this.enableStopPositionIncrements,
        streams.filteredTokenStream,
        StandardAnalyzer.STOP_WORDS_SET);
    return streams;
  }
}
