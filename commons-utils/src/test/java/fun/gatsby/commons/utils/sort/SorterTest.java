package fun.gatsby.commons.utils.sort;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SorterTest extends TestCase {

    public void test() throws InvocationTargetException, IllegalAccessException {
        int[] nums=new int[1000*10*3];
        for (int i = 0; i < nums.length; i++) {
            nums[i]=(int)(Math.random()*1024*100);
        }

        List<Method> methods = Arrays.stream(Sorter.class.getMethods()).filter(e ->
                e.getReturnType().getName().equals("[I")
                && e.getModifiers()==Modifier.PUBLIC
                && e.getParameterTypes().length==1)
                .collect(Collectors.toList()
        );

        Sorter sorter = new Sorter();
        for (Method m : methods) {
            String methodName = m.getName();
            Object ints = Arrays.copyOf(nums, nums.length);
            long s = System.currentTimeMillis();
            Object out = m.invoke(sorter, ints);
            long  duration = System.currentTimeMillis()-s;
            int[] outArr = (int[]) out;
            Assert.assertTrue(ISortAlgorithm.exam(outArr));
            log.info("{}: cost {} ms",methodName,duration);
        }
    }
}