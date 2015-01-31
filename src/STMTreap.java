import java.util.concurrent.atomic.AtomicLong;

public class STMTreap implements IntSet {
    static class Node {
        final int key;
        final int priority;
        Node left;
        Node right;

        Node(final int key, final int priority) {
            this.key = key;
            this.priority = priority;
        }

        @Override
        public String toString() {
            return "Node[key=" + key + ", prio=" + priority +
                    ", left=" + (left == null ? "null" : String.valueOf(left.key)) +
                    ", right=" + (right == null ? "null" : String.valueOf(right.key)) + "]";
        }
    }

    private AtomicLong randState = new AtomicLong();
    private Node root;

    @Override
    public synchronized boolean contains(final int key) {
        Node node = root;
        while (node != null) {
            if (key == node.key) {
                return true;
            }
            node = key < node.key ? node.left : node.right;
        }
        return false;
    }


    @Override
    public synchronized void add(final int key)
    {
        Node temp = addImpl(root, key);
        if (temp != root) root = temp;
    }

    private Node addImpl(final Node node, final int key) {
        if (node == null) {
            return new Node(key, randPriority());
        }
        else if (key == node.key) {
            // no insert needed
            return node;
        }
        else if (key < node.key) {
            Node tempLeft = addImpl(node.left, key);
            if(tempLeft != node.left) node.left = tempLeft;
            if (node.left.priority > node.priority) {
                return rotateRight(node);
            }
            return node;
        }
        else {
            Node tempRight = addImpl(node.right, key);
            if(tempRight != node.right) node.right = tempRight;
            if (node.right.priority > node.priority) {
                return rotateLeft(node);
            }
            return node;
        }
    }

    private int randPriority() {
        // The constants in this 64-bit linear congruential random number
        // generator are from http://nuclear.llnl.gov/CNP/rng/rngman/node4.html
        //randState = randState * 2862933555777941757L + 3037000493L;
        while(true){
            long temp = randState.get();
            long tempUpdate = temp * 2862933555777941757L + 3037000493L;
            boolean sameValue = randState.compareAndSet(temp, temp * 2862933555777941757L + 3037000493L);
            if (sameValue) {return (int) tempUpdate>>30;}
        }
    }

    private Node rotateRight(final Node node) {
        //       node                  nL
        //     /      \             /      \
        //    nL       z     ==>   x       node
        //  /   \                         /   \
        // x   nLR                      nLR   z
        final Node nL = node.left;
        node.left = nL.right;
        nL.right = node;
        return nL;
    }

    private Node rotateLeft(final Node node) {
        final Node nR = node.right;
        node.right = nR.left;
        nR.left = node;
        return nR;
    }

    @Override
    public synchronized void remove(final int key)
    {   Node temp = removeImpl(root, key);
        if (temp != root) root = temp;
    }

    private Node removeImpl(final Node node, final int key) {
        if (node == null) {
            // not present, nothing to do
            return null;
        }
        else if (key == node.key) {
            if (node.left == null) {
                // splice out this node
                return node.right;
            }
            else if (node.right == null) {
                return node.left;
            }
            else {
                // Two children, this is the hardest case.  We will pretend
                // that node has -infinite priority, move it down, then retry
                // the removal.
                if (node.left.priority > node.right.priority) {
                    // node.left needs to end up on top
                    final Node top = rotateRight(node);
                    Node topRight = removeImpl(top.right, key);
                   if (top.right != topRight) top.right = topRight;
                    return top;
                } else {
                    final Node top = rotateLeft(node);
                    Node topLeft = removeImpl(top.left, key);
                    if (top.left != topLeft) top.left = topLeft;
                    return top;
                }
            }
        }
        else if (key < node.key) {
            Node tempLeft = removeImpl(node.left,key);
            if (node.left != tempLeft) node.left = tempLeft;
            return node;
        }
        else {
            Node tempRight = removeImpl(node.right, key);
            if (node.right != tempRight) node.right = tempRight;
            return node;
        }
    }
}
