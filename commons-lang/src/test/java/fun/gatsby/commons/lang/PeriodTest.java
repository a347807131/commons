package fun.gatsby.commons.lang;

import junit.framework.TestCase;
import org.junit.Assert;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class PeriodTest extends TestCase {

    DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
    public void testDivideByMonth() throws ParseException {
        Period period=new Period(dateFormat.parse("2018-01-01"),dateFormat.parse("2018-05-31"));
        List<Period> periods = period.divideByMonth();
        Assert.assertEquals(5, periods.size());
    }

    public void testIntersectedTo() throws ParseException {
        Period period=new Period(dateFormat.parse("2022-02-01"),dateFormat.parse("2022-05-15"));
        Period period2=new Period(dateFormat.parse("2022-02-05"),dateFormat.parse("2022-05-31"));
        Period intersected = period2.intersectedTo(period);
        Assert.assertEquals(intersected.startDate, period2.startDate);
        Assert.assertEquals(intersected.endDate, period.endDate);

    }
}