package com.hank.algorithm.datastructure.map;

import androidx.annotation.NonNull;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * Author: hank.liu
 * Date: 2023/7/18 16:54
 * Copyright: 2023 www.xgimi.com Inc. All rights reserved.
 * Desc:
 */
public class HashMap2<K, V> extends AbstractMap<K, V> implements Map<K, V> {

    Set<Entry<K, V>> entrySet; // 键值对集合

    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 默认初始容量大小：2的4次方 16
    static final int MAXIMUM_CAPACITY = 1 << 30; //最大容量：2的30次方,Integer.MAX_VALUE
    static final float DEFAULT_LOAD_FACTOR = 0.75f;// todo 加载因子，注意这里的一个思维逻辑，判断是否需要扩容是键值对的size超过了哈希表容量 * 加载因子才会扩容
    static final int TREEIFY_THRESHOLD = 8; //计数阈值至少为8转化为使用树而不是列表
    static final int UNTREEIFY_THRESHOLD = 6; //计数阈值小于6反树化，即红黑树转为列表
    static final int MIN_TREEIFY_CAPACITY = 64; //可对桶进行树化的最小表容量
    transient Node<K, V>[] table; //表在第一次使用时初始化，大小调整为必要的。在分配时，长度总是2的幂。在某些操作中，我们也允许长度为零。目前不需要的引导机制。)
    transient int size; //包含的键值映射的元素数量

    int threshold; // 哈希表扩容阈值
    float loadFactor;

    public HashMap2() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
    }

    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }

    private V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
        Node<K, V>[] tab; // 临时哈希表
        Node<K, V> p; // 当前key哈希映射下的结点
        int n; // 当前哈希表长度
        int i; // 当前key哈希映射下的哈希表index

        // 哈希表为空，初始化哈希表
        if ((tab = table) == null || (n = tab.length) == 0) {
            n = (tab = resize()).length;
        }
        // (n - 1) & hash相当于取模，获取数组的索引位置
        // p为空代表此哈希表位置没有映射过任何值，直接赋值到此哈希表位置即可
        if ((p = tab[i = (n - 1) & hash]) == null) {
            tab[i] = newNode(hash, key, value, null);
        } else {
            // 否则，发生了哈希碰撞，需要分情况处理
            Node<K, V> e;
            K k;
            // key相同且hash相同，直接将哈希表当前索引下的P结点取出
            if (p.hash == hash &&
                    ((k = p.key) == key || (key != null && key.equals(k)))) {
                e = p;
            } else if (p instanceof TreeNode) {
                // hash不相同，且p是红黑树结构，将其插入到红黑树中
                e = ((TreeNode<K, V>) p).putTreeVal(this, tab, hash, key, value);
            } else {
                // hash不相同，p是链表，遍历链表并考虑是否转换成红黑树
                for (int binCount = 0; ; ++binCount) {
                    // p下一个结点为空，说明遍历到了链表尾部，直接插入
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        // 如果链表长度大于TREEIFY_THRESHOLD，转换为红黑树
                        if (binCount >= TREEIFY_THRESHOLD - 1) {
                            treeifyBin(tab, hash);
                        }
                        break;
                    }
                    // 链表中存在key相同且hash相同的结点
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            // key相同的情况，重新赋值value，返回旧值
            if (e != null) {
                V oldValue = e.value;
                // onlyIfAbsent 表示是否仅在 oldValue 为 null 的情况下更新键值对的值
                if (!onlyIfAbsent || oldValue == null) {
                    e.value = value;
                }

                afterNodeAccess(e);
                return oldValue;
            }
        }

        ++modCount;
        // 检测是否需要扩容
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }


    // Callbacks to allow LinkedHashMap post-actions
    void afterNodeAccess(Node<K, V> p) {
    }

    void afterNodeInsertion(boolean evict) {
    }

    final void treeifyBin(Node<K, V>[] tab, int hash) {

    }

    // Create a regular (non-tree) node
    Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {
        return new Node<>(hash, key, value, next);

    }

    /**
     * 哈希表扩容
     *
     * @return
     */
    private Node<K, V>[] resize() {
        Node<K, V>[] oldTab = table;
        // 现有容量的大小，等于数组的长度，如果数组为空，返回0
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        // 现有的扩容阈值
        int oldThr = threshold;
        // newCap表示新的容量，newThr新的扩容阈值
        int newCap, newThr = 0;
        // 如果现有容量大于0，表示已经初始化过了
        if (oldCap > 0) {
            // 如果现有容量已经大于最大容量。结束扩容，直接返回
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            // 否则，如果扩大两倍之后的容量小于最大容量，且现有容量大于等于初始容量16
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                    oldCap >= DEFAULT_INITIAL_CAPACITY)
                // 新的扩容阀值扩大为两倍，左移<<1 相当于乘以2
                newThr = oldThr << 1; // double threshold
        } else if (oldThr > 0) {
            // 否则如果当前容量等于0 ，但是当前扩容阈值 > 0,调用有参构造函数会到这里
            // 进入这里，新的容量等于当前的扩容阈值，
            newCap = oldThr;
            // 否则如果当前容量等于0,并且挡墙扩容阈值=0，调用无参构造函数进入这里
        } else {
            // 新的容量等于默认容量
            newCap = DEFAULT_INITIAL_CAPACITY;
            // 新的扩容阈值等于默认负载因子0.75*默认容量16=12
            newThr = (int) (DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        // 如果新的扩容阈值等于0
        if (newThr == 0) {
            // 设置新的扩容阈值等于新的容量*负载因子
            float ft = (float) newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float) MAXIMUM_CAPACITY ?
                    (int) ft : Integer.MAX_VALUE);
        }
        // 设置hashmap对象的扩容阈值位新的扩容阈值
        threshold = newThr;
        @SuppressWarnings({"rawtypes", "unchecked"})
        // 初始化数组
        Node<K, V>[] newTab = (Node<K, V>[]) new Node[newCap];
        // 设置hashmap对象的桶数组为newTab
        table = newTab;
        // 下面时rehash的过程
        // 如果旧的桶数组不为空，则遍历桶数组，并将键值对映射到新的桶数组中
        if (oldTab != null) {
            // 遍历老的数组
            for (int j = 0; j < oldCap; ++j) {
                Node<K, V> e;
                // 如果数组索引位置不为空
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    // 如果节点下面没有链表或者红黑树
                    if (e.next == null)
                        // 用新数组容量取模，设置到新数组中
                        newTab[e.hash & (newCap - 1)] = e;
                        // 如果节点是红黑树
                    else if (e instanceof TreeNode)
                        // 需要对红黑树进行拆分
                        ((TreeNode<K, V>) e).split(this, newTab, j, oldCap);
                        // 如果节点是红黑树
                    else { // preserve order
                        Node<K, V> loHead = null, loTail = null;
                        Node<K, V> hiHead = null, hiTail = null;
                        Node<K, V> next;
                        // 遍历链表，并将链表节点按原顺序根据高低位分组
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            } else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        // 将分组后的链表映射到新桶中
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }


    /**
     * 返回map的键值对集合
     *
     * @return
     */
    @NonNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        // todo 为什么要声明这个局部变量？
        Set<Entry<K, V>> temp;
        // 这种写法有点懒加载的意思
        return (temp = entrySet) == null ? entrySet = new EntrySet() : temp;
    }


    int modCount;


    @Override
    public void clear() {
        // todo 为什么要声明这个局部变量？
        Node<K, V>[] tab;
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            Arrays.fill(tab, null);
        }
    }

    /**
     * 结点集合set
     */
    final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        @Override
        public int size() {
            return size;
        }

        @Override
        public void clear() {
            HashMap2.this.clear();
        }

        @NonNull
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return null;
        }
    }

    abstract class HashIterator {
        Node<K, V> next;        // next entry to return
        Node<K, V> current;     // current entry
        int expectedModCount;  // for fast-fail
        int index;             // current slot

        HashIterator() {
            expectedModCount = modCount;
            Node<K, V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // advance to first entry
                do {
                } while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Node<K, V> nextNode() {
            Node<K, V>[] t;
            Node<K, V> e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {
                } while (index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        public final void remove() {
            Node<K, V> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            K key = p.key;
            removeNode(hash(key), key, null, false, false);
            expectedModCount = modCount;
        }
    }

    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    final Node<K, V> removeNode(int hash, Object key, Object value,
                                boolean matchValue, boolean movable) {
//        Node<K,V>[] tab; Node<K,V> p; int n, index;
//        if ((tab = table) != null && (n = tab.length) > 0 &&
//                (p = tab[index = (n - 1) & hash]) != null) {
//            Node<K,V> node = null, e; K k; V v;
//            if (p.hash == hash &&
//                    ((k = p.key) == key || (key != null && key.equals(k))))
//                node = p;
//            else if ((e = p.next) != null) {
//                if (p instanceof TreeNode)
//                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
//                else {
//                    do {
//                        if (e.hash == hash &&
//                                ((k = e.key) == key ||
//                                        (key != null && key.equals(k)))) {
//                            node = e;
//                            break;
//                        }
//                        p = e;
//                    } while ((e = e.next) != null);
//                }
//            }
//            if (node != null && (!matchValue || (v = node.value) == value ||
//                    (value != null && value.equals(v)))) {
//                if (node instanceof TreeNode)
//                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
//                else if (node == p)
//                    tab[index] = node.next;
//                else
//                    p.next = node.next;
//                ++modCount;
//                --size;
//                afterNodeRemoval(node);
//                return node;
//            }
//        }
        return null;
    }

    /**
     * 结点类，key-Value
     *
     * @param <K>
     * @param <V>
     */
    static class Node<K, V> implements Map.Entry<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        public Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return null;
        }

        @Override
        public V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node)) return false;
            Node<?, ?> node = (Node<?, ?>) o;
            return hash == node.hash && Objects.equals(key, node.key) && Objects.equals(value, node.value) && Objects.equals(next, node.next);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hash, key, value, next);
        }

        public final String toString() {
            return key + "=" + value;
        }
    }

    /**
     * 红黑树结点类
     *
     * @param <K>
     * @param <V>
     */
    static final class TreeNode<K, V> extends Node<K, V> {
        TreeNode<K, V> parent;  // red-black tree links
        TreeNode<K, V> left;
        TreeNode<K, V> right;
        TreeNode<K, V> prev;    // needed to unlink next upon deletion
        boolean red;

        TreeNode(int hash, K key, V val, Node<K, V> next) {
            super(hash, key, val, next);
        }

        final void split(HashMap2<K, V> map, Node<K, V>[] tab, int index, int bit) {

        }

        /**
         * 插入红黑树
         *
         * @param map
         * @param tab
         * @param h
         * @param k
         * @param v
         * @return
         */
        final TreeNode<K, V> putTreeVal(HashMap2<K, V> map, Node<K, V>[] tab,
                                        int h, K k, V v) {
            return null;
        }
    }
}
