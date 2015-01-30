import org.deuce.Atomic;
import java.util.concurrent.atomic.AtomicLong;
import java.util.*;

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
    @org.deuce.Atomic
	public boolean contains(final int key) {
        Node node = root;
        while (node != null) {
            int tempKey = node.key;
            if (key == tempKey) {
                return true;
            }
            node = key < tempKey ? node.left : node.right;
        }
        return false;
    }
    //Possible change: Don't change node.left if you don't have to
    @Override
    @org.deuce.Atomic
	public void add(final int key) {
        root = addImpl(root, key);
    }


    private Node addImpl(final Node node, final int key) {
        int tempKey;
        if (node == null) {
            return new Node(key, randPriority());
        }

        else{
            tempKey = node.key;
        }
        if(key == tempKey) {
            // no insert needed
            return node;
        }
        else if (key < tempKey) {
            node.left = addImpl(node.left, key);
            if (node.left.priority > node.priority) {
                return rotateRight(node);
            }
            return node;
        }
        else {
            node.right = addImpl(node.right, key);
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
           boolean sameValue = randState.compareAndSet(temp, temp * 2862933555777941757L + 3037000493L);
           if (sameValue) {return (int) randState.get()>>30;}

        }
//        return (int)(randState.get() >> 30);
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
    @org.deuce.Atomic
	public void remove(final int key) {
        root = removeImpl(root, key);
    }

    private Node removeImpl(final Node node, final int key) {
        int tempKey;
        if (node == null) {
            // not present, nothing to do
            return null;
        }else{
            tempKey = node.key;
        }
        if (key == tempKey) {
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
                    top.right = removeImpl(top.right, key);
                    return top;
                } else {
                    final Node top = rotateLeft(node);
                    top.left = removeImpl(top.left, key);
                    return top;
                }
            }
        }
        else if (key < tempKey) {
            node.left = removeImpl(node.left, key);
            return node;
        }
        else {
            node.right = removeImpl(node.right, key);
            return node;
        }
    }
}
