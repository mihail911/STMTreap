CS149 PA2 - STM Treap

Collaborators: Mihail Eric--meric
	           Raymond Wu--wur911

First, we removed the synchronized blocks and instead made the synchronized
methods to be atmoic transactions through the use of DeuceSTM.

The optimizations we considered were to remove the write barriers and to
ensure that a single unique random priority is assigned to each node, which
was not the case before.

We changed randState to be an AtomicLong. We used get() and compareAndSet() in
a while loop to to ensure that the modifying of randState and retrieving of a
a random number were atomic operations. We called get() and stored the result
in a temporary variable. Then when we wanted to update the randState value,
we used the temporary variable result to check if the randState had not been
tampered in the time in between, thus ensuring atomicity.

To reduce the number of writes, each time a node or node field was written to,
we first checked to make sure the new object being assigned was not the
same as it was previously. This ensured that we did not do any writes when we
didn't need to. 

Modifying root happened all the time in the original code in add() and remove().
We added the check here to reduce the number of times root was modified. We also
made similar changes in addImpl() and removeImpl() when variables were assigned
to be objects.

In total, there were 8 writes that we added checks to.
