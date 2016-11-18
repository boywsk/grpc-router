import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangshikai on 2016/11/14.
 */
public class Test {
    public static void main(String[] args) {
//        System.out.println(Test.class.getName());
//        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("执行！");
//            }
//        },0,30000, TimeUnit.MILLISECONDS);
        ConcurrentSkipListSet<String> set = new ConcurrentSkipListSet<>();
        set.add("1");
        set.add("2");
        set.add("3");
        set.add("4");
        set.add("5");
        set.add("6");
        System.out.println(set.toArray()[3]);
        System.out.println(set.toArray()[4]);
        System.out.println(set.toArray()[5]);

        System.out.println(set.toArray()[3]);
        System.out.println(set.toArray()[4]);
        System.out.println(set.toArray()[5]);

        System.out.println(set.toArray()[3]);
        System.out.println(set.toArray()[4]);
        System.out.println(set.toArray()[5]);

    }
}
