package fun.gatsby.commons.lang;

import junit.framework.TestCase;
import org.junit.Assert;

import java.text.ParseException;
import java.util.List;

public class PeriodTest extends TestCase {

    public void testDivideByMonth() throws ParseException {
        Period period = new Period("2022-01-01", "2022-05-05", "yyyy-MM-dd");
        List<Period> periods = period.divideByMonth();
        Assert.assertEquals(5, periods.size());
    }

    public void testIntersectedTo() throws ParseException {
        Period period = new Period("2022-01-01", "2022-05-05", "yyyy-MM-dd");
        Period period2 = new Period("2022-02-01", "2022-05-15", "yyyy-MM-dd");
        Period intersected = period2.intersectedTo(period);
        Assert.assertEquals(intersected.startDate, period2.startDate);
        Assert.assertEquals(intersected.endDate, period.endDate);

    }
}