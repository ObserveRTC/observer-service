//package com.observertc.gatekeeper.repositories;
//
//import java.util.Arrays;
//import java.util.UUID;
//
//class ObserverRepositoryTest implements CrudRepositoryTest<UUID, ObserverDTO, CrudRepository<ObserverDTO, UUID>> {
//
//	private static final String OBSERVER_REPOSITORY_TEST_QUERIES_FILENAME = "ObserverTestQueries.sql";
//
//	private final ObserverRepository observerRepository;
//
//	public ObserverRepositoryTest() {
//		this.observerRepository = new ObserverRepository(
//				new MockedDSLContextProvider(OBSERVER_REPOSITORY_TEST_QUERIES_FILENAME)
//		);
//	}
//
//	@Override
//	public CrudRepository<ObserverDTO, UUID> getRepository() {
//		return this.observerRepository;
//	}
//
//	@Override
//	public Iterable<ObserverDTO> getExistingEntities() {
//		return Arrays.asList(
//				getExistingEntity(),
//				makeObserverDTO(UUID.fromString("e02f0812-48f5-44db-addd-8acfd7bbcfa9"), "name2", "desc2")
//		);
//	}
//
//	@Override
//	public Iterable<ObserverDTO> getNotExistingEntities() {
//		return Arrays.asList(
//				getNotExistingEntity(),
//				makeObserverDTO(UUID.fromString("f02f0812-48f5-44db-addd-8acfd7bbcfa9"), "name2", "desc2")
//		);
//	}
//
//	@Override
//	public ObserverDTO getNotExistingEntity() {
//		return makeObserverDTO(getNotExistingID(), "name1", "desc1");
//	}
//
//	@Override
//	public ObserverDTO getExistingEntity() {
//		return makeObserverDTO(getExistingId(), "name1", "desc1");
//	}
//
//	@Override
//	public UUID getExistingId() {
//		UUID result = UUID.fromString("e02f0812-48f5-44db-addd-8acfd7bbcfa0");
////		System.out.println(String.join(": ",
////				"getExistingId", result.toString(), Helpers.toHexString(UUIDAdapter.toBytes(result))));
//		return result;
//	}
//
//	@Override
//	public UUID getNotExistingID() {
//		UUID result = UUID.fromString("f02f0812-48f5-44db-addd-8acfd7bbcfa0");
////		System.out.println(String.join(": ",
////				"getNotExistingID", result.toString(), Helpers.toHexString(UUIDAdapter.toBytes(result))));
//		return result;
//	}
//
//	private ObserverDTO makeObserverDTO(UUID uuid, String name, String description) {
//		ObserverDTO result = new ObserverDTO();
//		result.uuid = uuid;
//		result.name = name;
//		result.description = description;
//		return result;
//	}
//}