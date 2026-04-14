/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package jakarta.tutorial.producerfields.ejb;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.tutorial.producerfields.entity.ToDo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RequestBean {

    @PersistenceContext
    private EntityManager em;

    public ToDo createToDo(String inputString) {
        Date currentTime = Calendar.getInstance().getTime();

        ToDo toDo = new ToDo();
        toDo.setTaskText(inputString);
        toDo.setTimeCreated(currentTime);
        em.persist(toDo);
        return toDo;
    }

    @Transactional(readOnly = true)
    public List<ToDo> getToDos() {
        return em.createQuery("SELECT t FROM ToDo t ORDER BY t.timeCreated", ToDo.class)
                .getResultList();
    }
}
