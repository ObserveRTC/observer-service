import argparse
from resource_builders import WebRTCStatResourceBuilder

WEBRTCSTAT_SERVICE = 'webrtcstat'

DATASOURCE_H2 = 'h2'
DATASOURCE_MYSQL = 'mysql'

SUBSCRIBER_ELASTICSEARCH = 'elasticsearch'
SUBSCRIBER_PROMETHEUS = 'prometheus'
SUBSCRIBER_NONE = 'none'

parser = argparse.ArgumentParser(description="Customize and Install ObserveRTC/gatekeeper project")
parser.add_argument("service",
                    help="The datasource the service uses",
                    choices=[WEBRTCSTAT_SERVICE],
                    type=str.lower)

parser.add_argument("datasource",
                    help="The datasource the service uses",
                    choices=[DATASOURCE_H2, DATASOURCE_MYSQL],
                    type=str.lower)

parser.add_argument("-s", "--subscriber",
                    help="The subscriber listening the service calculated metrics",
                    choices=[SUBSCRIBER_ELASTICSEARCH, SUBSCRIBER_PROMETHEUS, SUBSCRIBER_NONE],
                    default=SUBSCRIBER_NONE,
                    type=str.lower)

parser.add_argument("-bt", "--buildtarget",
                    help="A directory where the files are generated",
                    default=None)

resource_builders = {WEBRTCSTAT_SERVICE: WebRTCStatResourceBuilder()}


def main():
    args = parser.parse_args()
    service = args.service
    datasource = args.datasource
    subscriber = args.subscriber

    resource_builder = resource_builders.get(service)
    resource_builder \
        .withDatasource(datasource)

    if subscriber != SUBSCRIBER_NONE:
        resource_builder.withSubscriber(subscriber)

    if args.buildtarget is not None:
        resource_builder.withBuildDirectory(args.buildtarget)

    resource_builder.build()
    return 0


if __name__ == "__main__":
    main()
