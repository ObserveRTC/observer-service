import json
import requests
import urllib


class ObserverClient:
    SERVICEMAPS = "servicemaps"
    SENTINEL_FILTERS = "sentinelFilters"
    SENTINELS = "sentinels"
    def __init__(self, host, port, debug = {"print_url": False}):
        self._host = host
        self._port = port
        self._debug = debug

    def get_servicemaps(self, name = None, uuid = None, onSuccess = None, onError = None):
        url = None
        if name is None and uuid is None:
            url = self._get_url(ObserverClient.SERVICEMAPS)
        elif uuid is None:
            url = self._get_url(ObserverClient.SERVICEMAPS, {"name": name})
        elif name is None:
            url = self._get_url(ObserverClient.SERVICEMAPS, {"uuid": uuid})
        else:
            url = self._get_url(ObserverClient.SERVICEMAPS, {"name": name, "uuid": uuid})
        response = requests.get(url)
        self._handle_response(response, onSuccess, onError)


    def add_servicemap(self, servicemap, onSuccess = None, onError = None):
        url = self._get_url(ObserverClient.SERVICEMAPS)
        serviceMapDTO = json.dumps( servicemap, indent = 4)
        headers = {'content-type': 'application/json'}
        response = requests.post(url, serviceMapDTO, headers=headers)
        self._handle_response(response, onSuccess, onError)

    def update_servicemap(self, servicemap, onSuccess = None, onError = None):
        url = self._get_url(ObserverClient.SERVICEMAPS)
        serviceMapDTO = json.dumps( servicemap, indent = 4)
        headers = {'content-type': 'application/json'}
        response = requests.put(url, serviceMapDTO, headers=headers)
        self._handle_response(response, onSuccess, onError)

    def remove_servicemap(self, service_name, onSuccess = None, onError = None):
        url = self._get_url(ObserverClient.SERVICEMAPS)
        headers = {'content-type': 'application/json'}
        response = requests.delete(url, data = service_name)
        self._handle_response(response, onSuccess, onError)

    def get_sentinel_filters(self, name = None, onSuccess = None, onError = None):
        url = None
        if name is not None:
            url = self._get_url(ObserverClient.SENTINEL_FILTERS, {"name": name})
        else:
            url = self._get_url(ObserverClient.SENTINEL_FILTERS)
        response = requests.get(url)
        self._handle_response(response, onSuccess, onError)


    def add_sentinel_filter(self, sentinel_filter, onSuccess = None, onError = None):
        url = self._get_url(ObserverClient.SENTINEL_FILTERS)
        sentinel_filter_dto = json.dumps( sentinel_filter, indent = 4)
        headers = {'content-type': 'application/json'}
        response = requests.post(url, sentinel_filter_dto, headers=headers)
        self._handle_response(response, onSuccess, onError)

    def update_sentinel_filter(self, sentinel_filter, onSuccess = None, onError = None):
        url = self._get_url(ObserverClient.SENTINEL_FILTERS)
        sentinel_filter_dto = json.dumps( sentinel_filter, indent = 4)
        headers = {'content-type': 'application/json'}
        response = requests.put(url, sentinel_filter_dto, headers=headers)
        self._handle_response(response, onSuccess, onError)

    def remove_sentinel_filter(self, sentinel_filter, onSuccess = None, onError = None):
        url = self._get_url(ObserverClient.SENTINEL_FILTERS)
        headers = {'content-type': 'application/json'}
        response = requests.delete(url, data = sentinel_filter)
        self._handle_response(response, onSuccess, onError)

    def get_sentinel(self, name = None, onSuccess = None, onError = None):
        url = None
        if name is not None:
            url = self._get_url(ObserverClient.SENTINELS, {"name": name})
        else:
            url = self._get_url(ObserverClient.SENTINELS)
        response = requests.get(url)
        self._handle_response(response, onSuccess, onError)

    def add_sentinel(self, sentinel, onSuccess = None, onError = None):
        url = self._get_url(ObserverClient.SENTINELS)
        sentinel_dto = json.dumps( sentinel, indent = 4)
        headers = {'content-type': 'application/json'}
        response = requests.post(url, sentinel_dto, headers=headers)
        self._handle_response(response, onSuccess, onError)

    def update_sentinel(self, sentinel, onSuccess = None, onError = None):
        url = self._get_url(ObserverClient.SENTINELS)
        sentinel_dto = json.dumps( sentinel, indent = 4)
        headers = {'content-type': 'application/json'}
        response = requests.put(url, sentinel_dto, headers=headers)
        self._handle_response(response, onSuccess, onError)

    def remove_sentinel(self, sentinel, onSuccess = None, onError = None):
        url = self._get_url(ObserverClient.SENTINELS)
        headers = {'content-type': 'application/json'}
        response = requests.delete(url, data = sentinel)
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


