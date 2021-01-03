@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(type=DateTime.class, 
        value=DateTimeAdapter.class),
    @XmlJavaTypeAdapter(type=LocalDate.class, 
        value=LocalDateAdapter.class),
    @XmlJavaTypeAdapter(type=LocalTime.class, 
        value=LocalTimeAdapter.class),
    @XmlJavaTypeAdapter(type=LocalDateTime.class, 
        value=LocalDateTimeAdapter.class)
})
package webhook.teamcity.statistics;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import webhook.teamcity.jaxb.DateTimeAdapter;
import webhook.teamcity.jaxb.LocalDateAdapter;
import webhook.teamcity.jaxb.LocalDateTimeAdapter;
import webhook.teamcity.jaxb.LocalTimeAdapter;