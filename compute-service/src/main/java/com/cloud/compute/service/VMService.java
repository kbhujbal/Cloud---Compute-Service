package com.cloud.compute.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.cloud.compute.model.VM;

public interface VMService {
    CompletableFuture<VM> createVM(VM vm);
    CompletableFuture<VM> startVM(String vmId);
    CompletableFuture<VM> stopVM(String vmId);
    CompletableFuture<VM> terminateVM(String vmId);
    CompletableFuture<VM> modifyVM(String vmId, VM.VMState newState);
    CompletableFuture<VM> updateResources(String vmId, VM.ResourceSpec newResources);
    CompletableFuture<VM> updateNetworkConfig(String vmId, VM.NetworkConfig newConfig);
    
    Optional<VM> getVM(String vmId);
    List<VM> getVMsByUserId(String userId);
    List<VM> getVMsByState(VM.VMState state);
    List<VM> getVMsByRegion(String region);
    List<VM> getVMsByAvailabilityZone(String availabilityZone);
    
    boolean existsVM(String vmId);
    boolean isVMAvailable(String vmId);
    void validateVMResources(VM.ResourceSpec resources);
} 