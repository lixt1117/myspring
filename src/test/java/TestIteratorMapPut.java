import java.util.*;

/**
 * @Auther: lixiaotian
 * @Date: 2020/1/15 16:22
 * @Description: 在spring初始化时，由于在遍历map时进行put操作导致了java.util
 * .ConcurrentModificationException，在此测试如何遍历map时进行添加元素
 */
public class TestIteratorMapPut {
    public static void main(String[] args) {
        Map<String, Integer> hashMap = new HashMap<>();
        hashMap.put("1", 1);
        hashMap.put("2", 2);
        hashMap.put("3", 3);
        hashMap.put("4", 4);
        hashMap.put("5", 5);
        hashMap.put("6", 6);
//        iteratorPut1(hashMap);
//        iteratorPut2(hashMap);
//        iteratorPut3(hashMap);
        iteratorPut4(hashMap);
        System.out.println(hashMap.get("7"));
    }

    /**
     * @Description:迭代map进行put操作，会报ConcurrentModificationException
     * @Param:
     * @Return:
     * @auther: lixiaotian
     * @date: 2020/1/15 16:28
     */
    private static void iteratorPut1(Map<String, Integer> hashMap) {
        for (String s : hashMap.keySet()) {
            if (s.equals("3")) {
                hashMap.put("7", 7);
            }
        }
    }

    /**
     * @Description:网上说的正确的迭代map进行put操作（），结果还是报错
     * @Param:
     * @Return:
     * @auther: lixiaotian
     * @date: 2020/1/15 16:29
     */
    private static void iteratorPut2(Map<String, Integer> hashMap) {
        Set<String> set = hashMap.keySet();
        Iterator<String> iterator =
                set.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals("3")) {
                hashMap.put("7", 7);
            }
        }
    }

    /**
     *
     * @Description:用了stream还是不行。。。
     * @Param:
     * @Return:
     * @auther: lixiaotian
     * @date: 2020/1/15 16:53
     */
    private static void iteratorPut3(Map<String, Integer> hashMap) {
        hashMap.keySet().stream().forEach(str -> {
            if (str.equals("3")) {
                hashMap.put("7", 7);
            }
        });
    }
    /**
     *
     * @Description:把set转换成list再进行for-each就行了，或者老老实实用for循环应该也可以
     * @Param:
     * @Return:
     * @auther: lixiaotian
     * @date: 2020/1/15 17:24
     */
    private static void iteratorPut4(Map<String, Integer> hashMap) {
        Set<String> set = hashMap.keySet();
        List<String> strings = new ArrayList<>(set);
        for (String s : strings) {
            if (s.equals("3")){
                hashMap.put("7",7);
            }
        }
    }
}
