package com.tracksure_be.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tracksure_be.dto.LocationBatchUploadRequest;
import com.tracksure_be.dto.LocationBatchUploadResponse;
import com.tracksure_be.dto.LocationPointDto;
import com.tracksure_be.enums.LocationSource;
import com.tracksure_be.service.LocationBatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationBatchController.class)
@AutoConfigureMockMvc(addFilters = false)
class LocationBatchControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private LocationBatchService locationBatchService;

	private final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	@Test
	void uploadBatch_returns200WithSummary() throws Exception {
		when(locationBatchService.uploadBatch(any()))
				.thenReturn(new LocationBatchUploadResponse(2, 0, 2));

		mockMvc.perform(post("/v1/locations:batch")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequest())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.inserted").value(2))
				.andExpect(jsonPath("$.duplicates").value(0))
				.andExpect(jsonPath("$.totalReceived").value(2));

		verify(locationBatchService).uploadBatch(any());
	}

	@Test
	void uploadBatch_returns400WhenPointsListIsEmpty() throws Exception {
		LocationBatchUploadRequest request =
				new LocationBatchUploadRequest(1L, 2L, List.of());

		mockMvc.perform(post("/v1/locations:batch")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void uploadBatch_returns400WhenSubjectDeviceIdIsMissing() throws Exception {
		LocationBatchUploadRequest request =
				new LocationBatchUploadRequest(null, 2L, List.of(validPoint("cp-1")));

		mockMvc.perform(post("/v1/locations:batch")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void uploadBatch_returns400WhenLatOutOfRange() throws Exception {
		LocationPointDto badPoint = new LocationPointDto("cp-1", 200.0, 10.0,
				null, Instant.now(), LocationSource.GPS);
		LocationBatchUploadRequest request =
				new LocationBatchUploadRequest(1L, 2L, List.of(badPoint));

		mockMvc.perform(post("/v1/locations:batch")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void uploadBatch_returns400WhenClientPointIdIsBlank() throws Exception {
		LocationPointDto badPoint = new LocationPointDto("", 51.5, -0.1,
				null, Instant.now(), LocationSource.GPS);
		LocationBatchUploadRequest request =
				new LocationBatchUploadRequest(1L, 2L, List.of(badPoint));

		mockMvc.perform(post("/v1/locations:batch")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void uploadBatch_withDuplicates_returnsSummary() throws Exception {
		when(locationBatchService.uploadBatch(any()))
				.thenReturn(new LocationBatchUploadResponse(1, 1, 2));

		mockMvc.perform(post("/v1/locations:batch")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(validRequest())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.inserted").value(1))
				.andExpect(jsonPath("$.duplicates").value(1))
				.andExpect(jsonPath("$.totalReceived").value(2));
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	private LocationBatchUploadRequest validRequest() {
		return new LocationBatchUploadRequest(1L, 2L,
				List.of(validPoint("cp-1"), validPoint("cp-2")));
	}

	private LocationPointDto validPoint(String clientPointId) {
		return new LocationPointDto(
				clientPointId,
				51.5074,
				-0.1278,
				5.0,
				Instant.now(),
				LocationSource.GPS
		);
	}
}
