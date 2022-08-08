package fun.gatsby.commons.utils;

import fun.gatsby.commons.lang.tuple.Tuple2;
import fun.gatsby.commons.lang.tuple.Tuples;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

@Data
public class Parallel<E> extends TreeSet<E> {

    int groups = 1;

    INumGenerator groupNumGenerator = new SerialGroupNumGenerator();


    public Parallel(E... els) {
        this.addAll(List.of(els));
    }

    public Parallel(Collection<E> periods) {
        this.addAll(periods);
    }

    public Parallel(int groups, Collection<E> els) {
        this.groups = groups;
        this.addAll(els);
    }

    public Tuple2<Integer, E> popFirst() {
        return Tuples.of(groupNumGenerator.gen(), this.pollFirst());
    }

    class RandomGroupNumGenerator implements INumGenerator {
        public int gen() {
            return (int) (Math.random() * 10 % groups);
        }
    }

    class SerialGroupNumGenerator implements INumGenerator {
        int currentGroupNum = 0;

        public int gen() {
            return (currentGroupNum++) % groups;
        }
    }

}
