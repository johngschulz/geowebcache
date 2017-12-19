package org.geowebcache.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.conveyor.ConveyorTile;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.grid.OutsideCoverageException;
import org.geowebcache.layer.AbstractTileLayer;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.layer.wms.WMSLayer;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class XMLConfigurationLayerConformanceTest extends LayerConfigurationTest {

    @Override
    protected TileLayer getGoodLayer(String id, int rand) {
        WMSLayer layer = new WMSLayer(id, new String[] {"http://example.com/"}, null, 
                Integer.toString(rand),null, null, null, null,
                null, false, null);
        return layer;
    }

    @Override
    protected TileLayer getBadLayer(String id, int rand) {
        return new AbstractTileLayer() {
            {
                this.name=id;
            }

            @Override
            protected boolean initializeInternal(GridSetBroker gridSetBroker) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public String getStyles() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ConveyorTile getTile(ConveyorTile tile)
                    throws GeoWebCacheException, IOException, OutsideCoverageException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public ConveyorTile getNoncachedTile(ConveyorTile tile) throws GeoWebCacheException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void seedTile(ConveyorTile tile, boolean tryCache)
                    throws GeoWebCacheException, IOException {
                // TODO Auto-generated method stub
                
            }

            @Override
            public ConveyorTile doNonMetatilingRequest(ConveyorTile tile)
                    throws GeoWebCacheException {
                // TODO Auto-generated method stub
                return null;
            }
            
        };
    }
    
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();
    private File configDir;
    private File configFile;
    
    @Override
    protected TileLayerConfiguration getConfig() throws Exception {
        if(configFile==null) {
            configDir = temp.getRoot();
            configFile = temp.newFile("geowebcache.xml");
            
            URL source = XMLConfiguration.class
                .getResource(XMLConfigurationBackwardsCompatibilityTest.LATEST_FILENAME);
            FileUtils.copyURLToFile(source, configFile);
        }
        
        GridSetBroker gridSetBroker = new GridSetBroker(true, true);
        config = new XMLConfiguration(null, configDir.getAbsolutePath());
        config.initialize(gridSetBroker);
        return config;
    }

    @Override
    protected Matcher<TileLayer> layerEquals(TileLayer expected) {
        return new CustomMatcher<TileLayer>("Layer matching "+expected.getId()+" with "+ ((WMSLayer)expected).getWmsLayers()){
            
            @Override
            public boolean matches(Object item) {
                return item instanceof WMSLayer && ((WMSLayer)item).getId().equals(((WMSLayer)expected).getId()) &&
                    ((WMSLayer)item).getWmsLayers().equals(((WMSLayer)expected).getWmsLayers());
            }
            
        };
    }

    @Override
    protected String getExistingLayer() throws Exception {
        return "topp:states";
    }

}