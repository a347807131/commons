package fun.gatsby.commons.lang.date;

import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.FastDateFormat;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public class Period {

    Date startDate;
    Date endDate;

    public Period(Date start, Date end) {
        this.startDate = start;
        this.endDate = end;
    }

    public Period(String start, String end, String pattern) throws ParseException {
        var format = new SimpleDateFormat(pattern);
        this.startDate = format.parse(start);
        this.endDate = format.parse(end);
    }

    public List<Period> divideByMonth() {
        return divideBy(Calendar.MONTH);
    }

    public List<Period> divideByYear() {
        return divideBy(Calendar.YEAR);
    }

    public List<Period> divideBy(int filed) {
        LinkedList<Period> periods = new LinkedList<>();
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        endCalendar.setTime(endDate);
        int yearOfEndDate = endCalendar.get(filed);
        int yearOfStartDate = startCalendar.get(filed);
        var years = yearOfEndDate - yearOfStartDate + 1;
        for (int i = 0; i < years - 1; i++) {
            var sd = startCalendar.getTime();
            startCalendar.add(filed, 1);
            var ed = DateUtils.truncate(startCalendar, filed).getTime();
            startCalendar.setTime(ed);
            periods.add(new Period(sd, ed));
        }
        periods.add(new Period(startCalendar.getTime(), endDate));
        return periods;
    }

    public Period intersectedTo(Period period) {
        if (endDate.before(period.startDate) || period.endDate.before(startDate)) {
            //无交集
            return null;
        }
        var dates = List.of(startDate, endDate, period.startDate, period.endDate);
        var sortedDates = dates.stream().sorted(Date::compareTo).collect(Collectors.toList());
        return new Period(sortedDates.get(1), sortedDates.get(2));
    }

    public List<Period> uniformlyDivid(int num) {
        long gap = endDate.getTime() - startDate.getTime();
        long stepLen = gap / num;
        LinkedList<Period> periods = new LinkedList<>();
        for (int i = 0; i < num; i++) {
            var s = new Date(startDate.getTime() + stepLen * i);
            var e = new Date(startDate.getTime() + stepLen * (i + 1));
            periods.add(new Period(s, e));
        }
        return periods;
    }
}
