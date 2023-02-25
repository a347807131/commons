package fun.gatsby.commons.utils;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class SnowflakeKeyGeneratorTest extends TestCase {

    public void testGenerateKey() {
        SnowFlakeKeyGenerator snowFlakeKeyGenerator = new SnowFlakeKeyGenerator();
        snowFlakeKeyGenerator.generateKey();
        long startTime = System.currentTimeMillis();
        SnowFlake idWorker = new SnowFlake(0, 0);
        Set<Object> set = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            long id = idWorker.nextId();
            set.add(id);
            log.info("id----"+i+":"+id);
        }
        long endTime = System.currentTimeMillis();
        log.info("set.size():" + set.size());
        log.info("endTime-startTime:" + (endTime - startTime));
    }
}