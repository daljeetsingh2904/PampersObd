package com.pampers.PampersObd.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.pampers.PampersObd.model.ObdChannel;

@Repository
public interface ObdChannelRepository extends JpaRepository<ObdChannel, Integer> {

    @Query("SELECT o FROM ObdChannel o WHERE o.obdChannelStatus = :obdChannelStatus AND o.obdChannelContext = :obdChannelContext AND o.status = 1")
    List<ObdChannel> findByObdChannelContextAndStatus(@Param("obdChannelStatus") String obdChannelStatus, @Param("obdChannelContext") String obdChannelContext);

}
