package com.pampers.PampersObd.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.pampers.PampersObd.model.ServiceParam;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


@Repository
public interface ServiceConfigRepository extends JpaRepository<ServiceParam,Integer> {
	
	@Transactional(readOnly = true)
	@Query("select s from ServiceParam s where s.ServiceName=:ServiceName and s.status=:status")
	ServiceParam findByServiceNameAndStatus(@Param("ServiceName") String ServiceName,@Param("status") int status);

}
