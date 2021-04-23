import json
import requests
import urllib


class ObserverClient:
    CONFIGS = "configs"
    def __init__(self, host, port, debug = {"print_url": False}):
        self._host = host
        self._port = port
        self._debug = debug

    def get_config(self, name = None, uuid = None, onSuccess = None, onError = None):
        url = self._get_url(ObserverClient.CONFIGS)
        response = requests.get(url)
        self._handle_response(response, onSuccess, onError)


    def update_configs(self, config, onSuccess = None, onError = None):
        url = self._get_url(ObserverClient.CONFIGS)
        observer_config = json.dumps( config, indent = 4)
        headers = {'content-type': 'application/json'}
        response = requests.put(url, observer_config, headers=headers)
        self._handle_response(response, onSuccess, onError)

    def delete_configs(self, keys, onSuccess = None, onError = None):
        url = self._get_url(ObserverClient.CONFIGS)
        response = requests.delete(url, data = keys)
        self._handle_response(response, onSuccess, onError)

    def _handle_response(self, response, onSuccess = None, onError = None):
        if response.status_code != 200:
            if onError is not None:
                onError(response.content)
            else:
                print("There was an error occurred during execution", response, response.content)
            return
        if onSuccess is not None:
            onSuccess(response.content)
        else:
            self._print(response.content)

    def _get_url(self, path, query = None):
        base_url = "http://{host}:{port}/{path}".format(host = self._host, port = self._port, path = path)
        if query is None:
            if self._debug["print_url"] is True:
                print(base_url)
            return base_url
        query_str = urllib.parse.urlencode(query, doseq=False)
        result = "{base}?{query}".format(base = base_url, query = query_str)
        if self._debug["print_url"] is True:
            print(result)
        return result

    def _print(self, json_string):
        if json_string is None or len(json_string) < 1:
            return
        parsed = json.loads(json_string)
        print(json.dumps(parsed, indent=4, sort_keys=True))


