package fun.gatsby.commons.lang;

import java.util.*;

//TODO
public class KVs extends HashMap<String, List<String>> {

    public KVs(Collection<Objects> list) {
        for (Objects objects : list) {
            String k = "";
            String v = "";
            add(k, v);
        }
    }

    void add(String k, String v) {
        List<String> vs = get(k);
        if (vs == null) {
            vs = new ArrayList<>();
        }
        vs.add(v);
    }
}
