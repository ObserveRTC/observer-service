import os
import shutil
from os import listdir
from os.path import isfile, join


class DBScriptBuilder:
    BASE_CONFIG_KEY = 'base'
    DATASOURCE_PREFIX = 'datasource'

    def __init__(self):
        self._datasource = None
        self._target_build_dir = "build/db_scripts/"
        self._source_db_scripts_dir = "db_scripts"
        self._datasource = None
        pass

    def getSourceDBScriptsDir(self):
        return self._source_db_scripts_dir

    def getTargetBuildDir(self):
        return self._target_build_dir

    def withTargetBuildDir(self, target_build_dir):
        self._target_build_dir = target_build_dir
        return self

    def withDatasource(self, datasource):
        self._datasource = datasource
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
        if self._datasource is None:
            raise Exception("DB script builder is not working without giving what is the target datasource")

        index = 1
        filenames = os.listdir(self.getSourceDBScriptsDir())
        target_filename = "_".join([self._datasource]) + ".sql"
        with open("/".join([self.getTargetBuildDir(), target_filename]), 'w') as targetfile:
            while True:
                sequence = '%003d' % index
                # print(sequence)
                filtered_scripts = list(filter(lambda filename: filename.startswith(sequence), filenames))

                print(filtered_scripts)
                if len(filtered_scripts) < 1:
                    break

                selected_script = ""
                if len(filtered_scripts) == 1:
                    selected_script = filtered_scripts[0]
                else:
                    subfiltered_scripts = list(filter(lambda filename: filename.count(".") == 1, filtered_scripts))
                    if len(subfiltered_scripts) < 1:
                        raise Exception("I have not found a default script for the sql for sequence", sequence)
                    datasourcefiltered_scripts = list(
                        filter(lambda filename: filename.endswith(".".join([self._datasource, "sql"])) == 1,
                               filtered_scripts))
                    if len(datasourcefiltered_scripts) == 1:
                        selected_script = datasourcefiltered_scripts[0]
                    else:
                        selected_script = subfiltered_scripts[0]

                source_file = "/".join([self.getSourceDBScriptsDir(), selected_script])
                with open(source_file, 'r') as file:
                    sql_script = file.read()
                    targetfile.write(sql_script)
                print("Datasource, sequence, chossen file:", self._datasource, sequence, selected_script)

                index = index + 1
        return


class WebRTCStatDBScriptBuilder(DBScriptBuilder):
    SERVICE_PREFIX = "webrtcstat"

    def getSourceDBScriptsDir(self):
        return "/".join([super().getSourceDBScriptsDir(), self.SERVICE_PREFIX])

    def getTargetBuildDir(self):
        return "/".join([super().getTargetBuildDir(), self.SERVICE_PREFIX])

    def __init__(self):
        DBScriptBuilder.__init__(self)
        pass

    def clean(self):
        super().clean()

    def build(self):
        super().build()
        # self.buildPostScript()
        return
