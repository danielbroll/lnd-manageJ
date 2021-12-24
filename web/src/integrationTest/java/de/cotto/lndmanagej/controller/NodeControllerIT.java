package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeDetailsService;
import de.cotto.lndmanagej.service.NodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_3;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_3;
import static de.cotto.lndmanagej.model.NodeDetailsFixtures.NODE_DETAILS;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = NodeController.class)
class NodeControllerIT {
    private static final String NODE_PREFIX = "/api/node/" + PUBKEY;
    private static final FeeReport FEE_REPORT = new FeeReport(Coins.ofMilliSatoshis(1_234), Coins.ofMilliSatoshis(567));

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NodeService nodeService;

    @MockBean
    private ChannelService channelService;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private BalanceService balanceService;

    @MockBean
    private FeeService feeService;

    @MockBean
    private NodeDetailsService nodeDetailsService;

    @Test
    void getAlias() throws Exception {
        when(nodeService.getAlias(PUBKEY)).thenReturn(ALIAS_2);
        mockMvc.perform(get(NODE_PREFIX + "/alias"))
                .andExpect(content().string(ALIAS_2));
    }

    @Test
    void getDetails() throws Exception {
        when(nodeDetailsService.getDetails(PUBKEY)).thenReturn(NODE_DETAILS);
        List<String> channelIds = List.of(CHANNEL_ID.toString());
        List<String> closedChannelIds = List.of(CHANNEL_ID_2.toString());
        List<String> waitingCloseChannelIds = List.of(CHANNEL_ID_3.toString());
        List<String> forceClosingChannelIds = List.of(CHANNEL_ID_4.toString());
        mockMvc.perform(get(NODE_PREFIX + "/details"))
                .andExpect(jsonPath("$.node", is(PUBKEY.toString())))
                .andExpect(jsonPath("$.alias", is(ALIAS)))
                .andExpect(jsonPath("$.channels", is(channelIds)))
                .andExpect(jsonPath("$.closedChannels", is(closedChannelIds)))
                .andExpect(jsonPath("$.waitingCloseChannels", is(waitingCloseChannelIds)))
                .andExpect(jsonPath("$.pendingForceClosingChannels", is(forceClosingChannelIds)))
                .andExpect(jsonPath("$.rebalanceReport.sourceCosts", is("1000000")))
                .andExpect(jsonPath("$.rebalanceReport.targetCosts", is("2000000")))
                .andExpect(jsonPath("$.rebalanceReport.sourceAmount", is("665000")))
                .andExpect(jsonPath("$.rebalanceReport.targetAmount", is("991000")))
                .andExpect(jsonPath("$.rebalanceReport.supportAsSourceAmount", is("100000")))
                .andExpect(jsonPath("$.rebalanceReport.supportAsTargetAmount", is("200000")))
                .andExpect(jsonPath("$.balance.localBalance", is("2000")))
                .andExpect(jsonPath("$.balance.localReserve", is("200")))
                .andExpect(jsonPath("$.balance.localAvailable", is("1800")))
                .andExpect(jsonPath("$.balance.remoteBalance", is("223")))
                .andExpect(jsonPath("$.balance.remoteReserve", is("20")))
                .andExpect(jsonPath("$.balance.remoteAvailable", is("203")))
                .andExpect(jsonPath("$.feeReport.earned", is("1234")))
                .andExpect(jsonPath("$.feeReport.sourced", is("567")))
                .andExpect(jsonPath("$.onChainCosts.openCosts", is("1000")))
                .andExpect(jsonPath("$.onChainCosts.closeCosts", is("2000")))
                .andExpect(jsonPath("$.onChainCosts.sweepCosts", is("3000")))
                .andExpect(jsonPath("$.onlineReport.online", is(true)))
                .andExpect(jsonPath("$.onlineReport.since", is("2021-12-23T01:02:03Z")));
    }

    @Test
    void getOpenChannelIds() throws Exception {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3));
        List<String> channelIds = List.of(CHANNEL_ID.toString(), CHANNEL_ID_3.toString());
        mockMvc.perform(get(NODE_PREFIX + "/open-channels"))
                .andExpect(jsonPath("$.node", is(PUBKEY.toString())))
                .andExpect(jsonPath("$.channels", is(channelIds)));
    }

    @Test
    void getAllChannelIds() throws Exception {
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_3));
        List<String> channelIds = List.of(CHANNEL_ID.toString(), CHANNEL_ID_3.toString());
        mockMvc.perform(get(NODE_PREFIX + "/all-channels"))
                .andExpect(jsonPath("$.node", is(PUBKEY.toString())))
                .andExpect(jsonPath("$.channels", is(channelIds)));
    }

    @Test
    void getBalance() throws Exception {
        when(balanceService.getBalanceInformationForPeer(PUBKEY)).thenReturn(BALANCE_INFORMATION);
        mockMvc.perform(get(NODE_PREFIX + "/balance"))
                .andExpect(jsonPath("$.localBalance", is("1000")))
                .andExpect(jsonPath("$.localReserve", is("100")))
                .andExpect(jsonPath("$.localAvailable", is("900")))
                .andExpect(jsonPath("$.remoteBalance", is("123")))
                .andExpect(jsonPath("$.remoteReserve", is("10")))
                .andExpect(jsonPath("$.remoteAvailable", is("113")));
    }

    @Test
    void getFeeReport() throws Exception {
        when(feeService.getFeeReportForPeer(PUBKEY)).thenReturn(FEE_REPORT);
        mockMvc.perform(get(NODE_PREFIX + "/fee-report"))
                .andExpect(jsonPath("$.earned", is("1234")))
                .andExpect(jsonPath("$.sourced", is("567")));
    }
}