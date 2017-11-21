package tzeth.exhume;

import tzeth.exhume.XmlBuilder;

public class XmlBuilderTest {

    public static void main(String[] args) {
        XmlBuilder xml = new XmlBuilder();
        xml.root("Player").withAttribute("id", "abc-123")
            .child("Name").withValue("George Smith").close()
            .child("DateOfBirth").withValue("1920-07-08").close()
            .child("GameLog")
                .child("Game").withAttribute("Date", "1942-08-31").close()
                .child("Game").withAttribute("Date", "1942-09-03").close()
                .child("Game").withAttribute("Date", "1942-09-07").close().close();
        System.out.println(xml.toXml());
    }

}
