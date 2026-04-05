package com.tracksure_be.service;

import com.tracksure_be.dto.LocationBatchUploadRequest;
import com.tracksure_be.dto.LocationBatchUploadResponse;
import com.tracksure_be.dto.LocationPointDto;
import com.tracksure_be.entity.Device;
import com.tracksure_be.entity.LocationLog;
import com.tracksure_be.entity.User;
import com.tracksure_be.repository.DeviceLinkRepository;
import com.tracksure_be.enums.LocationSource;
import com.tracksure_be.repository.DeviceRepository;
import com.tracksure_be.repository.LocationLogRepository;
import com.tracksure_be.service.impl.LocationBatchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LocationBatchServiceImplTest {
	private static final Long AUTH_USER_ID = 10L;

	@Mock
	private LocationLogRepository locationLogRepository;

	@Mock
	private DeviceRepository deviceRepository;

	@Mock
	private DeviceLinkRepository deviceLinkRepository;

	@InjectMocks
	private LocationBatchServiceImpl service;

	private Device subjectDevice;
	private Device uploaderDevice;

	@BeforeEach
	void setUp() {
		User uploaderOwner = new User();
		uploaderOwner.setUserId(AUTH_USER_ID);

		subjectDevice = new Device();
		subjectDevice.setDeviceId(1L);
		subjectDevice.setOwnerUser(uploaderOwner);

		uploaderDevice = new Device();
		uploaderDevice.setDeviceId(2L);
		uploaderDevice.setOwnerUser(uploaderOwner);

		when(deviceRepository.findById(1L)).thenReturn(Optional.of(subjectDevice));
		when(deviceRepository.findAllByOwnerUser_UserId(AUTH_USER_ID)).thenReturn(List.of(uploaderDevice));
	}

	@Test
	void uploadBatch_insertsNewPoints() {
		when(locationLogRepository.findClientPointIdsBySubjectAndUploader(1L, 2L))
				.thenReturn(Set.of());
		when(locationLogRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

		LocationBatchUploadRequest request = buildRequest(
				List.of(point("cp-1", 51.5, -0.1), point("cp-2", 51.6, -0.2)));

		LocationBatchUploadResponse response = service.uploadBatch(request, AUTH_USER_ID);

		assertThat(response.getTotalReceived()).isEqualTo(2);
		assertThat(response.getInserted()).isEqualTo(2);
		assertThat(response.getDuplicates()).isEqualTo(0);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<LocationLog>> captor = ArgumentCaptor.forClass(List.class);
		verify(locationLogRepository).saveAll(captor.capture());
		List<LocationLog> saved = captor.getValue();
		assertThat(saved).hasSize(2);
		assertThat(saved.get(0).getClientPointId()).isEqualTo("cp-1");
		assertThat(saved.get(1).getClientPointId()).isEqualTo("cp-2");
	}

	@Test
	void uploadBatch_skipsDuplicatesAlreadyInDatabase() {
		when(locationLogRepository.findClientPointIdsBySubjectAndUploader(1L, 2L))
				.thenReturn(Set.of("cp-1"));
		when(locationLogRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

		LocationBatchUploadRequest request = buildRequest(
				List.of(point("cp-1", 51.5, -0.1), point("cp-2", 51.6, -0.2)));

		LocationBatchUploadResponse response = service.uploadBatch(request, AUTH_USER_ID);

		assertThat(response.getTotalReceived()).isEqualTo(2);
		assertThat(response.getInserted()).isEqualTo(1);
		assertThat(response.getDuplicates()).isEqualTo(1);
	}

	@Test
	void uploadBatch_skipsIntraBatchDuplicates() {
		when(locationLogRepository.findClientPointIdsBySubjectAndUploader(1L, 2L))
				.thenReturn(Set.of());
		when(locationLogRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

		LocationBatchUploadRequest request = buildRequest(
				List.of(point("cp-1", 51.5, -0.1), point("cp-1", 51.5, -0.1)));

		LocationBatchUploadResponse response = service.uploadBatch(request, AUTH_USER_ID);

		assertThat(response.getTotalReceived()).isEqualTo(2);
		assertThat(response.getInserted()).isEqualTo(1);
		assertThat(response.getDuplicates()).isEqualTo(1);
	}

	@Test
	void uploadBatch_allDuplicates_savesEmptyList() {
		when(locationLogRepository.findClientPointIdsBySubjectAndUploader(1L, 2L))
				.thenReturn(Set.of("cp-1", "cp-2"));
		when(locationLogRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

		LocationBatchUploadRequest request = buildRequest(
				List.of(point("cp-1", 51.5, -0.1), point("cp-2", 51.6, -0.2)));

		LocationBatchUploadResponse response = service.uploadBatch(request, AUTH_USER_ID);

		assertThat(response.getInserted()).isEqualTo(0);
		assertThat(response.getDuplicates()).isEqualTo(2);
	}

	@Test
	void uploadBatch_throwsWhenSubjectDeviceNotFound() {
		when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

		LocationBatchUploadRequest request = new LocationBatchUploadRequest(
				"99L", 2L, List.of(point("cp-1", 51.5, -0.1)));

		assertThatThrownBy(() -> service.uploadBatch(request, AUTH_USER_ID))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Subject device not found");

		verify(locationLogRepository, never()).saveAll(any());
	}

	@Test
	void uploadBatch_throwsWhenAuthenticatedUserHasNoDevice() {
		when(deviceRepository.findAllByOwnerUser_UserId(AUTH_USER_ID)).thenReturn(List.of());

		LocationBatchUploadRequest request = new LocationBatchUploadRequest(
				"1L", 2L, List.of(point("cp-1", 51.5, -0.1)));

		assertThatThrownBy(() -> service.uploadBatch(request, AUTH_USER_ID))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("No device linked to authenticated user");

		verify(locationLogRepository, never()).saveAll(any());
	}

	@Test
	void uploadBatch_setsGeometryCorrectly() {
		when(locationLogRepository.findClientPointIdsBySubjectAndUploader(anyLong(), anyLong()))
				.thenReturn(Set.of());
		when(locationLogRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

		LocationBatchUploadRequest request = buildRequest(
				List.of(point("cp-1", 48.8566, 2.3522)));  // Paris

		service.uploadBatch(request, AUTH_USER_ID);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<LocationLog>> captor = ArgumentCaptor.forClass(List.class);
		verify(locationLogRepository).saveAll(captor.capture());

		LocationLog log = captor.getValue().get(0);
		assertThat(log.getLocation()).isNotNull();
		assertThat(log.getLocation().getSRID()).isEqualTo(4326);
		// JTS Point: X = lon, Y = lat
		assertThat(log.getLocation().getX()).isEqualTo(2.3522);
		assertThat(log.getLocation().getY()).isEqualTo(48.8566);
	}

	@Test
	void uploadBatch_throwsWhenSubjectDeviceIdIsSentinel() {
		LocationBatchUploadRequest request = new LocationBatchUploadRequest(
				"9_007_199_254_740_991L", 2L, List.of(point("cp-1", 51.5, -0.1)));

		assertThatThrownBy(() -> service.uploadBatch(request, AUTH_USER_ID))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Invalid subjectDeviceId sentinel value");

		verify(locationLogRepository, never()).saveAll(any());
	}

	@Test
	void uploadBatch_throwsWhenClientSpoofsUploaderDeviceId() {
		LocationBatchUploadRequest request = new LocationBatchUploadRequest(
				"1L", 999L, List.of(point("cp-1", 51.5, -0.1)));

		assertThatThrownBy(() -> service.uploadBatch(request, AUTH_USER_ID))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("uploaderDeviceId spoof detected");

		verify(locationLogRepository, never()).saveAll(any());
	}

	@Test
	void uploadBatch_allowsLinkedSubjectDevice() {
		User anotherOwner = new User();
		anotherOwner.setUserId(999L);
		subjectDevice.setOwnerUser(anotherOwner);

		when(deviceLinkRepository.existsByFollowerUser_UserIdAndTargetDevice_DeviceId(AUTH_USER_ID, 1L))
				.thenReturn(true);
		when(locationLogRepository.findClientPointIdsBySubjectAndUploader(1L, 2L)).thenReturn(Set.of());
		when(locationLogRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

		LocationBatchUploadResponse response = service.uploadBatch(buildRequest(List.of(point("cp-1", 1.0, 1.0))), AUTH_USER_ID);
		assertThat(response.getInserted()).isEqualTo(1);
	}

	@Test
	void uploadBatch_rejectsUnlinkedSubjectDevice() {
		User anotherOwner = new User();
		anotherOwner.setUserId(999L);
		subjectDevice.setOwnerUser(anotherOwner);

		when(deviceLinkRepository.existsByFollowerUser_UserIdAndTargetDevice_DeviceId(AUTH_USER_ID, 1L))
				.thenReturn(false);

		assertThatThrownBy(() -> service.uploadBatch(buildRequest(List.of(point("cp-1", 1.0, 1.0))), AUTH_USER_ID))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("not linked to authenticated user");

		verify(locationLogRepository, never()).saveAll(any());

	}

	// ── helpers ──────────────────────────────────────────────────────────────

	private LocationBatchUploadRequest buildRequest(List<LocationPointDto> points) {
		return new LocationBatchUploadRequest("1L", 2L, points);
	}

	private LocationPointDto point(String clientPointId, double lat, double lon) {
		return new LocationPointDto(
				clientPointId,
				lat,
				lon,
				null,
				Instant.now(),
				LocationSource.GPS
		);
	}
}
