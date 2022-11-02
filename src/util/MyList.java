package util;

import java.util.Iterator;

/**
 * 这是一个手写的双向链表
 * @param <T> 链表节点的元素类型
 */
public class MyList<T> implements Iterable<MyList.MyNode<T>>
{
    // 头节点
    private MyNode<T> head;
    // 尾节点
    private MyNode<T> tail;
    // 该链表上节点的个数
    private int nodeNum;

    public MyList()
    {
        this.nodeNum = 0;
        this.head = null;
        this.tail = null;
    }

    public MyNode<T> getHead()
    {
        return head;
    }

    public void setHead(MyNode<T> head)
    {
        this.head = head;
    }

    public MyNode<T> getTail()
    {
        return tail;
    }

    public void setTail(MyNode<T> tail)
    {
        this.tail = tail;
    }

    public int size()
    {
        return nodeNum;
    }

    public boolean isEmpty()
    {
        return nodeNum == 0;
    }

    public void clear()
    {
        this.head = null;
        this.tail = null;
        this.nodeNum = 0;
    }

    public void insertEnd(MyNode<T> node)
    {
        if (isEmpty())
        {
            node.insertNullList(this);
        }
        else
        {
            node.insertAfter(this.tail);
        }
    }

    public void insertBeforeTail(MyNode<T> node)
    {
        if (isEmpty())
        {
            node.insertNullList(this);
        }
        else
        {
            node.insertBefore(tail);
        }
    }

    public void insertBeforeHead(MyNode<T> node)
    {
        if (isEmpty())
        {
            node.insertNullList(this);
        }
        else
        {
            node.insertBefore(head);
        }
    }

    @Override
    public Iterator<MyNode<T>> iterator()
    {
        return new IIterator(head);
    }

    /**
     * 这是一个内部类，用于描述链表中的节点
     * @param <T> 链表节点的元素类型
     */
    public static class MyNode<T>
    {
        private T val;
        private MyNode<T> pre;
        private MyNode<T> next;
        // 表示该节点所在的链表
        private MyList<T> parent;

        public MyNode(T value)
        {
            this.val = value;
            this.pre = null;
            this.next = null;
            this.parent = null;
        }

        public T getVal()
        {
            return val;
        }

        public void setVal(T newVal)
        {
            this.val = newVal;
        }

        // 这里用于登记自己所在的链表
        public void setParent(MyList<T> parent)
        {
            this.parent = parent;
        }

        public MyList<T> getParent()
        {
            return parent;
        }

        public MyNode<T> getPre()
        {
            return pre;
        }

        public MyNode<T> getNext()
        {
            return next;
        }

        /**
         * 将节点插入到一个空链表中
         * @param parent 待插入的空链表
         */
        public void insertNullList(MyList<T> parent)
        {
            parent.nodeNum++;
            parent.head = this;
            parent.tail = this;
            this.parent = parent;
            this.pre = null;
            this.next = null;
        }

        public void insertBefore(MyNode<T> next)
        {
            // 更新 parent
            this.parent = next.parent;
            // 更新节点数目
            this.parent.nodeNum++;
            // 更新头结点
            if (parent.head == next)
            {
                parent.head = this;
            }
            // 正式插入
            this.pre = next.pre;
            this.next = next;
            next.pre = this;
            if (this.pre != null)
            {
                this.pre.next = this;
            }
        }

        // insert my self after prev node
        public void insertAfter(MyNode<T> pre)
        {
            this.parent = pre.parent;
            this.parent.nodeNum++;
            if (parent.tail == pre)
            {
                parent.tail = this;
            }
            this.pre = pre;
            this.next = pre.next;
            pre.next = this;
            if (this.next != null)
            {
                this.next.pre = this;
            }
        }

        /**
         * 头插法
         * @param parent 待插入的链表
         */
        public void insertHead(MyList<T> parent)
        {
            // 如果链表是空的
            if (parent.isEmpty())
            {
                insertNullList(parent);
            }
            // 如果链表不为空，那么就在头结点前面插入
            else
            {
                insertBefore(parent.head);
            }
        }

        /**
         * 尾插法
         * @param parent 待插入的链表
         */
        public void insertEnd(MyList<T> parent)
        {
            if (parent.isEmpty())
            {
                insertNullList(parent);
            }
            // 如果链表不为空，那么就在尾结点后面插入
            else
            {
                insertAfter(parent.tail);
            }
        }

        /**
         * 插入到最后一个节点前面
         * @param parent 待插入的链表
         */
        public void insertBeforeTail(MyList<T> parent)
        {
            if (parent.isEmpty())
            {
                insertNullList(parent);
            }
            else
            {
                insertBefore(parent.tail);
            }
        }


        /**
         * 将自己从链表中移除
         * @return 已经被删除的节点
         */
        public MyNode<T> removeSelf()
        {
            this.parent.nodeNum--;
            // 该节点是头结点
            if (parent.head == this)
            {
                parent.setHead(this.next);
            }
            // 该节点是尾节点
            if (parent.getTail() == this)
            {
                parent.setTail(this.pre);
            }

            if (this.pre == null && this.next == null)
            {

            }
            // 该节点为链内节点
            else if (this.pre != null && this.next != null)
            {
                this.pre.next = this.next;
                this.next.pre = this.pre;
            }
            // 该节点为头结点
            else if (this.pre == null)
            {
                this.next.pre = null;
            }
            // 该节点是尾节点
            else
            {
                this.pre.next = null;
            }
            return this;
        }
    }

    /**
     * 这是一个内部类, 用于实现迭代器
     */
    class IIterator implements Iterator<MyNode<T>>
    {
        MyNode<T> cur = new MyNode<>(null);
        MyNode<T> nxt = null;

        // 从这里可以看出, entry 是头结点
        IIterator(MyNode<T> head)
        {
            cur.next = head;
        }

        @Override
        public boolean hasNext()
        {
            return nxt != null || cur.next != null;
        }

        /**
         * cur 是当前指向的元素，nxt 是下一个元素
         * @return 返回当前元素
         */
        @Override
        public MyNode<T> next()
        {
            // 普遍情况，进行指针的移动
            if (nxt == null)
            {
                cur = cur.next;
            }
            // 我也不知道为啥会发生这种情况
            else
            {
                cur = nxt;
            }
            nxt = null;
            return cur;
        }

        @Override
        public void remove()
        {
            MyList.MyNode<T> prev = cur.pre;
            MyList.MyNode<T> next = cur.next;
            MyList<T> parent = cur.getParent();
            // 如果要移除的元素不是首元素，那么就进行简单链表替换
            if (prev != null)
            {
                prev.next = next;
            }
            // 如果移除的元素是首元素，那么就把首元素设置为 next
            else
            {
                parent.setHead(next);
            }
            // 如果移除的元素是不是尾元素，那么就进行简单链表移除
            if (next != null)
            {
                next.pre = prev;
            }
            // 如果移除的元素是尾元素，那么就把尾节点设置为 prev
            else
            {
                parent.setTail(prev);
            }
            // 从这里也可以看出，parent 就是节点所在的链表
            --parent.nodeNum;

            this.nxt = next;
            cur.next = cur.pre = null;
            cur.val = null;
        }
    }
}

