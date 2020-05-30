import hiyapyco
import os
import shutil
from os import listdir
from os.path import isfile, join


class ResourceBuilder:
    DATASOURCE_PREFIX = "datasource"
    SUBSCRIBER_PREFIX = "subscriber"
    SOURCE_CONFIGS_DIR = "source_configs"
    BASE_CONFIG_KEY = 'base'

    def __init__(self):
        self._datasource = None
        self._target_build_dir = "build"
        self._target_docker_compose_dir = "docker_compose"
        self._target_used_profiles_dir = "used_profiles"
        self._target_micronaut_application_yaml_dir = "micronaut_application_yaml"
        self._source_docker_compose_dir = "docker_compose"
        self._source_configs_dir = "source_configs"
        self._source_micronaut_application_yaml_dir = "micronaut_application_yaml"
        self._source_db_init_dir = "db_scripts"
        pass

    def getTargetDockerComposeDir(self):
        return "/".join([self._target_build_dir, self._target_docker_compose_dir])

    def getTargetUsedProfilesDir(self):
        return "/".join([self._target_build_dir, self._target_used_profiles_dir])

    def getTargetMicronautApplicationYamlDir(self):
        return "/".join([self._target_build_dir, self._target_micronaut_application_yaml_dir])

    def getSourceMicronautApplicationYamlDir(self):
        return "/".join([self._source_configs_dir, self._source_micronaut_application_yaml_dir])

    def getSourceDockerComposeDir(self):
        return "/".join([self._source_configs_dir, self._source_docker_compose_dir])

    def getSourceDBInitDir(self):
        return "/".join([self._source_configs_dir, self._source_db_init_dir])

    def withDatasource(self, datasource):
        self._datasource = datasource
        return self

    def withTargetBuildDir(self, target_build_dir):
        self._target_build_dir = target_build_dir
        return self

    def withTargetUsedProfilesDir(self, target_used_profiles_dir):
        self._target_used_profiles_dir = target_used_profiles_dir
        return self

    def withTargetDockerComposeDir(self, target_docker_compose_dir):
        self._target_docker_compose_dir = target_docker_compose_dir
        return self

    def withTargetUsedProfilesDir(self, target_used_profiles_dir):
        self._target_used_profiles_dir = target_used_profiles_dir
        return self

    def withTargetMicronautApplicationYamlDir(self, target_micronaut_application_yaml_dir):
        self._target_micronaut_application_yaml_dir = target_micronaut_application_yaml_dir
        return self

    def filesFor(self, path):
        for candidate in listdir(path):
            if isfile(join(path, f)) == False:
                continue
            yield candidate

    def _clean_folder(self, folder):
        for filename in os.listdir(folder):
            file_path = os.path.join(folder, filename)
            try:
                if os.path.isfile(file_path) or os.path.islink(file_path):
                    os.unlink(file_path)
                elif os.path.isdir(file_path):
                    shutil.rmtree(file_path)
            except Exception as e:
                print('Failed to delete %s. Reason: %s' % (file_path, e))
        return

    def clean(self):
        self._clean_folder(self.getTargetMicronautApplicationYamlDir())
        self._clean_folder(self.getTargetDockerComposeDir())
        self._clean_folder(self.getTargetUsedProfilesDir())

    def build(self):
        raise NotImplementedError("Subclasses should implement this!")
        return


class WebRTCStatResourceBuilder(ResourceBuilder):
    SERVICE_PREFIX = "webrtcstat"

    def __init__(self):
        ResourceBuilder.__init__(self)
        self._subscriber = None
        self._kafkastreamsStorage = None

    def withSubscriber(self, subscriber):
        self._subscriber = subscriber
        return self

    def withKafkastreamsStorage(self, kafkastreamsStorage):
        self._kafkastreamsStorage = kafkastreamsStorage
        return self

    def buildDockerComposeFile(self):
        self._mergeYamls(self.getSourceDockerComposeDir(), self.getTargetDockerComposeDir(), "yml")
        return

    def buildMicronautApplicationYamlFile(self):
        self._mergeYamls(self.getSourceMicronautApplicationYamlDir(), self.getTargetMicronautApplicationYamlDir(),
                         "yaml")
        return

    def clean(self):
        ResourceBuilder.clean(self)

    def build(self):
        self.buildDockerComposeFile()
        self.buildMicronautApplicationYamlFile()
        self.buildUsedProfileFile()
        # self.buildPostScript()
        return

    def buildUsedProfileFile(self):
        file_name_parts = [self.SERVICE_PREFIX]
        texts = ["=".join(['datasource', self._datasource])]
        file_name_parts.append(self._datasource)
        if self._subscriber is not None and self._subscriber != 'none':
            texts.append("=".join(['subscriber', self._subscriber]))
            file_name_parts.append(self._subscriber)

        target_file = "_".join(file_name_parts) + ".properties"
        target_path = "/".join([self.getTargetUsedProfilesDir(), target_file])
        text_file = open(target_path, "w")
        text_file.write("\n".join(texts))
        text_file.close()
        return

    def _mergeYamls(self, source_dir, target_dir, yaml_extension="yaml"):
        yaml_extension = "." + yaml_extension
        filenames_to_merge = []
        target_name_parts = []
        base_file = "_".join([
            WebRTCStatResourceBuilder.SERVICE_PREFIX,
            ResourceBuilder.BASE_CONFIG_KEY
        ]) + yaml_extension
        filenames_to_merge.append(base_file)
        target_name_parts.append(WebRTCStatResourceBuilder.SERVICE_PREFIX)

        datasource_file = "_".join([
            ResourceBuilder.DATASOURCE_PREFIX,
            self._datasource
        ]) + yaml_extension
        filenames_to_merge.append(datasource_file)
        target_name_parts.append(self._datasource)

        if self._subscriber is not None and self._subscriber != 'none':
            subscriber_file = "_".join([
                ResourceBuilder.SUBSCRIBER_PREFIX,
                self._subscriber
            ]) + yaml_extension
            filenames_to_merge.append(subscriber_file)
            target_name_parts.append(self._subscriber)
        source_files = [
            "/".join([
                source_dir,
                filename
            ]) for filename in filenames_to_merge
        ]
        conf = hiyapyco.load(*source_files, method=hiyapyco.METHOD_MERGE, interpolate=True,
                             failonmissingfiles=True)
        # print(target_name_parts)
        target_filename = "_".join(target_name_parts) + yaml_extension
        target_path = "/".join([target_dir, target_filename])
        text_file = open(target_path, "w")
        text_file.write(hiyapyco.dump(conf))
        text_file.close()
        return
