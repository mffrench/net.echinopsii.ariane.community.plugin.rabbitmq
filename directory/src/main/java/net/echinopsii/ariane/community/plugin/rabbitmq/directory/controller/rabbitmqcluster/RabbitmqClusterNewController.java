/**
 * Rabbitmq plugin directory bundle
 * Directories RabbitMQ Cluster Create controller
 * Copyright (C) 2014 Mathilde Ffrench
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.echinopsii.ariane.community.plugin.rabbitmq.directory.controller.rabbitmqcluster;

import net.echinopsii.ariane.community.plugin.rabbitmq.directory.RabbitmqDirectoryBootstrap;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.controller.rabbitmqnode.RabbitmqNodesListController;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqCluster;
import net.echinopsii.ariane.community.plugin.rabbitmq.directory.model.RabbitmqNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.transaction.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RabbitmqClusterNewController implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(RabbitmqClusterNewController.class);

    private EntityManager em = RabbitmqDirectoryBootstrap.getDirectoryJPAProvider().createEM();

    @PreDestroy
    public void clean() {
        log.debug("Close entity manager");
        em.close();
    }

    public EntityManager getEm() {
        return em;
    }

    private String name;
    private String description;

    private List<String>  rmqnodesToBind = new ArrayList<String>();
    private Set<RabbitmqNode> nodes = new HashSet<RabbitmqNode>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRmqnodesToBind() {
        return rmqnodesToBind;
    }

    public void setRmqnodesToBind(List<String> rmqnodesToBind) {
        this.rmqnodesToBind = rmqnodesToBind;
    }

    public Set<RabbitmqNode> getNodes() {
        return nodes;
    }

    public void setNodes(Set<RabbitmqNode> nodes) {
        this.nodes = nodes;
    }

    /**
     * populate RabbitMQ nodes list through rmqnodesToBind list provided through UI form
     *
     * @throws NotSupportedException
     * @throws SystemException
     */
    private void bindSelectedNodes() throws NotSupportedException, SystemException {
        for (RabbitmqNode rmqc: RabbitmqNodesListController.getAll()) {
            for (String rmqcToBind : rmqnodesToBind)
                if (rmqc.getName().equals(rmqcToBind)) {
                    rmqc = em.find(rmqc.getClass(), rmqc.getId());
                    this.nodes.add(rmqc);
                    log.debug("Synced RabbitMQ nodes : {} {}", new Object[]{rmqc.getId(), rmqc.getName()});
                    break;
                }
        }
    }

    public void save() throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        try {
            bindSelectedNodes();
        } catch (Exception e) {
            e.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                       "Exception raise while creating rabbitmq cluster " + name + " !",
                                                       "Exception message : " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        RabbitmqCluster rabbitmqCluster = new RabbitmqCluster();
        rabbitmqCluster.setName(name);
        rabbitmqCluster.setDescription(description);
        rabbitmqCluster.setNodesR(this.nodes);

        try {
            em.getTransaction().begin();
            em.persist(rabbitmqCluster);
            em.flush();
            em.getTransaction().commit();
            log.debug("Save new RabbitmqCluster {} !", new Object[]{name});
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                                       "RabbitmqCluster created successfully !",
                                                       "RabbitmqCluster name : " + rabbitmqCluster.getName());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (Throwable t) {
            log.debug("Throwable catched !");
            t.printStackTrace();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                       "Throwable raised while creating RabbitmqCluster " + rabbitmqCluster.getName() + " !",
                                                       "Throwable message : " + t.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);

            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        }
    }
}