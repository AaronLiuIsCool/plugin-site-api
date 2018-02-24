package io.jenkins.plugins.services;

import io.jenkins.plugins.services.impl.HttpClientWikiService;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WikiServiceTest {

  private HttpClientWikiService wikiService;

  @Before
  public void setUp() {
    wikiService = new HttpClientWikiService();
    wikiService.postConstruct();
  }

  @Test
  public void testGetWikiContent() {
    final String url = "https://wiki.jenkins.io/display/JENKINS/Git+Plugin";
    final String content = wikiService.getWikiContent(url);
    Assert.assertNotNull("Wiki content is null", content);
    Assert.assertFalse("Wiki content is empty", content.isEmpty());
  }

  @Test
  @Ignore("It's unclear what this is supposed to test")
  public void testGetWikiContent404() {
    final String url = "https://wiki.jenkins.io/display/JENKINS/nonexistant?foo";
    final String content = wikiService.getWikiContent(url);
    Assert.assertNotNull("Wiki content is null", content);
    Assert.assertEquals(HttpClientWikiService.getNonWikiContent(url), content);
  }

  @Test
  public void testGetWikiContentNotJenkins() {
    final String url = "https://www.google.com";
    final String content = wikiService.getWikiContent(url);
    Assert.assertNotNull("Wiki content is null", content);
    Assert.assertEquals(HttpClientWikiService.getNonWikiContent(url), content);
  }

  @Test
  public void testGetWikiContentNoUrl() {
    final String content = wikiService.getWikiContent(null);
    Assert.assertNotNull("Wiki content is null", content);
    Assert.assertEquals(HttpClientWikiService.getNoDocumentationFound(), content);
  }


  @Test
  public void testCleanWikiContent() throws IOException {
    final String url = "https://wiki.jenkins.io/display/Git+Plugin";
    final File file = new File("src/test/resources/wiki_content.html");
    final String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    final String cleanContent = wikiService.cleanWikiContent(content, url);
    Assert.assertNotNull("Wiki content is null", cleanContent);
    final Document html = Jsoup.parseBodyFragment(cleanContent);
    html.getElementsByAttribute("href").forEach(element -> {
      final String value = element.attr("href");
      Assert.assertFalse("Wiki content not clean - href references to root : " + value, value.startsWith("/"));
    });
    html.getElementsByAttribute("src").forEach(element -> {
      final String value = element.attr("src");
      Assert.assertFalse("Wiki content not clean - src references to root : " + value, value.startsWith("/"));
    });
  }

  @Test
  public void testReplaceAttribute() throws IOException {
    final String baseUrl = "https://wiki.jenkins.io";
    final String src = "/some-image.jpg";
    final Element element = Jsoup.parseBodyFragment(String.format("<img id=\"test-image\" src=\"%s\"/>", src)).getElementById("test-image");
    wikiService.replaceAttribute(element, "src", baseUrl);
    Assert.assertEquals("Attribute replacement failed", baseUrl + src, element.attr("src"));
  }

}
