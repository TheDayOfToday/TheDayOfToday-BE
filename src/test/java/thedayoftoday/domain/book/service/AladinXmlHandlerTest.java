package thedayoftoday.domain.book.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AladinXmlHandlerTest {

    private List<Map<String, String>> parse(String xml) throws Exception {
        AladinXmlHandler handler = new AladinXmlHandler();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(false); // 핸들러가 qName만 사용
        SAXParser parser = factory.newSAXParser();
        parser.parse(new InputSource(new StringReader(xml)), handler);
        return handler.getItems();
    }

    @Test
    @DisplayName("단일 item: title/author/description/cover를 파싱한다")
    void parse_single_item() throws Exception {
        // given
        String xml = """
            <rss><channel>
              <item>
                <title>테스트 제목</title>
                <author>홍길동</author>
                <description> 간단 설명 </description>
                <cover>http://example.com/cover.jpg</cover>
              </item>
            </channel></rss>
            """;

        // when
        List<Map<String, String>> items = parse(xml);

        // then
        assertThat(items).hasSize(1);
        Map<String, String> item = items.get(0);
        assertThat(item.get("title")).isEqualTo("테스트 제목");
        assertThat(item.get("author")).isEqualTo("홍길동");
        assertThat(item.get("description")).isEqualTo("간단 설명"); // trim() 적용
        assertThat(item.get("cover")).isEqualTo("http://example.com/cover.jpg");
    }

    @Test
    @DisplayName("복수 item + item 밖 태그는 무시")
    void parse_multiple_items_and_ignore_outside() throws Exception {
        // given
        String xml = """
            <rss><channel>
              <title>OUTSIDE</title>
              <item>
                <title>첫 번째</title><author>A</author>
                <description>desc1</description><cover>c1.jpg</cover>
              </item>
              <item>
                <title>두 번째</title><author>B</author>
                <description>desc2</description><cover>c2.jpg</cover>
              </item>
            </channel></rss>
            """;

        // when
        List<Map<String, String>> items = parse(xml);

        // then
        assertThat(items).hasSize(2);
        assertThat(items.get(0).get("title")).isEqualTo("첫 번째");
        assertThat(items.get(1).get("cover")).isEqualTo("c2.jpg");
        // item 바깥의 title은 저장되지 않아야 함
        assertThat(items.get(0)).doesNotContainKey("OUTSIDE");
    }

    @Test
    @DisplayName("대소문자 혼용 태그를 정상 파싱 (equalsIgnoreCase/소문자화 처리)")
    void parse_case_insensitive_tags() throws Exception {
        // given
        String xml = """
            <rss><channel>
              <ITEM>
                <TITLE>  공백 포함 제목  </TITLE>
                <AUTHOR>AUTHOR NAME</AUTHOR>
                <DESCRIPTION>   </DESCRIPTION>
                <COVER>Cover.JPG</COVER>
              </ITEM>
            </channel></rss>
            """;

        // when
        List<Map<String, String>> items = parse(xml);

        // then
        assertThat(items).hasSize(1);
        Map<String, String> item = items.get(0);
        assertThat(item.get("title")).isEqualTo("공백 포함 제목"); // trim()
        assertThat(item.get("author")).isEqualTo("AUTHOR NAME");
        assertThat(item.get("description")).isEqualTo(""); // 공백만 있으면 빈 문자열
        assertThat(item.get("cover")).isEqualTo("Cover.JPG");
    }

    @Test
    @DisplayName("정의되지 않은 태그(price 등)는 무시")
    void parse_ignores_unknown_tags() throws Exception {
        // given
        String xml = """
            <rss><channel>
              <item>
                <title>T</title><author>A</author>
                <price>10000</price>
                <description>D</description><cover>C.jpg</cover>
              </item>
            </channel></rss>
            """;

        // when
        List<Map<String, String>> items = parse(xml);

        // then
        assertThat(items).hasSize(1);
        Map<String, String> item = items.get(0);
        assertThat(item).doesNotContainKey("price");
        assertThat(item.get("title")).isEqualTo("T");
        assertThat(item.get("cover")).isEqualTo("C.jpg");
    }
}
