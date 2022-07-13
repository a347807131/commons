package fun.gatsby.commons.utils;

import junit.framework.TestCase;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Set;

public class BeanComparetorTest extends TestCase {

    public void testCompare() {
        Set<Integer> s1 = Set.of(1, 2);
        Set<Integer> s2 = Set.of(1, 2, 3);
        boolean compare = BeanComparetor.compare(s1, s2);
        Tuple2<String, Integer> of = Tuples.of("", 1);
    }
}