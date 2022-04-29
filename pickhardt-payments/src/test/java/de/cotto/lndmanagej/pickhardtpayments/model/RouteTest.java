package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.Policy;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE_2_3;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE_3_4;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static de.cotto.lndmanagej.pickhardtpayments.model.RouteFixtures.ROUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RouteTest {

    private static final int ONE_MILLION = 1_000_000;
    private static final int TIME_LOCK_DELTA = 40;
    private static final int BLOCK_HEIGHT = 700_000;

    @Test
    void default_liquidity_information() {
        Edge edge1 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, Coins.ofSatoshis(99), POLICY_1);
        Edge edge2 = new Edge(CHANNEL_ID_2, PUBKEY, PUBKEY_2, Coins.ofSatoshis(199), POLICY_1);
        Route route = new Route(new BasicRoute(List.of(edge1, edge2), Coins.ofSatoshis(1)));
        assertThat(route.getProbability()).isEqualTo(0.99 * 0.995);
    }

    @Test
    void explicit_liquidity_information() {
        Edge edge1 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, Coins.ofSatoshis(99), POLICY_1);
        Edge edge2 = new Edge(CHANNEL_ID_2, PUBKEY, PUBKEY_2, Coins.ofSatoshis(199), POLICY_1);
        BasicRoute basicRoute = new BasicRoute(List.of(edge1, edge2), Coins.ofSatoshis(1));
        Route route = new Route(basicRoute, List.of(
                EdgeWithLiquidityInformation.forKnownLiquidity(edge1, Coins.ofSatoshis(99)),
                EdgeWithLiquidityInformation.forUpperBound(edge2, Coins.ofSatoshis(199))
        ));
        assertThat(route.getProbability()).isEqualTo(1 * 0.995);
    }

    @Test
    void explicit_liquidity_information_missing_edge() {
        Edge edge1 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, Coins.ofSatoshis(1), POLICY_1);
        Edge edge2 = new Edge(CHANNEL_ID_2, PUBKEY, PUBKEY_2, Coins.ofSatoshis(2), POLICY_1);
        BasicRoute basicRoute = new BasicRoute(List.of(edge1, edge2), Coins.ofSatoshis(1));
        assertThatIllegalArgumentException().isThrownBy(
                () -> new Route(basicRoute, List.of(EdgeWithLiquidityInformation.forUpperBound(edge1, edge1.capacity())
                )));
    }

    @Test
    void explicit_liquidity_information_wrong_edge() {
        Edge edge1 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, Coins.ofSatoshis(1), POLICY_1);
        Edge edge2 = new Edge(CHANNEL_ID_2, PUBKEY, PUBKEY_2, Coins.ofSatoshis(2), POLICY_1);
        Edge wrongEdge = new Edge(CHANNEL_ID_3, PUBKEY, PUBKEY_2, Coins.ofSatoshis(3), POLICY_1);
        BasicRoute basicRoute = new BasicRoute(List.of(edge1, edge2), Coins.ofSatoshis(1));
        assertThatIllegalArgumentException().isThrownBy(() -> new Route(basicRoute, List.of(
                EdgeWithLiquidityInformation.forLowerBound(edge1, Coins.NONE),
                EdgeWithLiquidityInformation.forLowerBound(wrongEdge, Coins.NONE)
        )));
    }

    @Test
    void explicit_liquidity_information_additional_edge() {
        Edge edge1 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, Coins.ofSatoshis(1), POLICY_1);
        Edge additionalEdge = new Edge(CHANNEL_ID_3, PUBKEY_3, PUBKEY_4, Coins.ofSatoshis(3), POLICY_2);
        BasicRoute basicRoute = new BasicRoute(List.of(edge1), Coins.ofSatoshis(1));
        assertThatIllegalArgumentException().isThrownBy(() -> new Route(basicRoute, List.of(
                EdgeWithLiquidityInformation.forLowerBound(edge1, Coins.NONE),
                EdgeWithLiquidityInformation.forLowerBound(additionalEdge, Coins.NONE)
        )));
    }

    @Test
    void getProbability() {
        assertThat(ROUTE.getProbability()).isEqualTo(0.999_985_714_354_421_7);
    }

    @Test
    void getProbability_within_known_liquidity() {
        long availableLiquiditySat = 100;
        Coins capacity = Coins.ofSatoshis(200);
        Coins amount = Coins.ofSatoshis(90);
        Edge edge = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, capacity, POLICY_1);
        BasicRoute basicRoute = new BasicRoute(List.of(edge), amount);
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                EdgeWithLiquidityInformation.forKnownLiquidity(edge, Coins.ofSatoshis(availableLiquiditySat));
        Route route = new Route(basicRoute, List.of(edgeWithLiquidityInformation));
        assertThat(route.getProbability()).isEqualTo(1.0);
    }

    @Test
    void getProbability_exactly_known_liquidity() {
        Route route = routeForAmountAndCapacityAndKnownLiquidity(100, 200, 100);
        assertThat(route.getProbability())
                .isEqualTo(1.0);
    }

    @Test
    void getProbability_above_known_liquidity() {
        Route route = routeForAmountAndCapacityAndKnownLiquidity(250, 300, 200);
        assertThat(route.getProbability()).isEqualTo(0.0);
    }

    @Test
    void getProbability_above_known_lower_bound_for_liquidity() {
        long lowerBoundSat = 100;
        long capacitySat = 200;
        int amountSat = 150;
        Edge edge = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, Coins.ofSatoshis(capacitySat), POLICY_1);
        BasicRoute basicRoute = new BasicRoute(List.of(edge), Coins.ofSatoshis(amountSat));
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                EdgeWithLiquidityInformation.forLowerBound(edge, Coins.ofSatoshis(lowerBoundSat));
        Route route = new Route(basicRoute, List.of(edgeWithLiquidityInformation));
        assertThat(route.getProbability())
                .isEqualTo(1.0 * (capacitySat + 1 - amountSat) / (capacitySat + 1 - lowerBoundSat));
    }

    @Test
    void getProbability_below_known_upper_bound_for_liquidity() {
        long upperBoundSat = 100;
        Coins capacity = Coins.ofSatoshis(200);
        Coins amount = Coins.ofSatoshis(80);
        Edge edge = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, capacity, POLICY_1);
        BasicRoute basicRoute = new BasicRoute(List.of(edge), amount);
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                EdgeWithLiquidityInformation.forUpperBound(edge, Coins.ofSatoshis(upperBoundSat));
        Route route = new Route(basicRoute, List.of(edgeWithLiquidityInformation));
        assertThat(route.getProbability())
                .isEqualTo(1.0 * (upperBoundSat + 1 - amount.satoshis()) / (upperBoundSat + 1));
    }

    @Test
    void fees_amount_with_milli_sat() {
        Coins amount = Coins.ofMilliSatoshis(1_500_000_111);
        int ppm1 = 50;
        int ppm2 = 100;
        Coins baseFee1 = Coins.ofMilliSatoshis(15);
        Coins baseFee2 = Coins.ofMilliSatoshis(10);
        Coins expectedFees =
                Coins.ofMilliSatoshis((long) (amount.milliSatoshis() * 1.0 * ppm2 / ONE_MILLION))
                        .add(baseFee2);
        Policy policy1 = new Policy(ppm1, baseFee1, true, TIME_LOCK_DELTA);
        Edge hop1 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy1);
        Policy policy2 = new Policy(ppm2, baseFee2, true, TIME_LOCK_DELTA);
        Edge hop2 = new Edge(CHANNEL_ID_2, PUBKEY_2, PUBKEY_3, CAPACITY, policy2);
        BasicRoute basicRoute = new BasicRoute(List.of(hop1, hop2), amount);
        Route route = new Route(basicRoute);
        assertThat(route.fees()).isEqualTo(expectedFees);
    }

    @Test
    void fees_one_hop() {
        Coins amount = Coins.ofSatoshis(1_500_000);
        Coins baseFee = Coins.ofMilliSatoshis(10);
        int ppm = 100;
        BasicRoute basicRoute = new BasicRoute(List.of(
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm, baseFee, true, TIME_LOCK_DELTA))
        ), amount);
        Route route = new Route(basicRoute);
        assertThat(route.fees()).isEqualTo(Coins.NONE);
    }

    @Test
    void fees_two_hops() {
        Coins amount = Coins.ofSatoshis(1_500_000);
        Coins baseFee1 = Coins.ofMilliSatoshis(10);
        Coins baseFee2 = Coins.ofMilliSatoshis(5);
        int ppm1 = 100;
        int ppm2 = 200;
        Coins expectedFees2 =
                Coins.ofMilliSatoshis((long) (amount.milliSatoshis() * 1.0 * ppm2 / ONE_MILLION))
                        .add(baseFee2);
        Coins expectedFees1 = Coins.NONE;
        Coins expectedFees = expectedFees1.add(expectedFees2);
        BasicRoute basicRoute = new BasicRoute(List.of(
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm1, baseFee1, true, TIME_LOCK_DELTA)),
                new Edge(CHANNEL_ID_2, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm2, baseFee2, true, TIME_LOCK_DELTA))
        ), amount);
        Route route = new Route(basicRoute);
        assertThat(route.fees()).isEqualTo(expectedFees);
    }

    @Test
    void fees_three_hops() {
        Coins amount = Coins.ofSatoshis(3_000_000);
        Coins baseFee1 = Coins.ofMilliSatoshis(100);
        Coins baseFee2 = Coins.ofMilliSatoshis(50);
        Coins baseFee3 = Coins.ofMilliSatoshis(10);
        int ppm1 = 100;
        int ppm2 = 200;
        int ppm3 = 300;
        Coins expectedFees3 = Coins.NONE;
        Coins expectedFees2 =
                Coins.ofMilliSatoshis((long) (amount.milliSatoshis() * 1.0 * ppm3 / ONE_MILLION))
                        .add(baseFee3);
        long amountWithFeesLastHop = amount.add(expectedFees2).milliSatoshis();
        Coins expectedFees1 = Coins.ofMilliSatoshis(
                (long) (amountWithFeesLastHop * 1.0 * ppm2 / ONE_MILLION)
        ).add(baseFee2);
        Coins expectedFees = expectedFees1.add(expectedFees2).add(expectedFees3);
        BasicRoute basicRoute = new BasicRoute(List.of(
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm1, baseFee1, true, TIME_LOCK_DELTA)),
                new Edge(CHANNEL_ID_2, PUBKEY_2, PUBKEY_3, CAPACITY, new Policy(ppm2, baseFee2, true, TIME_LOCK_DELTA)),
                new Edge(CHANNEL_ID_3, PUBKEY_3, PUBKEY_4, CAPACITY, new Policy(ppm3, baseFee3, true, TIME_LOCK_DELTA))
        ), amount);
        Route route = new Route(basicRoute);
        assertThat(route.fees()).isEqualTo(expectedFees);
    }

    @Test
    void feesWithFirstHop_empty() {
        BasicRoute basicRoute = new BasicRoute(List.of(), Coins.ofSatoshis(1_500_000));
        Route route = new Route(basicRoute);
        assertThat(route.feesWithFirstHop()).isEqualTo(Coins.NONE);
    }

    @Test
    void feesWithFirstHop_one_hop() {
        Coins amount = Coins.ofSatoshis(1_500_000);
        Coins baseFee = Coins.ofMilliSatoshis(10);
        int ppm = 100;
        Coins expectedFees = Coins.ofMilliSatoshis(amount.milliSatoshis() * ppm / ONE_MILLION).add(baseFee);
        BasicRoute basicRoute = new BasicRoute(List.of(
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm, baseFee, true, TIME_LOCK_DELTA))
        ), amount);
        Route route = new Route(basicRoute);
        assertThat(route.feesWithFirstHop()).isEqualTo(expectedFees);
    }

    @Test
    void feesWithFirstHop_three_hops() {
        Coins amount = Coins.ofSatoshis(1_500_000);
        Coins baseFee1 = Coins.ofMilliSatoshis(10);
        Coins baseFee2 = Coins.ofMilliSatoshis(5);
        Coins baseFee3 = Coins.ofMilliSatoshis(1);
        int ppm1 = 100;
        int ppm2 = 200;
        int ppm3 = 300;
        Coins feesForThirdHop = Coins.ofMilliSatoshis(amount.milliSatoshis() * ppm3 / ONE_MILLION).add(baseFee3);
        Coins feesForSecondHop =
                Coins.ofMilliSatoshis(amount.add(feesForThirdHop).milliSatoshis() * ppm2 / ONE_MILLION).add(baseFee2);
        Coins amountForFirstHop = amount.add(feesForThirdHop).add(feesForSecondHop);
        Coins feesForFirstHop =
                Coins.ofMilliSatoshis(amountForFirstHop.milliSatoshis() * ppm1 / ONE_MILLION).add(baseFee1);
        Coins expectedFees = feesForFirstHop.add(feesForSecondHop).add(feesForThirdHop);
        BasicRoute basicRoute = new BasicRoute(List.of(
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm1, baseFee1, true, TIME_LOCK_DELTA)),
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm2, baseFee2, true, TIME_LOCK_DELTA)),
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm3, baseFee3, true, TIME_LOCK_DELTA))
        ), amount);
        Route route = new Route(basicRoute);
        assertThat(route.feesWithFirstHop()).isEqualTo(expectedFees);
    }

    @Test
    void feeForHop() {
        Coins amount = Coins.ofSatoshis(2_000);
        int ppm1 = 123;
        int ppm2 = 456;
        int ppm3 = 789;
        Coins expectedFees3 = Coins.NONE;
        Coins expectedFees2 =
                Coins.ofMilliSatoshis((long) (amount.milliSatoshis() * 1.0 * ppm3 / ONE_MILLION));
        Coins expectedFees1 =
                Coins.ofMilliSatoshis((long) (amount.add(expectedFees2).milliSatoshis() * 1.0 * ppm2 / ONE_MILLION));
        Route route = createRoute(amount, ppm1, ppm2, ppm3);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(route.feeForHop(2)).isEqualTo(expectedFees3);
        softly.assertThat(route.feeForHop(1)).isEqualTo(expectedFees2);
        softly.assertThat(route.feeForHop(0)).isEqualTo(expectedFees1);
        softly.assertAll();
    }

    @Test
    void forwardAmountForHop() {
        Coins amount = Coins.ofSatoshis(2_000);
        Route route = createRoute(amount, 123, 456, 789);
        Coins feeForHop2 = route.feeForHop(1);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(route.forwardAmountForHop(2)).isEqualTo(amount);
        softly.assertThat(route.forwardAmountForHop(1)).isEqualTo(amount);
        softly.assertThat(route.forwardAmountForHop(0)).isEqualTo(amount.add(feeForHop2));
        softly.assertAll();
    }

    @Test
    void expiryForHop_route_with_one_hop() {
        int timeLockDelta = 123;
        int finalCltvDelta = 456;
        List<Edge> edges = edgesWithTimeLockDeltas(timeLockDelta);
        BasicRoute basicRoute = new BasicRoute(edges, Coins.ofSatoshis(1));
        Route route = new Route(basicRoute);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(route.expiryForHop(0, BLOCK_HEIGHT, finalCltvDelta)).isEqualTo(BLOCK_HEIGHT + finalCltvDelta);
        softly.assertThat(route.totalTimeLock(BLOCK_HEIGHT, finalCltvDelta)).isEqualTo(BLOCK_HEIGHT + finalCltvDelta);
        softly.assertAll();
    }

    @Test
    void expiry_route_with_two_hops() {
        int timeLockDelta1 = 40;
        int timeLockDelta2 = 123;
        int finalCltvDelta = 456;
        List<Edge> edges = edgesWithTimeLockDeltas(timeLockDelta1, timeLockDelta2);
        BasicRoute basicRoute = new BasicRoute(edges, Coins.ofSatoshis(100));
        Route route = new Route(basicRoute);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(route.expiryForHop(0, BLOCK_HEIGHT, finalCltvDelta)).isEqualTo(BLOCK_HEIGHT + finalCltvDelta);
        softly.assertThat(route.expiryForHop(1, BLOCK_HEIGHT, finalCltvDelta)).isEqualTo(BLOCK_HEIGHT + finalCltvDelta);
        softly.assertThat(route.totalTimeLock(BLOCK_HEIGHT, finalCltvDelta))
                .isEqualTo(BLOCK_HEIGHT + finalCltvDelta + timeLockDelta2);
        softly.assertAll();
    }

    @Test
    void expiry_route_with_three_hops() {
        int finalCltvDelta = 456;
        int timeLockDelta1 = 40;
        int timeLockDelta2 = 123;
        int timeLockDelta3 = 9;
        List<Edge> edges = edgesWithTimeLockDeltas(timeLockDelta1, timeLockDelta2, timeLockDelta3);
        BasicRoute basicRoute = new BasicRoute(edges, Coins.ofSatoshis(2));
        Route route = new Route(basicRoute);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(route.expiryForHop(0, BLOCK_HEIGHT, finalCltvDelta))
                .isEqualTo(BLOCK_HEIGHT + finalCltvDelta + timeLockDelta3);
        softly.assertThat(route.expiryForHop(1, BLOCK_HEIGHT, finalCltvDelta))
                .isEqualTo(BLOCK_HEIGHT + finalCltvDelta);
        softly.assertThat(route.expiryForHop(2, BLOCK_HEIGHT, finalCltvDelta))
                .isEqualTo(BLOCK_HEIGHT + finalCltvDelta);
        softly.assertThat(route.totalTimeLock(BLOCK_HEIGHT, finalCltvDelta))
                .isEqualTo(BLOCK_HEIGHT + finalCltvDelta + timeLockDelta3 + timeLockDelta2);
        softly.assertAll();
    }

    @Test
    void expiry_route_with_four_hops() {
        int timeLockDelta1 = 1;
        int timeLockDelta2 = 10;
        int timeLockDelta3 = 100;
        int timeLockDelta4 = 1_000;
        int finalCltvDelta = 10_000;
        List<Edge> edges = edgesWithTimeLockDeltas(timeLockDelta1, timeLockDelta2, timeLockDelta3, timeLockDelta4);
        BasicRoute basicRoute = new BasicRoute(edges, Coins.ofSatoshis(3));
        Route route = new Route(basicRoute);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(route.expiryForHop(0, BLOCK_HEIGHT, finalCltvDelta))
                .isEqualTo(BLOCK_HEIGHT + finalCltvDelta + timeLockDelta4 + timeLockDelta3);
        softly.assertThat(route.expiryForHop(1, BLOCK_HEIGHT, finalCltvDelta))
                .isEqualTo(BLOCK_HEIGHT + finalCltvDelta + timeLockDelta4);
        softly.assertThat(route.expiryForHop(2, BLOCK_HEIGHT, finalCltvDelta))
                .isEqualTo(BLOCK_HEIGHT + finalCltvDelta);
        softly.assertThat(route.expiryForHop(3, BLOCK_HEIGHT, finalCltvDelta))
                .isEqualTo(BLOCK_HEIGHT + finalCltvDelta);
        softly.assertThat(route.totalTimeLock(BLOCK_HEIGHT, finalCltvDelta))
                .isEqualTo(BLOCK_HEIGHT + finalCltvDelta + timeLockDelta4 + timeLockDelta3 + timeLockDelta2);
        softly.assertAll();
    }

    @Test
    void feeRate_two_hops_without_base_fee() {
        int feeRate1 = 100;
        int feeRate2 = 987;
        Coins amount = Coins.ofSatoshis(1_234_000);
        Route route = createRoute(amount, feeRate1, feeRate2);
        assertThat(route.getFeeRate()).isEqualTo(feeRate2);
    }

    @Test
    void feeRate_one_hop_with_base_fee() {
        int feeRate1 = 100;
        int feeRate2 = 987;
        Policy policy1 = new Policy(feeRate1, Coins.ofSatoshis(100_000), true, TIME_LOCK_DELTA);
        Policy policy2 = new Policy(feeRate2, Coins.ofSatoshis(10_000), true, TIME_LOCK_DELTA);
        Coins amount = Coins.ofSatoshis(1_234_567);
        Edge hop1 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy1);
        Edge hop2 = new Edge(CHANNEL_ID_2, PUBKEY_2, PUBKEY_3, CAPACITY, policy2);
        BasicRoute basicRoute = new BasicRoute(List.of(hop1, hop2), amount);
        Route route = new Route(basicRoute);
        assertThat(route.getFeeRate()).isEqualTo(9087);
    }

    @Test
    void feeRate_three_hops() {
        int feeRate1 = 50;
        int feeRate2 = 100;
        int feeRate3 = 350;
        Coins amount = Coins.ofSatoshis(1_234_567);
        assertThat(createRoute(amount, feeRate1, feeRate2, feeRate3).getFeeRate())
                .isEqualTo(feeRate2 + feeRate3);
    }

    @Test
    void feeRateWithFirstHop_three_hops() {
        int feeRate1 = 50;
        int feeRate2 = 100;
        int feeRate3 = 350;
        Coins amount = Coins.ofSatoshis(1_234_567);
        assertThat(createRoute(amount, feeRate1, feeRate2, feeRate3).getFeeRateWithFirstHop())
                .isEqualTo(feeRate1 + feeRate2 + feeRate3);
    }

    @Test
    void zero_amount() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Route(List.of(), List.of(), Coins.NONE, List.of()));
    }

    @Test
    void negative_amount() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Route(List.of(), List.of(), Coins.ofSatoshis(-1), List.of()));
    }

    @Test
    void getForAmount() {
        Coins newAmount = Coins.ofSatoshis(1_000);
        assertThat(ROUTE.getForAmount(newAmount))
                .isEqualTo(new Route(new BasicRoute(ROUTE.edges(), newAmount), ROUTE.edgesWithLiquidityInformation()));
    }

    @Test
    void getForAmount_retains_liquidity_information() {
        BasicRoute basicRoute = new BasicRoute(ROUTE.edges(), ROUTE.amount());
        Route original = new Route(basicRoute, List.of(
                EdgeWithLiquidityInformation.forUpperBound(EDGE, Coins.ofSatoshis(123)),
                EdgeWithLiquidityInformation.forUpperBound(EDGE_2_3, EDGE_2_3.capacity()),
                EdgeWithLiquidityInformation.forUpperBound(EDGE_3_4, EDGE_3_4.capacity())
        ));
        Coins newAmount = Coins.ofSatoshis(1_000);
        List<Coins> updatedFeesForHops = List.of(Coins.ofMilliSatoshis(200), Coins.ofMilliSatoshis(200), Coins.NONE);
        Route expectedRoute =
                new Route(original.edges(), original.edgesWithLiquidityInformation(), newAmount, updatedFeesForHops);
        assertThat(original.getForAmount(newAmount)).isEqualTo(expectedRoute);
    }

    @Test
    void liquidityInformation_no_default() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Route(new BasicRoute(List.of(EDGE), Coins.ofSatoshis(1)), List.of()));
    }

    private Route routeForAmountAndCapacityAndKnownLiquidity(int amountSat, int capacitySat, int knownLiquiditySat) {
        Coins capacity = Coins.ofSatoshis(capacitySat);
        Coins amount = Coins.ofSatoshis(amountSat);
        Edge edge = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, capacity, POLICY_1);
        BasicRoute basicRoute = new BasicRoute(List.of(edge), amount);
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                EdgeWithLiquidityInformation.forKnownLiquidity(edge, Coins.ofSatoshis(knownLiquiditySat));
        return new Route(basicRoute, List.of(edgeWithLiquidityInformation));
    }

    private Route createRoute(Coins amount, int... feeRates) {
        List<Edge> edges = Arrays.stream(feeRates)
                .mapToObj(ppm -> new Policy(ppm, Coins.NONE, true, TIME_LOCK_DELTA))
                .map(policy -> new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy))
                .toList();
        return new Route(new BasicRoute(edges, amount));
    }

    private List<Edge> edgesWithTimeLockDeltas(int... timeLockDeltas) {
        return Arrays.stream(timeLockDeltas)
                .mapToObj(timeLockDelta -> new Policy(0, Coins.NONE, true, timeLockDelta))
                .map(policy -> new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy))
                .toList();
    }
}
