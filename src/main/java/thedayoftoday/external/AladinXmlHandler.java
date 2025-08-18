package thedayoftoday.external;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AladinXmlHandler extends DefaultHandler {
    private Map<String, String> currentItem;
    private boolean inItem = false;
    private StringBuilder content = new StringBuilder();
    private final List<Map<String, String>> items = new ArrayList<>();

    public List<Map<String, String>> getItems() {
        return items;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if ("item".equalsIgnoreCase(qName)) {
            currentItem = new HashMap<>();
            inItem = true;
        }
        content.setLength(0); // 문자열 버퍼 초기화
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        content.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (!inItem) return;

        String tag = qName.toLowerCase();
        String text = content.toString().trim();

        if ("item".equals(tag)) {
            items.add(currentItem);
            inItem = false;
        } else if (
                "title".equals(tag) ||
                        "author".equals(tag) ||
                        "description".equals(tag) ||
                        "cover".equals(tag)
        ) {
            currentItem.put(tag, text);
        }
    }
}
