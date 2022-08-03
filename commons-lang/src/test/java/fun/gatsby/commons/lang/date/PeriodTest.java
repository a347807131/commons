package fun.gatsby.commons.lang.date;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
public class PeriodTest extends TestCase {

    DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");

    public void testDivideByMonth() throws ParseException {
        Period period=new Period(dateFormat.parse("2018-01-01"),dateFormat.parse("2018-05-31"));
        List<Period> periods = period.divideByMonth();
        Assert.assertEquals(5, periods.size());
    }

    public void testIntersectedTo() throws ParseException {
        Period period = new Period(dateFormat.parse("2022-02-01"), dateFormat.parse("2022-05-15"));
        Period period2 = new Period(dateFormat.parse("2022-02-05"), dateFormat.parse("2022-05-31"));
        Period intersected = period2.intersectedWith(period);
        Assert.assertEquals(intersected.startDate, period2.startDate);
        Assert.assertEquals(intersected.endDate, period.endDate);
    }

    public void testUniformlyDivid() throws ParseException {
        Period period = new Period(dateFormat.parse("2022-02-01"), dateFormat.parse("2022-02-05"));
        List<Period> periods = period.uniformlyDivid(4);
        Assert.assertEquals(4, periods.size());
        Assert.assertEquals(periods.get(1).startDate, dateFormat.parse("2022-02-02"));
        Assert.assertEquals(periods.get(2).startDate, dateFormat.parse("2022-02-03"));
    }

    public void testDivideByYear() throws ParseException {
        var period = new Period(dateFormat.parse("2022-02-01"), dateFormat.parse("2022-02-05"));
        List<Period> periods = period.divideByYear();
        var p = periods.get(0);
        long gap = p.getEndDate().getTime() - p.getStartDate().getTime();
        periods.forEach(e -> {
            Assert.assertEquals(gap, e.endDate.getTime() - e.startDate.getTime());
        });
    }

    public void testCompletedWith() throws ParseException {
        Period period = new Period(dateFormat.parse("2022-02-01"), dateFormat.parse("2022-02-15"));
        Period period2 = new Period(dateFormat.parse("2022-05-05"), dateFormat.parse("2022-05-31"));
        Period completed = period2.completedWith(period);
        Assert.assertEquals(completed.endDate, period2.startDate);
        Assert.assertEquals(completed.startDate, period.endDate);
    }
}