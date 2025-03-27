package com.cloud.compute.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud.compute.exception.VMException;
import com.cloud.compute.model.VM;
import com.cloud.compute.repository.VMRepository;
import com.cloud.compute.service.VMService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VMServiceImpl implements VMService {
    private final VMRepository vmRepository;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    @Transactional
    @CircuitBreaker(name = "vmService")
    @RateLimiter(name = "vmService")
    public CompletableFuture<VM> createVM(VM vm) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                validateVMResources(vm.getResources());
                if (vmRepository.existsByName(vm.getName())) {
                    throw new VMException("VM with name " + vm.getName() + " already exists");
                }
                
                vm.setCreatedAt(LocalDateTime.now());
                vm.setUpdatedAt(LocalDateTime.now());
                vm.setState(VM.VMState.PENDING);
                
                VM savedVM = vmRepository.save(vm);
                log.info("Created VM with ID: {}", savedVM.getId());
                return savedVM;
            } catch (Exception e) {
                log.error("Error creating VM: {}", e.getMessage());
                throw new VMException("Failed to create VM: " + e.getMessage());
            }
        }, executorService);
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "vmService")
    @RateLimiter(name = "vmService")
    public CompletableFuture<VM> startVM(String vmId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                VM vm = getVM(vmId).orElseThrow(() -> new VMException("VM not found: " + vmId));
                if (vm.getState() != VM.VMState.STOPPED) {
                    throw new VMException("VM is not in STOPPED state");
                }
                
                vm.setState(VM.VMState.RUNNING);
                vm.setUpdatedAt(LocalDateTime.now());
                
                VM updatedVM = vmRepository.save(vm);
                log.info("Started VM with ID: {}", vmId);
                return updatedVM;
            } catch (Exception e) {
                log.error("Error starting VM: {}", e.getMessage());
                throw new VMException("Failed to start VM: " + e.getMessage());
            }
        }, executorService);
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "vmService")
    @RateLimiter(name = "vmService")
    public CompletableFuture<VM> stopVM(String vmId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                VM vm = getVM(vmId).orElseThrow(() -> new VMException("VM not found: " + vmId));
                if (vm.getState() != VM.VMState.RUNNING) {
                    throw new VMException("VM is not in RUNNING state");
                }
                
                vm.setState(VM.VMState.STOPPED);
                vm.setUpdatedAt(LocalDateTime.now());
                
                VM updatedVM = vmRepository.save(vm);
                log.info("Stopped VM with ID: {}", vmId);
                return updatedVM;
            } catch (Exception e) {
                log.error("Error stopping VM: {}", e.getMessage());
                throw new VMException("Failed to stop VM: " + e.getMessage());
            }
        }, executorService);
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "vmService")
    @RateLimiter(name = "vmService")
    public CompletableFuture<VM> terminateVM(String vmId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                VM vm = getVM(vmId).orElseThrow(() -> new VMException("VM not found: " + vmId));
                if (vm.getState() == VM.VMState.TERMINATED) {
                    throw new VMException("VM is already terminated");
                }
                
                vm.setState(VM.VMState.TERMINATED);
                vm.setUpdatedAt(LocalDateTime.now());
                
                VM updatedVM = vmRepository.save(vm);
                log.info("Terminated VM with ID: {}", vmId);
                return updatedVM;
            } catch (Exception e) {
                log.error("Error terminating VM: {}", e.getMessage());
                throw new VMException("Failed to terminate VM: " + e.getMessage());
            }
        }, executorService);
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "vmService")
    @RateLimiter(name = "vmService")
    public CompletableFuture<VM> modifyVM(String vmId, VM.VMState newState) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                VM vm = getVM(vmId).orElseThrow(() -> new VMException("VM not found: " + vmId));
                vm.setState(newState);
                vm.setUpdatedAt(LocalDateTime.now());
                
                VM updatedVM = vmRepository.save(vm);
                log.info("Modified VM state to {} for VM ID: {}", newState, vmId);
                return updatedVM;
            } catch (Exception e) {
                log.error("Error modifying VM state: {}", e.getMessage());
                throw new VMException("Failed to modify VM state: " + e.getMessage());
            }
        }, executorService);
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "vmService")
    @RateLimiter(name = "vmService")
    public CompletableFuture<VM> updateResources(String vmId, VM.ResourceSpec newResources) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                validateVMResources(newResources);
                VM vm = getVM(vmId).orElseThrow(() -> new VMException("VM not found: " + vmId));
                if (vm.getState() != VM.VMState.STOPPED) {
                    throw new VMException("VM must be stopped to update resources");
                }
                
                vm.setResources(newResources);
                vm.setUpdatedAt(LocalDateTime.now());
                
                VM updatedVM = vmRepository.save(vm);
                log.info("Updated resources for VM ID: {}", vmId);
                return updatedVM;
            } catch (Exception e) {
                log.error("Error updating VM resources: {}", e.getMessage());
                throw new VMException("Failed to update VM resources: " + e.getMessage());
            }
        }, executorService);
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "vmService")
    @RateLimiter(name = "vmService")
    public CompletableFuture<VM> updateNetworkConfig(String vmId, VM.NetworkConfig newConfig) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                VM vm = getVM(vmId).orElseThrow(() -> new VMException("VM not found: " + vmId));
                if (vm.getState() != VM.VMState.STOPPED) {
                    throw new VMException("VM must be stopped to update network configuration");
                }
                
                vm.setNetworkConfig(newConfig);
                vm.setUpdatedAt(LocalDateTime.now());
                
                VM updatedVM = vmRepository.save(vm);
                log.info("Updated network configuration for VM ID: {}", vmId);
                return updatedVM;
            } catch (Exception e) {
                log.error("Error updating VM network config: {}", e.getMessage());
                throw new VMException("Failed to update VM network configuration: " + e.getMessage());
            }
        }, executorService);
    }

    @Override
    public Optional<VM> getVM(String vmId) {
        return vmRepository.findById(vmId);
    }

    @Override
    public List<VM> getVMsByUserId(String userId) {
        return vmRepository.findByUserId(userId);
    }

    @Override
    public List<VM> getVMsByState(VM.VMState state) {
        return vmRepository.findByState(state);
    }

    @Override
    public List<VM> getVMsByRegion(String region) {
        return vmRepository.findByRegionAndAvailabilityZone(region, null);
    }

    @Override
    public List<VM> getVMsByAvailabilityZone(String availabilityZone) {
        return vmRepository.findByRegionAndAvailabilityZone(null, availabilityZone);
    }

    @Override
    public boolean existsVM(String vmId) {
        return vmRepository.existsById(vmId);
    }

    @Override
    public boolean isVMAvailable(String vmId) {
        return getVM(vmId)
                .map(vm -> vm.getState() == VM.VMState.RUNNING)
                .orElse(false);
    }

    @Override
    public void validateVMResources(VM.ResourceSpec resources) {
        if (resources == null) {
            throw new VMException("Resource specification cannot be null");
        }
        if (resources.getCpuCores() <= 0) {
            throw new VMException("CPU cores must be greater than 0");
        }
        if (resources.getMemoryGB() <= 0) {
            throw new VMException("Memory must be greater than 0");
        }
        if (resources.getStorageGB() <= 0) {
            throw new VMException("Storage must be greater than 0");
        }
        if (resources.getNetworkBandwidthMbps() < 0) {
            throw new VMException("Network bandwidth cannot be negative");
        }
    }
} 