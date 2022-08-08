package fun.gatsby.commons.utils;

import fun.gatsby.commons.lang.date.Period;
import fun.gatsby.commons.lang.tuple.Tuple2;
import junit.framework.TestCase;

import java.util.Date;
import java.util.List;

public class ParallelTest extends TestCase {

    public void testGen() {
        List<Period> periods = new Period(0, new Date().getTime()).divideByYear();
        Parallel<Period> parallel = new Parallel<>(10, periods);
        do {
            Tuple2<Integer, Period> pop = parallel.popFirst();
            System.out.println(pop);
        } while (!parallel.isEmpty());
    }
}
