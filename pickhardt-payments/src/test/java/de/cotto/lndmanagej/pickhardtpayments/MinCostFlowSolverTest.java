package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.pickhardtpayments.model.Flow;
import de.cotto.lndmanagej.pickhardtpayments.model.Flows;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE_1_3;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE_2_3;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE_3_2;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE_3_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class MinCostFlowSolverTest {

    private static final long QUANTIZATION = 1;
    private static final int PIECEWISE_LINEAR_APPROXIMATIONS = 1;
    private static final Coins ONE_SAT = Coins.ofSatoshis(PIECEWISE_LINEAR_APPROXIMATIONS);
    private static final Coins TWO_SATS = ONE_SAT.add(ONE_SAT);
    private static final Coins MANY_SATS = Coins.ofSatoshis(100_000_000);

    private static final Flow FLOW_1_2 = new Flow(EDGE, ONE_SAT);
    private static final Flow FLOW_1_3 = new Flow(EDGE_1_3, ONE_SAT);
    private static final Flow FLOW_2_3 = new Flow(EDGE_2_3, ONE_SAT);
    private static final Flow FLOW_3_4 = new Flow(EDGE_3_4, ONE_SAT);

    @Test
    void no_edge() {
        Set<EdgeWithLiquidityInformation> edgesWithCapacityInformation = Set.of();
        Map<Pubkey, Coins> sources = Map.of(PUBKEY, ONE_SAT);
        Map<Pubkey, Coins> sinks = Map.of(PUBKEY_2, ONE_SAT);
        assertThatCode(
                () -> new MinCostFlowSolver(
                        edgesWithCapacityInformation,
                        sources,
                        sinks,
                        QUANTIZATION,
                        PIECEWISE_LINEAR_APPROXIMATIONS
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void no_sink_and_no_source() {
        Set<EdgeWithLiquidityInformation> edgesWithCapacityInformation =
                Set.of(EdgeWithLiquidityInformation.forUpperBound(EDGE, ONE_SAT));
        Map<Pubkey, Coins> sources = Map.of();
        Map<Pubkey, Coins> sinks = Map.of();
        assertThatCode(
                () -> new MinCostFlowSolver(
                        edgesWithCapacityInformation,
                        sources,
                        sinks,
                        QUANTIZATION,
                        PIECEWISE_LINEAR_APPROXIMATIONS
                )
        ).doesNotThrowAnyException();
    }

    @Test
    void no_source() {
        Set<EdgeWithLiquidityInformation> edgesWithCapacityInformation =
                Set.of(EdgeWithLiquidityInformation.forUpperBound(EDGE_2_3, ONE_SAT));
        Map<Pubkey, Coins> sources = Map.of();
        Map<Pubkey, Coins> sinks = Map.of(PUBKEY_3, ONE_SAT);
        assertThatIllegalArgumentException().isThrownBy(
                () -> new MinCostFlowSolver(
                        edgesWithCapacityInformation,
                        sources,
                        sinks,
                        QUANTIZATION,
                        PIECEWISE_LINEAR_APPROXIMATIONS
                )
        );
    }

    @Test
    void no_sink() {
        Set<EdgeWithLiquidityInformation> edgesWithCapacityInformation =
                Set.of(EdgeWithLiquidityInformation.forUpperBound(EDGE_2_3, ONE_SAT));
        Map<Pubkey, Coins> sources = Map.of(PUBKEY_2, ONE_SAT);
        Map<Pubkey, Coins> sinks = Map.of();
        assertThatIllegalArgumentException().isThrownBy(
                () -> new MinCostFlowSolver(
                        edgesWithCapacityInformation,
                        sources,
                        sinks,
                        QUANTIZATION,
                        PIECEWISE_LINEAR_APPROXIMATIONS
                )
        );
    }

    @Test
    void sink_amount_does_not_match_source_amount() {
        Set<EdgeWithLiquidityInformation> edgesWithCapacityInformation =
                Set.of(EdgeWithLiquidityInformation.forUpperBound(EDGE, ONE_SAT));
        Map<Pubkey, Coins> sources = Map.of(PUBKEY, ONE_SAT);
        Map<Pubkey, Coins> sinks = Map.of(PUBKEY_2, TWO_SATS);
        assertThatIllegalArgumentException().isThrownBy(
                () -> new MinCostFlowSolver(
                        edgesWithCapacityInformation,
                        sources,
                        sinks,
                        QUANTIZATION,
                        PIECEWISE_LINEAR_APPROXIMATIONS
                )
        );
    }

    @Test
    void solve_simple() {
        Coins amount = Coins.ofSatoshis(PIECEWISE_LINEAR_APPROXIMATIONS);
        List<EdgeWithLiquidityInformation> edgesWithCapacityInformation =
                List.of(EdgeWithLiquidityInformation.forUpperBound(EDGE_2_3, amount));

        Flows flows = solve(edgesWithCapacityInformation, PUBKEY_2, PUBKEY_3, amount);

        assertThat(flows).isEqualTo(new Flows(FLOW_2_3));
    }

    @Test
    void solve_no_solution_due_to_gap() {
        Coins amount = Coins.ofSatoshis(PIECEWISE_LINEAR_APPROXIMATIONS);
        List<EdgeWithLiquidityInformation> edgesWithCapacityInformation = List.of(
                EdgeWithLiquidityInformation.forUpperBound(EDGE, amount),
                EdgeWithLiquidityInformation.forUpperBound(EDGE_3_4, amount)
        );

        Flows flows = solve(edgesWithCapacityInformation, PUBKEY, PUBKEY_4, amount);

        assertThat(flows).isEqualTo(new Flows());
    }

    @Test
    void solve_with_quantization() {
        int quantization = 10_000;
        Coins amount = Coins.ofSatoshis(100_000);
        Set<EdgeWithLiquidityInformation> edgesWithCapacityInformation =
                Set.of(EdgeWithLiquidityInformation.forUpperBound(EDGE_2_3, amount));

        Map<Pubkey, Coins> sources = Map.of(PUBKEY_2, amount);
        Map<Pubkey, Coins> sinks = Map.of(PUBKEY_3, amount);
        Flows flows = new MinCostFlowSolver(
                edgesWithCapacityInformation,
                sources,
                sinks,
                quantization,
                PIECEWISE_LINEAR_APPROXIMATIONS
        ).solve();

        assertThat(flows).isEqualTo(new Flows(new Flow(EDGE_2_3, amount)));
    }

    @Test
    void solve_with_quantization_but_requested_amount_not_divisible() {
        int quantization = 10_000;
        Coins amount = Coins.ofSatoshis(123_456);
        Set<EdgeWithLiquidityInformation> edgesWithCapacityInformation =
                Set.of(EdgeWithLiquidityInformation.forUpperBound(EDGE_3_4, MANY_SATS));

        Map<Pubkey, Coins> sources = Map.of(PUBKEY_3, amount);
        Map<Pubkey, Coins> sinks = Map.of(PUBKEY_4, amount);
        Flows flows = new MinCostFlowSolver(
                edgesWithCapacityInformation,
                sources,
                sinks,
                quantization,
                PIECEWISE_LINEAR_APPROXIMATIONS
        ).solve();

        assertThat(flows).isEqualTo(new Flows(new Flow(EDGE_3_4, Coins.ofSatoshis(120_000))));
    }

    @Test
    void quantization_larger_than_smallest_channel() {
        int quantization = 10_000;
        Set<EdgeWithLiquidityInformation> edgesWithCapacityInformation =
                Set.of(EdgeWithLiquidityInformation.forUpperBound(EDGE_2_3, ONE_SAT));

        Map<Pubkey, Coins> sources = Map.of(PUBKEY_2, ONE_SAT);
        Map<Pubkey, Coins> sinks = Map.of(PUBKEY_3, ONE_SAT);
        Flows flows = new MinCostFlowSolver(
                edgesWithCapacityInformation,
                sources,
                sinks,
                quantization,
                PIECEWISE_LINEAR_APPROXIMATIONS
        ).solve();

        assertThat(flows).isEqualTo(new Flows());
    }

    @Test
    void solve_two_sources_two_sinks() {
        Coins amount = Coins.ofSatoshis(PIECEWISE_LINEAR_APPROXIMATIONS);
        List<EdgeWithLiquidityInformation> edgesWithCapacityInformation = List.of(
                EdgeWithLiquidityInformation.forUpperBound(EDGE, amount),
                EdgeWithLiquidityInformation.forUpperBound(EDGE_3_4, amount)
        );
        Map<Pubkey, Coins> sources = Map.of(PUBKEY, amount, PUBKEY_3, amount);
        Map<Pubkey, Coins> sinks = Map.of(PUBKEY_2, amount, PUBKEY_4, amount);
        MinCostFlowSolver minCostFlowSolver = new MinCostFlowSolver(
                        edgesWithCapacityInformation,
                        sources,
                        sinks,
                        QUANTIZATION,
                        PIECEWISE_LINEAR_APPROXIMATIONS
                );

        assertThat(minCostFlowSolver.solve()).isEqualTo(new Flows(FLOW_1_2, FLOW_3_4));
    }

    @Test
    void solve_one_source_two_sinks() {
        List<EdgeWithLiquidityInformation> edgesWithCapacityInformation = List.of(
                EdgeWithLiquidityInformation.forUpperBound(EDGE, ONE_SAT),
                EdgeWithLiquidityInformation.forUpperBound(EDGE_1_3, ONE_SAT)
        );
        Map<Pubkey, Coins> sources = Map.of(PUBKEY, TWO_SATS);
        Map<Pubkey, Coins> sinks = Map.of(PUBKEY_2, ONE_SAT, PUBKEY_3, ONE_SAT);
        MinCostFlowSolver minCostFlowSolver = new MinCostFlowSolver(
                edgesWithCapacityInformation,
                sources,
                sinks,
                QUANTIZATION,
                PIECEWISE_LINEAR_APPROXIMATIONS
        );

        assertThat(minCostFlowSolver.solve()).isEqualTo(new Flows(FLOW_1_2, FLOW_1_3));
    }

    @Test
    void solve_long_path() {
        List<EdgeWithLiquidityInformation> edgesWithCapacityInformation = List.of(
                EdgeWithLiquidityInformation.forUpperBound(EDGE, ONE_SAT),
                EdgeWithLiquidityInformation.forUpperBound(EDGE_2_3, MANY_SATS),
                EdgeWithLiquidityInformation.forUpperBound(EDGE_3_4, ONE_SAT)
        );
        Flows flows = solve(edgesWithCapacityInformation, PUBKEY, PUBKEY_4, ONE_SAT);

        assertThat(flows).isEqualTo(new Flows(FLOW_1_2, FLOW_2_3, FLOW_3_4));
    }

    @Test
    void solve_long_path_with_cycle() {
        List<EdgeWithLiquidityInformation> edgesWithCapacityInformation = List.of(
                EdgeWithLiquidityInformation.forUpperBound(EDGE, TWO_SATS),
                EdgeWithLiquidityInformation.forUpperBound(EDGE_2_3, MANY_SATS),
                EdgeWithLiquidityInformation.forUpperBound(EDGE_3_2, MANY_SATS),
                EdgeWithLiquidityInformation.forUpperBound(EDGE_3_4, TWO_SATS)
        );
        Flows flows = solve(edgesWithCapacityInformation, PUBKEY, PUBKEY_4, ONE_SAT);

        assertThat(flows).isEqualTo(new Flows(FLOW_1_2, FLOW_2_3, FLOW_3_4));
    }

    private Flows solve(
            List<EdgeWithLiquidityInformation> edgesWithCapacityInformation,
            Pubkey source,
            Pubkey target,
            Coins amount
    ) {
        Map<Pubkey, Coins> sources = Map.of(source, amount);
        Map<Pubkey, Coins> sinks = Map.of(target, amount);
        return new MinCostFlowSolver(
                edgesWithCapacityInformation,
                sources,
                sinks,
                QUANTIZATION,
                PIECEWISE_LINEAR_APPROXIMATIONS
        ).solve();
    }
}
