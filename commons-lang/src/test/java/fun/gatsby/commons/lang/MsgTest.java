package fun.gatsby.commons.lang;

import fun.gatsby.commons.lang.dto.R;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MsgTest extends TestCase {

    public void testOf() {
        var msg =
                R.of("123", 0, System.out);
        log.debug("{}", R.ok(msg));
    }
}