package com.cloud.compute.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.cloud.compute.model.VM;

@Repository
public interface VMRepository extends MongoRepository<VM, String> {
    List<VM> findByUserId(String userId);
    List<VM> findByState(VM.VMState state);
    Optional<VM> findByName(String name);
    List<VM> findByRegionAndAvailabilityZone(String region, String availabilityZone);
    List<VM> findByUserIdAndState(String userId, VM.VMState state);
    boolean existsByName(String name);
} 