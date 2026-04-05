package com.tracksure_be.service.impl;

import com.tracksure_be.dto.LocationLogResponse;
import com.tracksure_be.mapper.LocationLogMapper;
import com.tracksure_be.repository.LocationLogRepository;
import com.tracksure_be.service.LocationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationLogServiceImpl implements LocationLogService {

	private final LocationLogRepository locationLogRepository;
	private final LocationLogMapper locationLogMapper;

	@Override
	@Transactional(readOnly = true)
	public List<LocationLogResponse> getAll() {
		return locationLogRepository.findAll().stream()
				.map(locationLogMapper::toResponse)
				.toList();
	}
}

