package de.cotto.lndmanagej.ui.page.general;

import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;

import java.util.List;

public class DashboardPage extends ThymeleafPage {

    private final List<NodeDto> nodes;
    private final List<OpenChannelDto> channels;

    public DashboardPage(List<OpenChannelDto> channels,
                         List<NodeDto> nodes,
                         NodesAndChannelsWithWarningsDto warnings) {
        super();
        this.nodes = nodes;
        this.channels = channels;
        add("warnings", warnings);
        add("channels", this.channels);
        add("nodes", this.nodes);
    }

    public List<NodeDto> getNodes() {
        return nodes;
    }

    @Override
    public String getView() {
        return "dashboard";
    }

    public List<OpenChannelDto> getChannels() {
        return channels;
    }
}
