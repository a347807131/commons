package fun.gatsby.commons.lang;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MsgTest extends TestCase {

    public void testOf() {
        var msg =
                Msg.of("123", 0, System.out);
        log.debug("{}", Msg.ok(msg));
    }
}