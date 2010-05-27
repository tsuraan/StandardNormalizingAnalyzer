package net.tsuraan.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.Version;

import org.junit.*;
import static org.junit.Assert.*;

public class StandardNormalizingAnalyzerTest
{
  private IndexWriter writer;
  private QueryParser parser;
  private final String field = "test";

  public StandardNormalizingAnalyzerTest() {
    this.parser = new QueryParser(Version.LUCENE_30, this.field,
        new StandardNormalizingAnalyzer());
  }

  /** Ensure that the basic indexing/searching things still work nicely. */
  @Test public void testBasic() throws Throwable {
    addPhrase("some data");
    addPhrase("some more stuff");
    addPhrase("and a stopword");

    assertResultCount("some", 2);
    assertResultCount("a", 0);
    assertResultCount("stopword", 1);
  }

  /** Make sure that kanji still works sanely. */
  @Test public void testKanji() throws Throwable {
    addPhrase("香港版");
    addPhrase("港版");

    assertResultCount("香", 1);
    assertResultCount("港", 2);
    assertResultCount("版", 2);
    assertResultCount("港版", 2);
    assertResultCount("\"港版\"", 2);
    assertResultCount("\"版港\"", 0);
  }

  /** And, actually test some normalization features. */
  @Test public void testNormalization() throws Throwable {
    addPhrase("eﬃcient"); // ligature
    addPhrase("ＣＡＴ");  // unicode fat versions of ascii characters

    assertResultCount("efficient", 1);
    assertResultCount("cat", 1);
    assertResultCount("efficient cat", 2);

    assertResultCount("eﬃcient", 1);
    assertResultCount("ＣＡＴ", 1);
    assertResultCount("eﬃcient cat", 2);
    assertResultCount("efficient ＣＡＴ", 2);
    assertResultCount("eﬃcient ＣＡＴ", 2);
  }

  public final static void main(String[] args)
  {
    org.junit.runner.JUnitCore.main(
        "net.tsuraan.lucene.StandardNormalizingAnalyzerTest");
  }

  @Before public void setup() throws Throwable {
    this.writer = new IndexWriter(
        new RAMDirectory(),
        new StandardNormalizingAnalyzer(),
        true,
        IndexWriter.MaxFieldLength.UNLIMITED);
  }

  private void addPhrase(String text) throws Throwable {
    Document d = new Document();
    d.add(new Field(this.field, text, Field.Store.NO, Field.Index.ANALYZED));
    this.writer.addDocument(d);
  }

  private void assertResultCount(String query, int hits) throws Throwable {
    this.writer.commit();
    IndexSearcher searcher = new IndexSearcher(this.writer.getReader());
    try {
      assertEquals(
          hits,
          searcher.search(this.parser.parse(query), hits+1).totalHits);
    }
    finally {
      searcher.close();
    }
  }
}
