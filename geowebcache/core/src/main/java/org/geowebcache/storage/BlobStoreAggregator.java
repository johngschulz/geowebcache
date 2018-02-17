package org.geowebcache.storage;

import com.google.common.base.Preconditions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.GeoWebCacheExtensions;
import org.geowebcache.config.BlobStoreConfiguration;
import org.geowebcache.config.BlobStoreInfo;
import org.geowebcache.config.ConfigurationAggregator;
import org.geowebcache.config.ServerConfiguration;
import org.geowebcache.config.meta.ServiceInformation;
import org.geowebcache.util.CompositeIterable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

public class BlobStoreAggregator implements ConfigurationAggregator<BlobStoreConfiguration>, ApplicationContextAware, InitializingBean {

    private List<BlobStoreConfiguration> configs;
    private static Log log = LogFactory.getLog(org.geowebcache.storage.BlobStoreAggregator.class);
    private ApplicationContext applicationContext;

    /**
     * Used to delegate calls to {@link BlobStoreConfiguration} objects
     */
    public BlobStoreAggregator() {
    }

    /**
     * Used to delegate calls to {@link BlobStoreConfiguration} objects
     * @param configs
     */
    public BlobStoreAggregator(List<BlobStoreConfiguration> configs) {
        this.configs = configs == null ? new ArrayList<BlobStoreConfiguration>() : configs;
    }

    /**
     * Add a new {@link BlobStoreConfiguration} to the BlobStoreAggregator configs configuration list.
     * @param config a new {@link BlobStoreConfiguration} to add to the configs list
     */
    public void addConfiguration(BlobStoreConfiguration config) {
        List<BlobStoreConfiguration> newList = new ArrayList<BlobStoreConfiguration>(configs);
        newList.add(config);
        this.configs = newList;
    }

    /**
     * Indicates if this configurations contains a {@link BlobStoreInfo) identified by a given name.
     * @param blobStoreInfoName the name of a {@link BlobStoreInfo} for which existence is desired.
     * @return True if a {@link BlobStoreInfo} currently exists with the unique name provided, false otherwise.
     */
    public boolean blobStoreExists(final String blobStoreInfoName) {
        for (int i = 0; i < configs.size(); i++) {
            BlobStoreConfiguration configuration = configs.get(i);
            Optional<BlobStoreInfo> blob = configuration.getBlobStore(blobStoreInfoName);
            if (blob.isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the blob store info named after the {@code blobStoreName} parameter.
     *
     * @throws GeoWebCacheException
     *             if no such blob store info exists
     */
    public BlobStoreInfo getBlobStore(final String blobStoreName) throws GeoWebCacheException {
        Preconditions.checkNotNull(blobStoreName, "blobStoreName is null");

        for (int i = 0; i < configs.size(); i++) {
            BlobStoreConfiguration configuration = configs.get(i);
            Optional<BlobStoreInfo> blob = configuration.getBlobStore(blobStoreName);
            if (blob.isPresent()) {
                return blob.get();
            }
        }
        throw new GeoWebCacheException("Thread " + Thread.currentThread().getId()
                + " Unknown blob store " + blobStoreName + ". Check the logfiles,"
                + " it may not have loaded properly.");
    }

    /**
     * Retrieves the number of {@link BlobStoreInfo}s in the blobstore configuration.
     * @return The number of {@link BlobStoreInfo}s currently configured.
     */
    public int getBlobStoreCount() {
        int count = 0;
        for (int i = 0; i < configs.size(); i++) {
            BlobStoreConfiguration configuration = configs.get(i);
            count += configuration.getBlobStoreCount();
        }
        return count;
    }

    /**
     * Retrieve a list of all {@link BlobStoreInfo} names for each configuration.
     * @return A list of names that can be used to identify each of the {@link BlobStoreInfo}s currently configured.
     */
    public List<String> getBlobStoreNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < configs.size(); i++) {
            BlobStoreConfiguration configuration = configs.get(i);
            names.addAll(configuration.getBlobStoreNames());
        }
        return names;
    }

    /**
     * Returns a list of all the blob stores. The consumer may still have to initialize each blob stores!
     * <p>
     * Modifications to the returned blob stores do not change the internal list of blob stores, but blob stores ARE
     * mutable.
     * </p>
     *
     * @return a list view of this blob store aggregators's internal blob stores
     */
    @SuppressWarnings("unchecked")
    public Iterable<BlobStoreInfo> getBlobStores() {
        List<Iterable<BlobStoreInfo>> perConfigBlobStores = new ArrayList<Iterable<BlobStoreInfo>>(
                configs.size());

        for (BlobStoreConfiguration config : configs) {
            perConfigBlobStores.add((Iterable<BlobStoreInfo>) config.getBlobStores());
        }

        return new CompositeIterable<BlobStoreInfo>(perConfigBlobStores);
    }

    /**
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public void destroy() throws Exception {
        //
    }

    /**
     * Finds out which {@link BlobStoreConfiguration} contains the given blob store, removes it, and saves the configuration.
     *
     * @param blobStoreName the name of the blob store to remove
     * @return true if the blob store was removed, false if it was not found.
     */
    public synchronized void removeBlobStore(final String blobStoreName)
            throws IllegalArgumentException {
        for (BlobStoreConfiguration config : configs) {
            if (config.containsBlobStore(blobStoreName)) {
                config.removeBlobStore(blobStoreName);
                return;
            }
        }
        throw new NoSuchElementException("No configuration found containing blob store "+blobStoreName);
    }
    /**
     * Adds a blob store and returns the {@link BlobStoreConfiguration} to which the blob stores was added.
     *
     * @param bs the blob store to add
     * @throws IllegalArgumentException if the given blob store can't be added to any configuration managed by this blob
     *         store aggregator.
     */
    public synchronized void addBlobStore(final BlobStoreInfo bs) throws IllegalArgumentException {
        for (BlobStoreConfiguration c : configs) {
            if (c.canSave(bs)) {
                c.addBlobStore(bs);
                return;
            }
        }
        throw new IllegalArgumentException("No configuration found capable of saving " + bs);
    }

    /**
     * Renames an existing blob store.
     *
     * @param oldName The name of the existing blob store
     * @param newName The name to rename the blob store to
     * @throws IllegalArgumentException
     */
    public synchronized void renameBlobStore(final String oldName, final String newName) throws NoSuchElementException, IllegalArgumentException {
        BlobStoreConfiguration config = getConfiguration(oldName);
        config.renameBlobStore(oldName, newName);
    }

    /**
     * Replaces the given blob store and returns the Blob Store's configuration, saving the configuration.
     *
     * @param bs The blob store to modify
     * @throws IllegalArgumentException
     */
    public synchronized void modifyBlobStore(final BlobStoreInfo bs) throws IllegalArgumentException {
        BlobStoreConfiguration config = getConfiguration(bs);
        //TODO: this won't work with GetCapabilitiesConfiguration
        config.modifyBlobStore(bs);
    }

    /**
     * Returns the BlobStoreConfiguration for the given BlobStoreInfo, if found in list of blob store configurations.
     *
     * @param bs BlobStoreInfo for this configuration
     * @return The BlobStoreConfiguration for BlobStoreInfo
     * @throws IllegalArgumentException
     */
    public BlobStoreConfiguration getConfiguration(BlobStoreInfo bs) throws IllegalArgumentException {
        Assert.notNull(bs, "blobStore is null");
        return getConfiguration(bs.getName());
    }

    /**
     * Returns the BlobStoreConfiguration for the given Blob Store Name, if found in list of blob store configurations.
     *
     * @param blobStoreName
     * @return
     * @throws IllegalArgumentException
     */
    public BlobStoreConfiguration getConfiguration(final String blobStoreName) throws IllegalArgumentException {
        Assert.notNull(blobStoreName, "blobStoreName is null");
        for (BlobStoreConfiguration c : configs) {
            if (c.containsBlobStore(blobStoreName)) {
                return c;
            }
        }
        throw new IllegalArgumentException("No configuration found containing blob store " + blobStoreName);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.configs = GeoWebCacheExtensions.configurations(BlobStoreConfiguration.class, applicationContext);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(this.applicationContext!=null) throw new IllegalStateException("Application context has already been set");
        Objects.requireNonNull(applicationContext);
        this.applicationContext = applicationContext;
    }
    @Override
    public <BSA extends BlobStoreConfiguration> List<? extends BSA> getConfigurations(Class<BSA> type) {
        if(type==BlobStoreConfiguration.class) {
            return (List<? extends BSA>) Collections.unmodifiableList(configs);
        } else {
            return configs.stream()
                    .filter(type::isInstance)
                    .map(type::cast)
                    .collect(Collectors.toList());
        }
    }
}
