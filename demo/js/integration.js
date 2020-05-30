class Integrator {
    constructor(websocketServer = '') {
        this.statsParser = new WebextraApp.StatsParser()
        this.statsSender = new WebextraApp.StatsSender(websocketServer)
        this.init()
    }
    init() {
        this.wobserver = new WebextraApp.init()
    }

    startCollection() {
        this.wobserver.attachPlugin(this.statsParser)
        this.wobserver.attachPlugin(this.statsSender)
        this.wobserver.addPC(pc1)
        this.wobserver.addPC(pc2)
        this.wobserver.startWorker()
    }

    stopCollection() {
        this.wobserver.dispose()
    }
}

const wsServerURL = 'ws://localhost:8088/ws/86ed98c6-b001-48bb-b31e-da638b979c72'
let integrator = new Integrator(wsServerURL);

