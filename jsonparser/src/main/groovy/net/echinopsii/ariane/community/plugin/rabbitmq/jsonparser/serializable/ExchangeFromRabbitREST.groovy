package net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.serializable

import net.echinopsii.ariane.community.plugin.rabbitmq.jsonparser.tools.RabbitClusterToConnect

import javax.persistence.Transient

class ExchangeFromRabbitREST implements Serializable {

    transient RabbitClusterToConnect cluster;

    String name
    String vhost
    Map<String, Object> properties

    ExchangeFromRabbitREST(String name, String vhost, RabbitClusterToConnect cluster) {
        this.name = name
        this.vhost = vhost
        this.cluster = cluster
    }

    ExchangeFromRabbitREST parse() {
        def restClient = this.cluster.getRestCli()

        String exchange_req_path =  '/api/exchanges/' + this.vhost + "/" + this.name
        def exchange_req = restClient.get(path : exchange_req_path)
        if (exchange_req.status == 200 && exchange_req.data != null) {
            //exchange_req.data.each { exchange ->
            //    if (exchange.name.equals(this.name) && exchange.vhost.equals(this.vhost))
            //        properties = exchange
            //}
            properties = exchange_req.data
            properties.remove("name")
            properties.remove("vhost")
        }

        return this
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ExchangeFromRabbitREST that = (ExchangeFromRabbitREST) o

        if (name != that.name) return false
        if (vhost != that.vhost) return false

        return true
    }

    int hashCode() {
        int result
        result = name.hashCode()
        result = 31 * result + vhost.hashCode()
        return result
    }
}
