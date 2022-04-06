/*
 * Galaxy
 * Copyright (c) 2012-2014, Parallel Universe Software Co. All rights reserved.
 * 
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *  
 *   or (per the licensee's choosing)
 *  
 * under the terms of the GNU Lesser General Public License version 3.0
 * as published by the Free Software Foundation.
 */
package co.paralleluniverse.galaxy;

import co.paralleluniverse.common.io.Persistable;
import co.paralleluniverse.common.io.Streamable;
import com.google.common.util.concurrent.ListenableFuture;
import java.nio.ByteBuffer;

/**
 * The grid's distributed data-store service. Internally, each data item is <b>owned</b> by one and only one node in the
 * cluster at any one time, though it may be <b>shared</b> by many. In order to write an item, a node gains ownership
 * over it.
 */
public interface Store extends Cache {
    /**
     * Returns the maximum size, in bytes, of a data item in the grid. Attempts to store larger items will result in an
     * exception. This limit is set in the cache spring-bean configuration.
     *
     * @return The maximum size, in bytes, of a data item in the grid.
     */
    int getMaxItemSize();

    /**
     * Creates a new transaction.
     * <p>
     * A transaction can be used by more than one thread.
     *
     * @return A newly created transaction.
     */
    StoreTransaction beginTransaction();

    /**
     * Ends a transaction, and makes all updates visible by all other nodes in the cluster.
     *
     * @param txn The current transaction, which we wish to complete.
     */
    void commit(StoreTransaction txn) throws InterruptedException;

    /**
     * Ends a transaction after a failure.
     * <p>
     * <b>This method must be called only after {@link #rollback(co.paralleluniverse.galaxy.StoreTransaction) rollback()}
     * has been called, or a manual rollback has been done.</b>
     *
     * @param txn The current transaction, which we wish to complete after failure.
     */
    void abort(StoreTransaction txn) throws InterruptedException;

    /**
     * Reverts {@code set} operations that were performed during the transactions.
     * <p>
     * This method does not complete the
     * transaction. {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) endTransaction()} must still be called.
     *
     * @param txn The current transaction.
     */
    void rollback(StoreTransaction txn);

    /**
     * Releases a line that's been pinned to this node by one of the {@code gets}, {@code getx}, {@code put} operations.
     * <p>
     * This method must be called to release a line used in one of the {@code gets}, {@code getx}, {@code put}
     * operations, if they were called with a {@code null} transaction.
     *
     * @param id
     */
    void release(long id);

    /**
     * Gets or possibly creates a root data item. The same item ID will be returned when this method is called on any
     * cluster node with the same root name.
     * <p>
     * You can test if the root has been newly created by this transaction by calling .
     *
     * @param rootName The root's name.
     * @return The root item's ID.
     * @param txn      The current transaction. May not be null.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    long getRoot(String rootName, StoreTransaction txn) throws TimeoutException;

    /**
     * Gets or possibly creates a root data item. The same item ID will be returned when this method is called on any
     * cluster node with the same root name.
     * <p>
     * You can test if the root has been newly created by this transaction by calling .
     *
     * @param rootName The root's name.
     * @param id       If the root does not yet exist, it will be created and given this ID.
     * @return The root item's ID.
     * @param txn      The current transaction. May not be null.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    long getRoot(String rootName, long id, StoreTransaction txn) throws TimeoutException;

    /**
     * Tests whether a root item has been newly created.
     *
     * @param rootId The root item's ID.
     * @param txn    The current transaction.
     * @return {@code true} if the root has been created by the current transaction; {@code false} if it existed before
     *         current transaction.
     */
    boolean isRootCreated(long rootId, StoreTransaction txn);

    /**
     * Sets a listener listening for local cache events on the given item.
     *
     * @param id       The item's ID.
     * @param listener The listener.
     */
    @Override
    void setListener(long id, CacheListener listener);

    /**
     * Sets a listener listening for local cache events on the given item if absent.
     *
     * @param id       The item's ID.
     * @param listener The listener.
     * @return The given listener if it was set or the existing one otherwise.
     */
    @Override
    CacheListener setListenerIfAbsent(long id, CacheListener listener);

    /**
     * @param id The item's ID.
     * @return The cacheListener of this line
     */
    @Override
    CacheListener getListener(long id);

    /**
     * Allocates one or more new (and empty) items in the store.<p>
     * When allocating a single item, it's better to use {@link #put(byte[], StoreTransaction) put()}, but some data
     * structures might require allocating an array of items.<br>
     *
     * @param count The number of items to allocate.
     * @param txn   The current transaction. May not be null.
     * @return The id of the first item in the allocated array. The following {@code count - 1} IDs belong to the
     *         following elements of the array.
     * @throws TimeoutException
     */
    long alloc(int count, StoreTransaction txn) throws TimeoutException;

    /**
     * Puts a new item into the store and returns its (newly allocated) ID.<p>
     *
     * @param data The item's contents.
     * @param txn  The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return The item's (newly allocated) ID.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    long put(byte[] data, StoreTransaction txn) throws TimeoutException;

    /**
     * Puts a new item into the store and returns its (newly allocated) ID.<p>
     *
     * @param data The item's contents.
     * @param txn  The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return The item's (newly allocated) ID.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    long put(ByteBuffer data, StoreTransaction txn) throws TimeoutException;

    /**
     * Puts a new item into the store and returns its (newly allocated) ID.<p>
     *
     * @param object The item's contents.
     * @param txn    The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return The item's (newly allocated) ID.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    long put(Persistable object, StoreTransaction txn) throws TimeoutException;

    /**
     * Retrieves a given data item.
     *
     * @param id The item's ID.
     * @return The contents of the item.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    byte[] get(long id) throws TimeoutException;

    /**
     * Retrieves a given data item into a {@link Persistable}.
     *
     * @param id     The item's ID.
     * @param object The object into which the contents of the item will be written. May be {@code null}.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    void get(long id, Persistable object) throws TimeoutException;

    /**
     * Retrieves a given data item, using a hint as to its {@link #getx(long, StoreTransaction) owner} in the
     * cluster.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * @param id       The item's ID.
     * @param nodeHint The ID of the node the data item is probably owned by.
     * @return The contents of the item.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    byte[] get(long id, short nodeHint) throws TimeoutException;

    /**
     * Retrieves a given data item into a {@link Persistable}, using a hint as to its {@link #getx(long, StoreTransaction) owner}
     * in the cluster.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * @param id       The item's ID.
     * @param nodeHint The ID of the node the data item is probably owned by.
     * @param object   The object into which the contents of the item will be written. May be {@code null}.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    void get(long id, short nodeHint, Persistable object) throws TimeoutException;

    /**
     * Retrieves a given data item, using a hint as to its {@link #getx(long, StoreTransaction) owner} in the cluster.
     * Unlike the direct hint given in {@link #get(long, short) get(long, short)}, the hinted node here is the owner of
     * a given item.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * @param id      The item's ID.
     * @param ownerOf The ID of an item whose owner is probably the owner of the requested item as well.
     * @return The contents of the item.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    byte[] getFromOwner(long id, long ownerOf) throws TimeoutException;

    /**
     * Retrieves a given data item, using a hint as to its {@link #getx(long, StoreTransaction) owner} in the cluster.
     * Unlike the direct hint given in {@link #get(long, short, Persistable) get(long, short, Persistable)}, the hinted
     * node here is the owner of a given item.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * @param id      The item's ID.
     * @param ownerOf The ID of an item whose owner is probably the owner of the requested item as well.
     * @param object  The object into which the contents of the item will be written. May be {@code null}.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    void getFromOwner(long id, long ownerOf, Persistable object) throws TimeoutException;

    /**
     * Retrieves a given data item, and pins the shared (cached) instance to this node. What this means is that while
     * other nodes will be able to read the same item, no node will be able to update it until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction}
     * or {@link #release(long) release} it.
     *
     * @param id  The item's ID.
     * @param txn The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return The contents of the item.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    byte[] gets(long id, StoreTransaction txn) throws TimeoutException;

    /**
     * Retrieves a given data item into a {@link Persistable}, and pins the shared (cached) instance to this node. What
     * this means is that while other nodes will be able to read the same item, no node will be able to update it until
     * until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction} or {@link #release(long) release}
     * it.
     *
     * @param id     The item's ID.
     * @param object The object into which the contents of the item will be written. May be {@code null}.
     * @param txn    The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    void gets(long id, Persistable object, StoreTransaction txn) throws TimeoutException;

    /**
     * Retrieves a given data item with a hint as to its {@link #getx(long, StoreTransaction) owner} in the cluster, and
     * pins the shared (cached) instance to this node. What this means is that while other nodes will be able to read
     * the same item, no node will be able to update it until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction}
     * or {@link #release(long) release} it. <br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * @param id       The item's ID.
     * @param nodeHint The ID of the node the data item is probably owned by.
     * @param txn      The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return The contents of the item.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    byte[] gets(long id, short nodeHint, StoreTransaction txn) throws TimeoutException;

    /**
     * Retrieves a given data item into a {@link Persistable} with a hint as to its {@link #getx(long, StoreTransaction) owner}
     * in the cluster, and pins the shared (cached) instance to this node. What this means is that while other nodes
     * will be able to read the same item, no node will be able to update it until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction}
     * or {@link #release(long) release} it.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * @param id       The item's ID.
     * @param nodeHint The ID of the node the data item is probably owned by.
     * @param txn      The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @param object   The object into which the contents of the item will be written. May be {@code null}.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    void gets(long id, short nodeHint, Persistable object, StoreTransaction txn) throws TimeoutException;

    /**
     * Retrieves a given data item with a hint as to its {@link #getx(long, StoreTransaction) owner} in the cluster, and
     * pins the shared (cached) instance to this node. What this means is that while other nodes will be able to update
     * it until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction} or {@link #release(long) release}
     * it. Unlike the direct hint given in
     * {@link #gets(long, short, StoreTransaction)}, the hinted node here is the owner of a given item.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * @param id      The item's ID.
     * @param ownerOf The ID of an item whose owner is probably the owner of the requested item as well.
     * @param txn     The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return The contents of the item.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    byte[] getsFromOwner(long id, long ownerOf, StoreTransaction txn) throws TimeoutException;

    /**
     * Retrieves a given data item with a hint as to its {@link #getx(long, StoreTransaction) owner} in the cluster, and
     * pins the shared (cached) instance to this node. What this means is that while other nodes will be able to read
     * the same item, no node will be able to update it until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction}
     * or {@link #release(long) release} it.<br> Unlike the direct hint given in {@link #gets(long, short, Persistable, StoreTransaction)},
     * the hinted node here is the owner of a given item.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * @param id      The item's ID.
     * @param ownerOf The ID of an item whose owner is probably the owner of the requested item as well.
     * @param object  The object into which the contents of the item will be written. May be {@code null}.
     * @param txn     The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    void getsFromOwner(long id, long ownerOf, Persistable object, StoreTransaction txn) throws TimeoutException;

    /**
     * Retrieves a given data item, makes this node its exclusive owner, and pins it. What this means is that no other
     * node will be able to read or update the same item until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction}
     * or {@link #release(long) release} it. it.
     *
     * @param id  The item's ID.
     * @return The contents of the item.
     * @param txn The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    byte[] getx(long id, StoreTransaction txn) throws TimeoutException;

    /**
     * Retrieves a given data item into a {@link Persistable}, makes this node its exclusive owner, and pins it. What
     * this means is that no other node will be able to read or update the same item until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction}
     * or {@link #release(long) release} it.
     *
     * @param id     The item's ID.
     * @param object The object into which the contents of the item will be written. May be {@code null}.
     * @param txn    The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    void getx(long id, Persistable object, StoreTransaction txn) throws TimeoutException;

    /**
     * Retrieves a given data item with a hint as to its {@link #getx(long, StoreTransaction) owner} in the cluster,
     * makes this node its exclusive owner, and pins it. What this means is that no other node will be able to read or
     * update the same item until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction}
     * or {@link #release(long) release} it.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * @param id       The item's ID.
     * @param nodeHint The ID of the node the data item is probably owned by.
     * @param txn      The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return The contents of the item.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    byte[] getx(long id, short nodeHint, StoreTransaction txn) throws TimeoutException;

    /**
     * Retrieves a given data item with a hint as to its {@link #getx(long, StoreTransaction) owner} in the cluster,
     * makes this node its exclusive owner, and pins it. What this means is that no other node will be able to read or
     * update the same item until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction} or {@link #release(long) release}
     * it.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * @param id       The item's ID.
     * @param nodeHint The ID of the node the data item is probably owned by.
     * @param object   The object into which the contents of the item will be written. May be {@code null}.
     * @param txn      The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    void getx(long id, short nodeHint, Persistable object, StoreTransaction txn) throws TimeoutException;

    /**
     * Retrieves a given data item with a hint as to its {@link #getx(long, StoreTransaction) owner} in the cluster,
     * makes this node its exclusive owner, and pins it. What this means is that no other node will be able to read or
     * update the same item until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction} or {@link #release(long) release}
     * it.
     *
     * Unlike the direct hint given in {@link #getx(long, short, StoreTransaction)}, the hinted node here is the owner
     * of a given item.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * @param id      The item's ID.
     * @param ownerOf The ID of an item whose owner is probably the owner of the requested item as well.
     * @param txn     The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return The contents of the item.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    byte[] getxFromOwner(long id, long ownerOf, StoreTransaction txn) throws TimeoutException;

    /**
     * Retrieves a given data item with a hint as to its {@link #getx(long, StoreTransaction) owner} in the cluster,
     * makes this node its exclusive owner, and pins it. What this means is that no other node will be able to read or
     * update the same item until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction} or {@link #release(long) release}
     * it.
     *
     * Unlike the direct hint given in {@link #getx(long, short, Persistable, StoreTransaction)}, the hinted node here
     * is the owner of a given item.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * @param id      The item's ID.
     * @param ownerOf The ID of an item whose owner is probably the owner of the requested item as well.
     * @param object  The object into which the contents of the item will be written. May be {@code null}.
     * @param txn     The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    void getxFromOwner(long id, long ownerOf, Persistable object, StoreTransaction txn) throws TimeoutException;

    /**
     * Gains ownership of an item and sets its contents. Upon return from this method, the item will be pinned if and
     * only if it had been pinned when the method was called.
     *
     * @param id   The item's ID.
     * @param data The contents to write into the item.
     * @param txn  The current transaction. May be null.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    void set(long id, byte[] data, StoreTransaction txn) throws TimeoutException;

    /**
     * Gains ownership of an item and sets its contents. Upon return from this method, the item will be pinned if and
     * only if it had been pinned when the method was called.
     *
     * @param id   The item's ID.
     * @param data The contents to write into the item.
     * @param txn  The current transaction. May be null.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    void set(long id, ByteBuffer data, StoreTransaction txn) throws TimeoutException;

    /**
     * Gains ownership of an item and sets its contents. Upon return from this method, the item will be pinned if and
     * only if it had been pinned when the method was called.
     *
     * @param id     The item's ID.
     * @param object The contents to write into the item.
     * @param txn    The current transaction. May be null.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    void set(long id, Persistable object, StoreTransaction txn) throws TimeoutException;

    /**
     * Deletes an item from the store.
     *
     * @param id
     * @param txn The current transaction. May be null.
     */
    void del(long id, StoreTransaction txn) throws TimeoutException;

    <T> T invoke(long id, LineFunction<T> function) throws TimeoutException;

    /**
     * Allocates one or more new (and empty) items in the store.<p>
     * When allocating a single item, it's better to use {@link #put(byte[], StoreTransaction) put()}, but some data
     * structures might require allocating an array of items.<br>
     *
     * @param count The number of items to allocate.
     * @param txn   The current transaction. May not be null.
     * @return The id of the first item in the allocated array. The following {@code count - 1} IDs belong to the
     *         following elements of the array.
     */
    ListenableFuture<Long> allocAsync(int count, StoreTransaction txn);

    /**
     * Puts a new item into the store and returns its (newly allocated) ID.<p>
     *
     * @param data The item's contents.
     * @param txn  The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return The item's (newly allocated) ID.
     */
    ListenableFuture<Long> putAsync(byte[] data, StoreTransaction txn);

    /**
     * Puts a new item into the store and returns its (newly allocated) ID.<p>
     *
     * @param data The item's contents.
     * @param txn  The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return The item's (newly allocated) ID.
     */
    ListenableFuture<Long> putAsync(ByteBuffer data, StoreTransaction txn);

    /**
     * Puts a new item into the store and returns its (newly allocated) ID.<p>
     *
     * @param object The item's contents.
     * @param txn    The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return The item's (newly allocated) ID.
     */
    ListenableFuture<Long> putAsync(Persistable object, StoreTransaction txn);

    /**
     * Retrieves a given data item asynchronously.
     *
     * <p>
     * The asynchronous version of {@link #get(long) get(long)}.
     *
     * @param id The item's ID.
     * @return A future that will return the contents of the item.
     */
    ListenableFuture<byte[]> getAsync(long id);

    /**
     * Retrieves a given data item into a {@link Persistable} asynchronously.
     *
     * <p>
     * The asynchronous version of {@link #get(long, co.paralleluniverse.common.io.Persistable) get(long, Persistable)}.
     *
     * @param id     The item's ID.
     * @param object The object into which the contents of the item will be written when the operation completes (after
     *               the future has been waited for).
     * @return A future that will return the passed object.
     */
    ListenableFuture<Persistable> getAsync(long id, Persistable object);

    /**
     * Retrieves a given data item asynchronously, using a hint as to its {@link #getx(long, StoreTransaction) owner} in
     * the cluster. <br>If the item is indeed found on the hinted node, the retrieval performance might be superior. If
     * not, the method will still work, but performance may be worse.
     *
     * <p>
     * The asynchronous version of {@link #get(long, short) get(long, short)}.
     *
     * @param id       The item's ID.
     * @param nodeHint The ID of the node the data item is probably owned by.
     * @return A future that will return the contents of the item.
     */
    ListenableFuture<byte[]> getAsync(long id, short nodeHint);

    /**
     * Retrieves a given data item asynchronously into a {@link Persistable}, using a hint as to its {@link #getx(long, StoreTransaction) owner}
     * in the cluster. <br>If the item is indeed found on the hinted node, the retrieval performance might be superior.
     * If not, the method will still work, but performance may be worse.
     *
     * <p>
     * The asynchronous version of {@link #get(long, short, co.paralleluniverse.common.io.Persistable) get(long, short, Persistable)}.
     *
     * @param id       The item's ID.
     * @param nodeHint The ID of the node the data item is probably owned by.
     * @param object   The object into which the contents of the item will be written.
     * @return A future that will return the passed object.
     */
    ListenableFuture<Persistable> getAsync(long id, short nodeHint, Persistable object);

    /**
     * Retrieves a given data item asynchronously, using a hint as to its {@link #getx(long, StoreTransaction) owner} in
     * the cluster. Unlike the direct hint given in {@link #get(long, short) get(long, short)}, the hinted node here is
     * the owner of a given item. <br>If the item is indeed found on the hinted node, the retrieval performance might
     * be superior. If not, the method will still work, but performance may be worse.
     *
     * <p>
     * The asynchronous version of {@link #getFromOwner(long, long)}
     *
     * @param id      The item's ID.
     * @param ownerOf The ID of an item whose owner is probably the owner of the requested item as well.
     * @return A future that will return the contents of the item.
     */
    ListenableFuture<byte[]> getFromOwnerAsync(long id, long ownerOf);

    /**
     * Retrieves a given data item asynchronously, using a hint as to its {@link #getx(long, StoreTransaction) owner} in
     * the cluster. Unlike the direct hint given in {@link #get(long, short, Persistable) get(long, short, Persistable)},
     * the hinted node here is the owner of a given item. <br>If the item is indeed found on the hinted node, the
     * retrieval performance might be superior. If not, the method will still work, but performance may be worse.
     *
     * <p>
     * The asynchronous version of {@link #getFromOwner(long, long, co.paralleluniverse.common.io.Persistable)  getFromOwner(long, long, Persistable)}
     *
     * @param id      The item's ID.
     * @param ownerOf The ID of an item whose owner is probably the owner of the requested item as well.
     * @param object  The object into which the contents of the item will be written. May be {@code null}.
     * @return A future that will return the passed object.
     */
    ListenableFuture<Persistable> getFromOwnerAsync(long id, long ownerOf, Persistable object);

    /**
     * Retrieves a given data item asynchronously, and pins the shared (cached) instance to this node. What this means
     * is that while other nodes will be able to read the same item, no node will be able to update it until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction}
     * or {@link #release(long) release} it.
     *
     * <p>
     * The asynchronous version of {@link #gets(long, co.paralleluniverse.galaxy.StoreTransaction) gets(long, StoreTransaction)}
     *
     * @param id  The item's ID.
     * @param txn The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return A future that will return contents of the item.
     */
    ListenableFuture<byte[]> getsAsync(long id, StoreTransaction txn);

    /**
     * Retrieves a given data asynchronously item into a {@link Persistable}, and pins the shared (cached) instance to
     * this node. What this means is that while other nodes will be able to read the same item, no node will be able to
     * update it until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction} or {@link #release(long) release}
     * it.
     *
     * <p>
     * The asynchronous version of {@link #gets(long, co.paralleluniverse.common.io.Persistable, co.paralleluniverse.galaxy.StoreTransaction) gets(long, Persistable, StoreTransaction)}
     *
     * @param id     The item's ID.
     * @param object The object into which the contents of the item will be written. May be {@code null}.
     * @param txn    The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return A future that will return the passed object.
     */
    ListenableFuture<Persistable> getsAsync(long id, Persistable object, StoreTransaction txn);

    /**
     * Retrieves a given data item asynchronously with a hint as to its {@link #getx(long, StoreTransaction) owner} in the cluster,
     * and pins the shared (cached) instance to this node. What this means is that while other nodes will be able to
     * read the same item, no node will be able to update it until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction}
     * or {@link #release(long) release} it. <br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * <p>
     * The asynchronous version of {@link #gets(long, short, co.paralleluniverse.galaxy.StoreTransaction) gets(long, short, StoreTransaction)}
     *
     * @param id       The item's ID.
     * @param nodeHint The ID of the node the data item is probably owned by.
     * @param txn      The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return A future that will return contents of the item.
     */
    ListenableFuture<byte[]> getsAsync(long id, short nodeHint, StoreTransaction txn);

    /**
     * Retrieves a given data asynchronously item into a {@link Persistable} with a hint as to its {@link #getx(long, StoreTransaction) owner}
     * in the cluster, and pins the shared (cached) instance to this node. What this means is that while other nodes
     * will be able to read the same item, no node will be able to update it until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction}
     * or {@link #release(long) release} it.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * <p>
     * The asynchronous version of {@link #gets(long, short, co.paralleluniverse.common.io.Persistable, co.paralleluniverse.galaxy.StoreTransaction) gets(long, short, Persistable, StoreTransaction)}
     *
     * @param id       The item's ID.
     * @param nodeHint The ID of the node the data item is probably owned by.
     * @param txn      The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @param object   The object into which the contents of the item will be written.
     * @return A future that will return the passed object.
     */
    ListenableFuture<Persistable> getsAsync(long id, short nodeHint, Persistable object, StoreTransaction txn);

    /**
     * Retrieves a given data item asynchronously with a hint as to its {@link #getx(long, StoreTransaction) owner} in the cluster,
     * and pins the shared (cached) instance to this node. What this means is that while other nodes will be able to
     * update it until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction}
     * or {@link #release(long) release} it. Unlike the direct hint given in
     * {@link #gets(long, short, StoreTransaction)}, the hinted node here is the owner of a given item.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * <p>
     * The asynchronous version of {@link #getsFromOwner(long, long, co.paralleluniverse.galaxy.StoreTransaction) getsFromOwner(long, long, StoreTransaction)}
     *
     * @param id      The item's ID.
     * @param ownerOf The ID of an item whose owner is probably the owner of the requested item as well.
     * @param txn     The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return A future that will return contents of the item.
     */
    ListenableFuture<byte[]> getsFromOwnerAsync(long id, long ownerOf, StoreTransaction txn);

    /**
     * Retrieves a given data item asynchronously with a hint as to its {@link #getx(long, StoreTransaction) owner} in the cluster, and
     * pins the shared (cached) instance to this node. What this means is that while other nodes will be able to read
     * the same item, no node will be able to update it until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction}
     * or {@link #release(long) release} it.<br> Unlike the direct hint given in {@link #gets(long, short, Persistable, StoreTransaction)},
     * the hinted node here is the owner of a given item.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * <p>
     * The asynchronous version of {@link #getsFromOwner(long, long, co.paralleluniverse.common.io.Persistable, co.paralleluniverse.galaxy.StoreTransaction) getsFromOwner(long, long, Persistable, StoreTransaction)}
     *
     * @param id      The item's ID.
     * @param ownerOf The ID of an item whose owner is probably the owner of the requested item as well.
     * @param object  The object into which the contents of the item will be written. May be {@code null}.
     * @param txn     The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return A future that will return the passed object.
     */
    ListenableFuture<Persistable> getsFromOwnerAsync(long id, long ownerOf, Persistable object, StoreTransaction txn);

    /**
     * Retrieves a given data item asynchronously, makes this node its exclusive owner, and pins it. What this means is that no other
     * node will be able to read or update the same item until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction}
     * or {@link #release(long) release} it. it.
     *
     * <p>
     * The asynchronous version of {@link #getx(long, co.paralleluniverse.galaxy.StoreTransaction) getx(long, StoreTransaction)}
     *
     * @param id  The item's ID.
     * @param txn The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return A future that will return contents of the item.
     */
    ListenableFuture<byte[]> getxAsync(long id, StoreTransaction txn);

    /**
     * Retrieves a given data item asynchronously into a {@link Persistable}, makes this node its exclusive owner, and
     * pins it. What this means is that no other node will be able to read or update the same item until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction}
     * or {@link #release(long) release} it.
     *
     * <p>
     * The asynchronous version of {@link #getx(long, co.paralleluniverse.common.io.Persistable, co.paralleluniverse.galaxy.StoreTransaction) getx(long, Persistable, StoreTransaction)}
     *
     * @param id     The item's ID.
     * @param object The object into which the contents of the item will be written. May be {@code null}.
     * @param txn    The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return A future that will return the passed object.
     */
    ListenableFuture<Persistable> getxAsync(long id, Persistable object, StoreTransaction txn);

    /**
     * Retrieves a given data item asynchronously with a hint as to its {@link #getx(long, StoreTransaction) owner} in the cluster,
     * makes this node its exclusive owner, and pins it. What this means is that no other node will be able to read or
     * update the same item until until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction}
     * or {@link #release(long) release} it.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * <p>
     * The asynchronous version of {@link #getx(long, short, co.paralleluniverse.galaxy.StoreTransaction) getx(long, short, StoreTransaction)}
     *
     * @param id       The item's ID.
     * @param nodeHint The ID of the node the data item is probably owned by.
     * @param txn      The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return A future that will return contents of the item.
     */
    ListenableFuture<byte[]> getxAsync(long id, short nodeHint, StoreTransaction txn);

    /**
     * Retrieves a given data item asynchronously with a hint as to its {@link #getx(long, StoreTransaction) owner} in the cluster,
     * makes this node its exclusive owner, and pins it. What this means is that no other node will be able to read or
     * update the same item until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction} or {@link #release(long) release}
     * it.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * <p>
     * The asynchronous version of {@link #getx(long, short, co.paralleluniverse.common.io.Persistable, co.paralleluniverse.galaxy.StoreTransaction) getx(long, short, Persistable, StoreTransaction)}
     *
     * @param id       The item's ID.
     * @param nodeHint The ID of the node the data item is probably owned by.
     * @param object   The object into which the contents of the item will be written. May be {@code null}.
     * @param txn      The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return A future that will return the passed object.
     */
    ListenableFuture<Persistable> getxAsync(long id, short nodeHint, Persistable object, StoreTransaction txn);

    /**
     * Retrieves a given data item asynchronously with a hint as to its {@link #getx(long, StoreTransaction) owner} in the cluster,
     * makes this node its exclusive owner, and pins it. What this means is that no other node will be able to read or
     * update the same item until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction} or {@link #release(long) release}
     * it.
     *
     * Unlike the direct hint given in {@link #getx(long, short, StoreTransaction)}, the hinted node here is the owner
     * of a given item.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * <p>
     * The asynchronous version of {@link #getxFromOwner(long, long, co.paralleluniverse.galaxy.StoreTransaction) getxFromOwner(long, long, StoreTransaction)}
     *
     * @param id      The item's ID.
     * @param ownerOf The ID of an item whose owner is probably the owner of the requested item as well.
     * @param txn     The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return A future that will return contents of the item.
     */
    ListenableFuture<byte[]> getxFromOwnerAsync(long id, long ownerOf, StoreTransaction txn);

    /**
     * Retrieves a given data item asynchronously with a hint as to its {@link #getx(long, StoreTransaction) owner} in the cluster,
     * makes this node its exclusive owner, and pins it. What this means is that no other node will be able to read or
     * update the same item until we {@link #commit(co.paralleluniverse.galaxy.StoreTransaction) end the transaction} or {@link #release(long) release}
     * it.
     *
     * Unlike the direct hint given in {@link #getx(long, short, Persistable, StoreTransaction)}, the hinted node here
     * is the owner of a given item.<br>
     *
     * If the item is indeed found on the hinted node, the retrieval performance might be superior. If not, the method
     * will still work, but performance may be worse.
     *
     * <p>
     * The asynchronous version of {@link #getxFromOwner(long, long, co.paralleluniverse.common.io.Persistable, co.paralleluniverse.galaxy.StoreTransaction) getxFromOwner(long, long, Persistable, StoreTransaction)}
     *
     * @param id      The item's ID.
     * @param ownerOf The ID of an item whose owner is probably the owner of the requested item as well.
     * @param object  The object into which the contents of the item will be written. May be {@code null}.
     * @param txn     The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return A future that will return the passed object.
     */
    ListenableFuture<Persistable> getxFromOwnerAsync(long id, long ownerOf, Persistable object, StoreTransaction txn);

    /**
     * Gains ownership of an item and sets its contents asynchronously. The asynchronous version of {@link #set(long, byte[], co.paralleluniverse.galaxy.StoreTransaction) set(long, byte[], StoreTransaction)}.
     * Upon completion of the future returned by this method, the item will be pinned if and only if it had been pinned
     * when the method was called.
     *
     * @param id   The item's ID.
     * @param data The contents to write into the item.
     * @param txn  The current transaction. May be null.
     * @return A Void future (that always returns null) that waits for the completion of this operation.
     */
    ListenableFuture<Void> setAsync(long id, byte[] data, StoreTransaction txn);

    /**
     * Gains ownership of an item and sets its contents asynchronously. The asynchronous version of {@link #set(long, java.nio.ByteBuffer, co.paralleluniverse.galaxy.StoreTransaction) set(long, ByteBuffer, StoreTransaction)}.
     * Upon completion of the future returned by this method, the item will be pinned if and only if it had been pinned
     * when the method was called.
     *
     * @param id   The item's ID.
     * @param data The contents to write into the item.
     * @param txn  The current transaction. May be null.
     * @return A Void future (that always returns null) that waits for the completion of this operation.
     */
    ListenableFuture<Void> setAsync(long id, ByteBuffer data, StoreTransaction txn);

    /**
     * Gains ownership of an item and sets its contents asynchronously. The asynchronous version of {@link #set(long, co.paralleluniverse.common.io.Persistable, co.paralleluniverse.galaxy.StoreTransaction)  set(long, Persistable, StoreTransaction)}.
     * Upon completion of the future returned by this method, the item will be pinned if and only if it had been pinned
     * when the method was called.
     *
     * @param id     The item's ID.
     * @param object The contents to write into the item.
     * @param txn    The current transaction. May be null.
     * @return A Void future (that always returns null) that waits for the completion of this operation.
     */
    ListenableFuture<Void> setAsync(long id, Persistable object, StoreTransaction txn);

    <T> ListenableFuture<T> invokeAsync(long id, LineFunction<T> function);

    ListenableFuture<Void> delAsync(long id, StoreTransaction txn);

    /**
     * Makes the given item available in the given nodes' cache. <br>
     *
     * While this method is never necessary for the correct operation of the grid, in some special circumstances it
     * might improve performance if we know that the given nodes will soon be interested in reading the item (e.g. as a
     * result of a message we're about to send them).
     *
     * @param id      The ID of item to push.
     * @param toNodes The nodes to which the item is to be pushed.
     */
    void push(long id, short... toNodes);

    /**
     * Makes the given item available in the given node's cache, and makes that node the owner of the item. <br>
     *
     * While this method is never necessary for the correct operation of the grid, in some special circumstances it
     * might improve performance if we know that the given node will soon be interested in reading or updating the item
     * (e.g. as a result of a message we're about to send it).
     *
     * @param id     The ID of item to push.
     * @param toNode The node to which the item is to be pushed.
     */
    void pushx(long id, short toNode);

    /**
     * Tests whether an item is pinned on this node.
     *
     * @param id The item's ID.
     * @return {@code true} if the item is pinned; {@code false} otherwise.
     */
    boolean isPinned(long id);

    /**
     * Pins item if it can be done locally.
     *
     * @param id    The item's ID.
     * @param state can be X for writePin or S for readPin.
     * @param txn   The current transaction. May be null, in which case you must later call {@link #release(long) release(id)}.
     * @return true if succeeded.
     * @throws IllegalStateException if state is not X or S
     */
    boolean tryPin(long id, ItemState state, StoreTransaction txn) throws IllegalStateException;

    /**
     * Returns an item's state in the local store.
     *
     * @param id The item's ID.
     * @return The item's state.
     */
    ItemState getState(long id);

    /**
     * CacheLine version
     *
     * @param id The item's ID.
     */
    long getVersion(long id);

    /**
     * Sends a message to an item, which will be received by {@link CacheListener#messageReceived(byte[]) CacheListener.messageReceived}
     * on the item's owning node.
     *
     * @param id  The item's ID.
     * @param msg The message.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    void send(long id, Streamable msg) throws TimeoutException;

    /**
     * Sends a message to an item, which will be received by {@link CacheListener#messageReceived(byte[]) CacheListener.messageReceived}
     * on the item's owning node.
     *
     * @param id  The item's ID.
     * @param msg The message.
     * @throws TimeoutException This exception is thrown if the operation has times-out.
     */
    void send(long id, byte[] msg) throws TimeoutException;

    /**
     * Sends a message to an item, which will be received by {@link CacheListener#messageReceived(byte[]) CacheListener.messageReceived}
     * on the item's owning node.
     *
     * @param id  The item's ID.
     * @param msg The message.
     */
    ListenableFuture<Void> sendAsync(long id, Streamable msg);

    /**
     * Sends a message to an item, which will be received by {@link CacheListener#messageReceived(byte[]) CacheListener.messageReceived}
     * on the item's owning node.
     *
     * @param id  The item's ID.
     * @param msg The message.
     */
    ListenableFuture<Void> sendAsync(long id, byte[] msg);
}
