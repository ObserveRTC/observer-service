import hiyapyco


class ResourceBuilder:
    def __init__(self):
        self._datasource = None
        pass

    def withDatasource(self, datasource):
        self._datasource = datasource
        return self

    def build(self):
        raise NotImplementedError("Subclasses should implement this!")
        return


class WebRTCStatResourceBuilder(ResourceBuilder):
    def __init__(self):
        ResourceBuilder.__init__(self)
        self._subscriber = None
        self._kafkastreamsStorage = None
        self._preRun = []
        self._postRun = []
        self._target_build_directory = 'build/'

    def withSubscriber(self, subscriber):
        self._subscriber = subscriber
        return self

    def withKafkastreamsStorage(self, kafkastreamsStorage):
        self._kafkastreamsStorage = kafkastreamsStorage
        return self

    def withBuildDirectory(self, target_build_directory):
        self._target_build_directory = target_build_directory

    def buildDockerComposeFile(self):
        basePath = 'docker_compose'
        yaml_extension = '.yaml'
        files = ['base' + yaml_extension]

        datasource_prefix = 'datasource'
        datasource_docker_compose_file = "_".join([datasource_prefix, self._datasource + yaml_extension])
        files.append(datasource_docker_compose_file)

        if self._subscriber is not None and self._subscriber != 'none':
            subscriber_prefix = 'subscriber'
            subscriber_docker_compose_file = "_".join([subscriber_prefix, self._subscriber + yaml_extension])
            files.append(subscriber_docker_compose_file)

        yaml_files = ["/".join([basePath, yaml_file]) for yaml_file in files]

        conf = hiyapyco.load(*yaml_files, method=hiyapyco.METHOD_MERGE, interpolate=True,
                             failonmissingfiles=True)
        hiyapyco.dump(conf, default_flow_style=True)

        target_file = self._target_build_directory + "docker-compose.yaml"
        text_file = open(target_file, "w")
        text_file.write(hiyapyco.dump(conf))
        text_file.close()

        return

    def buildResourceApplicationFile(self):
        basePath = 'resource_yaml'
        yaml_extension = '.yaml'
        prefix = 'webrtcstat'
        postfix = 'application'
        base_file = "_".join([prefix, 'base', postfix + yaml_extension])
        files = [base_file]

        datasource_prefix = 'datasource'
        datasource_docker_compose_file = "_".join(
            [prefix, datasource_prefix, self._datasource, postfix + yaml_extension])
        files.append(datasource_docker_compose_file)

        if self._subscriber is not None and self._subscriber != 'none':
            subscriber_prefix = 'subscriber'
            subscriber_docker_compose_file = "_".join(
                [prefix, subscriber_prefix, self._subscriber, postfix + yaml_extension])
            files.append(subscriber_docker_compose_file)

        yaml_files = ["/".join([basePath, yaml_file]) for yaml_file in files]

        conf = hiyapyco.load(*yaml_files, method=hiyapyco.METHOD_MERGE, interpolate=True,
                             failonmissingfiles=True)
        target_file = self._target_build_directory + "application.yaml"
        text_file = open(target_file, "w")
        text_file.write(hiyapyco.dump(conf, default_flow_style=False))
        text_file.close()
        return

    def buildUsedProfiles(self):
        texts = ["=".join(['datasource', self._datasource])]
        if self._subscriber is not None and self._subscriber != 'none':
            texts.append("=".join(['subscriber', self._subscriber]))

        target_file = self._target_build_directory + "used-profiles.properties"
        text_file = open(target_file, "w")
        text_file.write("\n".join(texts))
        text_file.close()

    def buildPostScript(self):
        basePath = 'postscripts'
        file_extension = '.sh'
        files = ['/webrtcstat.sh']
        file = "/".join(["datasource_" + self._datasource + file_extension])
        files.append(file)
        target = self._target_build_directory + "/postscript.sh"
        with open(target, 'w') as outfile:
            for fname in files:
                with open(basePath + "/" + fname) as infile:
                    outfile.write(infile.read())
        # for file in files:
        #     origin = basePath + "/" + file
        #     target = self._target_build_directory + "/postscript.sh"
        #     shutil.copyfile(origin, target)
        return

    def build(self):
        self.buildDockerComposeFile()
        self.buildResourceApplicationFile()
        self.buildUsedProfiles()
        self.buildPostScript()
        return
