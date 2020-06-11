package com.github.devbinnooh.conf;

import lombok.Data;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Properties;

@Data
public class DRServerConfig extends CombinedConfiguration implements Cloneable, Serializable {

    public DRServerConfig()  {

        this(null);
    }

    public DRServerConfig(Properties ... properties)
    {
        try {
            addConfiguration(new SystemConfiguration());
            addConfiguration(new Configurations().properties("config/application.properties"));
            if(properties != null)
                Arrays.stream(properties).forEach(prop -> super.addConfiguration(ConfigurationConverter.getConfiguration(prop)));
        }catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void addConfiguration(Properties properties) {
        super.addConfiguration(ConfigurationConverter.getConfiguration(properties));
    }
}
