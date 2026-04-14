/**
 * (C) Copyright IBM Corporation 2015.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.websphere.samples.daytrader.web.jsf;

import java.math.BigDecimal;
import java.util.ArrayList;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.annotation.RequestScope;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.Trace;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;
import com.ibm.websphere.samples.daytrader.util.TradeRunTimeModeLiteral;

@Controller("orderdata")
@RequestScope
@Trace
public class OrderDataJSF {

  @Autowired
  private TradeServices tradeAction;

  private OrderData[] allOrders;
  private OrderData orderData;

  public OrderDataJSF() {
  }

  public void getAllOrder() {
    try {
      HttpSession session = getSession();
      String userID = (String) session.getAttribute("uidBean");

      ArrayList<?> orderDataBeans = (TradeConfig.getLongRun() ? new ArrayList<Object>() : (ArrayList<?>) tradeAction.getOrders(userID));
      OrderData[] orders = new OrderData[orderDataBeans.size()];

      int count = 0;

      for (Object order : orderDataBeans) {
        OrderData r = new OrderData(((OrderDataBean) order).getOrderID(), ((OrderDataBean) order).getOrderStatus(),
            ((OrderDataBean) order).getOpenDate(), ((OrderDataBean) order).getCompletionDate(), ((OrderDataBean) order).getOrderFee(),
            ((OrderDataBean) order).getOrderType(), ((OrderDataBean) order).getQuantity(), ((OrderDataBean) order).getSymbol());
        r.setPrice(((OrderDataBean) order).getPrice());
        r.setTotal(r.getPrice().multiply(new BigDecimal(r.getQuantity())));
        orders[count] = r;
        count++;
      }

      setAllOrders(orders);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  @PostConstruct
  public void getOrder() {

    HttpSession session = getSession();
    OrderData order = (OrderData) session.getAttribute("orderData");

    if (order != null) {
      setOrderData(order);
    }
  }

  public void setAllOrders(OrderData[] allOrders) {
    this.allOrders = allOrders;
  }

  public OrderData[] getAllOrders() {
    return allOrders;
  }

  public void setOrderData(OrderData orderData) {
    this.orderData = orderData;
  }

  public OrderData getOrderData() {
    return orderData;
  }

  private HttpSession getSession() {
    ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    return attr.getRequest().getSession(true);
  }
}
