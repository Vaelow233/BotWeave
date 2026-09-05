package org.vaelow233.botweave.connector.qq.ob11.connector;

import org.vaelow233.botweave.connector.qq.ob11.config.OneBotConfig;
import org.vaelow233.botweave.core.connector.Connector;
import org.vaelow233.botweave.core.connector.ConnectorContext;
import org.vaelow233.botweave.core.connector.ConnectorFactory;

public class OneBotConnectorFactory implements ConnectorFactory<OneBotConfig> {
    @Override
    public String type() {
        return "qq-onebot-11";
    }

    @Override
    public Connector create(OneBotConfig configuration, ConnectorContext context) {
        return new OneBotConnector(configuration, context);
    }
}
