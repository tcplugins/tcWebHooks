package webhook.teamcity.jaxb;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.joda.time.LocalDateTime;
 
public class LocalDateTimeAdapter
    extends XmlAdapter<String, LocalDateTime>{
 
    public LocalDateTime unmarshal(String v) throws Exception {
        return new LocalDateTime(v);
    }
 
    public String marshal(LocalDateTime v) throws Exception {
        return v.toString();
    }
 
}