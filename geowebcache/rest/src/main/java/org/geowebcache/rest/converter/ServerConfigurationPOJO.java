package org.geowebcache.rest.converter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.GeoWebCacheExtensions;
import org.geowebcache.config.ServerConfiguration;
import org.geowebcache.config.meta.ServiceInformation;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.locks.LockProvider;
import org.geowebcache.locks.MemoryLockProvider;

import java.io.IOException;

/**
 * POJO implementation of {@link org.geowebcache.config.ServerConfiguration}, for serialization via XStream.
 */
public class ServerConfigurationPOJO implements ServerConfiguration {


    private ServiceInformation serviceInformation;
    private Boolean runtimeStatsEnabled;
    //The bean name for the lock provider
    private String lockProvider;
    private Boolean fullWMS;
    private Boolean wmtsCiteCompliant;
    private Integer backendTimeout;
    private Boolean cacheBypassAllowed;
    private final String version;
    private final String identifier;
    private final String location;

    /**
     * Constructs a new POJO based on the provided {@link ServerConfiguration}
     * @param template Template used to initialize properties
     */
    public ServerConfigurationPOJO(ServerConfiguration template) throws IOException {
        this.serviceInformation = template.getServiceInformation();
        this.runtimeStatsEnabled = template.isRuntimeStatsEnabled();

        setLockProvider(template.getLockProvider());

        this.fullWMS = template.isFullWMS();
        this.wmtsCiteCompliant = template.isWmtsCiteCompliant();
        this.backendTimeout = template.getBackendTimeout();
        this.cacheBypassAllowed = template.isCacheBypassAllowed();
        this.version = template.getVersion();
        this.identifier = template.getIdentifier();
        this.location = template.getLocation();
    }

    /**
     * Applies the values of this POJO to the provided ServerConfiguration
     * Note: Does not set version, identifier, or location, as those are read-only.
     *
     * @param config The configuration to set the values on.
     */
    public void apply(ServerConfiguration config) throws IOException {
        if (getServiceInformation() != null) {
            config.setServiceInformation(getServiceInformation());
        }
        if (isRuntimeStatsEnabled() != null) {
            config.setRuntimeStatsEnabled(isRuntimeStatsEnabled());
        }
        if (getLockProvider() != null) {
            config.setLockProvider(getLockProvider());
        }
        if (isFullWMS() != null) {
            config.setFullWMS(isFullWMS());
        }
        if (isWmtsCiteCompliant() != null) {
            config.setWmtsCiteCompliant(isWmtsCiteCompliant());
        }
        if (getBackendTimeout() != null) {
            config.setBackendTimeout(getBackendTimeout());
        }
        if (isCacheBypassAllowed() != null) {
            config.setCacheBypassAllowed(isCacheBypassAllowed());
        }
    }

    @Override
    public ServiceInformation getServiceInformation() {
        return serviceInformation;
    }

    @Override
    public void setServiceInformation(ServiceInformation serviceInfo) throws IOException {
        this.serviceInformation = serviceInfo;
    }

    @Override
    public Boolean isRuntimeStatsEnabled() {
        return runtimeStatsEnabled;
    }

    @Override
    public void setRuntimeStatsEnabled(Boolean isEnabled) throws IOException {
        this.runtimeStatsEnabled = isEnabled;
    }

    /**
     * Retrieves the configured {@link LockProvider} bean based on the lock provider bean name ({@link #lockProvider}).
     * @return The LockProvider bean, or a new {@link MemoryLockProvider} if no bean of the given name was found
     */
    @Override
    public LockProvider getLockProvider() {
        if (this.lockProvider != null) {
            LockProvider lockProviderBean = (LockProvider) GeoWebCacheExtensions.bean(this.lockProvider);
            if (lockProviderBean != null) {
                return lockProviderBean;
            }
        }
        return new MemoryLockProvider();
    }

    /**
     * Sets the lock provider bean name ({@link #lockProvider}) based on the provided {@link LockProvider} bean.
     * If the passed lockProvider is not a bean, sets the bean name to null.
     *
     * @param lockProvider The lock provider bean
     * @throws IOException
     */
    @Override
    public void setLockProvider(LockProvider lockProvider) throws IOException {
        //default to null
        this.lockProvider = null;

        if (lockProvider != null) {
            String[] lockProviderNames = GeoWebCacheExtensions.getBeansNamesOrderedByPriority(LockProvider.class);

            for (String beanName : lockProviderNames) {
                if (lockProvider.equals(GeoWebCacheExtensions.bean(beanName))) {
                    this.lockProvider = beanName;
                }
            }
        }
    }

    @Override
    public Boolean isFullWMS() {
        return fullWMS;
    }

    @Override
    public void setFullWMS(Boolean isFullWMS) throws IOException {
        this.fullWMS = fullWMS;
    }

    @Override
    public Boolean isWmtsCiteCompliant() {
        return wmtsCiteCompliant;
    }

    @Override
    public void setWmtsCiteCompliant(Boolean wmtsCiteStrictCompliant) throws IOException {
        this.wmtsCiteCompliant = wmtsCiteStrictCompliant;
    }

    @Override
    public Integer getBackendTimeout() {
        return backendTimeout;
    }

    @Override
    public void setBackendTimeout(Integer backendTimeout) throws IOException {
        this.backendTimeout = backendTimeout;
    }

    @Override
    public Boolean isCacheBypassAllowed() {
        return cacheBypassAllowed;
    }

    @Override
    public void setCacheBypassAllowed(Boolean cacheBypassAllowed) throws IOException {
        this.cacheBypassAllowed = cacheBypassAllowed;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public int initialize(GridSetBroker gridSetBroker) throws GeoWebCacheException {
        throw new UnsupportedOperationException("initialize() not supported for ServerConfigurationPOJO");
    }

    @Override
    public void save() throws IOException {
        throw new UnsupportedOperationException("save() not supported for ServerConfigurationPOJO");
    }
}