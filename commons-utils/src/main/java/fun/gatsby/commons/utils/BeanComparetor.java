package fun.gatsby.commons.utils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class BeanComparetor {

    public static boolean compare(Object o1, Object o2) {
        return compare(o1, o2, Collections.emptySet());
    }

    public static boolean compare(@NonNull Object o1, @NonNull Object o2, Set<String> ignoredFields) {
        try {
            Map<String, String> mapOfO1 = BeanUtils.describe(o1);
            Map<String, String> mapOfO2 = BeanUtils.describe(o2);
            Set<String> commonKeys = mapOfO1.keySet().stream().filter(mapOfO2::containsKey).collect(Collectors.toSet());
            if (commonKeys.size() == 0) {
                log.warn("no common field exits in the args to compare with each other");
                return false;
            }
            for (String commonKey : commonKeys) {
                String s1 = mapOfO1.get(commonKey);
                String s2 = mapOfO2.get(commonKey);
                boolean b = Objects.equals(s1, s2);
                if (!b) {
                    if (ignoredFields.contains(commonKey)) {
                        continue;
                    }
                    log.warn("{}.{} compared false ({}):({})", o1.getClass().getSimpleName(), commonKey, s1, s2);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("unexpected exception ocurred when comparing beans", e);
        }
    }
}
