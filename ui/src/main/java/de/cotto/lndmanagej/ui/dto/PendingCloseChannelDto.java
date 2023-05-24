package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.model.CloseInitiator;
import de.cotto.lndmanagej.model.Pubkey;

public record PendingCloseChannelDto(
        String remoteAlias,
        Pubkey remotePubkey,
        long capacitySat,
        boolean privateChannel,
        CloseInitiator initiator
) {
}
