package webhook.teamcity.jaxb;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.joda.time.DateMidnight;
 
public class DateMidnightAdapter
    extends XmlAdapter<String, DateMidnight> {
 
    public DateMidnight unmarshal(String v) throws Exception {
        return new DateMidnight(v);
    }
 
    public String marshal(DateMidnight v) throws Exception {
        return v.toString();
    }
 
}