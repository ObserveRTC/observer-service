import os
import shutil
from os import listdir
from os.path import isfile, join


class JsonSchemaBuilder:
    BASE_CONFIG_KEY = 'base'

    def __init__(self):
        self._datasource = None
        self._target_build_dir = "build/java_pojos"
        self._source_ts_dir = "source_ts"
        pass

    def getSourceTsDir(self):
        return self._source_ts_dir

    def getTargetJsonSchemaDir(self):
        return self._target_build_dir

    def withTargetBuildDir(self, target_build_dir):
        self._target_build_dir = target_build_dir
        return self

    def withSourceTsDir(self, source_ts_dir):
        self._source_ts_dir = source_ts_dir
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
        return

    def build(self):
        raise NotImplementedError("Subclasses should implement this!")
        return


class WebRTCStatJsonSchemaBuilder(JsonSchemaBuilder):
    SERVICE_PREFIX = "webrtcstat"

    def __init__(self):
        JsonSchemaBuilder.__init__(self)
        pass

    def clean(self):
        JsonSchemaBuilder.clean(self)

    def getSourceTsDir(self):
        return "/".join([JsonSchemaBuilder.getSourceTsDir(), self.SERVICE_PREFIX])

    def build(self):
        print("Maybe use quicktype, maybe jsonschema to java pojo.... IDK")
        # self.buildPostScript()
        return
