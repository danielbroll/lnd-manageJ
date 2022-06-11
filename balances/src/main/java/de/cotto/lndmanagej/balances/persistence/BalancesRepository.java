package de.cotto.lndmanagej.balances.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface BalancesRepository extends JpaRepository<BalancesJpaDto, String> {
    Optional<BalancesJpaDto> findTopByChannelIdOrderByTimestampDesc(long channelId);

    Optional<BalancesJpaDto> findTopByChannelIdAndTimestampAfterOrderByLocalBalance(long channelId, long timestamp);

    Optional<BalancesJpaDto> findTopByChannelIdAndTimestampAfterOrderByLocalBalanceDesc(long channelId, long timestamp);

    Stream<BalancesJpaDto> findByChannelIdOrderByTimestampDesc(long channelId);
}
