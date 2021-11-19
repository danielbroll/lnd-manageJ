package de.cotto.lndmanagej.model;

import static java.util.Objects.requireNonNull;

public class ForceClosedChannelBuilder extends ClosedChannelBuilder<ForceClosedChannel> {
    public ForceClosedChannelBuilder() {
        super();
    }

    @Override
    public ForceClosedChannel build() {
        return new ForceClosedChannel(
                requireNonNull(channelId),
                requireNonNull(channelPoint),
                requireNonNull(capacity),
                requireNonNull(ownPubkey),
                requireNonNull(remotePubkey),
                requireNonNull(closeTransactionHash),
                requireNonNull(openInitiator),
                requireNonNull(closeInitiator)
        );
    }
}