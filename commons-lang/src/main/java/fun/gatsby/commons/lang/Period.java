package fun.gatsby.commons.lang;

import lombok.Getter;
import lombok.ToString;

import java.text.DateFormat;
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

//    public Period(String start, String end, DateFormat dateFormat)  {
//        this.startDate = dateFormat.parse(start);
//        this.endDate = dateFormat.parse(end);
//    }

    public List<Period> divideByYear() {
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        endCalendar.setTime(endDate);

        List<Period> periods = new LinkedList<>();

        while (startCalendar.compareTo(endCalendar) < 0) {
            Date tempStartDate = startCalendar.getTime();
            startCalendar.set(startCalendar.get(Calendar.YEAR), Calendar.DECEMBER, 31);

            Date tempEndDate;
            if (startCalendar.compareTo(endCalendar) >= 0) {
                tempEndDate = endCalendar.getTime();
            } else {
                tempEndDate = startCalendar.getTime();
            }
            Period period = new Period(tempStartDate, tempEndDate);
            periods.add(period);

            if (startCalendar.compareTo(endCalendar) >= 0) {
                break;
            }
            startCalendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return periods;
    }

    public List<Period> divideByMonth() {

        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        endCalendar.setTime(endDate);

        List<Period> periods = new LinkedList<>();

        while (startCalendar.compareTo(endCalendar) < 0) {
            Date tempStartDate = startCalendar.getTime();

            startCalendar.add(Calendar.MONTH, 1);
            startCalendar.set(Calendar.DAY_OF_MONTH, 1);
            startCalendar.add(Calendar.DAY_OF_YEAR, -1);


            Date tempEndDate;
            if (startCalendar.compareTo(endCalendar) >= 0) {
                tempEndDate = endCalendar.getTime();
            } else {
                tempEndDate = startCalendar.getTime();
            }
            Period period = new Period(tempStartDate, tempEndDate);
            periods.add(period);

            if (startCalendar.compareTo(endCalendar) >= 0) {
                break;
            }
            startCalendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return periods;
    }

    public Period intersectedTo(Period period) {
        if (endDate.before(period.startDate) || period.endDate.before(startDate)) {
            //无交集
            return null;
        }
        List<Date> dates = List.of(startDate, endDate, period.startDate, period.endDate);
        List<Date> sortedDates = dates.stream().sorted(Date::compareTo).collect(Collectors.toList());
        return new Period(sortedDates.get(1), sortedDates.get(2));
    }
}
