from dbscript_builders import WebRTCStatDBScriptBuilder
from jsonschema_builders import WebRTCStatJsonSchemaBuilder
from resource_builders import WebRTCStatResourceBuilder, ResourceBuilder

SERVICES = ['webrtcstat']
DATASOURCES = ['mysql']
SUBSCRIBERS = [None, 'elasticsearch']

resource_builders = {SERVICES[0]: WebRTCStatResourceBuilder()}
jsonschema_builders = {SERVICES[0]: WebRTCStatJsonSchemaBuilder()}
dbscript_builders = {SERVICES[0]: WebRTCStatDBScriptBuilder()}


def main():
    # Lets generate it for all source we may possible have
    abstract_builder = ResourceBuilder()
    abstract_builder.clean()
    for service in SERVICES:
        jsonschema_builder = jsonschema_builders.get(service)
        jsonschema_builder.build()

        dbscript_builder = dbscript_builders.get(service)

        resource_builder = resource_builders.get(service)

        for datasource in DATASOURCES:
            if datasource is not None:
                resource_builder.withDatasource(datasource)
                dbscript_builder.withDatasource(datasource)

                dbscript_builder.build()

            for subscriber in SUBSCRIBERS:
                resource_builder.withSubscriber(subscriber)
                print("combination", service, datasource, subscriber)
                resource_builder.build()
    return 0


if __name__ == "__main__":
    main()
