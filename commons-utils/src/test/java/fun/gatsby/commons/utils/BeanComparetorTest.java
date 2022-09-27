package fun.gatsby.commons.utils;

import junit.framework.TestCase;

import java.util.Set;

public class BeanComparetorTest extends TestCase {

    public void testCompare() {
        Set<Integer> s1 = Set.of(1, 2);
        Set<Integer> s2 = Set.of(1, 2, 3);
        boolean compare = BeanComparator.compare(s1, s2);
    }
}