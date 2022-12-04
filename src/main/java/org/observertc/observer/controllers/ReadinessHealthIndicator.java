package org.observertc.observer.controllers;

// Source: https://blog.mrhaki.com/2018/08/micronaut-mastery-add-custom-health.html
//@Singleton
//@Requires(beans = HealthEndpoint.class)
//public class ReadinessHealthIndicator implements HealthIndicator {
//
//    /**
//     * Name for health indicator.
//     */
//    private static final String NAME = "observer-readiness";
//
//
//    @Inject
//    ObserverService observerService;
//
//    /**
//     * Implementaton of {@link HealthIndicator#getResult()} where we
//     * check if the url is reachable and return result based
//     * on the HTTP status code.
//     *
//     * @return Contains {@link HealthResult} with status UP or DOWN.
//     */
//    @Override
//    public Publisher<HealthResult> getResult() {
//        HealthStatus healthStatus;
////        if (this.observerService.isReady()) {
////            healthStatus = HealthStatus.UP;
////        } else {
////            healthStatus = HealthStatus.DOWN;
////        }
//        healthStatus = HealthStatus.UP;
//        var result = HealthResult.builder(NAME, healthStatus).build();
//        return Publishers.just(result);
//    }
//}
