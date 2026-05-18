package Ports;

import Aggregate.Order;
import Models.PaymentResult;

/*
Ports encaja mejor con tu arquitectura porque ya usás conceptos de sistemas distribuidos y 
Ports es el nombre correcto para interfaces que representan servicios externos.

*/

public interface PaymentGateway {
    PaymentResult process(Order order);
}
