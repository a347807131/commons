package fun.gatsby.commons.lang.date;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日期段
 *
 * @author Gatsby
 */
@Getter
@EqualsAndHashCode
@ToString
public class Period implements Comparable<Period> {

    static final Date DEFAULT_START_DATE = new Date(0);
    static final Date DEFAULT_END_DATE = new Date(Long.MAX_VALUE);

    static final Period DEFAULT_PERIOD = new Period(DEFAULT_START_DATE, DEFAULT_END_DATE);
    /**
     * 默认日期转换格式：yyyy-MM-dd
     *
     * @see DateFormatUtils#ISO_DATE_FORMAT
     */
    static final String DEFAULT_FORMAT_STRING = DateFormatUtils.ISO_DATE_FORMAT.getPattern();

    /**
     * 开始日期
     */
    final Date startDate;
    /**
     * 结束日期
     */
    final Date endDate;

    Period() {
        this.startDate = DEFAULT_START_DATE;
        this.endDate = DEFAULT_END_DATE;
    }

    /**
     * @param start 开始日期
     * @param end   结束日期
     */
    public Period(Date start, Date end) {
        this.startDate = start;
        this.endDate = end;
    }

    public Period(String start, String end) throws ParseException {
        this(start, end, DEFAULT_FORMAT_STRING);
    }

    public Period(String start, String end, String pattern) throws ParseException {
        var format = new SimpleDateFormat(pattern);
        this.startDate = format.parse(start);
        this.endDate = format.parse(end);
    }

    public Period(long left, long right) {
        this.startDate = new Date(left);
        this.endDate = new Date(right);
    }

    public List<Period> divideByMonth() {
        return divideBy(Calendar.MONTH);
    }

    public List<Period> divideByYear() {
        return divideBy(Calendar.YEAR);
    }

    /**
     * 根据日期字段分割日期
     *
     * @param field 日期字段
     * @return 分割后的日期段
     * @see Calendar
     * <pre>
     *     Calendar.YEAR - 年<br>
     *     Calendar.MONTH - 月<br>
     *     Calendar.DATE - 日<br>
     *     ...
     * </pre>
     */
    public List<Period> divideBy(int field) {
        LinkedList<Period> periods = new LinkedList<>();
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        endCalendar.setTime(endDate);
        int yearOfEndDate = endCalendar.get(field);
        int yearOfStartDate = startCalendar.get(field);
        var years = yearOfEndDate - yearOfStartDate + 1;
        for (int i = 0; i < years - 1; i++) {
            var sd = startCalendar.getTime();
            startCalendar.add(field, 1);
            var ed = DateUtils.truncate(startCalendar, field).getTime();
            startCalendar.setTime(ed);
            periods.add(new Period(sd, ed));
        }
        periods.add(new Period(startCalendar.getTime(), endDate));
        return periods;
    }

    public Period intersecteWith(Period period) {
        if (endDate.before(period.startDate) || period.endDate.before(startDate)) {
            //无交集
            return null;
        }
        var dates = List.of(startDate, endDate, period.startDate, period.endDate);
        var sortedDates = dates.stream().sorted(Date::compareTo).collect(Collectors.toList());
        return new Period(sortedDates.get(1), sortedDates.get(2));
    }

    /**
     * 并
     *
     * @param period
     * @return
     */
    public Period unionWith(Period period) {
        return null;
    }

    /**
     * 获取两个时间段的补集，不包括正负无穷段
     *
     * @param period 时间段
     * @return 补集
     */
    public Period completeWith(Period period) {
        var intersectedPeriod = intersecteWith(period);
        if (intersectedPeriod != null) {
            return null;
        }
        var dates = List.of(startDate, endDate, period.startDate, period.endDate);
        var sortedDates = dates.stream().sorted(Date::compareTo).collect(Collectors.toList());
        return new Period(sortedDates.get(1), sortedDates.get(2));
    }

    public List<Period> uniformlyDivide(int num) {
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

    public List<Period> divideByDuration(Duration duration) {
        long gap = endDate.getTime() - startDate.getTime();
        long len = gap / (duration.getSeconds() * 1000);
        long step = gap / len;
        LinkedList<Period> periods = new LinkedList<>();
        for (int i = 0; i < len; i++) {
            var s = new Date(startDate.getTime() + i * step);
            var e = new Date(startDate.getTime() + (i + 1) * step);
            Period period = new Period(s, e);
            periods.add(period);
        }
        var date = new Date(startDate.getTime() + (len + 1) * step);
        if (date.after(endDate)) {
            Period period = new Period(date, endDate);
            periods.add(period);
        }
        return periods;
    }

    @Override
    public int compareTo(Period period) {
        return startDate.compareTo(period.startDate);
    }
}
